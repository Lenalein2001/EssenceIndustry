package com.neolena.essenceindustry.mixin;

import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.neolena.essenceindustry.EssenceIndustry;
import com.neolena.essenceindustry.migration.LegacyCompatibility;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** MA owns these maps outside NeoForge's registries, so registry aliases cannot cover them. */
@Mixin(CropRegistry.class)
public abstract class LegacyCropIdsMixin {
    @ModifyVariable(method = {"getCropById", "getTierById", "getTypeById"}, at = @At("HEAD"), argsOnly = true)
    private Identifier essenceindustry$legacyCropId(Identifier id) {
        return id != null && id.getNamespace().equals(LegacyCompatibility.OLD_NAMESPACE)
                ? EssenceIndustry.id(id.getPath()) : id;
    }
}
