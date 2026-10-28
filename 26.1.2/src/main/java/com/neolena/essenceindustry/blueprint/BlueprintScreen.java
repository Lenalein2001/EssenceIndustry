package com.neolena.essenceindustry.blueprint;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import java.util.*;
public final class BlueprintScreen extends TextInputContainerScreen<BlueprintMenu> {
 private final BlueprintPreview preview=new BlueprintPreview();private boolean overview=true,cutaway=true;private float yaw=135;private int dropdown=-1;private final List<Button> choices=new ArrayList<>();
 private int paint=1,lastPaint=-1,designRevision=-1;private ReactorCoreDesign shownDesign;private int previewRevision=-1;private BlueprintSettings previewSettings;
 private static final int[] CORE_PALETTE={0,1,3,4};
 private static final int TEXT=0xFFD5E2E8,MUTED=0xFF8FA4B4,BLUE=0xFF74D2EA;
 private final Map<Integer,Button> buttons=new HashMap<>();private int tab,layer=1,page;private BlueprintSettings previous;private List<BlueprintPlan.Cell> cells=List.of(),visibleCells=List.of();private int cachedLayer=-1;
 public BlueprintScreen(BlueprintMenu m,Inventory i,Component t){super(m,i,t,320,236);}
 public static Component text(String key,Object... args){return Component.translatable("gui.essenceindustry.planner."+key,args);}
 public static void register(IEventBus bus){BlueprintPreview.register(bus);BlueprintWorldGuide.register(bus);bus.addListener((net.neoforged.neoforge.client.event.RegisterMenuScreensEvent e)->e.register(MultiblockPlanner.MENU.get(),BlueprintScreen::new));}
 private void button(String key,int id,int x,int y,int w){buttons.put(id,addRenderableWidget(Button.builder(text(key),b->{if(id==110){BlueprintWorldGuide.toggle(menu,overview?0:layer);if(BlueprintWorldGuide.activeFor(menu.controllerPosition())){minecraft.player.closeContainer();minecraft.setScreen(null);}}else if(id>=200&&id<=204){paint=id-200;}else if(id==109){tab=2;if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,40);}else if(id==106){overview=!overview;}else if(id==107){cutaway=!cutaway;}else if(id==108){yaw=(yaw+90)%360;}else if(id>=16&&id<=24){dropdown=dropdown==id?-1:id;}else if(id==100)tab=0;else if(id==101)tab=1;else if(id==102){if(menu.settings().kind()==1&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,29);else layer=(layer+1)%menu.settings().height();}else if(id==103)page=Math.max(0,page-1);else if(id==105){guideme.GuidesCommon.openGuide(minecraft.player,com.neolena.essenceindustry.guide.FieldGuideClient.ID,guideme.PageAnchor.page(com.neolena.essenceindustry.EssenceIndustry.id("multiblock_planner.md")));}else if(id==104)page=Math.min(Math.max(0,(menu.stat(14)-1)/7),page+1);else if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);update();}).bounds(leftPos+x,topPos+y,w,18).build()));}
 @Override protected void init(){super.init();buttons.clear();choices.clear();dropdown=-1;button("verify",28,100,3,58);button("guide",105,164,3,42);button("layout",100,10,28,90);button("materials",101,104,28,90);button("back",26,198,28,112);
  button("design",109,164,28,70);button("preset",41,158,138,150);for(int n=0;n<CORE_PALETTE.length;n++)button("paint_"+CORE_PALETTE[n],200+CORE_PALETTE[n],198,52+n*21,110);button("planner_slot",44,198,157,110);button("save_design",43,198,181,52);button("load_design",45,254,181,54);
  for(int n=0;n<3;n++){button("minus",10+n*2,158,54+n*21,18);button("plus",11+n*2,290,54+n*21,18);}button("pattern",16,158,117,150);button("filler",17,158,138,150);button("glass",18,158,159,150);button("cooling",19,158,180,150);button("hatches",20,158,138,150);
  button("coil",21,158,117,150);button("blades",22,158,138,150);button("rings",23,158,159,150);button("size",24,158,180,150);
  button("layer",102,12,180,132);button("project",27,80,180,64);button("previous",103,12,180,62);button("next",104,78,180,62);
  button("world_guide",110,10,208,48);button("me_off",30,150,180,158);button("check",0,60,208,48);button("supply",1,110,208,48);button("build",2,160,208,48);button("pause",3,210,208,48);button("return",4,260,208,48);
  button("overview",106,12,180,62);button("cutaway",107,78,180,66);button("rotate",108,112,52,30);
  for(int i=0;i<8;i++){final int choice=i;var b=Button.builder(Component.empty(),x->{int action=dropdown;dropdown=-1;if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1000+action*16+choice);update();}).bounds(leftPos+150,topPos+52+i*18,160,18).build();choices.add(addRenderableWidget(b));}
  buttons.get(110).setTooltip(Tooltip.create(text("world_guide_help")));buttons.get(28).setTooltip(Tooltip.create(text("verify_help")));buttons.get(27).setTooltip(Tooltip.create(text("project_help")));buttons.get(1).setTooltip(Tooltip.create(text("supply_help")));buttons.get(4).setTooltip(Tooltip.create(text("return_help")));buttons.get(2).setTooltip(Tooltip.create(text("keep_open")));update();
 }
 private void update(){page=Math.min(page,Math.max(0,(menu.stat(14)-1)/7));var s=menu.settings();if(s.kind()==1)layer=Math.min(menu.stat(22),s.height()-1);int state=menu.stat(12);boolean busy=state==1||state==3;
  for(var entry:buttons.entrySet()){int id=entry.getKey();var b=entry.getValue();b.visible=id<10||id>=100&&id<=101||id==105||id==26||tab==0&&id>=10&&id<=24||tab==0&&id==102||tab==1&&(id==103||id==104);b.active=!busy||id==3||id>=100;}
  for(int id=10;id<=15;id++){buttons.get(id).active&=!s.fixed()&&s.kind()!=5;if(s.kind()==5)buttons.get(id).visible=false;}
  buttons.get(16).visible=tab==0&&s.kind()<2;buttons.get(17).visible=tab==0&&s.kind()==0;buttons.get(18).visible=tab==0;buttons.get(18).setY(topPos+(s.kind()==5?83:159));buttons.get(19).visible=tab==0&&s.kind()==0;buttons.get(20).visible=tab==0&&s.kind()==1;
  for(int id=21;id<=24;id++)buttons.get(id).visible=tab==0&&s.kind()==5;
  buttons.get(100).active=tab!=0;buttons.get(101).active=tab!=1;buttons.get(26).visible=menu.controllerMode();buttons.get(16).setMessage(text((s.kind()==1?"rack_":"pattern_")+s.pattern()));buttons.get(17).setMessage(text("filler_"+s.filler()));buttons.get(18).setMessage(text("glass_"+s.glass()));buttons.get(19).setMessage(text("cooling_"+s.steam()));buttons.get(20).setMessage(text("hatch_count",s.hatches()));buttons.get(21).setMessage(text("coil_"+s.coil()));buttons.get(22).setMessage(text("blade_count",s.blades()));buttons.get(23).setMessage(text("ring_count",s.rings()));buttons.get(24).setMessage(text("turbine_layout_"+s.option(24)));buttons.get(102).setMessage(text(s.kind()==1?"layer_short":"layer_count",layer+1,s.height()));
  buttons.get(0).setTooltip(Tooltip.create(text(menu.stat(16)>0?"check_help":"bind")));buttons.get(102).active=true;
  buttons.get(0).active=!busy&&menu.stat(16)>0;buttons.get(1).active=state==2||state==4;buttons.get(2).active=state==2||state==4||state==8;buttons.get(3).active=busy||state==2||state==4;buttons.get(4).active=menu.stat(15)>0;buttons.get(103).active=page>0;buttons.get(104).active=(page+1)*7<menu.stat(14);
  buttons.get(30).visible=menu.controllerMode()&&tab==1;buttons.get(30).active=!busy;buttons.get(30).setMessage(text(menu.stat(23)>0?"me_label_"+menu.stat(23):"me_off"));buttons.get(30).setTooltip(Tooltip.create(text("me_state_"+menu.stat(23))));buttons.get(28).visible=menu.controllerMode();buttons.get(27).visible=menu.controllerMode()&&tab==0&&s.kind()==1;buttons.get(102).setWidth(s.kind()==1?64:132);
  boolean show3d=overview&&cells.size()<=4096;buttons.get(106).visible=tab==0&&menu.controllerMode();buttons.get(106).setMessage(text(show3d?"layers":"overview"));buttons.get(107).visible=tab==0&&show3d;buttons.get(107).setMessage(text(cutaway?"shell":"cutaway"));buttons.get(108).visible=tab==0&&show3d;buttons.get(102).visible=tab==0&&!show3d;buttons.get(27).visible=tab==0&&!show3d&&s.kind()==1;buttons.get(106).setY(topPos+show3dOffset(show3d));
  for(int id=16;id<=24;id++)buttons.get(id).setMessage(buttons.get(id).getMessage().copy().append("  v"));buttons.get(106).active=cells.size()<=4096;buttons.get(106).setTooltip(Tooltip.create(text("preview_help")));
  if(dropdown>=0&&(tab!=0||busy))dropdown=-1;
  for(var b:buttons.values())if(dropdown>=0&&b!=buttons.get(dropdown))b.active=false;
  for(int i=0;i<choices.size();i++){var b=choices.get(i);b.visible=dropdown>=0&&i<s.optionCount(dropdown);b.active=b.visible;if(b.visible)b.setMessage(optionLabel(s,dropdown,i));}
  if(!menu.controllerMode())for(var e:buttons.entrySet()){e.getValue().visible=e.getKey()==4||e.getKey()==105;}
  if(!s.equals(previous)||designRevision!=menu.coreRevision()){previous=s;designRevision=menu.coreRevision();shownDesign=menu.coreDesign();cachedLayer=-1;layer=Math.min(layer,s.height()-1);}
  if(tab==0&&(previewRevision!=designRevision||!s.equals(previewSettings))){previewRevision=designRevision;previewSettings=s;try{cells=BlueprintPlan.create(s,Direction.NORTH,shownDesign);}catch(RuntimeException ex){cells=List.of();}}
  boolean reactor=s.kind()==0&&menu.controllerMode();buttons.get(109).visible=reactor;buttons.get(109).active=tab!=2&&!busy;
  if(reactor){buttons.get(100).setWidth(70);buttons.get(101).setX(leftPos+84);buttons.get(101).setWidth(76);buttons.get(26).setX(leftPos+238);buttons.get(26).setWidth(72);}
  buttons.get(41).visible=reactor&&tab==0&&shownDesign!=null;buttons.get(41).active=!busy;
  if(shownDesign!=null){buttons.get(16).setMessage(text("custom_core"));buttons.get(16).active=false;buttons.get(17).visible=false;}
  for(int p:CORE_PALETTE){int id=200+p;buttons.get(id).visible=reactor&&tab==2;buttons.get(id).active=!busy&&paint!=p;buttons.get(id).setMessage(Component.literal(new String[]{"E  ","F  ","", "M  ","R  "}[p]).withStyle(style->style.withColor(new int[]{0x8FA4B4,0xB5BF73,0xB59ADA,0x718DA0,0xB9C5CA}[p])).append(text("paint_"+p)));}buttons.get(201).setTooltip(Tooltip.create(text("integrated_help")));
  for(int id:new int[]{43,44,45}){buttons.get(id).visible=reactor&&tab==2;buttons.get(id).active=!busy&&menu.stat(159)>0;}
  buttons.get(44).setMessage(text(menu.stat(159)==0?"no_planner":"planner_slot",menu.stat(158)));buttons.get(44).setTooltip(Tooltip.create(text("planner_slot_help")));
  buttons.get(43).setTooltip(Tooltip.create(text("save_design_help")));buttons.get(45).setTooltip(Tooltip.create(text("load_design_help")));
  buttons.get(27).visible=false;buttons.get(110).visible=menu.controllerMode();buttons.get(110).active=dropdown<0&&menu.stat(16)>0;buttons.get(110).setMessage(text(BlueprintWorldGuide.activeFor(menu.controllerPosition())?"world_hide":"world_guide"));if(tab==2)dropdown=-1;if(tab==0&&cachedLayer!=layer){cachedLayer=layer;visibleCells=cells.stream().filter(c->c.local().getY()==layer&&!c.state().isAir()).toList();}
 }
 @Override protected void containerTick(){super.containerTick();update();}
 @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float dt){super.extractBackground(g,mx,my,dt);g.fill(leftPos,topPos,leftPos+320,topPos+236,0xFF101923);g.outline(leftPos,topPos,320,236,0xFF53778B);g.fill(leftPos+1,topPos+1,leftPos+319,topPos+23,0xFF233746);g.fill(leftPos+10,topPos+50,leftPos+310,topPos+202,0xFF14212C);if(menu.controllerMode()&&tab==0){g.fill(leftPos+12,topPos+52,leftPos+144,topPos+176,0xFF0B141D);grid(g,mx,my);}else if(menu.controllerMode()&&tab==2)designGrid(g,mx,my);}
 private int show3dOffset(boolean show){return show?180:150;}
 private int coreCellSize(){var s=menu.settings();return Math.max(1,Math.min(174/(s.width()-2),110/(s.depth()-2)));}
 private int coreX(){return leftPos+100-(menu.settings().width()-2)*coreCellSize()/2;}
 private int coreY(){return topPos+66+(110-(menu.settings().depth()-2)*coreCellSize())/2;}
 private ReactorCoreDesign visibleDesign(){return shownDesign==null?ReactorCoreDesign.fromPreset(menu.settings()):shownDesign;}
 private void designGrid(GuiGraphicsExtractor g,int mx,int my){
  var design=visibleDesign();int size=coreCellSize(),ox=coreX(),oy=coreY(),w=design.width()-2,d=design.depth()-2;int[] colors={0xFF152432,0xFFB5BF73,0xFFB59ADA,0xFF718DA0,0xFFB9C5CA};
  g.fill(ox-3,oy-3,ox+w*size+3,oy+d*size+3,0xFF456577);
  for(int z=0;z<d;z++)for(int x=0;x<w;x++){int i=z*w+x,value=design.cells().get(i),px=ox+x*size,py=oy+z*size;
   g.fill(px,py,px+size-(size>3?1:0),py+size-(size>3?1:0),colors[value]);if(size>=11&&value>0)g.text(font,new String[]{"","F","","M","R"}[value],px+(size-6)/2,py+(size-8)/2,0xFF101923,false);
   if(mx>=px&&mx<px+size&&my>=py&&my<py+size){g.outline(px,py,size,size,0xFFFFFFFF);var tip=text("paint_"+value).copy().append(Component.literal("  ["+(x+1)+", "+(z+1)+"]"));if(value==1)tip.append(Component.literal("\n")).append(text("integrated_help"));g.setTooltipForNextFrame(font,font.split(tip,190),mx,my);}
  }
 }
 private boolean paintAt(double x,double y,int button){if(tab!=2||menu.settings().kind()!=0||button!=0&&button!=1||menu.stat(12)==1||menu.stat(12)==3)return false;int size=coreCellSize(),cx=(int)Math.floor((x-coreX())/size),cz=(int)Math.floor((y-coreY())/size),w=menu.settings().width()-2,d=menu.settings().depth()-2;if(cx<0||cz<0||cx>=w||cz>=d)return false;int cell=cz*w+cx,value=button==1?0:paint;if(cell!=lastPaint&&minecraft.gameMode!=null){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,10000+value*4096+cell);lastPaint=cell;}return true;}
 @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event,boolean doubleClick){lastPaint=-1;if(paintAt(event.x(),event.y(),event.button())){setDragging(true);return true;}return super.mouseClicked(event,doubleClick);}
 @Override public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event,double dx,double dy){if(paintAt(event.x(),event.y(),event.button()))return true;return super.mouseDragged(event,dx,dy);}
 @Override public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event){lastPaint=-1;return super.mouseReleased(event);}
 private Component optionLabel(BlueprintSettings s,int action,int i){return switch(action){case 16->text((s.kind()==1?"rack_":"pattern_")+i);case 17->text("filler_"+i);case 18->text("glass_"+i);case 19->text("cooling_"+i);case 20->text("hatch_count",i+1);case 21->text("coil_"+i);case 22->text("blade_count",i+1);case 23->text("ring_count",i+1);case 24->text("turbine_layout_"+i);default->Component.empty();};}
 private void grid(GuiGraphicsExtractor g,int mx,int my){var s=menu.settings();int size=Math.max(1,Math.min(116/s.width(),105/s.depth())),ox=leftPos+78-s.width()*size/2,oy=topPos+62;
  if(overview&&cells.size()<=4096){preview.draw(g,leftPos+12,topPos+64,132,100,s,cells,yaw,cutaway);return;}
  for(var c:visibleCells){String key=BuiltInRegistries.BLOCK.getKey(c.state().getBlock()).getPath();int color=key.contains("controller")||key.equals("processing_array")||key.matches("fuel_(enricher|fabricator|reprocessor)")?0xFFBD9DDC:key.contains("glass")||key.contains("window")?0xFF6DA6B5:key.contains("fuel_assembly")||key.contains("rack")?0xFFD9BB6B:key.contains("cooling")?0xFF489CDC:key.contains("control")?0xFFA285BC:key.contains("input")||key.contains("output")||key.contains("hatch")||key.contains("energy")?0xFFE09F76:key.contains("centrifuge")||key.contains("vessel")||key.contains("forming")||key.contains("conveyor")?0xFF70B9AA:0xFF526B7C;int x=c.local().getX(),z=c.local().getZ();g.fill(ox+x*size,oy+z*size,ox+(x+1)*size-(size>2?1:0),oy+(z+1)*size-(size>2?1:0) ,color);
   if(mx>=ox+x*size&&mx<ox+(x+1)*size&&my>=oy+z*size&&my<oy+(z+1)*size){g.outline(ox+x*size,oy+z*size,size,size,0xFFFFFFFF);g.setTooltipForNextFrame(font,font.split(c.state().getBlock().getName(),180),mx,my);}
  }
 }
 @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){g.text(font,title,10,8,TEXT,false);String status=menu.stat(21)==1?text(menu.stat(20)==1?"valid":"unformed").getString():text("status_"+menu.stat(12)).getString();if(menu.stat(12)==1||menu.stat(12)==3)status+=" "+menu.stat(13)+"%";g.text(font,status,310-font.width(status),8,BLUE,false);var s=menu.settings();if(!menu.controllerMode()){g.text(font,text("legacy"),16,68,TEXT,false);g.text(font,text("stored",menu.stat(15)),16,90,MUTED,false);return;}
  if(tab==0){for(int n=0;n<(s.kind()==5?1:3)&&dropdown<0;n++){int value=n==0?s.width():n==1?s.depth():s.height();g.text(font,s.kind()==5?Component.literal(s.width()+" x "+s.depth()+" x "+s.height()):text(new String[]{"width","depth","height"}[n],value),182,59+n*21,TEXT,false);}g.text(font,text(s.fixed()?"fixed":overview&&cells.size()<=4096?"overview":"layer_view"),16,54,MUTED,false);if(s.fixed()){g.text(font,text("fixed_hint"),158,122,MUTED,false);g.text(font,text("fixed_hint2"),158,136,MUTED,false);}}
  else if(tab==1){g.text(font,text("material"),16,55,MUTED,false);g.text(font,text("counts"),197,55,MUTED,false);int rows=menu.stat(14);for(int i=0;i<7&&page*7+i<rows;i++){int n=page*7+i,base=24+n*4;var item=BuiltInRegistries.ITEM.byId(menu.stat(base));int y=69+i*15;g.item(new ItemStack(item),14,y-3);g.text(font,font.plainSubstrByWidth(new ItemStack(item).getHoverName().getString(),157),34,y,TEXT,false);String count=menu.stat(base+2)+" / "+menu.stat(base+1)+" / "+menu.stat(base+3);g.text(font,count,305-font.width(count),y,menu.stat(base+2)>=menu.stat(base+1)?BLUE:0xFFE8C277,false);}if(rows==0)g.text(font,text(switch(menu.stat(12)){case 9->"materials_complete";case 1->"materials_scanning";case 2->"materials_none";default->"check_hint";}),16,82,MUTED,false);}
  if(tab==0)g.text(font,text(overview&&cells.size()<=4096?"preview_plan":"front_hint"),16,168,MUTED,false);
  if(tab==2){g.text(font,text("core_dimensions",s.width()-2,s.depth()-2,s.height()-2),14,54,MUTED,false);var design=shownDesign==null?ReactorCoreDesign.fromPreset(s):shownDesign;int fuel=design.columns(ReactorCoreDesign.FUEL).size();g.text(font,font.plainSubstrByWidth(text(fuel==0?"no_fuel_design":"fuel_columns",fuel).getString(),176),14,180,fuel==0?0xFFFFA08B:BLUE,false);g.text(font,text("actuators_auto"),198,143,MUTED,false);g.text(font,font.plainSubstrByWidth(text("design_message_"+menu.stat(156)).getString(),176),14,192,MUTED,false);}
  int st=menu.stat(12);if(tab==1&&st==4){var waiting=BuiltInRegistries.ITEM.byId(menu.stat(152));if(waiting!=net.minecraft.world.item.Items.AIR)g.text(font,font.plainSubstrByWidth(text("next_material",new ItemStack(waiting).getHoverName()).getString(),296),12,198,0xFFE8C277,false);}if(st>=5&&st<=7)g.text(font,menu.stat(17)+", "+menu.stat(18)+", "+menu.stat(19),158,198,0xFFFFA08B,false);
 }
}


