package com.neolena.essenceindustry.mixin;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.helpers.WirelessTerminalMenuHost;
import com.neolena.essenceindustry.construction.QuantumWireless;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=WirelessTerminalMenuHost.class,remap=false)
public abstract class BuilderWirelessSignalMixin {
 @Inject(method="getAccessPointSignal",at=@At("HEAD"),cancellable=true)
 private void signal(IWirelessAccessPoint point,CallbackInfoReturnable<WirelessTerminalMenuHost.AccessPointSignal> ci){if(point.isActive()&&QuantumWireless.upgraded(point))ci.setReturnValue(new WirelessTerminalMenuHost.AccessPointSignal(0,Double.MAX_VALUE));}
}
