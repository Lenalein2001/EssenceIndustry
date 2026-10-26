package com.neolena.essenceindustry.compat.ae2.client;

import com.neolena.essenceindustry.compat.ae2.BlockAcceleratorBlockEntity;
import com.neolena.essenceindustry.compat.ae2.BlockAcceleratorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockAcceleratorScreen extends AbstractContainerScreen<BlockAcceleratorMenu> {
    private static final int PANEL_COLOR = 0xEE17131F;
    private static final int PANEL_INNER_COLOR = 0xEE282035;
    private static final int BORDER_COLOR = 0xFFB45DFF;
    private static final int TEXT_COLOR = 0xFFF3EFFF;
    private static final int MUTED_TEXT_COLOR = 0xFFB8ABC7;
    private static final int[] MARKED_SPEEDS = {8, 16, 32, 64, 128, 256};
    private static final int SLIDER_X = 12;
    private static final int SLIDER_Y = 84;
    private static final int SLIDER_WIDTH = 166;
    private static final int SLIDER_HANDLE_MARGIN = 4;

    private SpeedSlider speedSlider;
    private boolean draggingSpeedSlider;

    public BlockAcceleratorScreen(
        BlockAcceleratorMenu menu,
        Inventory inventory,
        Component title
    ) {
        super(menu, inventory, title, 190, 132);
        this.titleLabelX = 12;
        this.titleLabelY = 9;
    }

    @Override
    protected void init() {
        super.init();
        this.speedSlider = addRenderableWidget(
            new SpeedSlider(
                this.leftPos + SLIDER_X,
                this.topPos + SLIDER_Y,
                SLIDER_WIDTH
            )
        );
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.speedSlider.syncFromMenu();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && this.speedSlider.mouseClicked(event, doubleClick)) {
            setFocused(this.speedSlider);
            setDragging(true);
            this.draggingSpeedSlider = true;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(
        MouseButtonEvent event,
        double dragX,
        double dragY
    ) {
        if (event.button() == 0 && this.draggingSpeedSlider) {
            return this.speedSlider.mouseDragged(event, dragX, dragY);
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && this.draggingSpeedSlider) {
            this.draggingSpeedSlider = false;
            setDragging(false);
            return this.speedSlider.mouseReleased(event);
        }
        return super.mouseReleased(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(
            this.leftPos,
            this.topPos,
            this.leftPos + this.imageWidth,
            this.topPos + this.imageHeight,
            PANEL_COLOR
        );
        graphics.outline(this.leftPos, this.topPos, this.imageWidth, this.imageHeight, BORDER_COLOR);
        graphics.fill(
            this.leftPos + 8,
            this.topPos + 27,
            this.leftPos + this.imageWidth - 8,
            this.topPos + 77,
            PANEL_INNER_COLOR
        );
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, TEXT_COLOR, false);
        graphics.text(
            this.font,
            Component.translatable("gui.essenceindustry.block_accelerator.attached"),
            12,
            32,
            MUTED_TEXT_COLOR,
            false
        );

        if (this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        BlockState targetState = this.minecraft.level.getBlockState(this.menu.getTargetPos());
        Component targetName = targetState.isAir()
            ? Component.translatable("gui.essenceindustry.block_accelerator.nothing")
            : targetState.getBlock().getName();
        String shortenedName = this.font.plainSubstrByWidth(targetName.getString(), 137);
        graphics.text(this.font, shortenedName, 35, 48, TEXT_COLOR, false);

        ItemStack icon = targetState.getBlock().getCloneItemStack(
            this.minecraft.level,
            this.menu.getTargetPos(),
            targetState,
            false,
            this.minecraft.player
        );
        if (!icon.isEmpty()) {
            graphics.item(icon, 13, 44);
        }

        String coordinates = Component.translatable(
            "gui.essenceindustry.block_accelerator.position",
            this.menu.getTargetPos().getX(),
            this.menu.getTargetPos().getY(),
            this.menu.getTargetPos().getZ()
        ).getString();
        graphics.text(this.font, coordinates, 13, 65, MUTED_TEXT_COLOR, false);
        renderSpeedMarkers(graphics);
    }

    private void renderSpeedMarkers(GuiGraphicsExtractor graphics) {
        int trackWidth = SLIDER_WIDTH - SLIDER_HANDLE_MARGIN * 2;
        for (int markedSpeed : MARKED_SPEEDS) {
            int markerX = SLIDER_X
                + SLIDER_HANDLE_MARGIN
                + (int) Math.round(speedToSliderValue(markedSpeed) * trackWidth);
            String label = Integer.toString(markedSpeed);
            graphics.fill(markerX, SLIDER_Y + 20, markerX + 1, SLIDER_Y + 23, MUTED_TEXT_COLOR);
            graphics.centeredText(
                this.font,
                label,
                markerX,
                SLIDER_Y + 25,
                MUTED_TEXT_COLOR
            );
        }
    }

    private final class SpeedSlider extends AbstractSliderButton {
        private int displayedSpeed;
        private int pendingSpeed = -1;

        private SpeedSlider(int x, int y, int width) {
            super(
                x,
                y,
                width,
                20,
                Component.empty(),
                speedToSliderValue(BlockAcceleratorScreen.this.menu.getSpeed())
            );
            this.displayedSpeed = clampSpeed(BlockAcceleratorScreen.this.menu.getSpeed());
            updateMessage();
        }

        private void syncFromMenu() {
            int serverSpeed = clampSpeed(BlockAcceleratorScreen.this.menu.getSpeed());
            if (serverSpeed == this.pendingSpeed) {
                this.pendingSpeed = -1;
            }
            if (this.pendingSpeed == -1 && serverSpeed != this.displayedSpeed) {
                this.displayedSpeed = serverSpeed;
                this.value = speedToSliderValue(serverSpeed);
                updateMessage();
            }
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(
                "gui.essenceindustry.block_accelerator.speed",
                this.displayedSpeed
            ));
        }

        @Override
        protected void applyValue() {
            int newSpeed = sliderValueToSpeed(this.value);
            if (newSpeed == this.displayedSpeed) {
                return;
            }

            this.displayedSpeed = newSpeed;
            this.pendingSpeed = newSpeed;
            updateMessage();
            if (BlockAcceleratorScreen.this.minecraft != null
                && BlockAcceleratorScreen.this.minecraft.gameMode != null) {
                BlockAcceleratorScreen.this.minecraft.gameMode.handleInventoryButtonClick(
                    BlockAcceleratorScreen.this.menu.containerId,
                    newSpeed
                );
            }
        }
    }

    private static int clampSpeed(int speed) {
        return Math.max(
            BlockAcceleratorBlockEntity.MIN_SPEED,
            Math.min(BlockAcceleratorBlockEntity.MAX_SPEED, speed)
        );
    }

    private static double speedToSliderValue(int speed) {
        return Math.log(clampSpeed(speed)) / Math.log(BlockAcceleratorBlockEntity.MAX_SPEED);
    }

    private static int sliderValueToSpeed(double value) {
        return clampSpeed((int) Math.round(
            Math.pow(BlockAcceleratorBlockEntity.MAX_SPEED, value)
        ));
    }
}

