package com.neolena.essenceindustry.array;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/** One continuous formed housing. Its detail count does not grow with the shell block count. */
final class ArrayHousing {
 private static final Identifier BODY=material("body"),EDGE=material("edge"),INSET=material("inset"),METAL=material("metal"),BLACK=material("black"),BLUE=material("blue"),GLASS=material("glass"),SERVICE=material("service");
 private static final java.util.Map<String,Identifier> MATERIALS=java.util.Map.of("body",BODY,"edge",EDGE,"inset",INSET,"metal",METAL,"black",BLACK,"blue",BLUE,"glass",GLASS,"service",SERVICE);
 private record DetailKey(int width,int depth,int height,List<BlockPos> windows){}
 record SideDetails(boolean panel,boolean trim,boolean light,boolean cartridge,boolean frontLatch,boolean rearLatch){}
 record Details(SideDetails left,SideDetails right,boolean rearPanel,boolean rearSpine){}
 private static final Map<DetailKey,Details> DETAILS=new LinkedHashMap<>(32,.75f,true){@Override protected boolean removeEldestEntry(Map.Entry<DetailKey,Details> entry){return size()>64;}};
 static synchronized Details details(int width,int depth,int height,List<BlockPos> windows){
  return DETAILS.computeIfAbsent(new DetailKey(width,depth,height,windows),key->{
   var openings=ArrayHullShapes.openings(width,windows);
   return new Details(sideDetails(openings,0,depth,height),sideDetails(openings,width-1,depth,height),clear(openings,false,depth-1,1.28125,3.75,.90625,width-.90625),clear(openings,false,depth-1,1.42,3.58,width*.5-.18,width*.5+.18));
  });
 }
 private static SideDetails sideDetails(List<ArrayHullShapes.Opening> openings,int wall,int depth,int height){
  return new SideDetails(clear(openings,true,wall,1.21875,2.5625,.875,depth-.875),clear(openings,true,wall,2.625,2.6875,.875,depth-.875),clear(openings,true,wall,height-2.09375,height-2.0625,.90625,depth-.90625),clear(openings,true,wall,1.28125,2.46875,depth-2.0625,depth-.875),clear(openings,true,wall,2.109375,2.359375,2.0625,2.125),clear(openings,true,wall,2.109375,2.359375,depth-2.21875,depth-2.15625));
 }
 private static boolean clear(List<ArrayHullShapes.Opening> openings,boolean side,int wall,double y0,double y1,double t0,double t1){
  for(var opening:openings)if(opening.side()==side&&opening.wall()==wall&&opening.y0()<y1&&opening.y1()>y0&&opening.t0()<t1&&opening.t1()>t0)return false;return true;
 }
 private static Identifier material(String name){return Identifier.parse("essenceindustry:textures/block/array/hull_"+name+".png");}
 private static void part(PoseStack p,SubmitNodeCollector c,double x,double y,double z,float w,float h,float d,Identifier texture,int light){ArrayRenderer.box(p,c,x,y,z,w,h,d,texture,-1,light,false);}
 private static void glass(PoseStack p,SubmitNodeCollector c,double x,double y,double z,float w,float h,float d,int light){ArrayRenderer.box(p,c,x,y,z,w,h,d,GLASS,-1,light,true);}
 static void draw(ArrayRenderer.State s,PoseStack p,SubmitNodeCollector c){
  float w=s.width,d=s.depth;int light=s.lightCoords;var details=details(s.width,s.depth,s.height,s.windows);
  // The fixed visible shell and its collision envelopes share this immutable geometry.
  for(var piece:ArrayHullShapes.pieces(s.width,s.depth,s.height,s.windows)){
   if(s.cutaway&&piece.cutaway())continue;var b=piece.bounds();var texture=MATERIALS.get(piece.material());
   if(piece.glass())glass(p,c,b.minX,b.minY,b.minZ,(float)b.getXsize(),(float)b.getYsize(),(float)b.getZsize(),light);
   else part(p,c,b.minX,b.minY,b.minZ,(float)b.getXsize(),(float)b.getYsize(),(float)b.getZsize(),texture,light);
  }
  part(p,c,.625,s.height-1.28125,.15625,w-1.25f,.03125f,.0625f,BLUE,15728880);
  p.pushPose();p.translate(s.controllerLocal.getX(),s.controllerLocal.getY()-1,s.controllerLocal.getZ());
  // A complete wall socket also supports manually relocated controllers on glass fronts.
  part(p,c,.0625,1.0625,-.0625,.875f,.875f,.21875f,BLACK,light);
  part(p,c,.09375,1.09375,-.078125,.8125f,.8125f,.03125f,METAL,light);
  part(p,c,.125,1.125,-.09375,.75f,.75f,.03125f,BLACK,light);
  part(p,c,.25,1.5,-.109375,.5f,.1875f,.015625f,INSET,light);
  part(p,c,.3125,1.703125,-.125,.375f,.03125f,.015625f,BLUE,15728880);
  for(int row=0;row<3;row++)part(p,c,.25,1.21875+row*.0625,-.109375,.5f,.015625f,.015625f,METAL,light);
  p.popPose();
  // All side fittings share one mirrored mounting plane and one geometric pixel grid.
  for(int side=0;side<2;side++){
   if(s.cutaway&&side==0)continue;
   var fittings=side==0?details.left:details.right;
   if(fittings.panel){
   sidePart(p,c,side,w,0,1.21875,.875,.015625f,1.34375f,d-1.75f,SERVICE,light);
   // Engraved service seams and fasteners use geometry: no stretched pixel decals.
   for(float yy:new float[]{1.25f,2.515625f})sidePart(p,c,side,w,.015625,yy,.90625,.015625f,.015625f,d-1.8125f,BLACK,light);
   for(float zz:new float[]{.90625f,d-.921875f})sidePart(p,c,side,w,.015625,1.265625,zz,.015625f,1.25f,.015625f,BLACK,light);
   }
   if(fittings.trim)sidePart(p,c,side,w,0,2.625,.875,.03125f,.0625f,d-1.75f,METAL,light);
   if(fittings.light)sidePart(p,c,side,w,0,s.height-2.09375,.90625,.03125f,.03125f,d-1.8125f,BLUE,15728880);
   if(s.detailed){
    if(fittings.cartridge){
    // One centered cartridge, with fins inset in a symmetrical metal frame.
    float rear=d-2.0625f;
    sidePart(p,c,side,w,.015625,1.28125,rear,.0625f,1.1875f,1.1875f,BLACK,light);
    sidePart(p,c,side,w,.078125,1.3125,rear+.03125,.03125f,1.125f,1.125f,METAL,light);
    sidePart(p,c,side,w,.109375,1.375,rear+.09375,.015625f,1.0f,1.0f,INSET,light);
    for(int fin=0;fin<7;fin++)sidePart(p,c,side,w,.125,1.421875,rear+.171875+fin*.125,.015625f,.90625f,.0625f,BLACK,light);
    for(float yy:new float[]{1.328125f,2.390625f})for(float zz:new float[]{rear+.046875f,rear+1.109375f}){
     sidePart(p,c,side,w,.109375,yy,zz,.03125f,.03125f,.03125f,BLACK,light);
     sidePart(p,c,side,w,.140625,yy+.0078125,zz+.0078125,.0078125f,.015625f,.015625f,METAL,light);
    }
    }
    if(fittings.frontLatch)sidePart(p,c,side,w,.015625,2.109375,2.0625,.0625f,.25f,.0625f,METAL,light);
    if(fittings.rearLatch)sidePart(p,c,side,w,.015625,2.109375,d-2.21875,.0625f,.25f,.0625f,METAL,light);
   }
  }
  // Rear spine: a restrained mechanical/service face rather than another front.
  if(details.rearPanel)part(p,c,.90625,1.28125,d-.125,w-1.8125f,2.46875f,.015625f,INSET,light);
  if(details.rearSpine){
  part(p,c,w*.5-.18,1.42,d-.109375,.36f,2.16f,.06f,METAL,light);
  part(p,c,w*.5-.025,1.60,d-.046875,.05f,1.80f,.025f,BLUE,15728880);
  }
  p.pushPose();p.translate(0,s.height-5,0);
  // Roof display, fan and conduit details sit on the shared structural shell.
  part(p,c,w*.5-.46875,4.625,.09375,.9375f,.1875f,.03125f,SERVICE,light);
  part(p,c,w*.5-.28125,4.703125,.078125,.5625f,.03125f,.015625f,BLUE,15728880);
  if(s.detailed){
   for(float x:new float[]{.515625f,w-.515625f}){
    part(p,c,x-.34375,4.65625,d-1.5625,.6875f,.09375f,.90625f,BLACK,light);
    ring(p,c,x,4.75,d-1.125,.28125f,.0625f,.09375f,true,METAL,light);
    for(int blade=0;blade<4;blade++){
     p.pushPose();p.translate(x,4.765625,d-1.125);p.mulPose(Axis.YP.rotationDegrees(blade*90+ArrayRenderer.fanAngle(s)));
     part(p,c,-.0625,0,.046875,.125f,.03125f,.1875f,EDGE,light);p.popPose();
    }
    part(p,c,x-.0625,4.765625,d-1.1875,.125f,.0625f,.125f,BLUE,15728880);
   }
   for(float x:new float[]{.89f,w-.99f}){
    part(p,c,x,3.94,.94,.10f,.10f,d-1.88f,BLACK,light);
    for(int clamp=0;clamp<4;clamp++)part(p,c,x-.025,3.92,1.0+(d-2.0)*clamp/3,.15f,.15f,.06f,METAL,light);
   }
  }
  p.popPose();
  // The rack frames themselves are baked into native chunk meshes, at their true positions.
  // A continuous pair of ceiling work lights makes the rear bays legible through the glazing.
  for(float x:new float[]{.90625f,w-1.03125f}){
   part(p,c,x,s.height-1.375,1.0,.125f,.0625f,d-2.0f,BLACK,light);
   part(p,c,x+.03125,s.height-1.390625,1.0625,.0625f,.015625f,d-2.125f,BLUE,15728880);
  }
 }
 static void carriage(ArrayRenderer.State s,PoseStack p,SubmitNodeCollector c,float z){
  int light=s.lightCoords;float center=s.width*.5f;
  p.pushPose();p.translate(0,s.height-1.2,0);p.scale(1,ArrayLayout.HEAD_SCALE,1);p.translate(0,-3.8,0);
  part(p,c,center-.51,3.15,z-.20,1.02f,.19f,.67f,EDGE,light);
  part(p,c,center-.36,2.57,z-.12,.72f,.58f,.52f,BODY,light);
  part(p,c,center-.27,2.67,z-.15,.54f,.27f,.025f,SERVICE,light);
  for(float x:new float[]{center-.45f,center+.33f}){
   part(p,c,x,2.39,z-.10,.12f,.74f,.45f,METAL,light);
   part(p,c,x+.025,2.48,z-.13,.07f,.40f,.025f,BLACK,light);
  }
  part(p,c,center-.15,2.31,z+.015,.30f,.26f,.30f,EDGE,light);
  part(p,c,center-.09,2.26,z+.075,.18f,.08f,.18f,BLUE,15728880);
  if(s.detailed){
   ring(p,c,center,2.39,z+.16,.22f,.045f,.10f,true,METAL,light);
   for(float x:new float[]{.70f,s.width-1.05f}){
    part(p,c,x,3.00,z-.22,.36f,.37f,.70f,BODY,light);
    part(p,c,x-.025,3.10,z-.17,.41f,.12f,.13f,METAL,light);
    part(p,c,x-.025,3.10,z+.25,.41f,.12f,.13f,METAL,light);
   }
   part(p,c,.90,3.29,z+.06,s.width-1.80f,.055f,.055f,BLACK,light);
  }
  p.popPose();
 }
 private static void sidePart(PoseStack p,SubmitNodeCollector c,int side,float width,double projection,double y,double z,float depth,float height,float length,Identifier material,int light){
  double x=side==0?.125-projection-depth:width-.125+projection;
  part(p,c,x,y,z,depth,height,length,material,light);
 }
 private static void ring(PoseStack p,SubmitNodeCollector c,double x,double y,double z,float radius,float thickness,float depth,boolean horizontal,Identifier material,int light){
  p.pushPose();p.translate(x,y,z);if(!horizontal)p.mulPose(Axis.ZP.rotationDegrees(-90));
  ArrayRing.submit(p,c,radius,thickness,depth,material,light);p.popPose();
 }
 private ArrayHousing(){}
}
