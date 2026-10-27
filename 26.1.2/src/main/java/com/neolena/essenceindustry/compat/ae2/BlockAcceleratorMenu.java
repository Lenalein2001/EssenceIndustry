package com.neolena.essenceindustry.compat.ae2;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;

import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class BlockAcceleratorMenu extends AbstractContainerMenu {
    private static final int SPEED_DATA_INDEX = 0;
    private static final int DATA_COUNT = 1;

    private final BlockPos acceleratorPos;
    private final BlockPos targetPos;
    private final ContainerData data;
    @Nullable
    private final AcceleratorDevice accelerator;

    public BlockAcceleratorMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(
            containerId,
            inventory,
            null,
            buffer.readBlockPos(),
            buffer.readBlockPos(),
            new SimpleContainerData(DATA_COUNT)
        );
    }

    public BlockAcceleratorMenu(
        int containerId,
        Inventory inventory,
        AcceleratorDevice accelerator
    ) {
        this(
            containerId,
            inventory,
            accelerator,
            accelerator.acceleratorPos(),
            accelerator.getTargetPos(),
            createData(accelerator)
        );
    }

    private BlockAcceleratorMenu(
        int containerId,
        Inventory inventory,
        @Nullable AcceleratorDevice accelerator,
        BlockPos acceleratorPos,
        BlockPos targetPos,
        ContainerData data
    ) {
        super(Ae2GrowthAccelerators.BLOCK_ACCELERATOR_MENU.get(), containerId);
        this.acceleratorPos = acceleratorPos;
        this.targetPos = targetPos;
        this.data = data;
        this.accelerator = accelerator;
        checkContainerDataCount(data, DATA_COUNT);
        addDataSlots(data);
    }

    private static ContainerData createData(AcceleratorDevice accelerator) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return index == SPEED_DATA_INDEX ? accelerator.getSpeed() : 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == SPEED_DATA_INDEX) {
                    accelerator.setSpeed(value);
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    public BlockPos getTargetPos() {
        return this.targetPos;
    }

    public int getSpeed() {
        return this.data.get(SPEED_DATA_INDEX);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (this.accelerator == null
            || buttonId < BlockAcceleratorBlockEntity.MIN_SPEED
            || buttonId > BlockAcceleratorBlockEntity.MAX_SPEED
            || !stillValid(player)) {
            return false;
        }
        this.accelerator.setSpeed(buttonId);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if(!player.isAlive()||!player.mayBuild()||!player.level().isLoaded(acceleratorPos)||player.distanceToSqr(acceleratorPos.getCenter())>64)return false;
        if(accelerator==null)return player.level().isClientSide();
        if(accelerator instanceof BlockAcceleratorPart part)return appeng.api.parts.PartHelper.getPart(player.level(),acceleratorPos,part.getSide())==part;
        return player.level().getBlockEntity(acceleratorPos)==accelerator;
    }
}
