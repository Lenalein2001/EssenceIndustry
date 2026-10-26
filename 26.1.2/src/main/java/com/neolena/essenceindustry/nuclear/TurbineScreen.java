package com.neolena.essenceindustry.nuclear;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
/** Rotor speed, steam flow and generator load are the primary controls, not a crafting grid. */
public final class TurbineScreen extends TextInputContainerScreen<TurbineMenu>{
 private Button valve,coil;public TurbineScreen(TurbineMenu m,Inventory i,Component t){super(m,i,t,350,236);}
 private void send(int id){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
 private Button button(String text,int id,int x,int y,int w){return addRenderableWidget(Button.builder(Component.literal(text),b->send(id)).bounds(leftPos+x,topPos+y,w,20).build());}
 @Override protected void init(){super.init();valve=button("",0,14,179,154);coil=button("",1,182,179,154);button("-",2,14,145,20);button("+",3,148,145,20);button("-",4,182,145,20);button("+",5,316,145,20);button("Construction",20,226,211,110);update();}
 private void update(){valve.setMessage(Component.literal(menu.value(8)==1?"Close steam valve":"Open steam valve"));coil.setMessage(Component.literal(menu.value(9)==1?"Disengage coils":"Engage coils"));}
 @Override protected void containerTick(){super.containerTick();update();}
 private void panel(GuiGraphicsExtractor g,int x,int y,int w,int h){g.fill(leftPos+x,topPos+y,leftPos+x+w,topPos+y+h,0xFF101922);g.outline(leftPos+x,topPos+y,w,h,0xFF354C5D);}
 @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float dt){super.extractBackground(g,x,y,dt);g.fill(leftPos,topPos,leftPos+350,topPos+236,0xFA151E28);g.outline(leftPos,topPos,350,236,0xFF456879);panel(g,14,34,154,99);panel(g,182,34,154,99);
  int rpm=(int)Math.min(2700,menu.value(0)/10);for(int i=0;i<27;i++){int c=(i+1)*100<=rpm?i>=18?0xFFE0B76E:0xFF74C9E6:0xFF283C4A;g.fill(leftPos+24+i*5,topPos+75,leftPos+27+i*5,topPos+87,c);}
  int target=(int)menu.value(6)/100;g.fill(leftPos+24+target*5,topPos+72,leftPos+25+target*5,topPos+90,0xFFFFFFFF);
  bar(g,192,78,134,menu.value(3),TurbineCore.TANK,0xFF74C9E6);bar(g,192,102,134,menu.value(4),TurbineCore.TANK,0xFFE0B76E);}
 private void bar(GuiGraphicsExtractor g,int x,int y,int w,long n,long max,int c){g.fill(leftPos+x,topPos+y,leftPos+x+w,topPos+y+4,0xFF283C4A);g.fill(leftPos+x,topPos+y,leftPos+x+(int)(w*Math.min(n,max)/max),topPos+y+4,c);}
 private static String amount(long n){return n>=1_000_000?String.format(java.util.Locale.ROOT,"%.2fM",n/1e6):n>=1000?String.format(java.util.Locale.ROOT,"%.1fk",n/1e3):Long.toString(n);}
 @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,"Steam Turbine",14,13,0xFFE2EDF2,false);g.text(font,menu.value(11)==1?"Formed":"Incomplete",270,13,0xFF74C9E6,false);
  g.text(font,String.format(java.util.Locale.ROOT,"%.1f RPM",menu.value(0)/10d),24,45,0xFFE2EDF2,false);g.text(font,amount(menu.value(1))+" FE/t",24,60,0xFF74C9E6,false);g.text(font,"Coils: "+amount(menu.value(10))+" FE/t",24,99,0xFF9AACB9,false);g.text(font,"rated at 1,800 RPM",24,114,0xFF9AACB9,false);
  g.text(font,amount(menu.value(2))+" mB/t steam",192,45,0xFFE2EDF2,false);g.text(font,"Steam  "+amount(menu.value(3))+" mB",192,65,0xFF9AACB9,false);g.text(font,"Water  "+amount(menu.value(4))+" mB",192,89,0xFF9AACB9,false);g.text(font,amount(menu.value(5))+" / 4G FE",192,115,0xFF74C9E6,false);
  g.text(font,"Target "+menu.value(6),42,151,0xFFE2EDF2,false);g.text(font,"Flow limit "+menu.value(7)+"%",211,151,0xFFE2EDF2,false);g.text(font,font.plainSubstrByWidth(menu.issue(),204),14,216,0xFF9AACB9,false);}
}
