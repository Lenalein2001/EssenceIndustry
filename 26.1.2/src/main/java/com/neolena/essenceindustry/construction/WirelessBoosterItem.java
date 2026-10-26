package com.neolena.essenceindustry.construction;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
public final class WirelessBoosterItem extends Item {
 public WirelessBoosterItem(Properties p){super(p);}
 @Override public void appendHoverText(ItemStack stack,TooltipContext c,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<Component> lines,TooltipFlag flag){lines.accept(Component.translatable("tooltip.essenceindustry.quantum_wireless_booster"));lines.accept(Component.translatable("gui.essenceindustry.builder.card_power"));}
}
