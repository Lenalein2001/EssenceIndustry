package com.neolena.essenceindustry.array;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.*;

/** Cached physical envelopes of the fixed housing. Moving tools and cosmetic fittings do not collide. */
final class ArrayHullShapes {
 record Piece(AABB bounds,String material,boolean glass,boolean cutaway){}
 record Opening(boolean side,int wall,double y0,double y1,double t0,double t1){}
 private record Size(int width,int depth,int height,List<BlockPos> windows){}
 private record Cell(int width,int depth,int x,int y,int z,Direction facing,int height,List<BlockPos> windows){}
 private static <K,V> Map<K,V> lru(int limit){return new LinkedHashMap<>(64,.75f,true){@Override protected boolean removeEldestEntry(Map.Entry<K,V> e){return size()>limit;}};}
 private static final Map<Size,List<Piece>> PROFILES=lru(64);
 private static final Map<Cell,VoxelShape> CELLS=lru(4096);
 private static final Map<Direction,VoxelShape> RACKS=new EnumMap<>(Direction.class),LOWER_RACKS=new EnumMap<>(Direction.class);
 static{var rack=rack();for(var face:List.of(Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST)){RACKS.put(face,rotate(rack,face));LOWER_RACKS.put(face,rotate(Shapes.or(rack,Shapes.box(0,-.2,0,1,0,1)),face));}}
 static VoxelShape shape(BlockState state,BlockGetter getter,BlockPos pos){
  if(!state.getValue(ArrayLayout.FORMED))return Shapes.block();
  if(state.is(ProcessingArray.RACK.get()))return (state.getValue(ArrayLayout.PART)==6?RACKS:LOWER_RACKS).get(ArrayLayout.facing(state));
  if(!(getter instanceof Level level))return Shapes.block();
  var owner=ArrayOwners.owner(level,pos);if(owner==null)return Shapes.block();
  var face=ArrayLayout.facing(owner.getBlockState());var local=ArrayLayout.local(owner.layoutAnchor(),face,pos);
  var shape=cell(owner.width(),owner.depth(),local,face,owner.height(),owner.visualWindows());
  if(state.getBlock() instanceof ArrayConnectorBlock||state.is(ProcessingArray.CONTROLLER.get())){
   // A hatch keeps a selectable socket even if fitted in a recessed window or floor tile.
   shape=Shapes.or(shape,rotate(Shapes.box(.0625,.0625,-.0625,.9375,.9375,.5),ArrayLayout.facing(state)));
  }
  return shape;
 }
 static synchronized VoxelShape cell(int width,int depth,BlockPos local,Direction face,int height,List<BlockPos> windows){
  var key=new Cell(width,depth,local.getX(),local.getY(),local.getZ(),face,height,windows);
  return CELLS.computeIfAbsent(key,k->{
   var boxes=pieces(width,depth,height,windows);var result=Shapes.empty();
   for(var piece:boxes){var box=piece.bounds;double a=Math.max(box.minX,k.x)-k.x,b=Math.max(box.minY,k.y)-k.y,c=Math.max(box.minZ,k.z)-k.z;
    double d=Math.min(box.maxX,k.x+1)-k.x,e=Math.min(box.maxY,k.y+1)-k.y,f=Math.min(box.maxZ,k.z+1)-k.z;
    if(a<d&&b<e&&c<f)result=Shapes.or(result,Shapes.box(a,b,c,d,e,f));
   }
   return rotate(result.optimize(),face);
  });
 }
 static synchronized void clear(){PROFILES.clear();CELLS.clear();}
 static synchronized List<Piece> pieces(int width,int depth,int height,List<BlockPos> windows){return PROFILES.computeIfAbsent(new Size(width,depth,height,windows),s->glaze(taller(profile(s.width,s.depth),s.height),s.windows,s.width,s.depth));}
 static boolean window(BlockPos p,int w,int d,int h){return p.getY()>=1&&p.getY()<=h-2&&((p.getX()==0||p.getX()==w-1)&&p.getZ()>0&&p.getZ()<d-1||(p.getZ()==0||p.getZ()==d-1)&&p.getX()>0&&p.getX()<w-1);}
 private static void add(List<Piece> out,Piece source,double x0,double y0,double z0,double x1,double y1,double z1){if(x0<x1&&y0<y1&&z0<z1)out.add(new Piece(new AABB(x0,y0,z0,x1,y1,z1),source.material,source.glass,source.cutaway));}
 private static List<Piece> glaze(List<Piece> original,List<BlockPos> windows,int w,int d){
  var pieces=new ArrayList<>(original);
  var openings=openings(w,windows);
  for(var opening:openings){
   var cut=opening.side?new AABB(opening.wall,opening.y0,opening.t0,opening.wall+1,opening.y1,opening.t1):new AABB(opening.t0,opening.y0,opening.wall,opening.t1,opening.y1,opening.wall+1);
   var next=new ArrayList<Piece>();
   for(var piece:pieces){var b=piece.bounds;if(!b.intersects(cut)){next.add(piece);continue;}
    double a=Math.max(b.minX,cut.minX),c=Math.max(b.minY,cut.minY),e=Math.max(b.minZ,cut.minZ),f=Math.min(b.maxX,cut.maxX),g=Math.min(b.maxY,cut.maxY),h=Math.min(b.maxZ,cut.maxZ);
    add(next,piece,b.minX,b.minY,b.minZ,a,b.maxY,b.maxZ);add(next,piece,f,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ);
    add(next,piece,a,b.minY,b.minZ,f,c,b.maxZ);add(next,piece,a,g,b.minZ,f,b.maxY,b.maxZ);
    add(next,piece,a,c,b.minZ,f,g,e);add(next,piece,a,c,h,f,g,b.maxZ);
   }
   pieces=next;
  }
  // Merge exact cut footprints, never a rectangular cell run with one cell's edge mask.
  // L/T corners keep their individual insets; complete rectangles still become one pane.
  for(var opening:mergedOpenings(openings)){
   double y0=opening.y0,t0=opening.t0,height=opening.y1-y0,length=opening.t1-t0;
   if(opening.side){double plane=opening.wall==0?.25:w-.28125;box(pieces,"glass",true,opening.wall==0,plane,y0,t0,.03125,height,length);}
   else {double plane=opening.wall==0?.25:d-.28125;box(pieces,"glass",true,opening.wall==0,t0,y0,plane,length,height,.03125);}
  }return List.copyOf(pieces);
 }
 static List<Opening> openings(int width,List<BlockPos> windows){
  var placed=new HashSet<>(windows);var result=new ArrayList<Opening>();
  for(var p:placed){boolean side=p.getX()==0||p.getX()==width-1;double along=side?p.getZ():p.getX();
   result.add(new Opening(side,side?p.getX():p.getZ(),p.getY()+(placed.contains(p.below())?0:.125),p.getY()+1-(placed.contains(p.above())?0:.125),along+(placed.contains(side?p.north():p.west())?0:.125),along+1-(placed.contains(side?p.south():p.east())?0:.125)));
  }
  result.sort(Comparator.comparing(Opening::side).thenComparingInt(Opening::wall).thenComparingDouble(Opening::y0).thenComparingDouble(Opening::t0));return List.copyOf(result);
 }
 static List<Opening> mergedOpenings(List<Opening> openings){
  var horizontal=mergeOpenings(openings,true);return List.copyOf(mergeOpenings(horizontal,false));
 }
 private static List<Opening> mergeOpenings(List<Opening> openings,boolean horizontal){
  var sorted=new ArrayList<>(openings);
  sorted.sort(Comparator.comparing(Opening::side).thenComparingInt(Opening::wall).thenComparingDouble(o->horizontal?o.y0:o.t0).thenComparingDouble(o->horizontal?o.y1:o.t1).thenComparingDouble(o->horizontal?o.t0:o.y0));
  var result=new ArrayList<Opening>();
  for(var b:sorted){
   if(!result.isEmpty()){var a=result.getLast();
    if(a.side==b.side&&a.wall==b.wall&&(horizontal?a.y0==b.y0&&a.y1==b.y1&&a.t1==b.t0:a.t0==b.t0&&a.t1==b.t1&&a.y1==b.y0)){
     result.set(result.size()-1,new Opening(a.side,a.wall,a.y0,horizontal?a.y1:b.y1,a.t0,horizontal?b.t1:a.t1));continue;
    }
   }result.add(b);
  }return result;
 }

