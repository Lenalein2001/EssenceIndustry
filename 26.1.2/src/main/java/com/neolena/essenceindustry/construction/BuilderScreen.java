package com.neolena.essenceindustry.construction;
import static com.neolena.essenceindustry.client.UtilityTheme.*;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
public final class BuilderScreen extends TextInputContainerScreen<BuilderMenu> {
 private RangeSlider range;
 private final class RangeSlider extends AbstractSliderButton {
  private int pending=-1;private boolean waiting;private int requested,waitTicks;
  RangeSlider(){super(leftPos+210,topPos+143,160,18,Component.empty(),(Math.max(menu.stat(1),Math.max(menu.stat(2),menu.stat(3)))-1)/31d);updateMessage();setTooltip(Tooltip.create(text("range_help")));}
  protected void updateMessage(){setMessage(text("range",1+(int)Math.round(value*31)));}
  protected void applyValue(){pending=1+(int)Math.round(value*31);updateMessage();}
  void tick(){active=menu.stat(10)==0&&menu.stat(14)==0;if(!active){pending=-1;waiting=false;}if(waiting&&++waitTicks>20)waiting=false;if(pending>0){waitTicks=0;requested=pending;net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new BuilderAction(menu.containerId,BuilderAction.RANGE,pending));pending=-1;waiting=true;}else if(waiting&&menu.stat(1)==requested&&menu.stat(2)==requested&&menu.stat(3)==requested)waiting=false;
   if(!isHoveredOrFocused()&&!waiting){value=(Math.max(menu.stat(1),Math.max(menu.stat(2),menu.stat(3)))-1)/31d;updateMessage();}}
 }
 private static final int BLUE=0xFF71C8E5;private final java.util.Map<Integer,Button> buttons=new java.util.HashMap<>();
 public BuilderScreen(BuilderMenu menu,Inventory inv,Component title){super(menu,inv,title,384,322);}
 public static Component text(String key,Object... args){return Component.translatable("gui.essenceindustry.builder."+key,args);}
 public static void register(IEventBus bus){bus.addListener((net.neoforged.neoforge.client.event.RegisterMenuScreensEvent e)->e.register(QuantumBuilder.MENU.get(),BuilderScreen::new));BuilderClient.register(bus);}
 private void button(String label,int id,int x,int y,int w){buttons.put(id,addRenderableWidget(Button.builder(text(label),b->{if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}).bounds(leftPos+x,topPos+y,w,18).build()));if(id==7)buttons.get(id).setTooltip(Tooltip.create(text("rotate_help")));}
 @Override protected void init(){super.init();buttons.clear();button("mode",0,14,32,182);for(int n=0;n<3;n++){button("minus",1+n*2,218,36+n*28,20);button("plus",2+n*2,350,36+n*28,20);}button("rotate",7,14,166,88);button("alignment",11,108,166,88);button("minus",8,218,120,20);button("plus",9,350,120,20);range=addRenderableWidget(new RangeSlider());button("source",10,210,166,160);button("unlink",15,210,186,76);buttons.put(16,addRenderableWidget(Button.builder(text("anchor_clear"),b->net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new BuilderAction(menu.containerId,BuilderAction.ANCHOR,0))).bounds(leftPos+292,topPos+186,78,18).tooltip(Tooltip.create(text("anchor_help"))).build()));button("undo",12,14,208,88);button("cancel",13,108,208,88);button("return",14,210,208,160);update();}
 private void update(){for(var entry:buttons.entrySet())entry.getValue().active=menu.stat(10)==0&&menu.stat(14)==0;buttons.get(13).active=menu.stat(10)!=0;buttons.get(12).active=menu.stat(10)==0&&menu.stat(14)==0&&menu.stat(17)>0;buttons.get(14).active=menu.stat(10)==0&&menu.stat(14)==0&&menu.stat(8)>0;buttons.get(15).active=menu.stat(10)==0&&menu.stat(14)==0&&menu.stat(12)>0;buttons.get(16).active&=menu.stat(16)>0;buttons.get(10).active&=net.neoforged.fml.ModList.get().isLoaded("ae2");int mode=menu.stat(0);for(int id:new int[]{1,2})buttons.get(id).active&=mode!=2;for(int id:new int[]{3,4})buttons.get(id).active&=mode!=1&&mode!=3;for(int id:new int[]{5,6})buttons.get(id).active&=mode==0||mode==3||mode>=6;
 buttons.get(12).setTooltip(Tooltip.create(text("undo_help",menu.stat(17),menu.stat(18))));buttons.get(0).setMessage(text("mode_"+menu.stat(0)));buttons.get(10).setMessage(text("source_"+menu.stat(6)));buttons.get(11).setMessage(text(menu.stat(7)>0?"center":"corner"));}
 @Override protected void containerTick(){super.containerTick();update();range.tick();}
 private int effective(int n){int mode=menu.stat(0);if(n==0&&mode==2||n==1&&(mode==1||mode==3)||n==2&&(mode==1||mode==2||mode==4))return 1;if(n==2&&mode==5)return menu.stat(2);return menu.stat(n+1);}
 @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float dt){super.extractBackground(g,mx,my,dt);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,PANEL);g.outline(leftPos,topPos,imageWidth,imageHeight,BORDER);g.fill(leftPos+14,topPos+58,leftPos+196,topPos+158,0xFF0C1926);g.outline(leftPos+14,topPos+58,182,100,0xFF365269);
  miniature(g,menu.settings());
  for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,INNER);g.outline(leftPos+slot.x-1,topPos+slot.y-1,18,18,MUTED);}
 }
 private void miniature(GuiGraphicsExtractor g,BuilderSettings settings){
  var s=new BuilderSettings(settings.mode(),Math.min(6,settings.width()),Math.min(6,settings.height()),Math.min(6,settings.depth()),settings.rotation(),0,0,settings.centered());
  java.util.List<net.minecraft.core.BlockPos> positions;
  if(s.mode()==0){var cells=new java.util.ArrayList<net.minecraft.core.BlockPos>();for(int x=0;x<Math.min(6,s.width());x++)for(int z=0;z<Math.min(6,s.depth());z++)cells.add(new net.minecraft.core.BlockPos(x,0,z));positions=cells;}
  else positions=BuilderPlan.create(minecraft.level,net.minecraft.core.BlockPos.ZERO,net.minecraft.core.Direction.UP,net.minecraft.core.Direction.SOUTH,s);
  if(positions.isEmpty())return;var occupied=new java.util.HashSet<>(positions);
  double minX=1e9,maxX=-1e9,minY=1e9,maxY=-1e9;
  for(var p:positions)for(int x=0;x<2;x++)for(int y=0;y<2;y++)for(int z=0;z<2;z++){double px=p.getX()+x-(p.getZ()+z),py=(p.getX()+x+p.getZ()+z)*.4-(p.getY()+y);minX=Math.min(minX,px);maxX=Math.max(maxX,px);minY=Math.min(minY,py);maxY=Math.max(maxY,py);}
  float scale=(float)Math.min(150/Math.max(1,maxX-minX),59/Math.max(1,maxY-minY));float ox=(float)(leftPos+105-(minX+maxX)*.5*scale),oy=(float)(topPos+116-(minY+maxY)*.5*scale);
  var cells=new java.util.ArrayList<>(positions);cells.sort(java.util.Comparator.comparingInt(p->p.getX()+p.getZ()));
  for(var p:cells){float x=ox+(p.getX()-p.getZ())*scale,y=oy+((p.getX()+p.getZ())*.4f-p.getY())*scale;
   if(!occupied.contains(p.south()))face(g,x-scale,y+.4f*scale,1,.4f,0,-1,scale,0xFF28465C);
   if(!occupied.contains(p.east()))face(g,x,y+.8f*scale,1,-.4f,0,-1,scale,0xFF365B73);
   if(!occupied.contains(p.above()))face(g,x,y-scale,1,.4f,-1,.4f,scale,0xFF77BED9);
  }
 }
 private static void face(GuiGraphicsExtractor g,float x,float y,float ax,float ay,float bx,float by,float size,int color){var pose=g.pose();pose.pushMatrix();pose.translate(x,y);pose.mul(new org.joml.Matrix3x2f(ax,ay,bx,by,0,0));g.fill(0,0,Math.max(1,(int)Math.ceil(size)),Math.max(1,(int)Math.ceil(size)),color);pose.popMatrix();}
 private static void line(GuiGraphicsExtractor g,float x,float y,float bx,float by,int color){var p=g.pose();p.pushMatrix();p.translate(x,y);p.rotate((float)Math.atan2(by-y,bx-x));g.fill(0,0,(int)Math.ceil(Math.hypot(bx-x,by-y)),1,color);p.popMatrix();}
 @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,title,14,11,TEXT,false);g.text(font,text(menu.stat(14)>0?"locked":menu.stat(10)>0?"working":menu.stat(16)>0?"anchored_short":"ready"),imageWidth-14-font.width(text(menu.stat(14)>0?"locked":menu.stat(10)>0?"working":menu.stat(16)>0?"anchored_short":"ready")),11,BLUE,false);
  for(int n=0;n<3;n++)g.text(font,text(new String[]{"width","height","depth"}[n],effective(n)),246,41+n*28,TEXT,false);g.text(font,text("offset",menu.stat(5)),246,125,TEXT,false);
  int stateId=(menu.stat(11)&65535)|(menu.stat(13)<<16);var state=Block.stateById(stateId);if(stateId>0){g.item(new net.minecraft.world.item.ItemStack(state.getBlock()),22,64);g.text(font,font.plainSubstrByWidth(state.getBlock().getName().getString(),140),44,68,TEXT,false);}else g.text(font,text("select_short"),22,68,MUTED,false);
  g.text(font,text("rotation",menu.stat(4)*90),22,144,MUTED,false);g.text(font,text("buffer",menu.stat(8)),14,234,MUTED,false);g.text(font,text(menu.stat(17)==1?"history_one":"history",menu.stat(17)),14,249,MUTED,false);g.text(font,text("link_"+menu.stat(12)),14,264,BLUE,false);g.text(font,text(menu.stat(15)>4096?"plan_large_short":"plan_count",menu.stat(15)),14,279,menu.stat(15)>4096?0xFFEF7878:MUTED,false);g.text(font,text("hint"),14,308,MUTED,false);
 }
}
