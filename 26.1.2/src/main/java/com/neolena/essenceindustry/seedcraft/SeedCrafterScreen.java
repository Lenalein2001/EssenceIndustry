package com.neolena.essenceindustry.seedcraft;
import static com.neolena.essenceindustry.client.UtilityTheme.*;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
/** Process-first controller: live batch, waiting supplies, progress and buffers. */
public final class SeedCrafterScreen extends TextInputContainerScreen<SeedCrafterMenu>{
 private final boolean compactFuel;private final Inventory screenInventory;private boolean replacingForResize;private Button fuelMode;
 public SeedCrafterScreen(SeedCrafterMenu menu,Inventory inv,Component title){this(menu,inv,title,net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight()<302);}
 private SeedCrafterScreen(SeedCrafterMenu menu,Inventory inv,Component title,boolean compact){super(menu,inv,title,320,menu.fuelProcessor()&&!compact?282:232);compactFuel=menu.fuelProcessor()&&compact;screenInventory=inv;}
 public static void register(IEventBus bus){bus.addListener((RegisterMenuScreensEvent e)->e.register(SeedCrafter.MENU.get(),(SeedCrafterMenu menu,Inventory inv,Component title)->new SeedCrafterScreen(menu,inv,title)));}
 private static Component text(String key){return Component.translatable("gui.essenceindustry.seed_crafter."+key);}
 private static Component fuel(String key,Object...args){return Component.translatable("gui.essenceindustry.fuel_ui."+key,args);}
 @Override protected void init(){
  menu.layoutFuelClient(compactFuel);super.init();
  if(menu.fuelProcessor()){
   addRenderableWidget(Button.builder(fuel("build"),b->{minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1);}).bounds(leftPos+212,topPos+7,48,20).tooltip(Tooltip.create(fuel("build_help"))).build());
   addRenderableWidget(Button.builder(fuel("guide"),b->guideme.GuidesCommon.openGuide(minecraft.player,com.neolena.essenceindustry.guide.FieldGuideClient.ID,guideme.PageAnchor.page(com.neolena.essenceindustry.EssenceIndustry.id(menu.fuelProcess()+".md")))).bounds(leftPos+264,topPos+7,44,20).tooltip(Tooltip.create(fuel("guide_help"))).build());
  }
  if(menu.fabricator())fuelMode=addRenderableWidget(Button.builder(fuel("mode_auto"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+204,topPos+35,92,18).tooltip(Tooltip.create(fuel("mode_help"))).build());
  updateFuelControls();
 }
 @Override public void resize(int width,int height){if(menu.fuelProcessor()&&(height<302)!=compactFuel){replacingForResize=true;minecraft.setScreen(new SeedCrafterScreen(menu,screenInventory,title,height<302));}else super.resize(width,height);}
 @Override public void removed(){if(!replacingForResize)super.removed();}
 @Override protected void containerTick(){super.containerTick();updateFuelControls();}
 private void updateFuelControls(){if(fuelMode!=null){fuelMode.setMessage(fuel("mode_"+Math.clamp(menu.fuelMode(),0,4)));fuelMode.active=true;}}
 @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float dt){super.extractBackground(g,x,y,dt);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,PANEL);g.outline(leftPos,topPos,imageWidth,imageHeight,BORDER);if(menu.fuelProcessor()){int h=compactFuel?47:97;g.fill(leftPos+12,topPos+31,leftPos+308,topPos+31+h,INNER);g.outline(leftPos+12,topPos+31,296,h,BORDER);}for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,INNER);g.outline(leftPos+slot.x-1,topPos+slot.y-1,18,18,MUTED);}}
 @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,menu.fuelProcessor()?font.plainSubstrByWidth(title.getString(),194):title.getString(),12,10,TEXT,false);if(menu.fuelProcessor()){fuelLabels(g);return;}g.text(font,text("inputs"),24,32,MUTED,false);g.text(font,text("outputs"),178,32,MUTED,false);g.text(font,"\u2192",118,67,TEXT,false);g.text(font,text("status_"+menu.status()),12,111,MUTED,false);g.text(font,text(menu.modelCopier()?"copy_hint":"hint"),12,127,MUTED,false);}
 private void fuelLabels(GuiGraphicsExtractor g){
  int shift=compactFuel?50:0;
  if(compactFuel){
   String now=menu.currentProduct().isEmpty()?fuel("idle").getString():menu.currentProduct().getHoverName().getString();
   g.text(font,font.plainSubstrByWidth(now,menu.fabricator()?174:264),24,39,TEXT,false);
   String next=menu.nextProduct().isEmpty()?fuel("no_next").getString():fuel("next_short",menu.nextProduct().getHoverName(),menu.readyBatches()).getString();
   g.text(font,font.plainSubstrByWidth(next,264),24,56,MUTED,false);
  }else{
   g.text(font,fuel("current"),24,39,MUTED,false);
   batch(g,menu.currentInput(),menu.currentProduct(),54,"idle");
   g.text(font,fuel("waiting",menu.readyBatches()),24,78,MUTED,false);
   batch(g,menu.nextInput(),menu.nextProduct(),92,"no_next");
  }
  progress(g,120-shift);
  g.fill(24,130-shift,114,134-shift,INNER);g.fill(24,130-shift,24+90*Math.clamp(menu.fuelEnergy(),0,1000)/1000,134-shift,0xff82bfce);
  g.text(font,fuel("energy",menu.fuelEnergy()),124,130-shift,MUTED,false);
  g.text(font,fuel("inputs"),24,141-shift,MUTED,false);g.text(font,fuel("outputs"),204,141-shift,MUTED,false);
  fuelStatus(g,180-shift);
 }
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){super.extractRenderState(g,mx,my,dt);if(!menu.fuelProcessor())return;
  if(!compactFuel){hoverItem(g,mx,my,24,54,menu.currentInput());hoverItem(g,mx,my,70,54,menu.currentProduct());hoverItem(g,mx,my,24,92,menu.nextInput());hoverItem(g,mx,my,70,92,menu.nextProduct());hover(g,mx,my,24,78,264,10,fuel("queue_help"));}
  else hover(g,mx,my,24,56,264,10,fuel("queue_help"));
 }
 private void hoverItem(GuiGraphicsExtractor g,int mx,int my,int x,int y,ItemStack item){if(!item.isEmpty())hover(g,mx,my,x,y,16,16,item.getHoverName());}
 private void hover(GuiGraphicsExtractor g,int mx,int my,int x,int y,int w,int h,Component tip){if(mx>=leftPos+x&&mx<leftPos+x+w&&my>=topPos+y&&my<topPos+y+h)g.setTooltipForNextFrame(font,font.split(tip,240),mx,my);}
 private void batch(GuiGraphicsExtractor g,ItemStack input,ItemStack output,int y,String empty){
  if(output.isEmpty()){g.text(font,fuel(empty),24,y+3,TEXT,false);return;}
  g.item(input,24,y);g.item(output,70,y);
  int c=0xff82bfce;g.fill(46,y+7,62,y+9,c);g.fill(58,y+4,60,y+12,c);g.fill(60,y+6,62,y+10,c);g.fill(62,y+7,64,y+9,c);
  var lines=font.split(output.getHoverName(),198);for(int i=0;i<Math.min(2,lines.size());i++)g.text(font,lines.get(i),94,y+2+i*9,TEXT,false);
 }
 private void progress(GuiGraphicsExtractor g,int y){g.fill(24,y,264,y+3,0xff263847);if(menu.fuelDuration()>0){g.fill(24,y,24+240*Math.clamp(menu.fuelProgress(),0,1000)/1000,y+3,0xff82bfce);g.text(font,(menu.fuelProgress()/10)+"%",272,y-2,TEXT,false);}}
 private void fuelStatus(GuiGraphicsExtractor g,int y){
  int color=menu.status()==0||menu.status()==1?MUTED:menu.status()==3||menu.status()==8?0xffff8c83:BRASS;
  String status=text("fuel_status_"+menu.status()).getString();g.text(font,font.plainSubstrByWidth(status,180),12,y-6,color,false);
  if(menu.status()==13){String detail=menu.foundComponents()+" / "+menu.requiredComponents()+" "+menu.missingComponent().getHoverName().getString();g.text(font,font.plainSubstrByWidth(detail,180),12,y+5,MUTED,false);}
 }
}
