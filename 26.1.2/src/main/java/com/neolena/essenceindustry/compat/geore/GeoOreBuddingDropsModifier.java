package com.neolena.essenceindustry.compat.geore;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public final class GeoOreBuddingDropsModifier extends LootModifier {
    public static final MapCodec<GeoOreBuddingDropsModifier> CODEC = RecordCodecBuilder.mapCodec(
            instance -> codecStart(instance).apply(instance, GeoOreBuddingDropsModifier::new));

    public GeoOreBuddingDropsModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        var state = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
        if (state == null) {
            return generatedLoot;
        }
        var block = state.getBlock();
        var id = BuiltInRegistries.BLOCK.getKey(block);
        // The movement option clears GeoOre's budding tag, so identify its source blocks by registry ID.
        if (id != null && id.getNamespace().equals("geore") && id.getPath().startsWith("budding_")) {
            var item = block.asItem();
            if (item != Items.AIR) {
                generatedLoot.clear();
                generatedLoot.add(new ItemStack(item));
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
