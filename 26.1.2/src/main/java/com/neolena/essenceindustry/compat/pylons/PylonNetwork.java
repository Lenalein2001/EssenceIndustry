package com.neolena.essenceindustry.compat.pylons;
import com.neolena.essenceindustry.essence.ConverterNetwork;
import net.neoforged.neoforge.transfer.item.ItemResource;
public interface PylonNetwork extends ConverterNetwork {
    long insert(ItemResource resource, int amount);
}
