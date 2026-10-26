package com.neolena.essenceindustry.essence;

import static com.neolena.essenceindustry.client.UtilityTheme.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import com.neolena.essenceindustry.client.TextInputContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class EssenceConverterScreen extends TextInputContainerScreen<EssenceConverterMenu> {
    private Button mode, meExport;
    private final EditBox[] reserves = new EditBox[6];
    private boolean syncing;
    public static void register(IEventBus bus) { bus.addListener(EssenceConverterScreen::screens); }
    private static void screens(RegisterMenuScreensEvent event) { event.register(EssenceConverter.MENU.get(), EssenceConverterScreen::new); }
    public EssenceConverterScreen(EssenceConverterMenu menu, Inventory player, Component title) { super(menu, player, title, 320, 264); }
    private static Component text(String key, Object... args) { return Component.translatable("gui.essenceindustry.converter." + key, args); }
    @Override protected void init() {
        super.init();
        mode = addRenderableWidget(Button.builder(modeName(), button -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, menu.upgrading() ? 1 : 0);
        }).pos(leftPos + 12, topPos + 26).size(136, 20).build());
        meExport = addRenderableWidget(Button.builder(text(menu.meEnabled() ? "me_on" : "me_off"), button -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2);
        }).pos(leftPos + 12, topPos + 141).size(136, 20).build());
        meExport.setTooltip(Tooltip.create(text("me_help")));
        for (int i = 0; i < 6; i++) {
            final int tier = i;
            var box = new EditBox(font, leftPos + 26 + i * 46, topPos + 99, 36, 18, text("keep_tier", i + 1));
            box.setMaxLength(2); box.setFilter(s -> s.isEmpty() || s.matches("[0-9]{1,2}") && Integer.parseInt(s) <= 64);
            box.setValue(Integer.toString(menu.reserve(i))); box.setEditable(i <= menu.highest());
            box.setResponder(value -> {
                if (!syncing && !value.isEmpty()) ClientPacketDistributor.sendToServer(new ConverterReserve(menu.containerId, tier, Integer.parseInt(value)));
            });
            box.setTooltip(Tooltip.create(text("keep_help"))); reserves[i] = addRenderableWidget(box);
        }
    }
    private Component modeName() { return text(menu.upgrading() ? "upgrade" : "downgrade"); }
    @Override protected void containerTick() {
        super.containerTick(); mode.setMessage(modeName());
        meExport.setMessage(text(menu.meEnabled() ? "me_on" : "me_off")); meExport.active = menu.meStatus() != 3 && menu.meStatus() != 4;
        syncing = true;
        try { for (int i = 0; i < 6; i++) {
            if (!reserves[i].isFocused()) reserves[i].setValue(Integer.toString(menu.reserve(i)));
            reserves[i].setEditable(i <= menu.highest());
        } } finally { syncing = false; }
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int x, int y, float delta) {
        super.extractBackground(graphics, x, y, delta);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, BORDER);
        graphics.fill(leftPos + 10, topPos + 49, leftPos + 310, topPos + 139, INNER);
        for (var slot : menu.slots) {
            graphics.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, INNER);
            graphics.outline(leftPos + slot.x - 1, topPos + slot.y - 1, 18, 18, MUTED);
        }
    }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.text(font, title, 12, 9, TEXT, false);
        graphics.text(font, text(menu.upgrading() ? "ratio_up" : "ratio_down"), 158, 32, MUTED, false);
        String[] labels = {"I", "II", "III", "IV", "V", "VI"};
        for (int tier = 0; tier < 6; tier++) graphics.centeredText(font, labels[tier], 44 + tier * 46, 51, tier <= menu.highest() ? TEXT : MUTED);
        for(int tier=0;tier<6;tier++) graphics.centeredText(font, compact(menu.count(tier)), 44+tier*46, 80, MUTED);
        graphics.text(font, text("keep"), 12, 89, TEXT, false);
        graphics.text(font, text("reserve_hint"), 12, 122, MUTED, false);
        var target = EssenceConverterTile.essence(menu.target());
        graphics.text(font, text("target", target.getName(target.getDefaultInstance())), 158, 122, TEXT, false);
        graphics.text(font, text("me_status_" + menu.meStatus()), 158, 147, MUTED, false);
        graphics.text(font, playerInventoryTitle, 79, 173, MUTED, false);
    }
    @Override protected void extractTooltip(GuiGraphicsExtractor graphics, int x, int y) {
        super.extractTooltip(graphics, x, y);
        for(int tier=0;tier<6;tier++) if(isHovering(26+tier*46,79,38,10,x,y)) graphics.setComponentTooltipForNextFrame(font,java.util.List.of(Component.literal(Long.toString(menu.count(tier)))),x,y);
        for (int tier = 0; tier < 6; tier++) if (!menu.slots.get(tier).hasItem() && isHovering(36 + tier * 46, 62, 16, 16, x, y)) {
            var item = EssenceConverterTile.essence(tier);
            graphics.setComponentTooltipForNextFrame(font, java.util.List.of(item == Items.AIR ? text("no_insanium") : item.getName(item.getDefaultInstance())), x, y);
        }
    }
    private static String compact(long n) {
        if(n<10000)return Long.toString(n);
        double d=n;String[] units={"","k","M","G","T","P","E"};int i=0;
        while(d>=1000 && i<6){d/=1000;i++;}
        return String.format(java.util.Locale.ROOT,"%.1f%s",d,units[i]);
    }
}
