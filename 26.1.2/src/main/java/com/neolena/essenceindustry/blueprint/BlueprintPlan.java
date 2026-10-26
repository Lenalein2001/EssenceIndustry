package com.neolena.essenceindustry.blueprint;
import com.neolena.essenceindustry.nuclear.*;
import com.neolena.essenceindustry.array.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import java.util.*;
/** Pure shared recipe for material accounting, layer preview and actual placement. */
public final class BlueprintPlan {
 public record Cell(BlockPos local,BlockState state){}
 public static int maximum(int kind,int axis){if(kind==1&&!net.neoforged.fml.ModList.get().isLoaded("ae2"))return 5;if(kind==1)return switch(axis){case 0->ArrayConfig.maxWidth();case 1->ArrayConfig.maxDepth();default->ArrayConfig.maxHeight();};return switch(axis){case 0->ReactorConfig.maxWidth();case 1->ReactorConfig.maxDepth();default->ReactorConfig.maxHeight();};}
 public static String machine(int kind){return switch(kind){case 2->"fuel_enricher";case 3->"fuel_reprocessor";case 4->"fuel_fabricator";default->"";};}
 public static BlockPos position(BlockPos anchor,Direction face,BlueprintSettings s,BlockPos local){return s.kind()==1?ArrayLayout.position(anchor,face,local.getX(),local.getY(),local.getZ()):NuclearLayout.pos(anchor,face,s.width(),local.getX(),local.getY(),local.getZ());}
 public static List<Cell> create(BlueprintSettings s,Direction facing){return create(s,facing,null);}
 /** Empty drafts can be previewed; the worker separately requires at least one fuel column. */
 public static List<Cell> create(BlueprintSettings s,Direction facing,ReactorCoreDesign design){
  if(design!=null&&(s.kind()!=0||design.width()!=s.width()||design.depth()!=s.depth()))throw new IllegalArgumentException("Core footprint dimensions do not match the reactor plan");
  if(s.kind()==5)return TurbineLayout.create(s,facing);if(s.kind()==1&&!net.neoforged.fml.ModList.get().isLoaded("ae2"))throw new IllegalArgumentException("Array requires AE2");
  if(s.fixed()){var layout=FuelPlantLayout.get(machine(s.kind()));if(s.width()!=layout.width()||s.depth()!=layout.depth()||s.height()!=layout.height())throw new IllegalArgumentException("Fixed layout dimensions");var map=new HashMap<BlockPos,BlockState>();for(var cell:layout.cells()){
    var pos=cell.local();var state=FuelPlantLayout.state(machine(s.kind()),cell,facing,false);
    if(s.glass()!=1&&Set.of("pressure_panel","glass","roof_window").contains(cell.kind())){
     boolean roof=pos.getY()==s.height()-1&&pos.getX()>0&&pos.getX()<s.width()-1&&pos.getZ()>0&&pos.getZ()<s.depth()-1;
     boolean window=roof?s.glass()>0:wallWindow(s,pos.getX(),pos.getY(),pos.getZ(),1);
     state=orient(NuclearReactor.block(window?"glass":"casing").defaultBlockState(),facing,pos.getX(),pos.getY(),pos.getZ(),s.width(),s.depth(),s.height());
    }
    map.put(pos,state);
   }return volume(s,map);}
  if(s.width()<5||s.depth()<5||s.height()<5||s.kind()==1&&s.height()%2==0)throw new IllegalArgumentException("Invalid dimensions");
  if(s.width()>maximum(s.kind(),0)||s.depth()>maximum(s.kind(),1)||s.height()>maximum(s.kind(),2))throw new IllegalArgumentException("Size exceeds server limits");
  var map=new HashMap<BlockPos,BlockState>();int w=s.width(),d=s.depth(),h=s.height();var controls=s.kind()==0?new HashSet<>(design==null?ReactorControlLayout.plannedControls(w,d,s.pattern()):design.columns(ReactorCoreDesign.FUEL)):Set.<BlockPos>of();
  for(int y=0;y<h;y++)for(int z=0;z<d;z++)for(int x=0;x<w;x++){
   boolean shell=x==0||x==w-1||y==0||y==h-1||z==0||z==d-1;BlockState state=Blocks.AIR.defaultBlockState();
   if(s.kind()==1){if(shell){boolean window=wallWindow(s,x,y,z,1);state=(window?ProcessingArray.GLASS:ProcessingArray.CASING).get().defaultBlockState();}else if(ArrayLayout.socket(x,y,z,w,d,h)&&(s.pattern()==0||x==2&&z==2))state=ProcessingArray.RACK.get().defaultBlockState();}
   else {
    if(shell){String k="casing";if(wallWindow(s,x,y,z,2))k="glass";if(s.glass()>0&&y==h-1&&x>0&&x<w-1&&z>0&&z<d-1)k="roof_window";state=NuclearReactor.block(k).defaultBlockState();}
    else {String k;
     if(design!=null)k=switch(design.at(x,z)){case ReactorCoreDesign.FUEL->"fuel_assembly";case ReactorCoreDesign.MODERATOR->"moderator";case ReactorCoreDesign.REFLECTOR->"reflector";default->"";};
     else{k="";boolean fuel=ReactorControlLayout.isFuel(x,z,w,d,s.pattern());if(fuel)k="fuel_assembly";else if(s.filler()>0)k=s.filler()==1?"moderator":"reflector";}
     if(!k.isEmpty())state=NuclearReactor.block(k).defaultBlockState();
    }
   }
   map.put(new BlockPos(x,y,z),orient(state,facing,x,y,z,w,d,h));
  }
  if(s.kind()==1){map.put(new BlockPos(0,1,0),ProcessingArray.CONTROLLER.get().defaultBlockState().setValue(ArrayLayout.FACING,facing));map.put(new BlockPos(2,1,0),ProcessingArray.PORT.get().defaultBlockState().setValue(ArrayConnectorBlock.FACING,facing));
   int left=s.hatches();for(int y=1;y<h-1&&left>0;y++)for(int x=1;x<w-1&&left>0;x++)if(!(y==1&&x==2)){map.put(new BlockPos(x,y,0),ProcessingArray.HATCH.get().defaultBlockState().setValue(ArrayConnectorBlock.FACING,facing));left--;}
   if(left!=0)throw new IllegalArgumentException("Not enough hatch positions");
  }else {put(map,"controller",w/2,1,0,facing,w,d,h);put(map,"fuel_input",1,1,0,facing,w,d,h);put(map,"waste_output",w-2,1,0,facing,w,d,h);put(map,"energy_out",w-1,1,1,facing,w,d,h);for(var p:controls)put(map,"actuator",p.getX(),h-1,p.getZ(),facing,w,d,h);if(s.steam()==1){put(map,"coolant_in",0,1,2,facing,w,d,h);put(map,"coolant_out",w-1,1,2,facing,w,d,h);}}
  return volume(s,map);
 }
 private static List<Cell> volume(BlueprintSettings s,Map<BlockPos,BlockState> states){var result=new ArrayList<Cell>(s.width()*s.depth()*s.height());for(int y=0;y<s.height();y++)for(int z=0;z<s.depth();z++)for(int x=0;x<s.width();x++){var p=new BlockPos(x,y,z);result.add(new Cell(p,states.getOrDefault(p,Blocks.AIR.defaultBlockState())));}return List.copyOf(result);}
 private static void put(Map<BlockPos,BlockState> map,String kind,int x,int y,int z,Direction face,int w,int d,int h){map.put(new BlockPos(x,y,z),orient(NuclearReactor.PORTS.contains(kind)?NuclearReactor.portState(kind):NuclearReactor.block(kind).defaultBlockState(),face,x,y,z,w,d,h));}
 private static BlockState orient(BlockState state,Direction face,int x,int y,int z,int w,int d,int h){Direction out=y==0?Direction.DOWN:y==h-1?Direction.UP:z==0?face:z==d-1?face.getOpposite():x==0?face.getCounterClockWise():x==w-1?face.getClockWise():face;
  if(state.hasProperty(BlockStateProperties.FACING))return state.setValue(BlockStateProperties.FACING,out);if(state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))return state.setValue(BlockStateProperties.HORIZONTAL_FACING,out.getAxis().isHorizontal()?out:face);return state;}
 /** Styles 0-2 retain their original footprints for saved blueprints. */
 public static boolean wallWindow(BlueprintSettings s,int x,int y,int z,int bottom){
  int w=s.width(),d=s.depth(),h=s.height(),style=s.glass();
  boolean side=(x==0||x==w-1)&&z>0&&z<d-1;
  boolean end=(z==0||z==d-1)&&x>0&&x<w-1;
  if(y<bottom||y>=h-1||!(side||end))return false;
  int u=side?z:x,span=side?d:w;
  return switch(style){
   case 1->side&&y==h-2;
   case 2->side;
   case 3->true;
   case 4->u>=Math.max(1,span/4)&&u<=span-1-Math.max(1,span/4)&&y>=Math.max(bottom,h/4)&&y<=h-1-Math.max(1,h/4);
   case 5->Math.abs(u-(span-1)/2d)+Math.abs(y-(h-1)/2d)<=Math.max(1,Math.min(span,h)/3d);
   case 6->u%3!=0;
   case 7->y%3!=0;
   default->false;
  };
 }
 private BlueprintPlan(){}
}
