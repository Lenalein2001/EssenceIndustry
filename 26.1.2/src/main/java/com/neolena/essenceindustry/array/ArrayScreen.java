package com.neolena.essenceindustry.array;

import static com.neolena.essenceindustry.client.UtilityTheme.*;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;

public final class ArrayScreen extends TextInputContainerScreen<ArrayMenu>{
 private static final int BLUE=0xFF71C8E5,SURFACE=0xF51C1B2B;
 private record Control(Button button,int tab,int id){}
 private final List<Control> controls=new ArrayList<>();
 private final List<Button> tabs=new ArrayList<>();private float previewYaw=35;private boolean cutaway=true;
 private final boolean compactLayout;private final Inventory screenInventory;private boolean replacingForResize;private Button glassButton;
 public ArrayScreen(ArrayMenu menu,Inventory inventory,Component title){this(menu,inventory,title,needsCompact(net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth(),net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight()));}
 private ArrayScreen(ArrayMenu menu,Inventory inventory,Component title,boolean compact){super(menu,inventory,title,compact?320:400,compact?236:332);compactLayout=compact;screenInventory=inventory;}
 private static boolean needsCompact(int width,int height){return width<420||height<352;}
 @Override public void resize(int width,int height){
  if(needsCompact(width,height)!=compactLayout){replacingForResize=true;var replacement=new ArrayScreen(menu,screenInventory,title,needsCompact(width,height));replacement.previewYaw=previewYaw;replacement.cutaway=cutaway;minecraft.setScreen(replacement);}else super.resize(width,height);
 }
 @Override public void removed(){if(!replacingForResize)super.removed();}
 static Component text(String key,Object... args){return Component.translatable("gui.essenceindustry.array."+key,args);}
 public static void register(IEventBus bus){bus.addListener((net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event)->event.register(ProcessingArray.MENU.get(),(ArrayMenu menu,Inventory inventory,Component title)->new ArrayScreen(menu,inventory,title)));}
 private void send(int id){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 private void button(String label,int id,int x,int y,int width,int tab,String help){button(label,id,x,y,width,18,tab,help);}
 private void button(String label,int id,int x,int y,int width,int height,int tab,String help){
  var button=addRenderableWidget(Button.builder(text(label),b->send(id)).bounds(leftPos+x,topPos+y,width,height).build());
  if(help!=null)button.setTooltip(Tooltip.create(text(help)));controls.add(new Control(button,tab,id));
 }
 @Override protected void init(){
  menu.layoutClient(compactLayout);super.init();controls.clear();tabs.clear();glassButton=null;
  String[] labels={"overview","patterns_tab","build_tab"};for(int i=0;i<3;i++){int tab=i;tabs.add(addRenderableWidget(Button.builder(text(labels[i]),b->{if(tab<2)menu.previewTab(tab);send(20+tab);updateControls();}).bounds(leftPos+12+i*(compactLayout?74:86),topPos+28,compactLayout?70:82,18).build()));}
  if(compactLayout){
   button("page",9,192,101,48,0,"outputs_help");button("throttle",2,34,112,34,0,"throttle_help");button("refund_short",1,238,122,66,0,"refund_help");
   button("previous_patterns",30,192,126,22,1,null);button("next_patterns",31,286,126,22,1,null);
   button("width_less",5,186,62,22,2,null);button("size_plus",3,282,62,22,2,null);
   button("depth_less",6,186,82,22,2,null);button("size_plus",4,282,82,22,2,null);
   button("height_less",11,186,102,22,2,null);button("size_plus",10,282,102,22,2,null);
   button("layer_short",7,184,122,38,2,"layer_help");button("project",8,226,122,38,2,"project_help");button("check",0,268,122,38,2,"check_help");
   button("build_calculate_short",40,190,88,54,16,3,"build_calculate_help");button("build_load_short",41,248,88,54,16,3,"build_load_help");
   button("build_start",42,190,106,54,16,3,"build_start_help");button("build_pause",43,248,106,54,16,3,null);button("build_return_short",44,190,124,112,16,3,"build_return_help");
  }else{
   var rotate=addRenderableWidget(Button.builder(text("rotate_short"),b->previewYaw=(previewYaw+45)%360).bounds(leftPos+228,topPos+130,48,18).tooltip(Tooltip.create(text("rotate_preview"))).build());controls.add(new Control(rotate,0,-1));
   glassButton=addRenderableWidget(Button.builder(text("glass_hidden"),b->{cutaway=!cutaway;updateControls();}).bounds(leftPos+110,topPos+130,64,18).tooltip(Tooltip.create(text("cutaway_preview"))).build());controls.add(new Control(glassButton,0,-1));
   button("page",9,16,214,80,0,"outputs_help");button("throttle",2,114,164,70,0,"throttle_help");button("refund",1,190,164,86,0,"refund_help");
   button("previous_patterns",30,18,204,22,1,null);button("next_patterns",31,104,204,22,1,null);
   button("width_less",5,232,78,22,2,null);button("size_plus",3,360,78,22,2,null);
   button("depth_less",6,232,102,22,2,null);button("size_plus",4,360,102,22,2,null);
   button("height_less",11,232,126,22,2,null);button("size_plus",10,360,126,22,2,null);
   button("layer",7,232,154,72,2,"layer_help");button("project",8,310,154,72,2,"project_help");button("check",0,232,180,150,2,"check_help");
   button("build_calculate",40,238,132,144,3,"build_calculate_help");button("build_load",41,238,154,144,3,"build_load_help");
   button("build_start",42,238,176,69,3,"build_start_help");button("build_pause",43,313,176,69,3,null);button("build_return",44,238,198,144,3,"build_return_help");
  }
  updateControls();
 }
 private void updateControls(){for(var control:controls){control.button().visible=control.tab()==menu.tab();int id=control.id();if(id==9)control.button().setMessage(compactLayout?Component.literal((menu.stat(10)+1)+"/11"):text("output_page",menu.stat(10)+1,11));if(id==2&&compactLayout)control.button().setMessage(Component.literal(menu.stat(2)+"x"));if(id==8&&!compactLayout)control.button().setMessage(text(menu.stat(55)!=0?"guide_on":"guide_off"));if(id>=40&&id<=44){int state=menu.stat(74);boolean busy=state==1||state==3,missing=false,stocked=false;for(int k=0;k<5;k++){missing|=menu.stat(64+k)<menu.stat(69+k);stocked|=menu.stat(64+k)>0;}boolean ready=state==2||state==4||state==8;control.button().active=switch(id){case 40->!busy;case 41->ready&&missing;case 42->ready&&!missing;case 43->busy;default->state!=10&&stocked;};}if(id==30)control.button().active=menu.stat(59)>0;if(id==31)control.button().active=menu.stat(59)+1<menu.stat(60);if(id>=3&&id<=6||id==10||id==11){int value=id==3||id==5?menu.stat(0):id==4||id==6?menu.stat(1):Math.max(5,menu.stat(57));int max=id==3||id==5?menu.stat(51):id==4||id==6?menu.stat(52):menu.stat(58);boolean plus=id==3||id==4||id==10;control.button().active=menu.stat(7)==0&&menu.stat(74)!=1&&menu.stat(74)!=3&&(plus?value<max:value>5);}}for(int i=0;i<tabs.size();i++)tabs.get(i).active=menu.tab()!=i;if(glassButton!=null)glassButton.setMessage(text(cutaway?"glass_hidden":"glass_shown"));}
 @Override protected void containerTick(){super.containerTick();updateControls();}
 private java.util.List<net.minecraft.core.BlockPos> previewRacks(){var tile=minecraft.level==null?null:minecraft.level.getBlockEntity(menu.controllerPosition());return tile instanceof ArrayTile array?array.visualRacks():java.util.List.of();}
 private java.util.List<net.minecraft.core.BlockPos> previewWindows(){var tile=minecraft.level==null?null:minecraft.level.getBlockEntity(menu.controllerPosition());return tile instanceof ArrayTile array?array.visualWindows():java.util.List.of();}
 private void panel(GuiGraphicsExtractor g,int x,int y,int width,int height){g.fill(leftPos+x,topPos+y,leftPos+x+width,topPos+y+height,SURFACE);g.outline(leftPos+x,topPos+y,width,height,0xFF453952);g.fill(leftPos+x+1,topPos+y+1,leftPos+x+width-1,topPos+y+2,0xFF40627A);}
 @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
  super.extractBackground(g,mouseX,mouseY,delta);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,PANEL);g.outline(leftPos,topPos,imageWidth,imageHeight,BORDER);
  g.fill(leftPos+1,topPos+23,leftPos+imageWidth-1,topPos+24,0xFF493A60);
  if(compactLayout)compactBackground(g);else if(menu.tab()==0){
   panel(g,12,50,84,98);panel(g,12,152,84,34);panel(g,104,50,178,136);panel(g,288,50,100,136);panel(g,12,190,224,44);panel(g,244,190,144,44);
   ArrayScenePreview.draw(g,leftPos+108,topPos+65,170,82,menu.stat(0),menu.stat(1),Math.max(5,menu.stat(57)),menu.stat(4),menu.stat(5),menu.machineIcon(),previewYaw,cutaway,previewRacks(),previewWindows());
   if(!menu.machineIcon().isEmpty()){g.fill(leftPos+244,topPos+68,leftPos+274,topPos+98,0xFF172738);g.outline(leftPos+244,topPos+68,30,30,0xFF476A81);var pose=g.pose();pose.pushMatrix();pose.translate(leftPos+247,topPos+71);pose.scale(1.5f);g.item(menu.machineIcon(),0,0);pose.popMatrix();}
   energyBar(g,298,176,46);
   for(int i=0;i<3;i++){int progress=Math.clamp(menu.stat(45+i),0,1000);g.fill(leftPos+257+i*42,topPos+220,leftPos+275+i*42,topPos+222,0xFF0D1520);g.fill(leftPos+257+i*42,topPos+220,leftPos+257+i*42+18*progress/1000,topPos+222,BLUE);}
  }else if(menu.tab()==1){
   panel(g,12,50,120,152);panel(g,140,50,248,176);ArrayScenePreview.draw(g,leftPos+154,topPos+88,226,100,menu.stat(0),menu.stat(1),Math.max(5,menu.stat(57)),menu.stat(4),menu.stat(5),menu.machineIcon(),previewYaw,cutaway,previewRacks(),previewWindows());
  }else if(menu.tab()==3){
   panel(g,12,50,210,176);panel(g,230,50,158,176);
   progressBar(g,238,122,144,menu.stat(75));
   for(int k=0;k<5;k++){g.fill(leftPos+20,topPos+77+k*28,leftPos+40,topPos+97+k*28,INNER);g.item(new net.minecraft.world.item.ItemStack(ArrayBuilder.material(k)),leftPos+22,topPos+79+k*28);}
  }else{
   panel(g,12,50,208,176);panel(g,226,50,162,176);ArrayPreview.layer(g,leftPos+42,topPos+78,menu.stat(0),menu.stat(1),Math.max(5,menu.stat(57)),menu.stat(9),menu.stat(48),menu.stat(49),menu.stat(50),menu.stat(13)==0);
  }
  for(int i=0;i<tabs.size();i++)if(menu.tab()==i)outlineSelection(g,tabs.get(i));
  for(var control:controls)if(control.id()==8&&control.button().visible&&menu.stat(55)!=0)outlineSelection(g,control.button());
  // Slots keep stable network indices, but only the current tab is visible or interactable.
  for(var slot:menu.slots)if(slot.isActive()){
   int color=slot.index<36&&menu.invalid(slot.index)?0xFFFF8888:slot.index>=ArrayMenu.UPGRADE_START?BLUE:0xFF8A7E9C;
   g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,INNER);g.outline(leftPos+slot.x-1,topPos+slot.y-1,18,18,color);
   if(!slot.hasItem()&&slot.index>=ArrayMenu.UPGRADE_START)glyph(g,leftPos+slot.x+3,topPos+slot.y+3,slot.index-ArrayMenu.UPGRADE_START);
  }
 }
 private void outlineSelection(GuiGraphicsExtractor g,Button button){g.outline(button.getX()-1,button.getY()-1,button.getWidth()+2,button.getHeight()+2,BLUE);}
 private void energyBar(GuiGraphicsExtractor g,int x,int y,int width){g.fill(leftPos+x,topPos+y,leftPos+x+width,topPos+y+4,0xFF101721);g.fill(leftPos+x,topPos+y,leftPos+x+(int)(width*Math.clamp(menu.stat(12)/(double)Math.max(1,menu.stat(40)),0,1)),topPos+y+4,BLUE);}
 private void progressBar(GuiGraphicsExtractor g,int x,int y,int width,int progress){g.fill(leftPos+x,topPos+y,leftPos+x+width,topPos+y+4,0xFF0D1520);g.fill(leftPos+x,topPos+y,leftPos+x+width*Math.clamp(progress,0,100)/100,topPos+y+4,BLUE);}
 private void compactBackground(GuiGraphicsExtractor g){
  if(menu.tab()==0){
   panel(g,12,50,164,90);panel(g,184,50,124,90);energyBar(g,16,134,118);
   for(int i=0;i<3;i++){int x=246+i*20;g.fill(leftPos+x,topPos+118,leftPos+x+16,topPos+120,INNER);g.fill(leftPos+x,topPos+118,leftPos+x+16*Math.clamp(menu.stat(45+i),0,1000)/1000,topPos+120,BLUE);}
  }else if(menu.tab()==1){
   panel(g,12,50,174,94);panel(g,192,50,116,94);
   ArrayScenePreview.draw(g,leftPos+198,topPos+77,104,45,menu.stat(0),menu.stat(1),Math.max(5,menu.stat(57)),menu.stat(4),menu.stat(5),menu.machineIcon(),previewYaw,cutaway,previewRacks(),previewWindows());
  }else if(menu.tab()==2){
   panel(g,12,50,160,94);panel(g,180,50,128,94);
   ArrayPreview.layer(g,leftPos+24,topPos+68,menu.stat(0),menu.stat(1),Math.max(5,menu.stat(57)),menu.stat(9),menu.stat(48),menu.stat(49),menu.stat(50),menu.stat(13)==0,136,57);
  }else{
   panel(g,12,50,164,94);panel(g,184,50,124,94);progressBar(g,190,78,112,menu.stat(75));
   for(int k=0;k<5;k++)g.item(new net.minecraft.world.item.ItemStack(ArrayBuilder.material(k)),leftPos+16,topPos+61+k*16);
  }
 }
 private static void glyph(GuiGraphicsExtractor g,int x,int y,int kind){
  int color=0xFF52718A;
  if(kind==0){g.fill(x+5,y,x+9,y+2,color);g.fill(x+3,y+2,x+7,y+5,color);g.fill(x+2,y+4,x+8,y+6,color);g.fill(x+4,y+6,x+6,y+9,color);}
  else if(kind==1){g.outline(x,y,10,10,color);g.fill(x+4,y+2,x+6,y+5,color);g.fill(x+2,y+5,x+5,y+7,color);g.fill(x+5,y+7,x+7,y+9,color);}
  else{g.outline(x+1,y+1,8,9,color);g.fill(x+3,y,x+7,y+1,color);g.fill(x+3,y+4,x+7,y+5,color);g.fill(x+3,y+7,x+7,y+8,color);}
 }
 private static String compact(long n){return n>=1_000_000_000?String.format(java.util.Locale.ROOT,"%.1fG",n/1e9):n>=1_000_000?String.format(java.util.Locale.ROOT,"%.1fM",n/1e6):n>=1000?String.format(java.util.Locale.ROOT,"%.1fk",n/1000d):Long.toString(n);}
 @Override protected List<Component> getTooltipFromContainerItem(net.minecraft.world.item.ItemStack stack){
  var tips=new ArrayList<>(super.getTooltipFromContainerItem(stack));if(hoveredSlot!=null){int i=hoveredSlot.index;
   if(i<36)tips.add(text(menu.invalid(i)?"pattern_invalid":"pattern_ready"));else if(i<52)tips.add(text("one_type"));
   else if(i<68)tips.add(text("exact_count",Integer.toUnsignedLong(menu.stat(24+i-56))));else if(i<71)tips.add(text("job_operations",menu.stat(14+i-68),menu.stat(17+i-68)));
  }return tips;
 }
 private int statusColor(){int state=menu.stat(11);return state==1||state==2?BLUE:state>=10?0xFFFF8C83:BRASS;}
 private Component status(){String[] states={"unknown","idle","running","shell","interior","rack","no_rack","capacity","unloaded","redstone","locked","mixed","work_error","size"};return text("status_"+states[Math.clamp(menu.stat(11),0,states.length-1)]);}
 private Component machineName(){return menu.machineIcon().isEmpty()?text("no_machine"):menu.machineIcon().getHoverName();}
 private String efficiency(){return (menu.stat(41)==0?"":"-")+menu.stat(41)+"%";}
 private ArrayBuilder.Status builderState(){return ArrayBuilder.Status.values()[Math.clamp(menu.stat(74),0,ArrayBuilder.Status.values().length-1)];}
 private boolean builderMissing(){for(int k=0;k<5;k++)if(menu.stat(64+k)<menu.stat(69+k))return true;return false;}
 private Component builderStatus(){var state=builderState();return text(state==ArrayBuilder.Status.READY&&!builderMissing()?"builder_loaded":"builder_"+state.name().toLowerCase(java.util.Locale.ROOT));}
 private int builderColor(){return switch(builderState()){case BLOCKED,DENIED,LOCKED->0xFFFF8C83;case MISSING,UNLOADED,PAUSED->BRASS;default->BLUE;};}
 private Component materialCounts(int k){return menu.stat(69+k)==0&&menu.stat(64+k)==0?text(builderState()==ArrayBuilder.Status.COMPLETE?"material_placed":"material_not_needed"):text("build_counts",menu.stat(64+k),menu.stat(69+k));}
 private Component selectedLayer(){int h=Math.max(5,menu.stat(57)),layer=menu.stat(9);String kind=layer==0?"floor":layer==h-1?"roof":layer==h-2?"clearance":layer%2==1?"rack_lower":"rack_upper";return text("selected_layer",layer+1,h,text("layer_"+kind));}
 private void fitted(GuiGraphicsExtractor g,Component value,int x,int y,int width,int color){g.text(font,font.plainSubstrByWidth(value.getString(),width),x,y,color,false);}
 private void legend(GuiGraphicsExtractor g,int x,int y,boolean small){int[] colors={0xFF3A5569,0xFF70BCDB,0xFFE1D0F3};String[] labels={"legend_shell","legend_racks","legend_front"};for(int i=0;i<3;i++){int px=x+i*(small?49:65);g.fill(px,y,px+6,y+6,colors[i]);g.text(font,text(labels[i]),px+9,y-1,MUTED,false);}}
 @Override protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){
  g.text(font,title,12,10,TEXT,false);var status=status();g.text(font,status,imageWidth-12-font.width(status),10,statusColor(),false);
  if(compactLayout){compactLabels(g);g.text(font,playerInventoryTitle,79,146,MUTED,false);return;}
  if(menu.tab()==0){
   g.text(font,text("machines"),18,55,MUTED,false);g.text(font,text("active_short",menu.stat(5)),18,157,BLUE,false);g.text(font,text("capacity_short",menu.stat(3)*8),18,172,MUTED,false);g.text(font,text("upgrades"),298,55,MUTED,false);
   for(int i=0;i<3;i++){String value=i==0?menu.stat(2)+"x":i==1?efficiency():compact(menu.stat(40))+" FE";g.text(font,text("upgrade_"+i),320,71+i*40,TEXT,false);g.text(font,value,320,83+i*40,BLUE,false);}
   fitted(g,machineName(),112,150,162,TEXT);g.text(font,text("installed_count",menu.stat(4),menu.stat(3)*8),112,54,MUTED,false);
   g.text(font,text("results"),18,194,MUTED,false);g.text(font,compact(menu.pending()),18,204,BLUE,false);g.text(font,text(menu.stat(53)!=0?"me_on":"me_off"),350,173,menu.stat(53)!=0?BLUE:MUTED,false);g.text(font,text("queue_short",menu.stat(7)),250,192,MUTED,false);
  }else if(menu.tab()==1){
   g.text(font,text("patterns_tab"),18,55,MUTED,false);g.text(font,text("patterns_ready",menu.stat(6)),150,57,TEXT,false);g.text(font,text("hatches_capacity",menu.stat(22),menu.stat(61)),150,70,MUTED,false);
   g.text(font,text("native_batches"),150,198,BLUE,false);g.text(font,text("patterns_stored",menu.stat(62),menu.stat(61)),18,188,MUTED,false);g.text(font,(menu.stat(59)+1)+" / "+Math.max(1,menu.stat(60)),46,209,MUTED,false);
  }else if(menu.tab()==3){
   g.text(font,text("build_materials"),20,57,TEXT,false);
   for(int k=0;k<5;k++){fitted(g,new net.minecraft.world.item.ItemStack(ArrayBuilder.material(k)).getHoverName(),46,78+k*28,168,TEXT);g.text(font,materialCounts(k),46,90+k*28,menu.stat(64+k)>=menu.stat(69+k)?BLUE:BRASS,false);}
   g.text(font,text("dimensions_axes",menu.stat(0),menu.stat(1),menu.stat(57)),238,57,TEXT,false);g.text(font,builderStatus(),238,76,builderColor(),false);g.text(font,text("build_progress",menu.stat(75)),238,90,MUTED,false);
   var state=builderState();if(state==ArrayBuilder.Status.BLOCKED||state==ArrayBuilder.Status.DENIED||state==ArrayBuilder.Status.UNLOADED)g.text(font,menu.stat(76)+", "+menu.stat(77)+", "+menu.stat(78),238,106,BRASS,false);
   else fitted(g,text(state==ArrayBuilder.Status.COMPLETE?"build_done_hint":state==ArrayBuilder.Status.BUILDING?"build_running_hint":state==ArrayBuilder.Status.READY&&!builderMissing()?"build_ready_hint":"build_order"),238,106,144,MUTED);
  }else{
   int h=Math.max(5,menu.stat(57));g.text(font,selectedLayer(),20,57,TEXT,false);g.text(font,text("size_title"),238,57,TEXT,false);
   g.text(font,text("size_width",menu.stat(0)),262,83,BLUE,false);g.text(font,text("size_depth",menu.stat(1)),262,107,BLUE,false);g.text(font,text("size_height",h),262,131,BLUE,false);
   int shell=menu.stat(0)*menu.stat(1)*h-(menu.stat(0)-2)*(menu.stat(1)-2)*(h-2)-1;g.text(font,text("shell_count",shell),20,204,MUTED,false);legend(g,20,217,false);
   var p=menu.problemPosition();var detail=menu.stat(13)!=0?text("structure_valid"):menu.stat(54)<100?text("checking",menu.stat(54)):text("world_position",p.getX(),p.getY(),p.getZ());fitted(g,detail,232,207,148,menu.stat(13)!=0?BLUE:BRASS);
  }
  g.text(font,playerInventoryTitle,119,235,MUTED,false);
 }
 private void compactLabels(GuiGraphicsExtractor g){
  if(menu.tab()==0){
   fitted(g,text("installed_compact",menu.stat(4),menu.stat(3)*8),16,52,100,MUTED);fitted(g,text("active_short",menu.stat(5)),120,52,52,BLUE);
   for(int i=0;i<3;i++)g.text(font,text("upgrade_compact_"+i),16+i*54,101,MUTED,false);
   g.text(font,efficiency(),88,118,BLUE,false);g.text(font,compact(menu.stat(40)).replace(".0",""),142,118,BLUE,false);
   g.text(font,text(menu.stat(53)!=0?"me_on":"me_off"),138,130,menu.stat(53)!=0?BLUE:MUTED,false);
   fitted(g,text("results_compact",compact(menu.pending())),192,52,110,MUTED);fitted(g,text("queue_short",menu.stat(7)),192,127,42,MUTED);
  }else if(menu.tab()==1){
   g.text(font,text("patterns_tab"),18,52,MUTED,false);g.text(font,text("patterns_stored",menu.stat(62),menu.stat(61)),18,134,MUTED,false);
   fitted(g,text("ready_compact",menu.stat(6)),198,54,104,TEXT);g.text(font,text("hatches_compact",menu.stat(22)),198,66,MUTED,false);g.text(font,(menu.stat(59)+1)+" / "+Math.max(1,menu.stat(60)),220,131,MUTED,false);
  }else if(menu.tab()==2){
   fitted(g,selectedLayer(),18,52,148,TEXT);legend(g,18,134,true);g.text(font,text("size_title"),186,52,TEXT,false);
   g.text(font,text("size_width",menu.stat(0)),212,67,BLUE,false);g.text(font,text("size_depth",menu.stat(1)),212,87,BLUE,false);g.text(font,text("size_height",Math.max(5,menu.stat(57))),212,107,BLUE,false);
  }else{
   g.text(font,text("build_materials"),18,52,TEXT,false);
   for(int k=0;k<5;k++){int y=64+k*16;String counts=menu.stat(69+k)==0&&menu.stat(64+k)==0?"-":menu.stat(64+k)+"/"+menu.stat(69+k);int x=170-font.width(counts);fitted(g,text("material_short_"+k),36,y,Math.max(12,x-40),TEXT);g.text(font,counts,x,y,menu.stat(64+k)>=menu.stat(69+k)?BLUE:BRASS,false);}
   fitted(g,text("dimensions_axes",menu.stat(0),menu.stat(1),menu.stat(57)),190,52,112,TEXT);fitted(g,builderStatus(),190,65,112,builderColor());
  }
 }
 @Override protected void extractTooltip(GuiGraphicsExtractor g,int mouseX,int mouseY){
  super.extractTooltip(g,mouseX,mouseY);
  if(isHovering(imageWidth/2,8,imageWidth/2-8,12,mouseX,mouseY))g.setComponentTooltipForNextFrame(font,List.of(status()),mouseX,mouseY);
  if(menu.tab()==2&&isHovering(12,50,compactLayout?160:208,compactLayout?94:176,mouseX,mouseY))g.setComponentTooltipForNextFrame(font,List.of(selectedLayer(),text("build_rules"),text("build_racks"),text("build_ports")),mouseX,mouseY);
  if(menu.tab()==0){
   if(isHovering(compactLayout?14:296,compactLayout?132:172,compactLayout?158:82,10,mouseX,mouseY))g.setComponentTooltipForNextFrame(font,List.of(text("energy_detail",menu.stat(12),menu.stat(40)),text(menu.stat(53)!=0?"me_connected":"me_disconnected"),text("power_detail",demandText()),text("energy_surplus")),mouseX,mouseY);
   if(isHovering(compactLayout?14:104,50,compactLayout?160:178,compactLayout?10:75,mouseX,mouseY))g.setComponentTooltipForNextFrame(font,List.of(machineName(),text("installed_count",menu.stat(4),menu.stat(3)*8),text("active_short",menu.stat(5)),text("busy_help"),text("power_detail",demandText())),mouseX,mouseY);
   if(!compactLayout&&isHovering(14,153,80,16,mouseX,mouseY))g.setComponentTooltipForNextFrame(font,List.of(text("active_short",menu.stat(5)),text("busy_help")),mouseX,mouseY);
   for(int i=0;i<3;i++){var slot=menu.slots.get(ArrayMenu.UPGRADE_START+i);if(isHovering(slot.x,slot.y,16,16,mouseX,mouseY)&&!slot.hasItem())g.setComponentTooltipForNextFrame(font,List.of(text("upgrade_slot_"+i),text("upgrade_lock")),mouseX,mouseY);}
  }
  if(compactLayout&&menu.tab()==3){
   for(int k=0;k<5;k++)if(isHovering(14,61+k*16,158,16,mouseX,mouseY))g.setComponentTooltipForNextFrame(font,List.of(new net.minecraft.world.item.ItemStack(ArrayBuilder.material(k)).getHoverName(),materialCounts(k)),mouseX,mouseY);
   if(isHovering(188,64,116,19,mouseX,mouseY)){var lines=new ArrayList<Component>();lines.add(builderStatus());lines.add(text("build_progress",menu.stat(75)));var state=builderState();if(state==ArrayBuilder.Status.BLOCKED||state==ArrayBuilder.Status.DENIED||state==ArrayBuilder.Status.UNLOADED)lines.add(text("world_position",menu.stat(76),menu.stat(77),menu.stat(78)));g.setComponentTooltipForNextFrame(font,lines,mouseX,mouseY);}
  }
 }
 private String demandText(){return compact(menu.demand());}
}
