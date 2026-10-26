package com.neolena.essenceindustry.nuclear;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Called only by the client registration branch. */
public final class ReactorClient {
 public static void register(IEventBus bus){bus.addListener((RegisterMenuScreensEvent event)->{event.register(NuclearReactor.MENU.get(),ReactorScreen::new);event.register(NuclearReactor.PORT_MENU.get(),NuclearPortScreen::new);});bus.addListener((net.neoforged.neoforge.client.event.RegisterFluidModelsEvent event)->{var m=new net.minecraft.client.resources.model.sprite.Material(net.minecraft.resources.Identifier.parse("essenceindustry:block/reactor/steam"));event.register(new net.minecraft.client.renderer.block.FluidModel.Unbaked(m,m,null,state->0xFFFFFFFF),NuclearSteam.STEAM);});ReactorEffects.register();ReactorControlRenderer.register();}
 private ReactorClient(){}
}
