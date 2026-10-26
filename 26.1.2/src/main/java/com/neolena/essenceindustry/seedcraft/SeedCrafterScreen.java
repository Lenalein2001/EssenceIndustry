package com.neolena.essenceindustry.seedcraft;
import static com.neolena.essenceindustry.client.UtilityTheme.*;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
public final class SeedCrafterScreen extends TextInputContainerScreen<SeedCrafterMenu>{
 private final boolean compactFuel;private final Inventory screenInventory;private boolean replacingForResize,showBuild;private int buildLayer;private net.minecraft.client.gui.components.Button buildButton,previousLayer,nextLayer;
 public SeedCrafterScreen(SeedCrafterMenu menu,Inventory inv,Component title){this(menu,inv,title,net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight()<302);}
 private SeedCrafterScreen(SeedCrafterMenu menu,Inventory inv,Component title,boolean compact){super(menu,inv,title,320,menu.fuelProcessor()&&!compact?282:232);compactFuel=menu.fuelProcessor()&&compact;screenInventory=inv;}
 public static void register(IEventBus bus){bus.addListener((RegisterMenuScreensEvent e)->e.register(SeedCrafter.MENU.get(),(SeedCrafterMenu menu,Inventory inv,Component title)->new SeedCrafterScreen(menu,inv,title)));}
 private net.minecraft.client.gui.components.Button fuelMode;
 @Override protected void init(){
  menu.layoutFuelClient(compactFuel);super.init();
  if(menu.fuelProcessor()){
   buildButton=addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Build"),b->{minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1);}).bounds(leftPos+116,topPos+8,40,18).build());
   addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Guide"),b->guideme.GuidesCommon.openGuide(minecraft.player,com.neolena.essenceindustry.guide.FieldGuideClient.ID,guideme.PageAnchor.page(com.neolena.essenceindustry.EssenceIndustry.id(menu.fuelProcess()+".md")))).bounds(leftPos+160,topPos+8,40,18).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Open the GuideME construction guide"))).build());
   previousLayer=addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("<"),b->buildLayer=Math.floorMod(buildLayer-1,plantLayout().height())).bounds(leftPos+196,topPos+34,20,18).build());
   nextLayer=addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal(">"),b->buildLayer=(buildLayer+1)%plantLayout().height()).bounds(leftPos+280,topPos+34,20,18).build());
  }
  if(menu.fabricator())fuelMode=addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Fuel mode"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+204,topPos+8,90,18).build());
  updateFuelControls();
 }
 @Override public void resize(int width,int height){if(menu.fuelProcessor()&&(height<302)!=compactFuel){replacingForResize=true;var replacement=new SeedCrafterScreen(menu,screenInventory,title,height<302);replacement.showBuild=showBuild;replacement.buildLayer=buildLayer;minecraft.setScreen(replacement);}else super.resize(width,height);}
 @Override public void removed(){if(!replacingForResize)super.removed();}
 @Override protected void containerTick(){super.containerTick();updateFuelControls();}
 private void updateFuelControls(){
  if(fuelMode!=null){fuelMode.setMessage(Component.literal(java.util.List.of("Plutonium","MOX","Thorium","Advanced").get(Math.clamp(menu.fuelMode(),0,3))));fuelMode.active=menu.fuelDuration()==0;}
  if(buildButton!=null)buildButton.setMessage(Component.literal(showBuild?"Recipe":"Build"));
  if(previousLayer!=null){previousLayer.visible=showBuild&&!compactFuel;nextLayer.visible=showBuild&&!compactFuel;}
 }
 private com.neolena.essenceindustry.nuclear.FuelPlantLayout.Layout plantLayout(){return com.neolena.essenceindustry.nuclear.FuelPlantLayout.get(menu.fuelProcess());}
 private static Component text(String key){return Component.translatable("gui.essenceindustry.seed_crafter."+key);}
 @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float dt){super.extractBackground(g,x,y,dt);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,PANEL);g.outline(leftPos,topPos,imageWidth,imageHeight,BORDER);if(menu.fuelProcessor()){int h=compactFuel?47:97;g.fill(leftPos+12,topPos+31,leftPos+308,topPos+31+h,INNER);g.outline(leftPos+12,topPos+31,296,h,BORDER);}for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,INNER);g.outline(leftPos+slot.x-1,topPos+slot.y-1,18,18,MUTED);}}
 @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,title,12,10,TEXT,false);if(menu.fuelProcessor()){fuelLabels(g);return;}g.text(font,text("inputs"),24,32,MUTED,false);g.text(font,text("outputs"),178,32,MUTED,false);g.text(font,"\u2192",118,67,TEXT,false);g.text(font,text("status_"+menu.status()),12,111,MUTED,false);g.text(font,text(menu.modelCopier()?"copy_hint":"hint"),12,127,MUTED,false);}

 private void pipe(GuiGraphicsExtractor g,int x,int y,int end,int color){
  g.fill(x,y,end,y+3,color);g.fill(end-3,y-2,end,y+5,color);g.fill(end,y,end+3,y+3,color);
 }
 private void pellet(GuiGraphicsExtractor g,int x,int y,int color){
  g.fill(x+2,y,x+12,y+2,TEXT);g.fill(x,y+2,x+14,y+12,color);g.fill(x+2,y+12,x+12,y+14,MUTED);g.fill(x+2,y+3,x+4,y+10,TEXT);
 }
 private void fuelLabels(GuiGraphicsExtractor g){
  String process=menu.fuelProcess();int metal=0xff83949e,blue=0xff82bfce,green=0xffa5c77e,amber=0xffcfab70;
  if(compactFuel){compactFuelLabels(g,process,blue);return;}
  g.text(font,"Input buffer",24,141,MUTED,false);g.text(font,"Output buffer",204,141,MUTED,false);
  fuelStatus(g,180);
  g.fill(24,130,114,134,INNER);g.fill(24,130,24+90*menu.fuelEnergy()/1000,134,blue);
  g.text(font,menu.fuelEnergy()+" / 1,000 kFE",124,130,MUTED,false);
  if(showBuild){plantBuild(g,false);return;}
  progress(g,120);
  if(process.equals("fuel_enricher")){
   g.text(font,"Uranium",24,47,TEXT,false);pellet(g,43,66,green);pipe(g,64,73,104,metal);
   // Sectioned rotor with axial spindle, jacket and two distinct outlet streams.
   g.text(font,"Centrifuge",98,37,TEXT,false);
   g.fill(112,54,154,112,metal);g.fill(116,58,150,108,PANEL);
   g.fill(130,52,136,114,TEXT);g.fill(120,63,126,101,green);g.fill(140,63,146,101,amber);
   for(int yy=64;yy<105;yy+=10){g.fill(118,yy,128,yy+2,metal);g.fill(138,yy,148,yy+2,metal);}
   pipe(g,154,65,204,green);pipe(g,154,99,204,amber);
   pellet(g,214,61,green);g.text(font,"Enriched",237,64,TEXT,false);
   pellet(g,214,95,amber);g.text(font,"Depleted",237,98,TEXT,false);
  }else if(process.equals("fuel_reprocessor")){
   g.text(font,"Spent fuel",22,45,TEXT,false);pellet(g,42,66,amber);pipe(g,64,73,105,metal);
   g.text(font,"Recovery cell",94,37,TEXT,false);
   g.fill(111,55,157,113,metal);g.fill(116,60,152,107,PANEL);
   for(int xx=120;xx<151;xx+=8)g.fill(xx,69,xx+3,99,amber);
   g.fill(117,87,151,90,blue);g.fill(117,101,151,106,blue);
   pipe(g,157,65,204,green);pipe(g,157,99,204,amber);
   pellet(g,214,61,green);g.text(font,"Recovered",233,64,TEXT,false);
   g.fill(214,96,228,111,metal);g.fill(212,94,230,97,TEXT);g.fill(219,99,223,107,amber);
   g.text(font,"Waste",237,99,TEXT,false);
  }else{
   int mode=Math.clamp(menu.fuelMode(),0,3);
   int plutonium=0xffdb7852,mox=0xffd9a347,thorium=0xff54b8b2,advanced=0xff9690df;
   int fuelColor=switch(mode){case 0->plutonium;case 1->mox;case 2->thorium;default->advanced;};
   String product=switch(mode){case 0->"1 Plutonium";case 1->"4 MOX";case 2->"1 Thorium";default->"4 Advanced";};
   g.text(font,mode==0||mode==2?"Feed":"Blend",26,38,TEXT,false);g.text(font,"Press",124,38,TEXT,false);g.text(font,"Fuel pellet",222,38,TEXT,false);
   // Show the selected recipe's ingredient types, not a fixed two-ingredient blend.
   if(mode==0||mode==2)pellet(g,40,57,mode==0?plutonium:thorium);
   else if(mode==1){pellet(g,27,57,plutonium);pellet(g,53,57,amber);}
   else{pellet(g,22,57,advanced);pellet(g,42,57,green);pellet(g,62,57,thorium);}
   g.fill(mode==3?19:23,77,mode==3?80:70,81,metal);g.fill(mode==3?24:28,81,mode==3?75:65,86,metal);g.fill(mode==3?31:35,86,mode==3?68:58,91,metal);g.fill(42,91,51,100,blue);
   pipe(g,52,98,112,blue);
   g.fill(114,56,121,111,metal);g.fill(161,56,168,111,metal);g.fill(114,54,168,61,metal);
   g.fill(136,61,146,78,TEXT);g.fill(126,78,156,84,metal);
   g.fill(125,94,157,107,metal);g.fill(132,90,150,98,fuelColor);g.fill(112,109,170,114,TEXT);
   pipe(g,170,99,216,blue);pellet(g,235,83,fuelColor);g.text(font,product,215,111,MUTED,false);
  }
 }
 private void compactFuelLabels(GuiGraphicsExtractor g,String process,int blue){
  String name,summary;
  if(process.equals("fuel_enricher")){name="Centrifuge separation";summary="9 Uranium -> 1 Enriched + 8 Depleted";}
  else if(process.equals("fuel_reprocessor")){name="Spent-fuel recovery";summary="8 spent fuel -> 1 recovered + 7 waste";}
  else{name="Fuel fabrication";summary=switch(Math.clamp(menu.fuelMode(),0,3)){case 0->"1 recovered plutonium -> 1 Plutonium";case 1->"Plutonium + depleted uranium -> 4 MOX";case 2->"1 Thorium ingot -> 1 Thorium fuel";default->"Fissile + enriched + thorium -> 4 fuel";};}
  if(showBuild)plantBuild(g,true);else{g.text(font,name,24,39,TEXT,false);g.text(font,summary,24,57,MUTED,false);progress(g,70);}
  g.fill(24,80,114,84,INNER);g.fill(24,80,24+90*menu.fuelEnergy()/1000,84,blue);g.text(font,menu.fuelEnergy()+" / 1,000 kFE",124,80,MUTED,false);
  g.text(font,"Input buffer",24,91,MUTED,false);g.text(font,"Output buffer",204,91,MUTED,false);fuelStatus(g,130);
 }
 private void progress(GuiGraphicsExtractor g,int y){
  g.fill(24,y,264,y+3,0xff263847);if(menu.fuelDuration()>0){g.fill(24,y,24+240*Math.clamp(menu.fuelProgress(),0,1000)/1000,y+3,0xff82bfce);g.text(font,(menu.fuelProgress()/10)+"%",272,y-2,TEXT,false);}
 }
 private void fuelStatus(GuiGraphicsExtractor g,int y){
  int color=menu.status()==0||menu.status()==1?MUTED:menu.status()==3||menu.status()==8?0xffff8c83:BRASS;
  String status=text("fuel_status_"+menu.status()).getString();g.text(font,font.plainSubstrByWidth(status,180),12,y-6,color,false);
  if(menu.status()==13){String detail=menu.foundComponents()+" / "+menu.requiredComponents()+" "+menu.missingComponent().getHoverName().getString();g.text(font,font.plainSubstrByWidth(detail,180),12,y+5,MUTED,false);}
 }
 private void plantBuild(GuiGraphicsExtractor g,boolean compact){
  var layout=plantLayout();g.text(font,layout.width()+" W x "+layout.depth()+" D x "+layout.height()+" H",24,38,TEXT,false);
  if(compact){g.text(font,menu.fuelFormed()?"Structure complete":"Guide shows every layer and material",24,57,menu.fuelFormed()?0xff82bfce:BRASS,false);return;}
  g.text(font,menu.fuelFormed()?"Structure complete":"Fixed plant structure",24,55,menu.fuelFormed()?0xff82bfce:BRASS,false);
  g.text(font,"Controller: front centre",24,72,MUTED,false);g.text(font,"Guide: exact block list",24,86,MUTED,false);
  if(menu.status()>=5&&menu.status()<=8){var p=menu.fuelIssue();g.text(font,p.getX()+", "+p.getY()+", "+p.getZ(),24,106,BRASS,false);}
  g.text(font,(buildLayer+1)+" / "+layout.height(),228,39,TEXT,false);
  float size=Math.min(90f/layout.width(),64f/layout.depth());int ox=202+(int)((90-layout.width()*size)/2),oz=55+(int)((64-layout.depth()*size)/2);
  for(var cell:layout.cells())if(cell.local().getY()==buildLayer){String kind=cell.kind();int color=kind.equals("controller")?0xffdfb5ed:kind.contains("input")||kind.contains("output")||kind.equals("energy_out")?0xffc5a66b:kind.equals("glass")||kind.equals("roof_window")?0xff79b7cf:kind.equals("centrifuge")||kind.equals("treatment_vessel")||kind.startsWith("fabrication_")?0xff85b89e:0xff4b5d6b;int x=ox+(int)(cell.local().getX()*size),z=oz+(int)(cell.local().getZ()*size);g.fill(x,z,x+Math.max(1,(int)size-1),z+Math.max(1,(int)size-1),color);}
 }

}
