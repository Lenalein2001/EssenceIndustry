package com.neolena.essenceindustry.compat.ae2;
import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.*;
import appeng.api.parts.*;
import appeng.parts.AEBasePart;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
/** Network-powered accelerator mounted on an ME cable face. */
public final class BlockAcceleratorPart extends AEBasePart implements AcceleratorDevice,IGridTickable {
 private int speed=256;
 public BlockAcceleratorPart(IPartItem<?> item){super(item);getMainNode().setFlags().setIdlePowerUsage(64).addService(IGridTickable.class,this);}
 public BlockPos acceleratorPos(){return getBlockEntity().getBlockPos();}
 public BlockPos getTargetPos(){return acceleratorPos().relative(getSide());}
 public int getSpeed(){return speed;}
 public void setSpeed(int value){if(getLevel() instanceof ServerLevel level)AcceleratorEngine.setSpeed(level,this,value);else setSpeedDirect(value);}
 public void setSpeedDirect(int value){int v=Mth.clamp(value,1,256);if(speed!=v){speed=v;if(getHost()!=null)getHost().markForSave();}}
 public boolean payOperationEnergy(){var grid=getMainNode().getGrid();if(grid==null||!getMainNode().isPowered())return false;double cost=64.0*speed;var energy=grid.getEnergyService();return energy.extractAEPower(cost,Actionable.SIMULATE,PowerMultiplier.CONFIG)>=cost&&energy.extractAEPower(cost,Actionable.MODULATE,PowerMultiplier.CONFIG)>=cost;}
 @Override public TickingRequest getTickingRequest(IGridNode node){return new TickingRequest(1,1,false);}
 @Override public TickRateModulation tickingRequest(IGridNode node,int ticks){if(getLevel() instanceof ServerLevel level)AcceleratorEngine.tick(level,this);return TickRateModulation.SAME;}
 @Override public void getBoxes(IPartCollisionHelper helper){helper.addBox(2,2,13,14,14,16);helper.addBox(6,6,10,10,10,13);}
 @Override public float getCableConnectionLength(appeng.api.util.AECableType cable){return 2;}
 @Override public void readFromNBT(ValueInput input){super.readFromNBT(input);speed=Mth.clamp(input.getIntOr("acceleratorSpeed",256),1,256);}
 @Override public void writeToNBT(ValueOutput output){super.writeToNBT(output);output.putInt("acceleratorSpeed",speed);}
 @Override public void exportSettings(appeng.util.SettingsFrom mode,net.minecraft.core.component.DataComponentMap.Builder builder){
  super.exportSettings(mode,builder);
  if(mode==appeng.util.SettingsFrom.DISMANTLE_ITEM){var tag=new net.minecraft.nbt.CompoundTag();tag.putInt("essenceindustryAcceleratorSpeed",speed);builder.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));}
 }
 @Override public void importSettings(appeng.util.SettingsFrom mode,net.minecraft.core.component.DataComponentMap input,Player player){
  super.importSettings(mode,input,player);
  if(mode==appeng.util.SettingsFrom.DISMANTLE_ITEM){var data=input.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);if(data!=null)setSpeedDirect(data.copyTag().getIntOr("essenceindustryAcceleratorSpeed",256));}
 }
 @Override public boolean onUseWithoutItem(Player player,Vec3 hit){
  if(player instanceof ServerPlayer server&&player.mayBuild())server.openMenu(new SimpleMenuProvider((id,inv,p)->new BlockAcceleratorMenu(id,inv,this),Component.translatable("container.essenceindustry.block_accelerator")),b->{b.writeBlockPos(acceleratorPos());b.writeBlockPos(getTargetPos());});
  return true;
 }
}
