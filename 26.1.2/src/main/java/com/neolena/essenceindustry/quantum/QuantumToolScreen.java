package com.neolena.essenceindustry.quantum;

import static com.neolena.essenceindustry.client.UtilityTheme.*;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class QuantumToolScreen extends TextInputContainerScreen<QuantumToolMenu> {
    private final boolean compactLayout;
    private final Inventory screenInventory;
    private boolean replacingForResize;
    public QuantumToolScreen(QuantumToolMenu menu,Inventory inv,Component title){this(menu,inv,title,needsCompact(net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth(),net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight()));}
    private QuantumToolScreen(QuantumToolMenu menu,Inventory inv,Component title,boolean compact){super(menu,inv,title,compact?320:384,compact?236:300);compactLayout=compact;screenInventory=inv;}
    private static boolean needsCompact(int width,int height){return width<404 || height<320;}
    @Override protected void init(){menu.layoutClient(compactLayout);super.init();}
    @Override public void resize(int width,int height){
        if(needsCompact(width,height)!=compactLayout){replacingForResize=true;minecraft.setScreen(new QuantumToolScreen(menu,screenInventory,title,needsCompact(width,height)));}
        else super.resize(width,height);
    }
    @Override public void removed(){if(!replacingForResize)super.removed();}
    public static void register(IEventBus bus){bus.addListener((RegisterMenuScreensEvent e)->{e.<QuantumToolMenu,QuantumToolScreen>register(QuantumTools.MENU.get(),(menu,inv,title)->new QuantumToolScreen(menu,inv,title));});}
    private static Component text(String key,Object... args){return Component.translatable("gui.essenceindustry.quantum_tool."+key,args);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float dt) {
        super.extractBackground(g,x,y,dt);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,PANEL);g.outline(leftPos,topPos,imageWidth,imageHeight,BORDER);
        for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,INNER);g.outline(leftPos+slot.x-1,topPos+slot.y-1,18,18,MUTED);}
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y) {
        g.text(font,title,12,compactLayout?8:10,TEXT,false);g.text(font,text("modules"),compactLayout?12:24,compactLayout?20:26,MUTED,false);
        int statsX=compactLayout?168:196,statsY=compactLayout?32:41;
        var tool=menu.tool();
        if(menu.staff()) {
            g.text(font,text("mining",12+6*QuantumLoadout.count(tool,QuantumModule.Kind.MINING_SPEED)),statsX,statsY,TEXT,false);
            int size=1+2*QuantumLoadout.count(tool,QuantumModule.Kind.AREA);
            g.text(font,text("area",size,size),statsX,statsY+15,TEXT,false);
            g.text(font,text("fortune",2*QuantumLoadout.count(tool,QuantumModule.Kind.FORTUNE)),statsX,statsY+30,MUTED,false);
            g.text(font,text("silk",QuantumLoadout.count(tool,QuantumModule.Kind.SILK_TOUCH)>0?"On":"Off"),statsX,statsY+45,MUTED,false);
            g.text(font,text("experience",25*QuantumLoadout.count(tool,QuantumModule.Kind.EXPERIENCE)),statsX,statsY+60,MUTED,false);
            g.text(font,text("reach",QuantumLoadout.count(tool,QuantumModule.Kind.REACH)),statsX,statsY+75,MUTED,false);
            g.text(font,text("vein",16*QuantumLoadout.count(tool,QuantumModule.Kind.VEIN_MINING)),statsX,statsY+90,MUTED,false);
            g.text(font,text("submerged",20*QuantumLoadout.count(tool,QuantumModule.Kind.SUBMERGED_MINING)),statsX,statsY+105,MUTED,false);
        } else {
            g.text(font,text("damage",29+4*QuantumLoadout.count(tool,QuantumModule.Kind.DAMAGE)+32*QuantumLoadout.count(tool,QuantumModule.Kind.DAMAGE_AMPLIFIER)),statsX,statsY,TEXT,false);
            g.text(font,text("attack_speed",String.format(java.util.Locale.ROOT,"%.1f",1.6+0.2*QuantumLoadout.count(tool,QuantumModule.Kind.ATTACK_SPEED))),statsX,statsY+12,TEXT,false);
            g.text(font,text("reach",String.format(java.util.Locale.ROOT,"%.1f",1.0*QuantumLoadout.count(tool,QuantumModule.Kind.REACH))),statsX,statsY+24,MUTED,false);
            g.text(font,text("sweeping",String.format(java.util.Locale.ROOT,"%.1f",QuantumCombat.aoeRadius(tool))),statsX,statsY+36,MUTED,false);
            g.text(font,text("crit_chance",QuantumCombat.critChance(tool)),statsX,statsY+48,TEXT,false);
            g.text(font,text("crit_damage",String.format(java.util.Locale.ROOT,"%.2f",QuantumCombat.critMultiplier(tool)-1.5f)),statsX,statsY+60,TEXT,false);
            g.text(font,text("looting",QuantumLoadout.count(tool,QuantumModule.Kind.LOOTING)),statsX,statsY+72,MUTED,false);
            g.text(font,text("life_steal",QuantumLoadout.count(tool,QuantumModule.Kind.LIFE_STEAL)),statsX,statsY+84,MUTED,false);
            g.text(font,text("knockback",String.format(java.util.Locale.ROOT,"%.2f",0.25*QuantumLoadout.count(tool,QuantumModule.Kind.KNOCKBACK))),statsX,statsY+96,MUTED,false);
        }
        if(!compactLayout){g.text(font,text(menu.staff()?"staff_hint":"sword_hint"),12,159,MUTED,false);g.text(font,text("removable"),12,173,MUTED,false);}
        g.text(font,playerInventoryTitle,compactLayout?79:111,compactLayout?145:196,MUTED,false);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float dt){
        super.extractRenderState(g,x,y,dt);
        if(compactLayout && isHovering(12,20,144,10,x,y))g.setComponentTooltipForNextFrame(font,java.util.List.of(text("modules"),text("removable"),text(menu.staff()?"staff_hint":"sword_hint")),x,y);
    }
}
