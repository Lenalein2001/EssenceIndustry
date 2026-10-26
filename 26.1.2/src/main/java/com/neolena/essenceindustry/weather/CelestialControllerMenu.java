package com.neolena.essenceindustry.weather;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class CelestialControllerMenu extends AbstractContainerMenu {
 public static final int RUN_NOW=100;
 private final ContainerLevelAccess access;private final ContainerData data;
 public CelestialControllerMenu(int id,Inventory player,RegistryFriendlyByteBuf buffer){this(id,player,buffer.readBlockPos());}
 public CelestialControllerMenu(int id,Inventory player,BlockPos pos){
  super(WeatherUtilities.MENU.get(),id);var level=player.player.level();access=ContainerLevelAccess.create(level,pos);
  if(level.isClientSide())data=new SimpleContainerData(3);else data=new ContainerData(){
   @Override public int get(int index){var state=level.getBlockState(pos);if(!state.is(WeatherUtilities.CONTROLLER.get()))return 0;return index==0?state.getValue(CelestialControllerBlock.MODE).ordinal():index==1?(state.getValue(CelestialControllerBlock.POWERED)?1:0):(level.dimension()==net.minecraft.world.level.Level.OVERWORLD?1:0);}
   @Override public void set(int index,int value){}
   @Override public int getCount(){return 3;}
  };addDataSlots(data);
 }
 public CelestialMode mode(){return CelestialMode.values()[Math.clamp(data.get(0),0,CelestialMode.values().length-1)];}
 public boolean powered(){return data.get(1)!=0;}
 public boolean supported(){return data.get(2)!=0;}
 @Override public boolean clickMenuButton(Player player,int button){
  if(!player.mayBuild() || !stillValid(player))return false;
  if(button==RUN_NOW)return access.evaluate((level,pos)->{
   var state=level.getBlockState(pos);
   if(!(level instanceof net.minecraft.server.level.ServerLevel server) || level.dimension()!=net.minecraft.world.level.Level.OVERWORLD || !state.is(WeatherUtilities.CONTROLLER.get()))return false;
   CelestialControllerBlock.apply(server,state.getValue(CelestialControllerBlock.MODE));
   return true;
  },false);
  if(button<0 || button>=CelestialMode.values().length)return false;
  access.execute((level,pos)->{var state=level.getBlockState(pos);if(state.is(WeatherUtilities.CONTROLLER.get()))level.setBlock(pos,state.setValue(CelestialControllerBlock.MODE,CelestialMode.values()[button]),net.minecraft.world.level.block.Block.UPDATE_CLIENTS);});return true;
 }
 @Override public boolean stillValid(Player player){return stillValid(access,player,WeatherUtilities.CONTROLLER.get());}
 @Override public ItemStack quickMoveStack(Player player,int slot){return ItemStack.EMPTY;}
}
