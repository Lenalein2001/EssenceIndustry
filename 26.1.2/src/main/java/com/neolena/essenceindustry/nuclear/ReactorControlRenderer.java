package com.neolena.essenceindustry.nuclear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;

/** One center absorber per fuel column. Four surrounding fuel rods stay baked and do not tick. */
public final class ReactorControlRenderer {
    private static final Identifier GRAPHITE=Identifier.parse("essenceindustry:textures/block/reactor/graphite.png");
    private static final ContextKey<View> KEY=new ContextKey<>(Identifier.parse("essenceindustry:reactor_controls"));
    private static final Map<ReactorTile,Motion> MOTION=new WeakHashMap<>();
    private static ClientLevel currentLevel;
    private static boolean registered;
    private static final class Motion {
        double before,current;
        Motion(int target){before=current=target;}
        void tick(int target){before=current;current+=Math.clamp(target-current,-5,5);}
        double sample(double partial){return before+(current-before)*Math.clamp(partial,0,1);}
    }
    private record Rod(BlockPos base,int height,double insertion,int light){}
    private record View(List<Rod> rods,Vec3 camera){}

    public static void register(){
        if(registered)return;registered=true;
        NeoForge.EVENT_BUS.addListener(ReactorControlRenderer::tick);
        NeoForge.EVENT_BUS.addListener(ReactorControlRenderer::extract);
        NeoForge.EVENT_BUS.addListener(ReactorControlRenderer::submit);
    }
    /** Exposes actual interpolated position for the native visual regression fixture. */
    public static double visualInsertion(ReactorTile tile,double partial){
        var motion=MOTION.get(tile);return motion==null?tile.visualRodInsertion():motion.sample(partial);
    }
    private static void world(ClientLevel level){if(level!=currentLevel){MOTION.clear();currentLevel=level;}}
    private static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();world(mc.level);if(mc.level==null||mc.isPaused())return;
        MOTION.entrySet().removeIf(e->e.getKey().isRemoved()||!e.getKey().formed()||e.getKey().getLevel()!=mc.level);
        for(var tile:ReactorTile.clientTiles(mc.level))if(tile.formed())
            MOTION.computeIfAbsent(tile,t->new Motion(t.visualRodInsertion())).tick(tile.visualRodInsertion());
    }
    private static void extract(ExtractLevelRenderStateEvent event){
        var level=event.getLevel();world(level);var camera=event.getCamera().position();var rods=new ArrayList<Rod>();
        double partial=event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        for(var tile:ReactorTile.clientTiles(level)){
            if(tile.isRemoved()||!tile.formed())continue;
            double insertion=visualInsertion(tile,partial);if(insertion<=0)continue;
            int height=tile.height()-2;
            for(var base:tile.visualControlColumns()){
                var bounds=new AABB(base.getX(),base.getY(),base.getZ(),base.getX()+1,base.getY()+height,base.getZ()+1);
                if(bounds.getCenter().distanceToSqr(camera)>128*128||!event.getFrustum().isVisible(bounds)||!level.hasChunkAt(base))continue;
                var state=level.getBlockState(base);
                // Until formation packets arrive, the baked model remains the sole absorber.
                if(!state.hasProperty(NuclearPart.CONTROLLED)||!state.getValue(NuclearPart.CONTROLLED))continue;
                var lightAt=base.above(height/2);
                int light=level.getBrightness(LightLayer.BLOCK,lightAt)<<4|level.getBrightness(LightLayer.SKY,lightAt)<<20;
                rods.add(new Rod(base,height,insertion,light));
            }
        }
        event.getRenderState().setRenderData(KEY,new View(List.copyOf(rods),camera));
    }
    private static void submit(SubmitCustomGeometryEvent event){
        var view=event.getLevelRenderState().getRenderData(KEY);if(view==null||view.rods.isEmpty())return;
        var stack=event.getPoseStack();stack.pushPose();stack.translate(-view.camera.x,-view.camera.y,-view.camera.z);
        event.getSubmitNodeCollector().submitCustomGeometry(stack,RenderTypes.entitySolid(GRAPHITE),(pose,out)->{
            var normals=new org.joml.Vector3f[]{new org.joml.Vector3f(0,0,-1),new org.joml.Vector3f(0,0,1),new org.joml.Vector3f(-1,0,0),new org.joml.Vector3f(1,0,0),new org.joml.Vector3f(0,-1,0),new org.joml.Vector3f(0,1,0)};
            for(var normal:normals)pose.transformNormal(normal,normal);
            for(var rod:view.rods)draw(rod,pose,out,normals);
        });stack.popPose();
    }
    private static void draw(Rod rod,PoseStack.Pose pose,VertexConsumer out,org.joml.Vector3f[] normals){
        double top=rod.base.getY()+rod.height,bottom=top-rod.height*rod.insertion/100;
        double x0=rod.base.getX()+7/16d,x1=rod.base.getX()+9/16d;
        double z0=rod.base.getZ()+7/16d,z1=rod.base.getZ()+9/16d;
        // Texture repeats once per block without stretching it over tall absorbers.
        // Positions and UVs are emitted directly: no allocations per segment, face or vertex.
        for(double y=bottom;y<top;){double end=Math.min(top,y+Math.max(1,rod.height/4d));float v=1;
            quad(pose,out,x0,y,z0, x0,end,z0, x1,end,z0, x1,y,z0,v,rod.light,normals[0]);
            quad(pose,out,x1,y,z1, x1,end,z1, x0,end,z1, x0,y,z1,v,rod.light,normals[1]);
            quad(pose,out,x0,y,z1, x0,end,z1, x0,end,z0, x0,y,z0,v,rod.light,normals[2]);
            quad(pose,out,x1,y,z0, x1,end,z0, x1,end,z1, x1,y,z1,v,rod.light,normals[3]);y=end;
        }
        quad(pose,out,x0,bottom,z1, x0,bottom,z0, x1,bottom,z0, x1,bottom,z1,1,rod.light,normals[4]);
        quad(pose,out,x0,top,z0, x0,top,z1, x1,top,z1, x1,top,z0,1,rod.light,normals[5]);
    }
    private static void quad(PoseStack.Pose pose,VertexConsumer out,
                             double ax,double ay,double az,double bx,double by,double bz,
                             double cx,double cy,double cz,double dx,double dy,double dz,
                             float v,int light,org.joml.Vector3f normal){
        vertex(pose,out,ax,ay,az,0,v,light,normal);
        vertex(pose,out,bx,by,bz,0,0,light,normal);
        vertex(pose,out,cx,cy,cz,1,0,light,normal);
        vertex(pose,out,dx,dy,dz,1,v,light,normal);
    }
    private static void vertex(PoseStack.Pose pose,VertexConsumer out,double x,double y,double z,float u,float v,int light,org.joml.Vector3f normal){
        out.addVertex(pose,(float)x,(float)y,(float)z).setColor(0xFFFFFFFF).setUv(u,v)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x,normal.y,normal.z);
    }
    private ReactorControlRenderer(){}
}
