package com.neolena.essenceindustry.compat.ae2;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public final class FlawlessBuddingDropsModifier extends LootModifier {
    public static final MapCodec<FlawlessBuddingDropsModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> codecStart(instance).apply(instance, FlawlessBuddingDropsModifier::new));
    private static final Identifier FLAWLESS = Identifier.parse("ae2:flawless_budding_quartz");
    private static final Identifier FLAWED = Identifier.parse("ae2:flawed_budding_quartz");

    public FlawlessBuddingDropsModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        var state = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
        if (state == null || !FLAWLESS.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) return loot;
        // Replace only AE2's downgraded drop; retain extra drops from other loot modifiers.
        for (int i = 0; i < loot.size(); i++) {
            var stack = loot.get(i);
            if (FLAWED.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
                loot.set(i, new ItemStack(state.getBlock().asItem(), stack.getCount()));
            }
        }
        return loot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() { return CODEC; }
}
