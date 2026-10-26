package com.neolena.essenceindustry.compat.pylons;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.permutated.pylons.machines.base.AbstractPylonScreen;

public final class PylonClient {
    public static void register(IEventBus bus) { bus.addListener(PylonClient::screens); }
    private static void screens(RegisterMenuScreensEvent event) { event.register(EnhancedHarvestingPylon.MENU.get(), Screen::new); }
    private static final class Screen extends AbstractPylonScreen<EnhancedHarvestingPylonMenu> {
        private net.minecraft.client.gui.components.Button me;
        private Component meText() { return Component.translatable("gui.essenceindustry.pylon_me_" + menu.meStatus()); }
        @Override protected void containerTick() { super.containerTick(); me.setMessage(meText()); me.active = menu.meStatus() != 3 && menu.meStatus() != 4; }
        Screen(EnhancedHarvestingPylonMenu menu, Inventory inventory, Component title) { super(menu, inventory, Component.translatable("gui.essenceindustry.harvest_pylon")); }
        @Override protected void init() {
            super.init();
            rangeButton.setX(leftPos + 86); rangeButton.setWidth(52);
            me = addRenderableWidget(net.minecraft.client.gui.components.Button.builder(meText(), b -> {
                if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId,100);
            }).pos(leftPos+8,topPos+54).size(160,20).build());
        }
        @Override public void extractBackground(GuiGraphicsExtractor graphics, int x, int y, float delta) {
            super.extractBackground(graphics, x, y, delta);
            graphics.fill(leftPos + 7, topPos + 53, leftPos + 169, topPos + 72, 0xFFC6C6C6);
        }
        @Override protected void extractLabels(GuiGraphicsExtractor graphics, int x, int y) {
            super.extractLabels(graphics, x, y);
            drawText(graphics, Component.translatable("gui.essenceindustry.self_watering"), 42);

        }
    }
    private PylonClient() {}
}
