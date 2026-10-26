package com.neolena.essenceindustry.mixin;
import appeng.menu.implementations.WirelessAccessPointMenu;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import com.neolena.essenceindustry.construction.QuantumWireless;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=WirelessAccessPointMenu.class,remap=false)
public abstract class BuilderWirelessMenuMixin {
 @Shadow @Final private WirelessAccessPointBlockEntity accessPoint;
 @Shadow private void setRange(long n){} @Shadow private void setDrain(long n){}
 @Inject(method="broadcastChanges",at=@At(value="INVOKE",target="Lappeng/menu/AEBaseMenu;broadcastChanges()V"))
 private void details(CallbackInfo ci){if(QuantumWireless.upgraded(accessPoint)){setRange(Long.MAX_VALUE);setDrain((long)(100*QuantumWireless.DRAIN));}}
}
