package com.neolena.essenceindustry.blueprint;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.neolena.essenceindustry.nuclear.ReactorControlLayout;
import net.minecraft.core.BlockPos;
import java.util.*;

/** Immutable horizontal reactor footprint. Dimensions include the shell; cells contain only the interior. */
public record ReactorCoreDesign(int width,int depth,List<Integer> cells) {
 public static final int AIR=0,FUEL=1,CONTROL=2,MODERATOR=3,REFLECTOR=4,MAX_CELLS=62*62;
 private record Stored(int width,int depth,List<Integer> cells){}
 private static final Codec<Stored> STORED=RecordCodecBuilder.create(i->i.group(
  Codec.intRange(5,64).fieldOf("width").forGetter(Stored::width),Codec.intRange(5,64).fieldOf("depth").forGetter(Stored::depth),
  Codec.intRange(AIR,REFLECTOR).listOf(9,MAX_CELLS).fieldOf("cells").forGetter(Stored::cells)).apply(i,Stored::new));
 public static final Codec<ReactorCoreDesign> CODEC=STORED.flatXmap(raw->{
  try{return DataResult.success(new ReactorCoreDesign(raw.width(),raw.depth(),raw.cells()));}
  catch(IllegalArgumentException|NullPointerException ex){return DataResult.error(()->"Invalid reactor footprint: "+ex.getMessage());}
 },design->DataResult.success(new Stored(design.width(),design.depth(),design.cells())));

 public ReactorCoreDesign {
  dimensions(width,depth);Objects.requireNonNull(cells,"cells");
  if(cells.size()!=(width-2)*(depth-2))throw new IllegalArgumentException("Cell count does not match the interior footprint");
  for(var cell:cells)if(cell==null||cell<AIR||cell>REFLECTOR)throw new IllegalArgumentException("Unknown core cell");
  // Keep old footprints readable without silently turning their former absorber
  // positions into extra fuel. New assemblies contain their own control rod.
  cells=cells.stream().map(cell->cell==CONTROL?AIR:cell).toList();
 }
 private static void dimensions(int width,int depth){if(width<5||width>64||depth<5||depth>64)throw new IllegalArgumentException("Outside width and depth must be 5 to 64 blocks");}

 public static ReactorCoreDesign fromPreset(BlueprintSettings settings){
  if(settings.kind()!=0)throw new IllegalArgumentException("Core designs belong to reactors");
  var cells=new ArrayList<Integer>();
  for(int z=1;z<settings.depth()-1;z++)for(int x=1;x<settings.width()-1;x++)cells.add(ReactorControlLayout.isFuel(x,z,settings.width(),settings.depth(),settings.pattern())?FUEL:settings.filler()==1?MODERATOR:settings.filler()==2?REFLECTOR:AIR);
  return new ReactorCoreDesign(settings.width(),settings.depth(),cells);
 }

 /** Local coordinates measured inside the outer shell, from 1 through width/depth minus 2. */
 public int at(int x,int z){if(x<1||x>=width-1||z<1||z>=depth-1)throw new IllegalArgumentException("Core cell is outside the interior");return cells.get((z-1)*(width-2)+x-1);}
 public ReactorCoreDesign withCell(int index,int value){
  if(index<0||index>=cells.size()||value<AIR||value>REFLECTOR||value==CONTROL)throw new IllegalArgumentException("Invalid core cell edit");
  if(cells.get(index)==value)return this;var next=new ArrayList<>(cells);next.set(index,value);return new ReactorCoreDesign(width,depth,next);
 }
 public ReactorCoreDesign resized(int newWidth,int newDepth){
  dimensions(newWidth,newDepth);if(newWidth==width&&newDepth==depth)return this;
  for(int z=1;z<depth-1;z++)for(int x=1;x<width-1;x++)if((x>=newWidth-1||z>=newDepth-1)&&at(x,z)!=AIR)throw new IllegalArgumentException("Shrinking would remove non-empty core cells");
  var next=new ArrayList<Integer>((newWidth-2)*(newDepth-2));for(int z=1;z<newDepth-1;z++)for(int x=1;x<newWidth-1;x++)next.add(x<width-1&&z<depth-1?at(x,z):AIR);
  return new ReactorCoreDesign(newWidth,newDepth,next);
 }
 public List<BlockPos> columns(int type){
  if(type<AIR||type>REFLECTOR)throw new IllegalArgumentException("Unknown core cell");var result=new ArrayList<BlockPos>();
  for(int z=1;z<depth-1;z++)for(int x=1;x<width-1;x++)if(at(x,z)==type)result.add(new BlockPos(x,1,z));return List.copyOf(result);
 }
 /** Retained for saved UI consumers; every fuel column has an integrated control. */
 public int uncoveredCount(){return 0;}
 public String validationIssue(){
  return columns(FUEL).isEmpty()?"Add at least one fuel column.":"";
 }
}
