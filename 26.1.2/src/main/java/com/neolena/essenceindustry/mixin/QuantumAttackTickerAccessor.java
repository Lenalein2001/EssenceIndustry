package com.neolena.essenceindustry.mixin;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(LivingEntity.class)
public interface QuantumAttackTickerAccessor {
 @Accessor("attackStrengthTicker") int essenceindustry$getAttackTicker();
 @Accessor("attackStrengthTicker") void essenceindustry$setAttackTicker(int value);
}
