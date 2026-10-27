package com.neolena.essenceindustry.nuclear;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class NuclearPortScreen extends TextInputContainerScreen<NuclearPortMenu>{
 private Button input,output,active;
 public NuclearPortScreen(NuclearPortMenu m,Inventory i,Component t){super(m,i,t,280,194);}
 private static Component text(String key){return Component.translatable("gui.essenceindustry.port."+key);}
 private void send(int mode){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,mode);}
 @Override protected void init(){
  super.init();
  input=addRenderableWidget(Button.builder(text("input"),b->send(0)).bounds(leftPos+16,topPos+112,120,22).build());
  output=addRenderableWidget(Button.builder(text("output"),b->send(1)).bounds(leftPos+144,topPos+112,120,22).build());
  active=addRenderableWidget(Button.builder(text("active_on"),b->send(2)).bounds(leftPos+16,topPos+142,248,22).build());
  update();
 }
 private void update(){
  boolean known=menu.type()>=0,energy=menu.type()==2;
  input.visible=output.visible=known&&!energy;
  input.active=known&&!energy&&menu.output();output.active=known&&!energy&&!menu.output();
  active.visible=known;
  active.setY(topPos+(energy?112:142));
  String label=energy?(menu.activeTransfer()?"energy_active":"energy_passive"):(menu.activeTransfer()?"active_on":"active_off");
  active.setMessage(text(label));
  active.setTooltip(Tooltip.create(text(energy?"energy_help":"active_help")));
 }
 @Override protected void containerTick(){super.containerTick();update();}
 @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float dt){
  super.extractBackground(g,mx,my,dt);g.fill(leftPos,topPos,leftPos+280,topPos+194,0xFA151E28);g.outline(leftPos,topPos,280,194,0xFF456879);
  g.fill(leftPos+12,topPos+33,leftPos+268,topPos+100,0xFF101922);g.outline(leftPos+12,topPos+33,256,67,0xFF354C5D);
  if(menu.type()>=0&&menu.type()!=2){
   int c=menu.output()?0xFFE0B76E:0xFF74C9E6,x=leftPos+124,y=topPos+61;
   g.fill(x+5,y+1,x+27,y+4,c);for(int n=0;n<6;n++){int a=menu.output()?x+n:x+31-n;g.fill(a,y+2-n,a+1,y+3+n,c);}
   g.outline(leftPos+(menu.output()?144:16),topPos+112,120,22,c);
  }
 }
 @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
  boolean energy=menu.type()==2;
  g.text(font,text(menu.type()==0?"item":menu.type()==1?"fluid":energy?"energy":"title"),14,12,0xFFE2EDF2,false);
  g.text(font,text(menu.connected()?"connected":"unformed"),14,177,0xFF9AACB9,false);
  if(energy){
   g.text(font,text("automatic"),24,43,0xFF9AACB9,false);
   g.text(font,text(!menu.connected()?"direction_pending":menu.output()?"energy_output":"energy_input"),24,63,0xFFE2EDF2,false);
   g.text(font,text("energy_external"),16,147,0xFF9AACB9,false);
  }else if(menu.type()>=0){
   g.text(font,text("network"),24,59,0xFF9AACB9,false);g.text(font,text("machine"),188,59,0xFFE2EDF2,false);
   g.text(font,text(menu.output()?"out_flow":"in_flow"),24,84,0xFF74C9E6,false);
  }
 }
}
