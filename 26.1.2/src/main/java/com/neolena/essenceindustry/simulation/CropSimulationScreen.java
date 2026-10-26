package com.neolena.essenceindustry.simulation;
import static com.neolena.essenceindustry.client.UtilityTheme.*;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
public final class CropSimulationScreen extends TextInputContainerScreen<CropSimulationMenu>{
 private Button me;
 public CropSimulationScreen(CropSimulationMenu m,Inventory i,Component t){super(m,i,t,320,236);}
 public static void register(IEventBus bus){bus.addListener((RegisterMenuScreensEvent e)->e.register(CropSimulation.MENU.get(),CropSimulationScreen::new));}
 private static Component text(String k,Object...args){return Component.translatable("gui.essenceindustry.crop_simulator."+k,args);}
 @Override protected void init(){super.init();me=addRenderableWidget(Button.builder(text("me_off"),b->{if(minecraft!=null && minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);}).pos(leftPos+12,topPos+116).size(136,20).build());}
 @Override protected void containerTick(){super.containerTick();me.setMessage(text(menu.me()?"me_on":"me_off"));me.active=menu.meStatus()!=3;}
 @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float dt){super.extractBackground(g,x,y,dt);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,PANEL);g.outline(leftPos,topPos,imageWidth,imageHeight,BORDER);for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,INNER);g.outline(leftPos+slot.x-1,topPos+slot.y-1,18,18,MUTED);}}
 @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,title,12,10,TEXT,false);g.text(font,text("seed"),24,28,MUTED,false);g.text(font,text("speed"),76,28,MUTED,false);g.text(font,text("yield"),133,28,MUTED,false);g.text(font,text("farmland"),193,28,MUTED,false);g.text(font,text("crux"),261,28,MUTED,false);g.text(font,text("multipliers",menu.speed(),menu.yield()),12,70,TEXT,false);g.text(font,text("stored",menu.count()),12,84,TEXT,false);g.text(font,text("status_"+menu.status()),12,98,MUTED,false);g.text(font,text("me_status_"+menu.meStatus()),158,122,MUTED,false);g.text(font,playerInventoryTitle,79,138,MUTED,false);}
 @Override protected void extractTooltip(GuiGraphicsExtractor g,int x,int y){
  super.extractTooltip(g,x,y);
  if(isHovering(210,43,16,16,x,y))g.setComponentTooltipForNextFrame(font,java.util.List.of(text("farmland_help")),x,y);
  if(isHovering(270,43,16,16,x,y)){
   var seed=menu.slots.getFirst().getItem();var crop=CropSimulationTile.crop(net.neoforged.neoforge.transfer.item.ItemResource.of(seed));
   var crux=crop==null?null:crop.getCruxBlock();
   g.setComponentTooltipForNextFrame(font,java.util.List.of(crux==null?text("no_crux"):text("required_crux",crux.getName())),x,y);
  }
 }
}
