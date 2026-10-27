package com.neolena.essenceindustry.quantum;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;

public final class QuantumModule extends Item {
    public enum Kind {
        MINING_SPEED("mining_speed", 32, true), AREA("area", 3, true), FORTUNE("fortune", 32, true), SILK_TOUCH("silk_touch", 1, true),
        DAMAGE("damage", 32, false), ATTACK_SPEED("attack_speed", 8, false), REACH("reach", 8, false), SWEEPING("sweeping", 8, false),
        DAMAGE_AMPLIFIER("damage_amplifier", 24, false), CRIT_CHANCE("crit_chance", 10, false), CRIT_DAMAGE("crit_damage", 8, false),
        LOOTING("looting", 16, false), LIFE_STEAL("life_steal", 8, false), KNOCKBACK("knockback", 8, false), EXPERIENCE("experience", 8, true),
        VEIN_MINING("vein_mining", 4, true), SUBMERGED_MINING("submerged_mining", 4, true);
        private final String id; public final int limit; public final boolean staff;
        Kind(String id, int limit, boolean staff) { this.id=id; this.limit=limit; this.staff=staff; }
        public String id() { return id; }
        public boolean accepts(boolean staffTool) { return this==REACH || staff==staffTool; }
    }
    public final Kind kind;
    public QuantumModule(Properties p, Kind kind) { super(p.stacksTo(1)); this.kind=kind; }
    @Override public void appendHoverText(ItemStack s, TooltipContext c, TooltipDisplay d, Consumer<Component> lines, TooltipFlag f) {
        lines.accept(Component.translatable("tooltip.essenceindustry.quantum_module." + kind.id()).withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.essenceindustry.quantum_module.limit", kind.limit).withStyle(ChatFormatting.DARK_GRAY));
    }
}
