package com.neolena.essenceindustry.nuclear;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.neolena.essenceindustry.blueprint.*;
/** The native preview, bill of materials and assembled rotor use this same layout. */
public final class TurbineLayout {
 public static List<BlueprintPlan.Cell> create(BlueprintSettings s,Direction face){
  if(s.vertical())return vertical(s,face);
  int w=s.width(),h=s.height(),d=s.depth(),c=w/2,start=s.blades()+3;
  if((w!=5&&w!=7)||h!=w||d!=Math.max(9,s.blades()+s.rings()+4))throw new IllegalArgumentException("Turbine dimensions do not fit the rotor");
  var cells=new ArrayList<BlueprintPlan.Cell>();
  for(int y=0;y<h;y++)for(int z=0;z<d;z++)for(int x=0;x<w;x++){
   boolean shell=x==0||x==w-1||y==0||y==h-1||z==0||z==d-1;BlockState state=Blocks.AIR.defaultBlockState();
   if(shell){String k="casing";if(s.glass()>0&&(s.glass()<3?(x==0||x==w-1)&&y>=2&&y<h-1&&z>=2&&z<d-1:BlueprintPlan.wallWindow(s,x,y,z,2)))k="glass";if(s.glass()>0&&y==h-1&&x>1&&x<w-2&&z>=2&&z<d-1)k="roof_window";state=NuclearReactor.block(k).defaultBlockState();}
   if(x==c&&y==c&&z>0&&z<d-1)state=SteamTurbine.block(z==1||z==start-1?"bearing":z>=2&&z<2+s.blades()?"blade":"shaft").defaultBlockState();
   if(z>=start&&z<start+s.rings()&&Math.abs(x-c)<=1&&Math.abs(y-c)<=1&&(x!=c||y!=c))state=SteamTurbine.block("coil_"+TurbineCore.COILS[s.coil()]).defaultBlockState();
   if(y==0&&(x==c-1||x==c+1)&&z>0&&z<d-1)state=SteamTurbine.block("condenser").defaultBlockState();
   if((z==1||z==start-1)&&x==c&&y>0&&y<c)state=NuclearReactor.block("frame").defaultBlockState();
   if(w==7&&z>=start&&z<start+s.rings()&&y==1&&(x==c-1||x==c+1))state=SteamTurbine.block("foot").defaultBlockState();
   if(x==c&&y==h-2&&z>=2&&z<start-1)state=SteamTurbine.block("light").defaultBlockState();
   if(x==c&&y==1&&z==0)state=SteamTurbine.block("controller").defaultBlockState();
   if(x==c&&y==c&&z==0)state=NuclearReactor.portState("coolant_in");
   if(x==c&&y==c&&z==d-1)state=NuclearReactor.portState("energy_out");
   if(x==c&&y==1&&z==d-1)state=NuclearReactor.portState("coolant_out");
   var out=z==0?face:z==d-1?face.getOpposite():x==0?face.getCounterClockWise():x==w-1?face.getClockWise():y==0?Direction.DOWN:y==h-1?Direction.UP:face;
   if(state.hasProperty(BlockStateProperties.FACING))state=state.setValue(BlockStateProperties.FACING,out);
   if(state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))state=state.setValue(BlockStateProperties.HORIZONTAL_FACING,(SteamTurbine.kind(state).isEmpty()&&out.getAxis().isHorizontal())?out:face);
   cells.add(new BlueprintPlan.Cell(new BlockPos(x,y,z),state));
  }return List.copyOf(cells);
 }
 private static List<BlueprintPlan.Cell> vertical(BlueprintSettings s,Direction face){
  int w=s.width(),h=s.height(),d=s.depth(),c=w/2,start=s.blades()+3;
  if((w!=5&&w!=7)||d!=w||h!=Math.max(9,s.blades()+s.rings()+4))throw new IllegalArgumentException("Vertical turbine dimensions do not fit the rotor");
  var cells=new ArrayList<BlueprintPlan.Cell>();
  for(int y=0;y<h;y++)for(int z=0;z<d;z++)for(int x=0;x<w;x++){
   boolean shell=x==0||x==w-1||y==0||y==h-1||z==0||z==d-1;BlockState state=Blocks.AIR.defaultBlockState();
   if(shell){String k="casing";if(s.glass()>0&&(s.glass()<3?y>=2&&y<h-1&&((x==0||x==w-1)&&z>0&&z<d-1||(z==0||z==d-1)&&x>0&&x<w-1):BlueprintPlan.wallWindow(s,x,y,z,2)))k="glass";if(s.glass()>0&&y==h-1&&x>1&&x<w-2&&z>1&&z<d-2)k="roof_window";state=NuclearReactor.block(k).defaultBlockState();}
   if(x==c&&z==c&&y>0&&y<h-1)state=SteamTurbine.block(y==1||y==start-1?"bearing":y>=2&&y<2+s.blades()?"blade":"shaft").defaultBlockState();
   if(y>=start&&y<start+s.rings()&&Math.abs(x-c)<=1&&Math.abs(z-c)<=1&&(x!=c||z!=c))state=SteamTurbine.block("coil_"+TurbineCore.COILS[s.coil()]).defaultBlockState();
   // Bearing cross-members tie the rotating stack to the outer shell.
   if((y==1||y==start-1)&&!shell&&(x==c||z==c)&&(x!=c||z!=c))state=NuclearReactor.block("frame").defaultBlockState();
   if(y==0&&z>0&&z<d-1&&(x==c-1||x==c+1))state=SteamTurbine.block("condenser").defaultBlockState();
   if(y==1&&x==c&&z==0)state=SteamTurbine.block("controller").defaultBlockState();
   if(y==1&&x==1&&z==0)state=NuclearReactor.portState("coolant_in");
   if(y==1&&x==w-2&&z==0)state=NuclearReactor.portState("coolant_out");
   if(y==h-2&&x==c&&z==d-1)state=NuclearReactor.portState("energy_out");
   Direction out=z==0?face:z==d-1?face.getOpposite():x==0?face.getCounterClockWise():x==w-1?face.getClockWise():y==0?Direction.DOWN:y==h-1?Direction.UP:face;
   if(state.hasProperty(BlockStateProperties.FACING))state=state.setValue(BlockStateProperties.FACING,out);
   if(state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))state=state.setValue(BlockStateProperties.HORIZONTAL_FACING,SteamTurbine.kind(state).isEmpty()&&out.getAxis().isHorizontal()?out:face);
   if(state.hasProperty(SteamTurbine.Part.VERTICAL))state=state.setValue(SteamTurbine.Part.VERTICAL,true);
   cells.add(new BlueprintPlan.Cell(new BlockPos(x,y,z),state));
  }return List.copyOf(cells);
 }
 private TurbineLayout(){}
}
