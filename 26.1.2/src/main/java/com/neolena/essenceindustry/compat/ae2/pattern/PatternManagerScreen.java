package com.neolena.essenceindustry.compat.ae2.pattern;

import static com.neolena.essenceindustry.client.UtilityTheme.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class PatternManagerScreen extends TextInputContainerScreen<PatternManagerMenu> {
    private final Button[] toggles = new Button[PatternManagerMenu.ROWS];
    private EditBox search;
    public static void register(IEventBus bus) { bus.addListener((RegisterMenuScreensEvent e) -> e.register(EssencePatternManager.MENU.get(), PatternManagerScreen::new)); }
    public PatternManagerScreen(PatternManagerMenu menu, Inventory player, Component title) { super(menu, player, title, 320, 240); }
    private static Component text(String key, Object... args) { return Component.translatable("gui.essenceindustry.pattern." + key, args); }
    private void action(int id) { if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id); }
    @Override protected void init() {
        String query = search == null ? "" : search.getValue(); super.init();
        search = new EditBox(font, leftPos + 12, topPos + 27, 296, 18, text("search")); search.setHint(text("search")); search.setMaxLength(64); search.setValue(query);
        search.setResponder(value -> ClientPacketDistributor.sendToServer(new PatternManagerSearch(menu.containerId, value))); addRenderableWidget(search);
        for (int row = 0; row < toggles.length; row++) { final int r = row; toggles[row] = addRenderableWidget(Button.builder(text("on"), b -> action(10 + r)).pos(leftPos + 250, topPos + 53 + row * 24).size(58, 20).build()); }
        addRenderableWidget(Button.builder(Component.literal("<"), b -> action(0)).pos(leftPos + 12, topPos + 218).size(22, 18).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> action(1)).pos(leftPos + 286, topPos + 218).size(22, 18).build());
    }
    @Override protected void containerTick() { super.containerTick(); for (int row = 0; row < toggles.length; row++) { toggles[row].active = menu.stat(5 + row) >= 0 && menu.stat(3) != 2; toggles[row].setMessage(text(menu.stat(5 + row) == 1 ? "on" : "off")); } }
    @Override public void extractBackground(GuiGraphicsExtractor g, int x, int y, float delta) {
        super.extractBackground(g, x, y, delta); g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL); g.outline(leftPos, topPos, imageWidth, imageHeight, BORDER);
        for (int row = 0; row < toggles.length; row++) g.fill(leftPos + 8, topPos + 51 + row * 24, leftPos + 312, topPos + 75 + row * 24, INNER);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g, int x, int y) {
        g.text(font, title, 12, 9, TEXT, false);
        for (int row = 0; row < toggles.length; row++) {
            var stack = menu.slots.get(row * 2 + 1).getItem(); if (stack.isEmpty()) continue;
            String name = stack.getHoverName().getString(); g.text(font, font.plainSubstrByWidth(name, 164), 78, 54 + row * 24, TEXT, false);
            g.text(font, text("ratio", menu.slots.get(row * 2).getItem().getCount(), stack.getCount()), 78, 64 + row * 24, MUTED, false);
        }
        g.centeredText(font, (menu.stat(0) + 1) + " / " + menu.stat(1) + "  -  " + menu.stat(2), 160, 222, MUTED);
        g.text(font, text(menu.stat(3) == 2 ? "locked" : menu.stat(3) == 1 ? "online" : menu.stat(3) == 3 ? "standby" : "offline", menu.stat(4)), 12, 202, MUTED, false);
    }
}
