package com.neolena.essenceindustry.mixin;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.WirelessAccessPointScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.implementations.WirelessAccessPointMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=WirelessAccessPointScreen.class,remap=false)
public abstract class BuilderWirelessScreenMixin extends AEBaseScreen<WirelessAccessPointMenu> {
 protected BuilderWirelessScreenMixin(WirelessAccessPointMenu m,Inventory inv,Component title,ScreenStyle style){super(m,inv,title,style);}
 @Inject(method="updateBeforeRender",at=@At("TAIL"))
 private void details(CallbackInfo ci){if(menu.getRange()==Long.MAX_VALUE)setTextContent("range",Component.translatable("gui.essenceindustry.builder.interdimensional"));}
}
