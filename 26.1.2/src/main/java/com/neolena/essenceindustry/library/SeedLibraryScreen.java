package com.neolena.essenceindustry.library;

import static com.neolena.essenceindustry.client.UtilityTheme.*;
import java.text.NumberFormat;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class SeedLibraryScreen extends TextInputContainerScreen<SeedLibraryMenu> {
    private EditBox search;
    private final Button[] pageButtons = new Button[4];
    public static void register(IEventBus bus) { bus.addListener(SeedLibraryScreen::screens); }
    private static void screens(RegisterMenuScreensEvent event) { event.register(SeedLibrary.MENU.get(), SeedLibraryScreen::new); }
    public SeedLibraryScreen(SeedLibraryMenu menu, Inventory player, Component title) { super(menu, player, title, 320, 240); }
    private static Component text(String key, Object... args) { return Component.translatable("gui.essenceindustry.library." + key, args); }
    private void action(int button) { if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button); }
    @Override protected void init() {
        String query = search == null ? "" : search.getValue();
        super.init();
        search = new EditBox(font, leftPos + 12, topPos + 24, 182, 18, text("search"));
        search.setMaxLength(64); search.setHint(text("search")); search.setValue(query);
        search.setResponder(value -> ClientPacketDistributor.sendToServer(new SeedLibrarySearch(menu.containerId, value)));
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(text("deposit"), b -> action(4)).pos(leftPos + 200, topPos + 23).size(108, 20).build());
        int[] x = {12, 94, 120, 290};
        for (int i = 0; i < 4; i++) {
            final int button = i;
            pageButtons[i] = addRenderableWidget(Button.builder(Component.literal(i % 2 == 0 ? "<" : ">"), b -> action(button)).pos(leftPos + x[i], topPos + 130).size(18, 16).build());
        }
    }
    @Override protected void containerTick() {
        super.containerTick();
        pageButtons[0].active = menu.stat(0) > 0; pageButtons[1].active = menu.stat(0) + 1 < menu.stat(1);
        pageButtons[2].active = menu.stat(2) > 0; pageButtons[3].active = menu.stat(2) + 1 < menu.stat(3);
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int x, int y, float delta) {
        super.extractBackground(graphics, x, y, delta);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, BORDER);
        graphics.fill(leftPos + 10, topPos + 54, leftPos + 113, topPos + 128, INNER);
        graphics.fill(leftPos + 119, topPos + 54, leftPos + 310, topPos + 128, INNER);
        for (var slot : menu.slots) {
            graphics.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, INNER);
            var selected = menu.slots.get(SeedLibraryMenu.CROPS).getItem();
            int outline = slot.index < SeedLibraryMenu.CROPS && !selected.isEmpty() && slot.getItem().is(selected.getItem()) ? BORDER : MUTED;
            graphics.outline(leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18, outline);
        }
    }
    /** Full stat names; native item tooltips still expose any additional mod data. */
    public static String traits(ItemStack stack) {
        var stats = SeedTraits.read(stack);
        return stats.known() ? text("production", stats.growth(), stats.yield()).getString() : "";
    }
    private String fit(String value, int width) { return font.width(value) <= width ? value : font.plainSubstrByWidth(value, width - font.width("...")) + "..."; }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.text(font, title, 12, 9, TEXT, false);
        graphics.text(font, text("crops", menu.stat(4)), 12, 44, MUTED, false);
        var first = menu.slots.get(SeedLibraryMenu.CROPS).getItem();
        graphics.text(font, fit(first.isEmpty() ? text("variants").getString() : first.getItem().getName(first.getItem().getDefaultInstance()).getString() + " (" + menu.stat(5) + ")", 188), 121, 44, TEXT, false);
        var format = NumberFormat.getIntegerInstance(Locale.ROOT);
        for (int row = 0; row < SeedLibraryMenu.VARIANTS; row++) {
            var seed = menu.slots.get(SeedLibraryMenu.CROPS + row).getItem();
            if (seed.isEmpty()) continue;
            graphics.text(font, fit(format.format(menu.count(row)), 158), 146, 55 + row * 18, TEXT, false);
            String traits = traits(seed);
            graphics.text(font, fit(traits.isEmpty() ? (seed.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME) ? seed.getHoverName().getString() : text(seed.isComponentsPatchEmpty() ? "standard" : "custom").getString()) : traits, 158), 146, 64 + row * 18, BRASS, false);
        }
        graphics.centeredText(font, (menu.stat(0) + 1) + " / " + Math.max(1, menu.stat(1)), 62, 134, MUTED);
        graphics.centeredText(font, (menu.stat(2) + 1) + " / " + Math.max(1, menu.stat(3)), 214, 134, MUTED);
        graphics.text(font, playerInventoryTitle, 79, 149, MUTED, false);
        graphics.textWithWordWrap(font, text("help"), 12, 161, 60, MUTED);
        if (menu.stat(4) == 0) graphics.textWithWordWrap(font, text(menu.stat(6) == 1 ? "locked" : "empty"), 124, 65, 176, MUTED);
    }
    @Override protected void extractTooltip(GuiGraphicsExtractor graphics, int x, int y) {
        super.extractTooltip(graphics, x, y);
        for (int row = 0; row < SeedLibraryMenu.VARIANTS; row++) {
            if (isHovering(145, 55 + row * 18, 162, 18, x, y) && menu.count(row) > 0) {
                var seed = menu.slots.get(SeedLibraryMenu.CROPS + row).getItem();
                var lines = new ArrayList<Component>();
                lines.add(seed.getHoverName()); lines.add(text("stored", NumberFormat.getIntegerInstance(Locale.ROOT).format(menu.count(row))));
                var stats = SeedTraits.read(seed);
                if (stats.known()) {
                    lines.add(text("growth", stats.growth())); lines.add(text("yield", stats.yield()));
                    lines.add(text("resistance", stats.resistance())); lines.add(text("mutability", stats.mutability()));
                }
                lines.add(text("best_first"));
                lines.add(text("withdraw")); graphics.setComponentTooltipForNextFrame(font, lines, x, y);
            }
        }
    }
}
