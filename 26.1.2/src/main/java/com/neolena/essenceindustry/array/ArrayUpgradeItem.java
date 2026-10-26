package com.neolena.essenceindustry.array;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;

public final class ArrayUpgradeItem extends Item {
 public final ArrayUpgrades.Kind kind;
 public final int tier;
 public ArrayUpgradeItem(Properties properties,ArrayUpgrades.Kind kind,int tier){super(properties.stacksTo(1));this.kind=kind;this.tier=tier;}
 @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> lines,TooltipFlag flag){
  Object value=switch(kind){case SPEED->1<<tier;case EFFICIENCY->tier*15;case CAPACITY->ArrayUpgrades.BASE_CAPACITY*(1<<tier)/1_000_000;};
  lines.accept(Component.translatable("tooltip.essenceindustry.array_upgrade."+kind.id,value).withStyle(ChatFormatting.GRAY));
 }
}
