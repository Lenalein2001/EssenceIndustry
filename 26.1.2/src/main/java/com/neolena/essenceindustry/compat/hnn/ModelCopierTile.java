package com.neolena.essenceindustry.compat.hnn;
import com.neolena.essenceindustry.seedcraft.SeedCrafterTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
/** Shares the tested transactional inventory/scheduling implementation, not the MA recipe catalog. */
public final class ModelCopierTile extends SeedCrafterTile {
 public ModelCopierTile(BlockPos pos,BlockState state){super(HnnUtilities.TILE.get(),pos,state);}
 @Override protected boolean copiesModels(){return true;}
}
