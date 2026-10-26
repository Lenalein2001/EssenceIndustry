package com.neolena.essenceindustry.mixin;
import appeng.util.inv.AppEngInternalInventory;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import com.neolena.essenceindustry.construction.QuantumWireless;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=AppEngInternalInventory.class,remap=false)
public abstract class BuilderWirelessInventoryMixin {
 @Inject(method="isItemValid",at=@At("HEAD"),cancellable=true)
 private void accept(int slot,ItemStack s,CallbackInfoReturnable<Boolean> ci){if(slot==0&&s.is(QuantumWireless.CARD.get())&&((AppEngInternalInventory)(Object)this).getHost() instanceof WirelessAccessPointBlockEntity)ci.setReturnValue(true);}
}
