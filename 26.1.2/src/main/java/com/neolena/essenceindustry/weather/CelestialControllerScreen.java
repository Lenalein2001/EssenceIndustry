package com.neolena.essenceindustry.weather;
import static com.neolena.essenceindustry.client.UtilityTheme.*;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
public final class CelestialControllerScreen extends TextInputContainerScreen<CelestialControllerMenu> {
 private Button runNow;
 private final Button[] modes=new Button[CelestialMode.values().length];
 public CelestialControllerScreen(CelestialControllerMenu menu,Inventory inventory,Component title){super(menu,inventory,title,320,224);}
 public static void register(IEventBus bus){bus.addListener(CelestialControllerScreen::screens);}
 private static void screens(RegisterMenuScreensEvent event){event.register(WeatherUtilities.MENU.get(),CelestialControllerScreen::new);}
 private static Component text(String key){return Component.translatable("gui.essenceindustry.celestial."+key);}
 @Override protected void init(){super.init();for(var mode:CelestialMode.values()){
  int id=mode.ordinal();modes[id]=addRenderableWidget(Button.builder(text(mode.getSerializedName()),button->{if(minecraft!=null && minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}).pos(leftPos+12+(id%3)*100,topPos+57+(id/3)*27).size(96,22).build());
  modes[id].setTooltip(Tooltip.create(text(mode.time()>=0?"time_help":"weather_help")));
 }
 runNow=addRenderableWidget(Button.builder(text("run_now"),button->{if(minecraft!=null && minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,CelestialControllerMenu.RUN_NOW);}).pos(leftPos+12,topPos+144).size(296,22).build());
 runNow.setTooltip(Tooltip.create(text("run_help")));updateButtons();}
 private void updateButtons(){runNow.active=menu.supported();for(var mode:CelestialMode.values())modes[mode.ordinal()].active=mode!=menu.mode();}
 @Override protected void containerTick(){super.containerTick();updateButtons();}
 @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float delta){super.extractBackground(g,x,y,delta);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,PANEL);g.outline(leftPos,topPos,imageWidth,imageHeight,BORDER);g.fill(leftPos+8,topPos+48,leftPos+312,topPos+137,INNER);}
 @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,title,12,10,TEXT,false);g.text(font,Component.translatable("gui.essenceindustry.celestial.selected",text(menu.mode().getSerializedName())),12,31,TEXT,false);g.text(font,text(menu.supported()?(menu.powered()?"powered":"ready"):"unsupported"),12,178,MUTED,false);g.text(font,text("hint"),12,202,MUTED,false);}
}
