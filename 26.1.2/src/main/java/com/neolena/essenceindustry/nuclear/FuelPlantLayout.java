package com.neolena.essenceindustry.nuclear;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/** Fixed, buildable process plants. The same manifest supplies GuideME layer diagrams. */
public final class FuelPlantLayout {
 public record Cell(BlockPos local,String kind,Map<String,String> properties){}
 public record Layout(int width,int depth,int height,List<Cell> cells){
  public BlockPos position(BlockPos controller,Direction facing,BlockPos local){return NuclearLayout.pos(controller,facing,width,local.getX(),local.getY(),local.getZ());}
  public BlockPos minimum(BlockPos controller,Direction facing){var a=position(controller,facing,BlockPos.ZERO);var b=position(controller,facing,new BlockPos(width-1,height-1,depth-1));return new BlockPos(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()));}
  public BlockPos maximum(BlockPos controller,Direction facing){var a=position(controller,facing,BlockPos.ZERO);var b=position(controller,facing,new BlockPos(width-1,height-1,depth-1));return new BlockPos(Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ()));}
 }
 private static final Map<String,Layout> LAYOUTS=load();
 private static Map<String,Layout> load(){
  try(var stream=FuelPlantLayout.class.getResourceAsStream("/data/essenceindustry/fuel_plant_layouts.json")){
   if(stream==null)throw new IllegalStateException("Missing fuel plant layout manifest");
   var root=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();var result=new LinkedHashMap<String,Layout>();
   for(var entry:root.entrySet()){
    var object=entry.getValue().getAsJsonObject();var size=object.getAsJsonArray("dimensions");var cells=new ArrayList<Cell>();var unique=new HashSet<BlockPos>();
    for(var value:object.getAsJsonArray("blocks")){var cell=value.getAsJsonObject();var xyz=cell.getAsJsonArray("pos");var pos=new BlockPos(xyz.get(0).getAsInt(),xyz.get(1).getAsInt(),xyz.get(2).getAsInt());if(!unique.add(pos))throw new IllegalStateException("Overlapping plant cells");var properties=new LinkedHashMap<String,String>();for(var p:cell.entrySet())if(!p.getKey().equals("pos")&&!p.getKey().equals("kind")&&p.getValue().isJsonPrimitive())properties.put(p.getKey(),p.getValue().getAsString());cells.add(new Cell(pos,cell.get("kind").getAsString(),Map.copyOf(properties)));}
    result.put(entry.getKey(),new Layout(size.get(0).getAsInt(),size.get(1).getAsInt(),size.get(2).getAsInt(),List.copyOf(cells)));
   }
   return Map.copyOf(result);
  }catch(IOException|RuntimeException error){throw new IllegalStateException("Cannot read fixed fuel plant layouts",error);}
 }
 public static Layout get(String machine){var layout=LAYOUTS.get(machine);if(layout==null)throw new IllegalArgumentException("Unknown fuel processor: "+machine);return layout;}
 public static Block block(String machine,String kind){return kind.equals("controller")?NuclearFuels.MACHINES.get(machine).get():NuclearReactor.block(kind);}
 public static BlockState state(String machine,Cell cell,Direction facing,boolean running){
  var state=NuclearReactor.PORTS.contains(cell.kind())?NuclearReactor.portState(cell.kind()):block(machine,cell.kind()).defaultBlockState();
  for(var entry:cell.properties().entrySet())state=property(state,entry.getKey(),entry.getValue());if(cell.kind().equals("roof_window"))state=property(state,"facing","up");
  state=property(state,"powered",Boolean.toString(running));state=property(state,"formed","true");
  if(facing!=Direction.NORTH){var rotation=switch(facing){case EAST->net.minecraft.world.level.block.Rotation.CLOCKWISE_90;case SOUTH->net.minecraft.world.level.block.Rotation.CLOCKWISE_180;case WEST->net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90;default->net.minecraft.world.level.block.Rotation.NONE;};state=state.rotate(rotation);}
  return state;
 }
 private static <T extends Comparable<T>> BlockState value(BlockState state,Property<T> property,String value){var decoded=property.getValue(value);return decoded.isPresent()?state.setValue(property,decoded.get()):state;}
 public static BlockState property(BlockState state,String key,String value){var property=state.getBlock().getStateDefinition().getProperty(key);return property==null?state:value(state,property,value);}
 private FuelPlantLayout(){}
}
