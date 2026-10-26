package com.neolena.essenceindustry.compat.ae2.bulk;
import static com.neolena.essenceindustry.client.UtilityTheme.*;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
public final class DiskManagerScreen extends TextInputContainerScreen<DiskManagerMenu>{
 private final Button[] configure=new Button[DiskManagerMenu.ROWS];
 public static void register(IEventBus bus){bus.addListener((RegisterMenuScreensEvent e)->e.register(BulkDiskManager.MENU.get(),DiskManagerScreen::new));}
 public DiskManagerScreen(DiskManagerMenu menu,Inventory player,Component title){super(menu,player,title,320,240);}
 private static Component text(String key){return Component.translatable("gui.essenceindustry.disk."+key);}
 private void action(int id){if(minecraft!=null && minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 @Override protected void init(){super.init();addRenderableWidget(Button.builder(text("scan"),b->action(0)).pos(leftPos+230,topPos+25).size(78,20).build());
  for(int i=0;i<configure.length;i++){final int row=i;configure[i]=addRenderableWidget(Button.builder(text("configure"),b->action(10+row)).pos(leftPos+230,topPos+53+row*24).size(78,20).build());}
  addRenderableWidget(Button.builder(Component.literal("<"),b->action(1)).pos(leftPos+12,topPos+198).size(22,18).build());
  addRenderableWidget(Button.builder(Component.literal(">"),b->action(2)).pos(leftPos+286,topPos+198).size(22,18).build());
 }
 @Override protected void containerTick(){super.containerTick();for(int i=0;i<configure.length;i++)configure[i].active=menu.stat(2)!=0 && !menu.slots.get(i).getItem().isEmpty();}
 @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float dt){super.extractBackground(g,x,y,dt);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,PANEL);g.outline(leftPos,topPos,imageWidth,imageHeight,BORDER);for(int i=0;i<configure.length;i++)g.fill(leftPos+8,topPos+51+i*24,leftPos+312,topPos+75+i*24,INNER);}
 @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,title,12,9,TEXT,false);g.text(font,text("largest"),12,31,MUTED,false);
  for(int i=0;i<configure.length;i++){var stack=menu.slots.get(i).getItem();if(stack.isEmpty())continue;g.text(font,font.plainSubstrByWidth(stack.getHoverName().getString(),180),38,54+i*24,TEXT,false);var lore=stack.get(DataComponentsHolder.LORE);if(lore!=null && !lore.lines().isEmpty())g.text(font,lore.lines().getFirst(),38,65+i*24,MUTED,false);}
  g.centeredText(font,(menu.stat(0)+1)+" / "+menu.stat(1),160,202,MUTED);g.text(font,text(menu.stat(2)==0?"offline":menu.stat(2)==2?"limited":"ready"),12,222,MUTED,false);g.text(font,text(menu.stat(3)==1?"cell_ready":"cell_waiting"),160,222,MUTED,false);
 }
 private static final class DataComponentsHolder{static final net.minecraft.core.component.DataComponentType<net.minecraft.world.item.component.ItemLore> LORE=net.minecraft.core.component.DataComponents.LORE;}
}
