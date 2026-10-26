package com.neolena.essenceindustry.nuclear;

import com.neolena.essenceindustry.client.TextInputContainerScreen;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Compact reactor console: clear material flow, real state, and a separate construction view. */
public final class ReactorScreen extends TextInputContainerScreen<ReactorMenu> {
 private static final int PANEL=0xF5151E28,SURFACE=0xFF1B2936,INSET=0xFF101922,BORDER=0xFF456879,TEXT=0xFFE2EDF2,MUTED=0xFF9AACB9,BLUE=0xFF74C9E6,AMBER=0xFFE0B76E,RED=0xFFE88376;
 private final Map<Integer,Button> controls=new LinkedHashMap<>();private final List<Button> tabs=new ArrayList<>();private Button previousLayer,nextLayer;private int layer;
 public ReactorScreen(ReactorMenu menu,Inventory inventory,Component title){super(menu,inventory,title,320,236);}
 private static Component text(String key,Object... args){return Component.translatable("gui.essenceindustry.reactor."+key,args);}
 private void send(int button){if((button==1||button==2)&&(com.mojang.blaze3d.platform.InputConstants.isKeyDown(minecraft.getWindow(),org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT)||com.mojang.blaze3d.platform.InputConstants.isKeyDown(minecraft.getWindow(),org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT)))button+=5;if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,button);}
 private void control(String key,int id,int x,int y,int width,String tooltip){var button=addRenderableWidget(Button.builder(text(key),b->send(id)).bounds(leftPos+x,topPos+y,width,18).build());if(tooltip!=null)button.setTooltip(Tooltip.create(text(tooltip)));controls.put(id,button);}
 @Override protected void init(){
  super.init();controls.clear();tabs.clear();
  for(int tab=0;tab<2;tab++){int selected=tab;tabs.add(addRenderableWidget(Button.builder(text(tab==0?"overview":"build"),b->{if(selected==0)menu.previewTab(0);send(20+selected);updateControls();}).bounds(leftPos+12+tab*84,topPos+26,80,18).build()));}
  addRenderableWidget(Button.builder(text("guide"),b->{if(minecraft.player!=null)guideme.GuidesCommon.openGuide(minecraft.player,com.neolena.essenceindustry.guide.FieldGuideClient.ID,guideme.PageAnchor.page(com.neolena.essenceindustry.EssenceIndustry.id(menu.tab()==1?"reactor_building.md":"reactor.md")));}).bounds(leftPos+250,topPos+26,58,18).tooltip(Tooltip.create(text("guide_help"))).build());
  control("direct_mode",5,182,26,62,"mode_help");
  control("start",0,12,128,64,"start_help");control("reset",3,80,128,52,"reset_help");control("minus",1,174,128,20,"rods_help");control("plus",2,288,128,20,"rods_help");
  for(int axis=0;axis<3;axis++){control("minus",30+axis*2,78,52+axis*22,20,"size_help");control("plus",31+axis*2,134,52+axis*22,20,"size_help");}
  control("check",4,174,128,56,"check_help");
  previousLayer=addRenderableWidget(Button.builder(text("previous_layer"),b->layer=Math.max(0,layer-1)).bounds(leftPos+240,topPos+128,20,18).build());
  nextLayer=addRenderableWidget(Button.builder(text("next_layer"),b->layer=Math.min(menu.height()-1,layer+1)).bounds(leftPos+288,topPos+128,20,18).build());updateControls();
 }
 private void updateControls(){
  boolean main=menu.tab()==0;layer=Math.clamp(layer,0,menu.height()-1);for(int i=0;i<tabs.size();i++)tabs.get(i).active=menu.tab()!=i;
  for(var entry:controls.entrySet()){int id=entry.getKey();var button=entry.getValue();button.visible=(id==4||id>=30)?!main:main;
   button.active=true;if(id==0){button.setMessage(text(menu.enabled()?"stop":"start"));button.active=!menu.tripped()&&(menu.enabled()||menu.formed());}
   if(id==5){button.setMessage(text(menu.activeCooling()?"steam_mode":"direct_mode"));button.active=menu.canSwitchMode();}
   if(id==3)button.active=menu.tripped()&&menu.heatUnits()<=menu.heatCapacityUnits()*3/4;
   if(id==1)button.active=menu.rods()>0;if(id==2)button.active=menu.rods()<100;
   if(id>=30){int axis=(id-30)/2,value=axis==0?menu.width():axis==1?menu.depth():menu.height();button.active=!menu.enabled()&&(id%2==0?value>5:value<menu.maximum(axis));}
  }
  previousLayer.visible=!main;nextLayer.visible=!main;previousLayer.active=layer>0;nextLayer.active=layer+1<menu.height();
 }
 @Override protected void containerTick(){super.containerTick();updateControls();}
 private void panel(GuiGraphicsExtractor g,int x,int y,int width,int height){g.fill(leftPos+x,topPos+y,leftPos+x+width,topPos+y+height,SURFACE);g.outline(leftPos+x,topPos+y,width,height,BORDER);}
 @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float partialTick){
  super.extractBackground(g,mouseX,mouseY,partialTick);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,PANEL);g.outline(leftPos,topPos,imageWidth,imageHeight,BORDER);g.fill(leftPos+1,topPos+22,leftPos+319,topPos+23,BORDER);
  if(menu.tab()==0){panel(g,12,48,88,61);panel(g,108,48,104,61);panel(g,220,48,88,61);}
  else {panel(g,12,48,148,99);panel(g,168,48,140,79);}
  for(var slot:menu.slots)if(slot.isActive()){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,INSET);g.outline(leftPos+slot.x-1,topPos+slot.y-1,18,18,BORDER);}
  var active=tabs.get(Math.clamp(menu.tab(),0,1));g.outline(active.getX()-1,active.getY()-1,active.getWidth()+2,active.getHeight()+2,BLUE);
  if(menu.tab()==0){
   hover(g,mouseX,mouseY,12,48,88,61,text("fuel_help",fuelName(),exactMb(menu.fuelUnits()),exactMb(menu.fuelCapacityUnits())));
   var coreHelp=text("core_columns",fuelColumnCount(),menu.controlColumnCount()).copy().append(Component.literal("\n")).append(text("control_coverage_help")).append(Component.literal("\n")).append(menu.activeCooling()?text("fluid_ports_help",menu.water(),menu.waterInputPorts(),menu.steamOutputPorts()):text("direct_cooling_help"));
   if(menu.returnedWater()>0)coreHelp.append(Component.literal("\n")).append(text("retained_water_help",menu.returnedWater()));
   hover(g,mouseX,mouseY,108,48,104,61,coreHelp);
   hover(g,mouseX,mouseY,220,48,88,61,text("waste_help",exactMb(menu.wasteUnits()),exactMb(menu.wasteCapacityUnits())));
   hover(g,mouseX,mouseY,12,110,144,16,menu.activeCooling()?text("steam_help",menu.steam(),menu.fluidCapacity(),menu.water()):text("energy_help",String.format(Locale.ROOT,"%,d",menu.energy()),String.format(Locale.ROOT,"%,d",menu.energyCapacity())));
   hover(g,mouseX,mouseY,164,110,144,16,text("heat_help",exactMb(menu.heatUnits()),exactMb(menu.heatCapacityUnits())));
  }else hover(g,mouseX,mouseY,12,118,148,28,menu.issue());
  hover(g,mouseX,mouseY,142,5,166,17,menu.formed()?statusText():statusText().copy().append(Component.literal("\n")).append(menu.issue()));
 }
 private void hover(GuiGraphicsExtractor g,int mouseX,int mouseY,int x,int y,int width,int height,Component tooltip){if(mouseX>=leftPos+x&&mouseX<leftPos+x+width&&mouseY>=topPos+y&&mouseY<topPos+y+height)g.setTooltipForNextFrame(font,font.split(tooltip,Math.min(240,Math.max(100,this.width/2))),mouseX,mouseY);}
 @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){
  label(g,text("title"),12,9,126,TEXT);label(g,statusText(),142,9,166,statusColor());
  g.text(font,playerInventoryTitle,79,147,MUTED,false);
  if(menu.tab()==0)overview(g);else build(g);
 }
 private void overview(GuiGraphicsExtractor g){
  g.text(font,text("fuel"),18,53,TEXT,false);g.text(font,text("spent"),226,53,TEXT,false);
  meter(g,20,87,72,4,menu.fuelUnits(),menu.fuelCapacityUnits(),BLUE);meter(g,228,87,72,4,menu.wasteUnits(),menu.wasteCapacityUnits(),AMBER);
  label(g,Component.literal(volume(menu.fuelUnits())+" mB"),18,96,76,MUTED);label(g,Component.literal(volume(menu.wasteUnits())+" mB"),226,96,76,MUTED);
  coreSchematic(g);label(g,Component.literal(number(menu.production())+(menu.activeCooling()?" mB/t":" FE/t")),114,98,92,TEXT);
  g.text(font,text(menu.activeCooling()?"steam":"energy"),12,112,MUTED,false);right(g,number(menu.activeCooling()?menu.steam():menu.energy())+(menu.activeCooling()?" mB":" FE"),154,112,TEXT);meter(g,12,122,142,3,menu.activeCooling()?menu.steam():menu.energy(),menu.activeCooling()?menu.fluidCapacity():menu.energyCapacity(),BLUE);
  g.text(font,text("heat"),164,112,MUTED,false);right(g,percent(menu.heatUnits(),menu.heatCapacityUnits())+"%",308,112,statusColor()==RED?RED:TEXT);meter(g,164,122,144,3,menu.heatUnits(),menu.heatCapacityUnits(),menu.tripped()?RED:AMBER);
  label(g,text("rods",menu.rods()),200,133,82,TEXT);
 }
 private void coreSchematic(GuiGraphicsExtractor g){
  label(g,text("segments",menu.fuelSegments()),114,53,92,MUTED);
  int[] colors={0xFF9CB370,0xFFB9CE73,0xFFD5956F,0xFFCBB078,0xFF7ABAB6,0xFFABA2D3};int fuel=0;for(int i=0;i<ReactorFuels.ALL.size();i++)if(ReactorFuels.ALL.get(i).id().equals(menu.fuelId()))fuel=i;
  int fill=menu.fuelUnits()>0?colors[fuel]:0xFF465662;
  int count=Math.min(5,fuelColumnCount()),start=155-Math.max(0,count-1)*8;
  if(count>0){g.fill(start-4,66,start+(count-1)*16+14,69,BORDER);g.fill(start-4,92,start+(count-1)*16+14,95,BORDER);}
  for(int rod=0;rod<count;rod++){int x=start+rod*16;g.fill(x,67,x+10,94,INSET);g.fill(x+2,70,x+8,92,fill);g.fill(x-1,68,x+11,71,MUTED);g.fill(x-1,90,x+11,93,MUTED);int inserted=20*menu.rods()/100;g.fill(x+4,69,x+6,69+inserted,0xFFE0E9EE);}
  if(menu.status()==ReactorCore.Status.RUNNING){g.fill(116,72,118,90,BLUE);g.fill(202,72,204,90,BLUE);}
 }
 private int fuelColumnCount(){return (menu.fuelSegments()+Math.max(1,menu.height()-2)-1)/Math.max(1,menu.height()-2);}
 private void build(GuiGraphicsExtractor g){
  int[] values={menu.width(),menu.depth(),menu.height()};String[] labels={"width","depth","height"};
  for(int axis=0;axis<3;axis++){label(g,text(labels[axis]),18,57+axis*22,56,MUTED);right(g,Integer.toString(values[axis]),127,57+axis*22,TEXT);}
  String issue=menu.issue().getString();if(issue.isEmpty())issue=text(menu.formed()?"complete":"build_hint").getString();var lines=font.split(Component.literal(issue),136);
  for(int line=0;line<Math.min(2,lines.size());line++)g.text(font,lines.get(line),18,122+line*10,menu.formed()?BLUE:AMBER,false);
  label(g,text("layer",layer+1,menu.height()),176,53,126,TEXT);
  int size=Math.max(1,Math.min(112/menu.width(),64/menu.depth())),gridW=menu.width()*size,gridD=menu.depth()*size,ox=238-gridW/2,oz=62+(64-gridD)/2;
  for(int z=0;z<menu.depth();z++)for(int x=0;x<menu.width();x++){
   boolean shell=layer==0||layer==menu.height()-1||x==0||z==0||x==menu.width()-1||z==menu.depth()-1;
   int color=shell?0xFF516D7C:INSET;if(layer==1&&z==0&&x==menu.width()/2)color=BLUE;
   int gap=size>=3?1:0;g.fill(ox+x*size,oz+z*size,ox+(x+1)*size-gap,oz+(z+1)*size-gap,color);
  }
  label(g,text(layer==0?"base":layer==menu.height()-1?"roof":"walls"),262,133,25,MUTED);
 }
 private Component fuelName(){return text(menu.fuelId().isEmpty()?"fuel_empty":"fuel_"+menu.fuelId());}
 private Component statusText(){return text(menu.activeCooling()&&menu.status()==ReactorCore.Status.RUNNING?"status_running_steam":"status_"+menu.status().name().toLowerCase(Locale.ROOT));}
 private int statusColor(){return menu.tripped()?RED:menu.status()==ReactorCore.Status.RUNNING?BLUE:menu.status()==ReactorCore.Status.OFF?MUTED:AMBER;}
 private void label(GuiGraphicsExtractor g,Component value,int x,int y,int width,int color){String full=value.getString();String fitted=font.width(full)<=width?full:font.plainSubstrByWidth(full,Math.max(0,width-font.width("...")))+"...";g.text(font,fitted,x,y,color,false);}
 private void right(GuiGraphicsExtractor g,String value,int end,int y,int color){g.text(font,value,end-font.width(value),y,color,false);}
 private void meter(GuiGraphicsExtractor g,int x,int y,int width,int height,long value,long capacity,int color){g.fill(x,y,x+width,y+height,INSET);int filled=capacity<=0?0:(int)Math.round(width*Math.clamp(value/(double)capacity,0,1));if(filled>0)g.fill(x,y,x+filled,y+height,color);}
 private static int percent(long amount,long capacity){return capacity<=0?0:(int)Math.clamp(Math.round(100d*amount/capacity),0,999);}
 private static String exactMb(long units){return String.format(Locale.ROOT,"%,.3f",units/(double)ReactorCore.UNITS_PER_MB);}
 private static String volume(long units){return number(units/(double)ReactorCore.UNITS_PER_MB);}
 private static String number(double amount){String[] suffix={"","k","M","G","T"};int tier=0;while(amount>=1000&&tier<suffix.length-1){amount/=1000;tier++;}return String.format(Locale.ROOT,amount>=100||amount==Math.floor(amount)?"%.0f%s":"%.1f%s",amount,suffix[tier]);}
}
