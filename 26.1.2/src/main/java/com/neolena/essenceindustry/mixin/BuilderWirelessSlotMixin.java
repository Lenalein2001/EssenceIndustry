package com.neolena.essenceindustry.mixin;
import appeng.menu.slot.RestrictedInputSlot;
import com.neolena.essenceindustry.construction.QuantumWireless;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=RestrictedInputSlot.class,remap=false)
public abstract class BuilderWirelessSlotMixin {
 @Shadow @Final private RestrictedInputSlot.PlacableItemType which;
 @Inject(method="mayPlace",at=@At("HEAD"),cancellable=true)
 private void accept(ItemStack s,CallbackInfoReturnable<Boolean> ci){if(which==RestrictedInputSlot.PlacableItemType.RANGE_BOOSTER&&s.is(QuantumWireless.CARD.get()))ci.setReturnValue(true);}
}
