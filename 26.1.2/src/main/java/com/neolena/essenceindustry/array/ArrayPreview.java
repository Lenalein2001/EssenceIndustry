package com.neolena.essenceindustry.array;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

/** Client-only schematic: bounded draw count, no world queries or block-entity renderer. */
final class ArrayPreview {
 private static final int EDGE=0xFF65BDDE,DARK=0xFF172532,TOP=0xFF324454,SIDE=0xFF223240;
 private static void face(GuiGraphicsExtractor g,float x,float y,float ax,float ay,float bx,float by,int w,int h,int color){
  var pose=g.pose();pose.pushMatrix();pose.translate(x,y);pose.mul(new Matrix3x2f(ax,ay,bx,by,0,0));g.fill(0,0,w,h,color);pose.popMatrix();
 }
 private static void line(GuiGraphicsExtractor g,float x,float y,float endX,float endY,int color){
  var pose=g.pose();pose.pushMatrix();pose.translate(x,y);pose.rotate((float)Math.atan2(endY-y,endX-x));g.fill(0,0,(int)Math.ceil(Math.hypot(endX-x,endY-y)),1,color);pose.popMatrix();
 }
 static void draw(GuiGraphicsExtractor g,int x,int y,int w,int d,int racks,int installed,boolean formed,net.minecraft.world.item.ItemStack machine){
  // Native item rendering identifies the actual machine; repeated symbols represent occupied bays.
  g.fill(x+3,y+5,x+167,y+82,0xFF0D1722);g.outline(x+3,y+5,164,77,0xFF345064);
  g.fill(x+9,y+79,x+160,y+82,0xFF263C4C);g.fill(x+9,y+12,x+160,y+15,0xFF263C4C);
  if(!machine.isEmpty()){
   int shown=Math.min(6,Math.max(1,installed));
   for(int i=0;i<shown;i++){int col=i%3,row=i/3;var pose=g.pose();pose.pushMatrix();pose.translate(x+18+col*49,y+20+row*28);pose.scale(1.65f);g.item(machine,0,0);pose.popMatrix();}
  }else{g.outline(x+65,y+33,32,32,0xFF31495A);g.fill(x+79,y+40,x+82,y+58,0xFF5A7587);g.fill(x+71,y+48,x+90,y+51,0xFF5A7587);}
  int led=formed?EDGE:0xFFC8AA77;for(int n=0;n<7;n++)g.fill(x+12+n*22,y+84,x+23+n*22,y+86,n<Math.min(7,installed)?led:0xFF283847);
 }

 static void layer(GuiGraphicsExtractor g,int x,int y,int width,int depth,int height,int layer,int errorX,int errorY,int errorZ,boolean error){
  layer(g,x,y,width,depth,height,layer,errorX,errorY,errorZ,error,148,122);
 }
 static void layer(GuiGraphicsExtractor g,int x,int y,int width,int depth,int height,int layer,int errorX,int errorY,int errorZ,boolean error,int areaWidth,int areaHeight){
  width=Math.max(5,width);depth=Math.max(5,depth);float size=Math.min(areaWidth/(float)width,areaHeight/(float)depth);
  int ox=x+(int)((areaWidth-width*size)/2),oy=y+(int)((areaHeight-depth*size)/2);
  g.fill(ox-2,oy-2,ox+(int)(width*size)+2,oy+(int)(depth*size)+2,0xFF0E1823);
  for(int z=0;z<depth;z++)for(int a=0;a<width;a++){
   boolean shell=layer==0||layer==height-1||a==0||z==0||a==width-1||z==depth-1;
   boolean rack=(layer>=1&&layer<=height-3)&&a>=2&&a<=width-3&&z>=2&&z<=depth-3&&a%2==0&&z%2==0;
   if(!shell&&!rack)continue;
   int color=shell?0xFF3A5569:EDGE;if(error&&a==errorX&&layer==errorY&&z==errorZ)color=0xFFFF8C83;
   int gap=size>=4?1:0;g.fill(ox+(int)(a*size),oy+(int)(z*size),ox+(int)((a+1)*size)-gap,oy+(int)((z+1)*size)-gap,color);
  }
  if(layer==1)g.fill(ox,oy,ox+Math.max(2,(int)size),oy+Math.max(2,(int)size),0xFFE1D0F3);
 }
 private ArrayPreview(){}
}
