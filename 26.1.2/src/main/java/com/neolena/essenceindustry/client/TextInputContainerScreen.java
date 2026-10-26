package com.neolena.essenceindustry.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** Focused text fields own keyboard input before container/game shortcuts see it. */
public abstract class TextInputContainerScreen<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> {
    protected TextInputContainerScreen(M menu, Inventory player, Component title, int width, int height) {
        super(menu, player, title, width, height);
    }

    private EditBox editing() {
        return getFocused() instanceof EditBox box && box.canConsumeInput() ? box : null;
    }

    @Override public boolean keyPressed(KeyEvent event) {
        var box = editing();
        if (box == null || event.isEscape()) return super.keyPressed(event);
        if (event.key() == InputConstants.KEY_TAB) {
            // Follow Screen's normal focus traversal without falling through to
            // AbstractContainerScreen's inventory/hotbar shortcuts afterward.
            var navigation = new FocusNavigationEvent.TabNavigation(!event.hasShiftDown());
            var path = nextFocusPath(navigation);
            if (path == null) { clearFocus(); path = nextFocusPath(navigation); }
            if (path != null) changeFocus(path);
        } else {
            // Printable letters arrive later through charTyped. EditBox returns
            // false here for E/W/Q/digits, which must still be consumed now.
            box.keyPressed(event);
        }
        return true;
    }

    @Override public boolean charTyped(CharacterEvent event) {
        var box = editing();
        if (box == null) return super.charTyped(event);
        box.charTyped(event);
        return true; // Rejected numeric characters must not become other shortcuts.
    }

    @Override public boolean keyReleased(KeyEvent event) {
        var box = editing();
        if (box == null) return super.keyReleased(event);
        // Preserve normal key-up clearing while preventing release shortcuts and
        // leaving no held movement key behind when text focus changes.
        KeyMapping.set(InputConstants.getKey(event), false);
        box.keyReleased(event);
        return true;
    }
}
