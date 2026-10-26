package com.neolena.essenceindustry.simulation;
import com.neolena.essenceindustry.essence.ConverterNetwork;
import net.neoforged.neoforge.transfer.item.ItemResource;
public interface SimulationNetwork extends ConverterNetwork { long insert(ItemResource resource,int amount); }
