package com.neolena.essenceindustry.farmland;

import com.blakebr0.mysticalagriculture.api.farmland.IEssenceFarmland;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public final class VitalizingFertilizerItem extends Item {
    public VitalizingFertilizerItem(Properties properties) { super(properties); }
    @Override public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer(); var level = context.getLevel();
        if (player == null) return InteractionResult.PASS;
        BlockPos pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof FarmlandBlock) && level.getBlockState(pos.below()).getBlock() instanceof FarmlandBlock) {
            pos = pos.below(); state = level.getBlockState(pos);
        }
        if (!(state.getBlock() instanceof FarmlandBlock) || state.getBlock() instanceof VitalizedFarmlandBlock) return InteractionResult.PASS;
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) return InteractionResult.FAIL;
        if (!level.isClientSide() && level.setBlockAndUpdate(pos, converted(state))) {
            context.getItemInHand().consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }
    public static BlockState converted(BlockState original) {
        int tier = original.getBlock() instanceof IEssenceFarmland essence ? Math.clamp(essence.getTier().getValue(), 1, 6) : 1;
        return VitalizedFarmlands.SOILS.get(tier - 1).get().defaultBlockState().setValue(FarmlandBlock.MOISTURE, original.getValue(FarmlandBlock.MOISTURE));
    }
}
