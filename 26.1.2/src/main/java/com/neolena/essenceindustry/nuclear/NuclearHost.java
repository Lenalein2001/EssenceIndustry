package com.neolena.essenceindustry.nuclear;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
public interface NuclearHost {
 boolean formed(); boolean contains(BlockPos pos); void partChanged(BlockPos pos); void open(Player player);
 ResourceHandler<ItemResource> itemPort(String role);
 EnergyHandler energyPort(String role);
 default ResourceHandler<FluidResource> fluidPort(String role){return null;}
}