 private static double lift(double y,int h){return y>=2.875?y+h-5:y;}
 private static List<Piece> taller(List<Piece> pieces,int h){if(h==5)return pieces;var out=new ArrayList<Piece>();for(var piece:pieces){var b=piece.bounds;out.add(new Piece(new AABB(b.minX,lift(b.minY,h),b.minZ,b.maxX,lift(b.maxY,h),b.maxZ),piece.material,piece.glass,piece.cutaway));}return List.copyOf(out);}
 private static void box(List<Piece> out,String material,double x,double y,double z,double w,double h,double d){box(out,material,false,false,x,y,z,w,h,d);}
 private static void box(List<Piece> out,String material,boolean glass,boolean cut,double x,double y,double z,double w,double h,double d){out.add(new Piece(new AABB(x,y,z,x+w,y+h,z+d),material,glass,cut));}
 private static List<Piece> profile(int w,int d){
  var out=new ArrayList<Piece>();
  box(out,"black",0,0,0,w,.28,d);box(out,"body",.08,.28,.08,w-.16,.32,d-.16);box(out,"edge",.22,.60,.22,w-.44,.12,d-.44);box(out,"inset",.45,.72,.45,w-.9,.08,d-.9);
  for(double x:new double[]{.0625,w-.625})for(double z:new double[]{.0625,d-.625}){
   box(out,"body",x,.75,z,.5625,3.34375,.5625);box(out,"metal",x-.03125,.75,z-.03125,.625,.25,.625);box(out,"metal",x-.03125,3.8125,z-.03125,.625,.28125,.625);
  }
  box(out,"body",.625,.78125,.125,w-1.25,.25,.3125);box(out,"body",.625,3.78125,.125,w-1.25,.3125,.3125);
  box(out,"body",.0625,1.0625,.03125,.6875,1,.5625);
  box(out,"glass",true,true,.65625,1.0625,.25,w-1.3125,2.625,.03125);
  for(double y:new double[]{1.03125,3.6875})box(out,"black",.625,y,.21875,w-1.25,y<2?.03125:.09375,.09375);
  for(int side=0;side<2;side++){
   double x=side==0?.125:w-.375;boolean cut=side==0;
   box(out,"body",false,cut,x,.78125,.625,.25,2.09375,d-1.25);box(out,"edge",false,cut,x,2.875,.625,.25,.1875,d-1.25);box(out,"body",false,cut,x,3.78125,.625,.25,.3125,d-1.25);
   for(double y:new double[]{3.0625,3.75})box(out,"black",false,cut,x,y,.625,.25,.03125,d-1.25);
   box(out,"glass",true,cut,x+.09375,3.09375,.65625,.03125,.65625,d-1.3125);
  }
  box(out,"body",.625,.78125,d-.375,w-1.25,3.3125,.25);
  for(double z:new double[]{.03125,d-.75})box(out,"edge",.03125,4.09375,z,w-.0625,.40625,.71875);
  for(double x:new double[]{.03125,w-.875})box(out,"edge",x,4.09375,.75,.84375,.40625,d-1.5);
  for(double z:new double[]{.15625,d-.65625})box(out,"body",.15625,4.5,z,w-.3125,.15625,.50);
  for(double x:new double[]{.15625,w-.78125})box(out,"body",x,4.5,.65625,.625,.15625,d-1.3125);
  box(out,"glass",true,true,.9375,4.3125,.78125,w-1.875,.03125,d-1.5625);
  box(out,"black",w*.5-.78125,4.5,.125,1.5625,.25,.875);box(out,"body",w*.5-.65625,4.75,.21875,1.3125,.1875,.65625);box(out,"edge",w*.5-.5,4.9375,.3125,1,.0625,.46875);
  return List.copyOf(out);
 }
 private static VoxelShape rack(){
  var shape=Shapes.box(0,0,0,1,.140625,1);
  for(double x:new double[]{0,.9375})for(double z:new double[]{0,.9375})shape=Shapes.or(shape,Shapes.box(x,.125,z,x+.0625,1,z+.0625));
  return Shapes.or(shape,Shapes.box(.0625,.125,.9375,.9375,1,1),Shapes.box(.0625,.9375,0,.9375,1,.0625)).optimize();
 }
 static VoxelShape rotate(VoxelShape source,Direction face){
  if(face==Direction.NORTH)return source;final VoxelShape[] result={Shapes.empty()};source.forAllBoxes((a,b,c,d,e,f)->{
   var rotated=switch(face){case EAST->Shapes.box(1-f,b,a,1-c,e,d);case SOUTH->Shapes.box(1-d,b,1-f,1-a,e,1-c);case WEST->Shapes.box(c,b,1-d,f,e,1-a);case UP->Shapes.box(a,1-f,b,d,1-c,e);case DOWN->Shapes.box(a,c,1-e,d,f,1-b);default->Shapes.box(a,b,c,d,e,f);};result[0]=Shapes.or(result[0],rotated);
  });return result[0].optimize();
 }
 private ArrayHullShapes(){}
}
