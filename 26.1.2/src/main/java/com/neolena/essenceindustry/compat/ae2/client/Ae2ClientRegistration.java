package com.neolena.essenceindustry.compat.ae2.client;

import com.neolena.essenceindustry.compat.ae2.Ae2GrowthAccelerators;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class Ae2ClientRegistration {
    private Ae2ClientRegistration() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(Ae2ClientRegistration::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Ae2GrowthAccelerators.BLOCK_ACCELERATOR_MENU.get(), BlockAcceleratorScreen::new);
    }
}
