package com.neolena.essenceindustry.nuclear;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import java.util.*;
/** Continuous client-local rotation; bounded steam wisps have no server tick cost. */
public final class TurbineClient {
 private static final Map<TurbineTile,TurbineMotion> MOTION=new WeakHashMap<>();
 public static double visualAngle(TurbineTile t){var m=MOTION.get(t);return m==null?0:m.angle();}
 private static final ContextKey<View> KEY=new ContextKey<>(Identifier.parse("essenceindustry:turbine_rotor"));
 private record Rotor(Vec3 origin,Direction facing,boolean vertical,int width,int length,int blades,double angle,int light,int wisps) {
  Vec3 point(double x,double y,double z){var r=facing.getClockWise();var b=facing.getOpposite();return vertical?origin.add(r.getStepX()*x-b.getStepX()*y,z,r.getStepZ()*x-b.getStepZ()*y):origin.add(r.getStepX()*x+b.getStepX()*z,y,r.getStepZ()*x+b.getStepZ()*z);}
 }
 private record View(List<Rotor> rotors,Vec3 camera,double time){}
 public static void register(IEventBus bus){bus.addListener((RegisterMenuScreensEvent e)->e.register(SteamTurbine.MENU.get(),TurbineScreen::new));NeoForge.EVENT_BUS.addListener(TurbineClient::extract);NeoForge.EVENT_BUS.addListener(TurbineClient::submit);}
 private static void extract(ExtractLevelRenderStateEvent e){
  long now=System.nanoTime();var mc=Minecraft.getInstance();boolean paused=mc.isPaused();var rotors=new ArrayList<Rotor>();var particles=mc.options.particles().get();
  for(var t:TurbineTile.clients(e.getLevel()))if(t.formed()&&t.getBlockPos().distToCenterSqr(e.getCamera().position())<16384){
   boolean vertical=t.layout().vertical();var p=vertical?t.position(t.width()/2,0,t.width()/2):t.position(t.width()/2,t.height()/2,0);
   int light=e.getLevel().getBrightness(net.minecraft.world.level.LightLayer.BLOCK,p)<<4|e.getLevel().getBrightness(net.minecraft.world.level.LightLayer.SKY,p)<<20;
   int wisps=TurbineVisuals.wisps(t.flow(),particles==ParticleStatus.DECREASED,particles==ParticleStatus.MINIMAL);
   rotors.add(new Rotor(p.getCenter(),t.facing(),vertical,t.width(),vertical?t.height():t.depth(),t.layout().blades(),Math.toRadians(MOTION.computeIfAbsent(t,k->new TurbineMotion()).sample(now,t.rpm(),paused)),light,wisps));
  }
  double time=e.getLevel().getGameTime()/20d+mc.getDeltaTracker().getGameTimeDeltaPartialTick(!paused)/20d;
  e.getRenderState().setRenderData(KEY,new View(List.copyOf(rotors),e.getCamera().position(),time));
 }
 private static void submit(SubmitCustomGeometryEvent e){var v=e.getLevelRenderState().getRenderData(KEY);if(v==null)return;var stack=e.getPoseStack();stack.pushPose();stack.translate(-v.camera.x,-v.camera.y,-v.camera.z);
  e.getSubmitNodeCollector().submitCustomGeometry(stack,RenderTypes.entitySolid(Identifier.parse("essenceindustry:textures/block/reactor/steel.png")),(pose,vertices)->draw(v,pose,vertices));
  if(v.rotors.stream().anyMatch(r->r.wisps>0))e.getSubmitNodeCollector().submitCustomGeometry(stack,RenderTypes.entityTranslucent(Identifier.parse("minecraft:textures/particle/generic_5.png")),(pose,vertices)->steam(v,pose,vertices));stack.popPose();
 }
 private static void draw(View v,PoseStack.Pose p,VertexConsumer out){for(var s:v.rotors){
  // The whole axle and generator core share the blade angle, including the coil chamber.
  for(int segment=1;segment<s.length-1;segment++)cylinder(s,p,out,.1875,segment-.5,segment+.5,0xFFBBC8CE);
  for(int stage=0;stage<s.blades;stage++)cylinder(s,p,out,.23,stage+1.9,stage+2.1,0xFFCBD5DA);
  cylinder(s,p,out,.36,s.blades+2.65,s.length-1.5,0xFF728A96);
  cylinder(s,p,out,.48,s.length-1.7,s.length-1.5,0xFFBBC8CE);
  cylinder(s,p,out,.28,s.length-1.5,s.length-1.38,0xFF728A96);
  for(int slice=0;slice<s.blades;slice++){
  for(int blade=0;blade<4;blade++){
   double theta=s.angle+blade*Math.PI/2+slice*.14,r=TurbineVisuals.radius(s.width),tip=TurbineVisuals.bladeHalfWidth(s.width);
   double[][] points={{.16,-.12,-.12},{r,-tip,-.04},{r,tip,.12},{.16,.12,.04},{.16,-.12,-.04},{r,-tip,.04},{r,tip,.20},{.16,.12,.12}};var positions=new Vec3[8];
   for(int i=0;i<8;i++){var q=points[i];positions[i]=s.point(q[0]*Math.cos(theta)-q[1]*Math.sin(theta),q[0]*Math.sin(theta)+q[1]*Math.cos(theta),q[2]+2+slice);}box(p,out,positions,s.light,0xFFFFFFFF);
  }
  for(int nozzle=0;nozzle<2;nozzle++){
   var q=TurbineVisuals.jet(s.width,slice,nozzle,0);double angle=Math.PI/4+nozzle*Math.PI;var points=new Vec3[8];
   for(int i=0;i<8;i++){double a=(i&1)==0?-.07:.07,b=(i&2)==0?-.12:.02,z=(i&4)==0?-.09:.09;points[i]=s.point(q.x()+a*Math.cos(angle)-b*Math.sin(angle),q.y()+a*Math.sin(angle)+b*Math.cos(angle),q.z()+z);}
   box(p,out,new Vec3[]{points[0],points[1],points[3],points[2],points[4],points[5],points[7],points[6]},s.light,0xFF9DAFB7);
   if(slice==0){double r=TurbineVisuals.radius(s.width)+.25,x=r*Math.cos(angle),y=r*Math.sin(angle);var rail=new Vec3[]{s.point(x-.04,y-.04,1.2),s.point(x+.04,y-.04,1.2),s.point(x+.04,y+.04,1.2),s.point(x-.04,y+.04,1.2),s.point(x-.04,y-.04,s.blades+1.8),s.point(x+.04,y-.04,s.blades+1.8),s.point(x+.04,y+.04,s.blades+1.8),s.point(x-.04,y+.04,s.blades+1.8)};box(p,out,rail,s.light,0xFF657F88);for(double z:new double[]{1.2,s.blades+1.8}){var link=new Vec3[8];for(int i=0;i<8;i++){double rr=(i&1)==0?.4:r,side=(i&2)==0?-.05:.05,zz=z+((i&4)==0?-.05:.05);link[i]=s.point(rr*Math.cos(angle)-side*Math.sin(angle),rr*Math.sin(angle)+side*Math.cos(angle),zz);}box(p,out,new Vec3[]{link[0],link[1],link[3],link[2],link[4],link[5],link[7],link[6]},s.light,0xFF657F88);}}
  }
 }}}
 private static void steam(View v,PoseStack.Pose p,VertexConsumer out){for(var s:v.rotors)for(int stage=0;stage<s.blades;stage++)for(int nozzle=0;nozzle<2;nozzle++)for(int n=0;n<s.wisps;n++){
  double age=(v.time*2.8+n/(double)s.wisps+stage*.21+nozzle*.37)%1;var q=TurbineVisuals.jet(s.width,stage,nozzle,age);Vec3 center=s.point(q.x(),q.y(),q.z());Vec3 normal=v.camera.subtract(center).normalize(),right=new Vec3(0,1,0).cross(normal).normalize();if(right.lengthSqr()<.01)right=new Vec3(1,0,0);Vec3 up=normal.cross(right).normalize();double size=.06+age*.13;right=right.scale(size);up=up.scale(size);
  int alpha=(int)(Math.sin(Math.PI*age)*135);quad(p,out,new Vec3[]{center.subtract(right).subtract(up),center.add(right).subtract(up),center.add(right).add(up),center.subtract(right).add(up)},s.light,(alpha<<24)|0xD9EFF5);
 }}
 private static void cylinder(Rotor s,PoseStack.Pose p,VertexConsumer out,double radius,double start,double end,int color){
  if(end<=start)return;for(int n=0;n<16;n++){double a=s.angle+n*Math.PI/8,b=a+Math.PI/8;double x0=Math.cos(a)*radius,y0=Math.sin(a)*radius,x1=Math.cos(b)*radius,y1=Math.sin(b)*radius;
   int shade=n%8==0?0xFFE0E8EC:color;
   quad(p,out,new Vec3[]{s.point(x0,y0,start),s.point(x1,y1,start),s.point(x1,y1,end),s.point(x0,y0,end)},s.light,shade);
   quad(p,out,new Vec3[]{s.point(0,0,start),s.point(x1,y1,start),s.point(x0,y0,start),s.point(0,0,start)},s.light,color);
   quad(p,out,new Vec3[]{s.point(0,0,end),s.point(x0,y0,end),s.point(x1,y1,end),s.point(0,0,end)},s.light,color);
  }
 }
 private static void box(PoseStack.Pose p,VertexConsumer out,Vec3[] points,int light,int color){int[][] faces={{0,3,2,1},{4,5,6,7},{0,1,5,4},{3,7,6,2},{0,4,7,3},{1,2,6,5}};for(var f:faces)quad(p,out,new Vec3[]{points[f[0]],points[f[1]],points[f[2]],points[f[3]]},light,color);}
 private static void quad(PoseStack.Pose p,VertexConsumer out,Vec3[] q,int light,int color){var n=q[1].subtract(q[0]).cross(q[2].subtract(q[0])).normalize();var normal=new org.joml.Vector3f((float)n.x,(float)n.y,(float)n.z);p.transformNormal(normal,normal);for(int i=0;i<4;i++)out.addVertex(p,(float)q[i].x,(float)q[i].y,(float)q[i].z).setColor(color).setUv(i==1||i==2?1:0,i>=2?1:0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x,normal.y,normal.z);}
 private TurbineClient(){}
}
