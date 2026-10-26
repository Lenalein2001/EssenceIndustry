package com.neolena.essenceindustry.nuclear;
/** Bounded local geometry shared by the renderer and regression checks. */
public final class TurbineVisuals {
 public record Point(double x,double y,double z){}
 public static double radius(int width){return (width-2)/2d-.22;}
 public static double bladeHalfWidth(int width){return .16+radius(width)*.045;}
 public static int wisps(int flow,boolean reduced,boolean minimal){return flow<=0||minimal?0:reduced?1:3;}
 public static Point jet(int width,int stage,int nozzle,double progress){
  double a=Math.PI/4+nozzle*Math.PI,r=radius(width)+.12,radial=r-progress*.45,tangent=progress*.72;
  return new Point(Math.cos(a)*radial-Math.sin(a)*tangent,Math.sin(a)*radial+Math.cos(a)*tangent,2+stage-.24+progress*.25);
 }
 private TurbineVisuals(){}
}
