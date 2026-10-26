package com.neolena.essenceindustry.construction;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
/** Shared deterministic preview/placement geometry. No chunk acquisition, inventory access or mutation. */
public final class BuilderPlan {
 public static final int LIMIT=4096;
 public static List<BlockPos> create(Level level,BlockPos clicked,Direction face,Direction heading,BuilderSettings s){
  if(count(s)>LIMIT)return List.of();
  Direction u=face.getAxis()==Direction.Axis.Y?heading.getClockWise():face.getClockWise();
  Direction v=face.getAxis()==Direction.Axis.Y?heading:Direction.UP;
  for(int n=0;n<s.rotation();n++){var old=u;u=v;v=old.getOpposite();}
  BlockPos start=clicked.relative(face,1+s.offset());var out=new ArrayList<BlockPos>();
  if(s.mode()==0){var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(clicked);var match=level.getBlockState(clicked).getBlock();
   while(!queue.isEmpty()&&seen.size()<LIMIT){var p=queue.removeFirst();if(!seen.add(p))continue;
    if(!within(p.getX()-clicked.getX(),s.width())||!within(p.getY()-clicked.getY(),s.height())||!within(p.getZ()-clicked.getZ(),s.depth())||!level.hasChunkAt(p)||!level.getBlockState(p).is(match))continue;
    out.add(p.relative(face,1+s.offset()));for(var d:List.of(u,u.getOpposite(),v,v.getOpposite())){var next=p.relative(d);if(!seen.contains(next))queue.add(next);}
   }return List.copyOf(out);
  }
  Direction forward=heading;for(int n=0;n<s.rotation();n++)forward=forward.getClockWise();
  Direction along=forward;
  if(s.mode()>=1){u=forward.getClockWise();v=Direction.UP;}
  int w=s.mode()==2?1:s.width(),h=(s.mode()==1||s.mode()==3)?1:s.height(),d=s.mode()>=6?s.depth():1;
  if(s.mode()==3){v=forward;h=s.depth();}


  if(s.mode()==5){v=forward;h=s.height();}
  int x0=s.centered()?-(w-1)/2:0,y0=s.centered()&&s.mode()!=2&&s.mode()!=4&&s.mode()!=5&&s.mode()<6?-(h-1)/2:0;
  var unique=new LinkedHashSet<BlockPos>();
  outer:for(int z=0;z<d;z++)for(int y=0;y<h;y++)for(int x=0;x<w;x++){
   int edges=(x==0||x==w-1?1:0)+(y==0||y==h-1?1:0)+(z==0||z==d-1?1:0);
   if(s.mode()==7&&edges==0||s.mode()==8&&edges<2)continue;
   var p=start.relative(u,x+x0).relative(v,y+y0).relative(along,z-(s.centered()?(d-1)/2:0));
   if(s.mode()==5)p=p.above(y);
   unique.add(p);if(unique.size()>LIMIT)return List.of();
  }return List.copyOf(unique);
 }
 private static boolean within(int delta,int size){return delta>=-((size-1)/2)&&delta<=size/2;}
 public static int count(BuilderSettings s){int w=s.width(),h=s.height(),d=s.depth(),x=Math.max(0,w-2),y=Math.max(0,h-2),z=Math.max(0,d-2);return switch(s.mode()){case 0->0;case 1->w;case 2->h;case 3->w*d;case 4,5->w*h;case 6->w*h*d;case 7->w*h*d-x*y*z;default->w*h*d-x*y*z-(w-x)*y*z-x*(h-y)*z-x*y*(d-z);};}
 private BuilderPlan(){}
}
