package com.neolena.essenceindustry.item;

import com.mojang.serialization.Dynamic;
import com.neolena.essenceindustry.library.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.*;
import net.minecraft.nbt.*;
import net.minecraft.world.item.component.CustomData;

public final class InventoryRecovery {
    public static void collect(DataComponentMap.Builder out, String key, Dynamic<?> raw) {
        out.set(SeedLibrary.RECOVERY.get(), null);
        if (raw == null) return;
        var tag = new CompoundTag(); tag.put(key, raw.convert(NbtOps.INSTANCE).getValue().copy());
        out.set(SeedLibrary.RECOVERY.get(), CustomData.of(tag));
    }
    public static Dynamic<?> read(DataComponentGetter data, String key, HolderLookup.Provider lookup) {
        var stored = data.get(SeedLibrary.RECOVERY.get());
        if (stored == null) return null;
        var tag = stored.copyTag().get(key);
        return tag == null ? null : new Dynamic<>(SeedDataSafety.ops(lookup), tag);
    }
    public static boolean safe(Dynamic<?> raw) { return raw == null || SeedDataSafety.tagWeight(raw.convert(NbtOps.INSTANCE).getValue()) <= SeedDataSafety.BUDGET; }
    private InventoryRecovery() {}
}
