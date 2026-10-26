package com.neolena.essenceindustry.array;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;

/** One renderer per controller. Rack machines are item models, never ticking machine entities. */
public final class ArrayRenderer implements BlockEntityRenderer<ArrayTile,ArrayRenderer.State> {
 private static final Identifier METAL=Identifier.parse("essenceindustry:textures/block/array/hull_metal.png"),LIGHT=Identifier.parse("essenceindustry:textures/block/array/hull_blue.png"),WHITE=Identifier.parse("essenceindustry:textures/block/array/white.png");
 private static final ModelPart CUBE=cube();
 private final net.minecraft.client.renderer.item.ItemModelResolver items;private final net.minecraft.client.renderer.block.BlockModelResolver blocks;
 private record Ghost(float x,float y,float z,boolean wrong){}
 private record Cache(long tick,int layer,int width,int depth,List<Ghost> ghosts){}
 private record BayCache(List<BlockPos> racks,int installed,List<ArrayBayLayout.Bay> bays){}
 private final Map<ArrayTile,BayCache> bayCaches=new WeakHashMap<>();
 private final Map<ArrayTile,Cache> guides=new WeakHashMap<>();
 private final Map<ArrayTile,Motion> motions=new WeakHashMap<>();
 /** Integrate observed client time, retaining the exact pose while idle. No server animation clock. */
 static final class Motion {
  private java.lang.ref.WeakReference<Object> world=new java.lang.ref.WeakReference<>(null);private long tick;private double partial,time;private boolean initialized,running;
  double sample(Object level,long now,float delta,boolean active){
   double fraction=Math.clamp((double)delta,0,1);
   if(!initialized||world.get()!=level||now<tick){world=new java.lang.ref.WeakReference<>(level);tick=now;partial=fraction;time=0;initialized=true;running=active;return time;}
   // Subtract integral ticks before adding fractions: old worlds must not discard partial ticks.
   double elapsed=(now-tick)+fraction-partial;if(running&&elapsed>0)time+=elapsed;
   tick=now;partial=fraction;running=active;return time;
  }
 }
 public static final class State extends BlockEntityRenderState {
  int width,depth,height=5,installed,active;BlockPos offset=BlockPos.ZERO,controllerLocal=new BlockPos(0,1,0);float viewX,viewZ;List<BlockPos> racks=List.of(),windows=List.of();List<ArrayBayLayout.Bay> bays=List.of();boolean preview,cutaway,detailed;Direction face;boolean formed;float time;List<Ghost> ghosts=List.of();
  double animationTime=Double.NaN;
  final ItemStackRenderState machine=new ItemStackRenderState();final net.minecraft.client.renderer.block.BlockModelRenderState machineBlock=new net.minecraft.client.renderer.block.BlockModelRenderState();boolean hasMachineBlock;
 }
 public ArrayRenderer(BlockEntityRendererProvider.Context context){items=context.itemModelResolver();blocks=context.blockModelResolver();}
 public static void register(IEventBus bus){bus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e)->e.registerBlockEntityRenderer(ProcessingArray.TILE.get(),ArrayRenderer::new));}
 private static ModelPart cube(){var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,0).addBox(0,0,0,16,16,16),PartPose.ZERO);return LayerDefinition.create(mesh,64,32).bakeRoot();}
 @Override public State createRenderState(){return new State();}
 @Override public void extractRenderState(ArrayTile tile,State state,float delta,Vec3 camera,net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay){
  BlockEntityRenderer.super.extractRenderState(tile,state,delta,camera,overlay);state.offset=tile.layoutAnchor().subtract(tile.getBlockPos());state.controllerLocal=ArrayLayout.local(tile.layoutAnchor(),ArrayLayout.facing(tile.getBlockState()),tile.getBlockPos());state.height=tile.height();state.width=tile.width();state.depth=tile.depth();state.face=ArrayLayout.facing(tile.getBlockState());state.installed=tile.visualInstalled();state.active=tile.visualWorking()?Math.max(1,tile.visualActive()):0;state.formed=tile.formed();
  state.animationTime=motions.computeIfAbsent(tile,key->new Motion()).sample(tile.getLevel(),tile.getLevel().getGameTime(),delta,state.formed&&state.active>0);
  state.windows=tile.visualWindows();state.racks=tile.visualRacks();var bayCache=bayCaches.get(tile);
  if(bayCache==null||bayCache.racks!=state.racks||bayCache.installed!=state.installed){bayCache=new BayCache(state.racks,state.installed,ArrayBayLayout.allocate(state.racks,state.installed));bayCaches.put(tile,bayCache);}state.bays=bayCache.bays;
  var local=localCamera(tile.layoutAnchor(),state.face,camera);state.viewX=(float)local.x;state.viewZ=(float)local.z;state.detailed=housingDistanceSqr(local,state.width,state.height,state.depth)<32*32;
  var item=tile.visualMachine();state.hasMachineBlock=item.getItem() instanceof net.minecraft.world.item.BlockItem;if(item.getItem() instanceof net.minecraft.world.item.BlockItem machine)blocks.update(state.machineBlock,machine.getBlock().defaultBlockState(),net.minecraft.client.renderer.block.model.BlockDisplayContext.create());else items.updateForTopItem(state.machine,item,ItemDisplayContext.FIXED,tile.getLevel(),null,0);
  if(!tile.guide()){state.ghosts=List.of();guides.remove(tile);return;}
  var cached=guides.get(tile);long tick=tile.getLevel().getGameTime();
  if(cached==null||tick-cached.tick>=10||cached.layer!=tile.layer()||cached.width!=state.width||cached.depth!=state.depth){
   var ghosts=new ArrayList<Ghost>();var player=Minecraft.getInstance().player;
   for(int z=0;z<state.depth&&ghosts.size()<256;z++)for(int x=0;x<state.width&&ghosts.size()<256;x++){
    int y=tile.layer();boolean shell=ArrayLayout.shell(x,y,z,state.width,state.depth,state.height),socket=ArrayLayout.socket(x,y,z,state.width,state.depth,state.height);boolean error=!tile.formed()&&x==tile.problemX()&&y==tile.problemY()&&z==tile.problemZ();if(!shell&&!socket&&!error)continue;
    var pos=tile.position(x,y,z);if(pos.equals(tile.getBlockPos())||!tile.getLevel().hasChunkAt(pos)||player==null||player.distanceToSqr(Vec3.atCenterOf(pos))>32*32)continue;
    var actual=tile.getLevel().getBlockState(pos);boolean correct=socket?(actual.isAir()||actual.is(ProcessingArray.RACK.get())):actual.is(ProcessingArray.CASING.get())||actual.is(ProcessingArray.GLASS.get())||actual.is(ProcessingArray.HATCH.get())||actual.is(ProcessingArray.PORT.get());
    if(correct&&!actual.isAir()&&!error)continue;ghosts.add(new Ghost(x,y,z,!actual.isAir()));
   }
   cached=new Cache(tick,tile.layer(),state.width,state.depth,List.copyOf(ghosts));guides.put(tile,cached);
  }state.ghosts=cached.ghosts;
 }
 static Vec3 localCamera(BlockPos controller,Direction face,Vec3 camera){
  var right=face.getClockWise();var back=face.getOpposite();double x=camera.x-controller.getX()-.5,z=camera.z-controller.getZ()-.5;
  return new Vec3(.5+x*right.getStepX()+z*right.getStepZ(),camera.y-controller.getY()+1,.5+x*back.getStepX()+z*back.getStepZ());
 }
 static double housingDistanceSqr(Vec3 local,int width,int height,int depth){
  double x=Math.max(Math.max(-local.x,local.x-width),0),y=Math.max(Math.max(-local.y,local.y-height),0),z=Math.max(Math.max(-local.z,local.z-depth),0);return x*x+y*y+z*z;
 }
 static float gantryZ(State state){
  if(Double.isNaN(state.animationTime)&&state.active==0)return state.depth*.5f;
  double time=Double.isNaN(state.animationTime)?state.time:state.animationTime;return (float)(1.2+(state.depth-3.4)*(.5+.5*Math.sin(time*.035)));
 }
 static float fanAngle(State state){
  if(Double.isNaN(state.animationTime))return state.active>0?(state.time*6)%360:30;
  return (float)((30+state.animationTime*6)%360);
 }
 static void box(PoseStack pose,SubmitNodeCollector collector,double x,double y,double z,float w,float h,float d,Identifier texture,int color,int light,boolean ghost){
  pose.pushPose();pose.translate(x,y,z);pose.scale(w,h,d);collector.submitModelPart(CUBE,pose,ghost?RenderTypes.entityTranslucent(texture):RenderTypes.entityCutout(texture),light,OverlayTexture.NO_OVERLAY,null,color,null);pose.popPose();
 }
 @Override public void submit(State s,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){submitScene(s,pose,collector);}
 static void submitScene(State s,PoseStack pose,SubmitNodeCollector collector){
  pose.pushPose();pose.translate(s.offset.getX(),s.offset.getY(),s.offset.getZ());pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(s.face==Direction.EAST?-90:s.face==Direction.SOUTH?180:s.face==Direction.WEST?90:0));pose.translate(-.5,-1,-.5);
  if(s.formed){
   ArrayHousing.draw(s,pose,collector);
   // Rails and a travelling crossbeam share one client-only phase, with no packets per animation frame.
   float z=gantryZ(s);
   box(pose,collector,.65,s.height-1.22,1,.18f,.12f,s.depth-2,METAL,-1,s.lightCoords,false);
   box(pose,collector,s.width-1,s.height-1.22,1,.18f,.12f,s.depth-2,METAL,-1,s.lightCoords,false);
   box(pose,collector,.7,s.height-1.35,z,s.width-1.4f,.22f,.28f,METAL,-1,s.lightCoords,false);
   ArrayHousing.carriage(s,pose,collector,z);
   // Frames come from native chunk meshes; nearby occupied bays show every machine.
   // Large or distant banks retain a representative model per occupied level.
   int detailedModels=0;
   for(var bay:s.bays){
    int col=bay.x(),row=bay.z(),baseY=bay.y(),count=bay.machines();
    boolean detail=!s.preview&&Math.pow(col+.5-s.viewX,2)+Math.pow(row+.5-s.viewZ,2)<16*16&&detailedModels+count<=64;
    if(detail){for(int slot=0;slot<count;slot++){
     pose.pushPose();pose.translate(col+.28125+(slot%2)*.4375,baseY+.34375+(slot/4),row+.28125+(slot/2%2)*.4375);pose.scale(.40625f,.40625f,.40625f);submitMachine(s,pose,collector);pose.popPose();
    }detailedModels+=count;}
    else for(int level=0;level<(count+3)/4;level++){
     pose.pushPose();pose.translate(col+.5,baseY+.5+level,row+.5);pose.scale(.71875f,.71875f,.71875f);submitMachine(s,pose,collector);pose.popPose();
    }
    for(int level=0;level<2;level++){
     float fill=Math.clamp((count-level*4)/4f,0,1);if(fill>0)box(pose,collector,col+.125,baseY+.875+level,row-.015625,.75f*fill,.03125f,.015625f,LIGHT,-1,15728880,false);
    }
   }
  }
  for(var ghost:s.ghosts)box(pose,collector,ghost.x+.035,ghost.y+.035,ghost.z+.035,.93f,.93f,.93f,WHITE,ghost.wrong?0x66FF756C:0x4081D9FF,15728880,true);
  pose.popPose();
 }
 private static void submitMachine(State state,PoseStack pose,SubmitNodeCollector collector){
  if(state.hasMachineBlock){pose.pushPose();pose.translate(-.5,-.5,-.5);state.machineBlock.submit(pose,collector,state.lightCoords,OverlayTexture.NO_OVERLAY,0);pose.popPose();}else state.machine.submit(pose,collector,state.lightCoords,OverlayTexture.NO_OVERLAY,0);
 }
 @Override public boolean shouldRenderOffScreen(){return true;}
 @Override public int getViewDistance(){return 96;}
}
