package com.neolena.essenceindustry.mixin;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import com.neolena.essenceindustry.construction.QuantumWireless;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=WirelessAccessPointBlockEntity.class,remap=false)
public abstract class BuilderWirelessPointMixin {
 @Inject(method="getRange",at=@At("HEAD"),cancellable=true)
 private void range(CallbackInfoReturnable<Double> ci){if(QuantumWireless.upgraded((WirelessAccessPointBlockEntity)(Object)this))ci.setReturnValue(Double.MAX_VALUE);}
 @Inject(method="updatePower",at=@At("HEAD"),cancellable=true)
 private void power(CallbackInfo ci){var point=(WirelessAccessPointBlockEntity)(Object)this;if(QuantumWireless.upgraded(point)){point.getMainNode().setIdlePowerUsage(QuantumWireless.DRAIN);ci.cancel();}}
}
