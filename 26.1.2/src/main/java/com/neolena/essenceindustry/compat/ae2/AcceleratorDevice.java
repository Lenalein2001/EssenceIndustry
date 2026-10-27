package com.neolena.essenceindustry.compat.ae2;
import net.minecraft.core.BlockPos;
/** Common control surface shared by block and cable-mounted accelerators. */
public interface AcceleratorDevice {
 BlockPos acceleratorPos(); BlockPos getTargetPos(); int getSpeed();
 void setSpeed(int speed); void setSpeedDirect(int speed); boolean isPowered(); boolean payOperationEnergy();
}
