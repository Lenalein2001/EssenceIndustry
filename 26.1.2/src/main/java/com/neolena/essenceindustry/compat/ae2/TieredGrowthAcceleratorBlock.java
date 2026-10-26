package com.neolena.essenceindustry.compat.ae2;

import appeng.block.misc.GrowthAcceleratorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class TieredGrowthAcceleratorBlock extends GrowthAcceleratorBlock {
    private final int multiplier;

    public TieredGrowthAcceleratorBlock(BlockBehaviour.Properties properties, int multiplier) {
        super(metalProps(properties));
        this.multiplier = multiplier;
    }

    public int multiplier() {
        return this.multiplier;
    }
}
