package com.neolena.essenceindustry.compat.hnn;
import com.neolena.essenceindustry.seedcraft.SeedCrafterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class ModelCopierBlock extends SeedCrafterBlock {
 public ModelCopierBlock(Properties p){super(p);}
 @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new ModelCopierTile(pos,state);}
}
