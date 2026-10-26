package com.neolena.essenceindustry.essence;

import net.minecraft.world.level.storage.*;

/** Keeps optional network classes out of the converter's common class loading. */
public interface ConverterNetwork {
    void start();
    void stop();
    void changed();
    void settingsChanged();
    void load(ValueInput data);
    void save(ValueOutput data);
    int status();
}
