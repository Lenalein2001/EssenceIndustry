package com.neolena.essenceindustry.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.Set;

public final class BlockAcceleratorBlockEntity extends AENetworkedPoweredBlockEntity implements IPowerChannelState, AcceleratorDevice {
    private static final double POWER_PER_TICK = 64.0;
    public static final int MIN_SPEED = 1;
    public static final int MAX_SPEED = 256;
    private static final String SPEED_TAG = "acceleratorSpeed";
    private static final String LEGACY_STRENGTH_TAG = "acceleratorStrength";

    private int speed = MAX_SPEED;

    public BlockAcceleratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        updatePowerCapacity();
        setPowerSides(getGridConnectableSides(getOrientation()));
        getMainNode()
            .setFlags()
            .setIdlePowerUsage(POWER_PER_TICK)
            .setVisualRepresentation(state.getBlock())
            .addService(IGridTickable.class, new AcceleratorTicker());
    }

    @Override
    public InternalInventory getInternalInventory() {
        return InternalInventory.empty();
    }

    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return orientation.getSides(EnumSet.complementOf(EnumSet.of(RelativeSide.BACK)));
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        setPowerSides(getGridConnectableSides(orientation));
    }

    private void tick() {
        boolean powered = isPowered();
        if (powered != getBlockState().getValue(BlockAcceleratorBlock.POWERED)) {
            markForUpdate();
        }
        if (!powered || !(getLevel() instanceof ServerLevel level)) {
            return;
        }

        AcceleratorEngine.tick(level,this);
    }

    public BlockPos acceleratorPos(){return getBlockPos();}

    public BlockPos getTargetPos() {
        Direction top = getFront();
        return getBlockPos().relative(top.getOpposite());
    }

    public int getSpeed() {
        return this.speed;
    }

    public void setSpeed(int speed) {
        int clampedSpeed = Mth.clamp(speed, MIN_SPEED, MAX_SPEED);
        if (!(getLevel() instanceof ServerLevel level)) {
            setSpeedDirect(clampedSpeed);
            return;
        }

        AcceleratorEngine.setSpeed(level,this,clampedSpeed);
    }

    public void setSpeedDirect(int speed) {
        if (this.speed != speed) {
            this.speed = speed;
            updatePowerCapacity();
            saveChanges();
        }
    }

    private double getEnergyUsage() {
        return POWER_PER_TICK * this.speed;
    }

    private void updatePowerCapacity() {
        setInternalMaxPower(getEnergyUsage() * 200.0);
    }

    @Override
    public void loadTag(ValueInput input) {
        super.loadTag(input);
        this.speed = Mth.clamp(input.getInt(SPEED_TAG)
            .orElseGet(() -> input.getIntOr(LEGACY_STRENGTH_TAG, MAX_SPEED)), MIN_SPEED, MAX_SPEED);
        updatePowerCapacity();
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(SPEED_TAG, this.speed);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason == IGridNodeListener.State.POWER) {
            markForUpdate();
        }
    }

    @Override
    public AECableType getCableConnectionType(Direction direction) {
        return AECableType.COVERED;
    }

    public boolean payOperationEnergy() {
        double cost = getEnergyUsage();
        var grid = getMainNode().getGrid();
        if (grid != null && getMainNode().isPowered()) {
            // Pick one source only. Never partially drain the grid before falling back to FE.
            var energy = grid.getEnergyService();
            if (energy.extractAEPower(cost, Actionable.SIMULATE, PowerMultiplier.CONFIG) >= cost)
                return energy.extractAEPower(cost, Actionable.MODULATE, PowerMultiplier.CONFIG) >= cost;
        }
        // An initialized standalone accelerator also owns an unpowered AE grid.
        // Its independently supplied FE buffer must remain usable in that state.
        return extractAEPower(cost, Actionable.SIMULATE) >= cost
                && extractAEPower(cost, Actionable.MODULATE) >= cost;
    }

    @Override
    public boolean isPowered() {
        if (!isClientSide()) {
            if (getMainNode().isPowered()) return true;
            return extractAEPower(getEnergyUsage(), Actionable.SIMULATE) >= getEnergyUsage();
        }
        return getBlockState().getValue(BlockAcceleratorBlock.POWERED);
    }

    @Override
    public boolean isActive() {
        return isPowered();
    }

    private final class AcceleratorTicker implements IGridTickable {
        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(1, 1, false);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            tick();
            return TickRateModulation.SAME;
        }
    }
}
