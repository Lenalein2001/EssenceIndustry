package com.neolena.essenceindustry.array;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import java.util.HashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Closed annulus with shared edges: no overlapping coplanar bar faces or jagged joints. */
final class ArrayRing {
 private static final int SEGMENTS=16;
 private static final Map<Float,float[]> MESHES=new HashMap<>();
 static void submit(PoseStack pose,SubmitNodeCollector collector,float radius,float thickness,float depth,Identifier texture,int light){
  var mesh=MESHES.computeIfAbsent(1-thickness/radius,ArrayRing::bake);
  pose.pushPose();pose.scale(radius,depth,radius);
  collector.submitCustomGeometry(pose,RenderTypes.entityCutout(texture),(transform,buffer)->draw(mesh,transform,buffer,light));
  pose.popPose();
 }
 private static float[] bake(float inner){
  var data=new float[SEGMENTS*4*4*8];int offset=0;
  for(int i=0;i<SEGMENTS;i++){
   double a=i*Math.PI*2/SEGMENTS,b=((i+1)%SEGMENTS)*Math.PI*2/SEGMENTS;
   float ax=(float)Math.cos(a),az=(float)Math.sin(a),bx=(float)Math.cos(b),bz=(float)Math.sin(b);
   offset=quad(data,offset,new float[]{ax,1,az,inner*ax,1,inner*az,inner*bx,1,inner*bz,bx,1,bz},0,1,0);
   offset=quad(data,offset,new float[]{ax,0,az,bx,0,bz,inner*bx,0,inner*bz,inner*ax,0,inner*az},0,-1,0);
   float nx=(float)Math.cos(a+Math.PI/SEGMENTS),nz=(float)Math.sin(a+Math.PI/SEGMENTS);
   offset=quad(data,offset,new float[]{ax,0,az,ax,1,az,bx,1,bz,bx,0,bz},nx,0,nz);
   offset=quad(data,offset,new float[]{inner*ax,0,inner*az,inner*bx,0,inner*bz,inner*bx,1,inner*bz,inner*ax,1,inner*az},-nx,0,-nz);
  }
  return data;
 }
 private static int quad(float[] data,int offset,float[] positions,float nx,float ny,float nz){
  for(int i=0;i<4;i++){
   data[offset++]=positions[i*3];data[offset++]=positions[i*3+1];data[offset++]=positions[i*3+2];
   data[offset++]=nx;data[offset++]=ny;data[offset++]=nz;
   data[offset++]=(i==0||i==3)?.125f:.375f;data[offset++]=i<2?.625f:.875f;
  }
  return offset;
 }
 private static void draw(float[] mesh,PoseStack.Pose pose,VertexConsumer buffer,int light){
  var normal=new org.joml.Vector3f();
  for(int quad=0;quad<mesh.length;quad+=32){
   pose.transformNormal(mesh[quad+3],mesh[quad+4],mesh[quad+5],normal);
   for(int i=quad;i<quad+32;i+=8)buffer.addVertex(pose,mesh[i],mesh[i+1],mesh[i+2]).setColor(-1).setUv(mesh[i+6],mesh[i+7]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x,normal.y,normal.z);
  }
 }
 private ArrayRing(){}
}
