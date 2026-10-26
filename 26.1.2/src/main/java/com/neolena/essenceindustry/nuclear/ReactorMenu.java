package com.neolena.essenceindustry.nuclear;

import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.transfer.item.*;

/** Eight durable inventory slots; changing tabs or resizing only changes presentation. */
public final class ReactorMenu extends AbstractContainerMenu {
 public static final int MACHINE_SLOTS=8,PLAYER_START=8,PLAYER_END=44;
 private static final int LONG_BASE=64,TEXT_BASE=128,TEXT_LIMIT=160;
 private final ReactorTile tile;private final BlockPos controller;private final ContainerLevelAccess access;
 private final ContainerData data=new SimpleContainerData(TEXT_BASE+TEXT_LIMIT);
 public ReactorMenu(int id,Inventory inventory,RegistryFriendlyByteBuf buffer){this(id,inventory,buffer.readBlockPos(),null);}
 public ReactorMenu(int id,Inventory inventory,BlockPos pos){this(id,inventory,pos,inventory.player.level().getBlockEntity(pos) instanceof ReactorTile reactor?reactor:null);}
 private ReactorMenu(int id,Inventory inventory,BlockPos pos,ReactorTile tile){
  super(NuclearReactor.MENU.get(),id);this.tile=inventory.player.level().isClientSide()?null:tile;controller=pos;access=ContainerLevelAccess.create(inventory.player.level(),pos);
  if(!inventory.player.level().isClientSide()&&this.tile==null)throw new IllegalArgumentException("Reactor menu needs a live controller");
  stat(5,ReactorCore.Status.UNFORMED.ordinal());stat(6,50);stat(9,-1);stat(11,Direction.NORTH.ordinal());addDataSlots(data);
  var handler=this.tile==null?new ItemStacksResourceHandler(MACHINE_SLOTS):this.tile.inventory();
  for(int slot=0;slot<MACHINE_SLOTS;slot++)addSlot(new FuelSlot(handler,slot,(slot<4?20:228)+(slot%4)*18,66));
  for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(inventory,9+y*9+x,79+x*18,157+y*18));
  for(int x=0;x<9;x++)addSlot(new Slot(inventory,x,79+x*18,215));
 }
 private final class FuelSlot extends ResourceHandlerSlot {
  FuelSlot(ItemStacksResourceHandler handler,int index,int x,int y){super(handler,handler::set,index,x,y);}
  void refresh(){clearCachedReturnStack();}
  @Override public boolean isActive(){return tab()==0;}
  @Override public boolean mayPlace(ItemStack stack){return inventoryAvailable()&&isActive()&&getContainerSlot()<4&&ReactorFuels.charge(stack)!=null&&super.mayPlace(stack);}
  @Override public boolean mayPickup(Player player){return inventoryAvailable()&&isActive()&&super.mayPickup(player);}
 }
 private void stat(int index,int value){data.set(index*2,value&65535);data.set(index*2+1,value>>>16);}
 private int stat(int index){return (data.get(index*2)&65535)|(data.get(index*2+1)&65535)<<16;}
 private void amount(int index,long value){for(int part=0;part<4;part++)data.set(LONG_BASE+index*4+part,(int)(value>>>(part*16))&65535);}
 private long amount(int index){long result=0;for(int part=0;part<4;part++)result|=(data.get(LONG_BASE+index*4+part)&65535L)<<(part*16);return result;}
 public int width(){return Math.max(5,stat(0));}public int depth(){return Math.max(5,stat(1));}public int height(){return Math.max(5,stat(2));}
 public int fuelSegments(){return Math.max(0,stat(3));}public boolean formed(){return stat(4)!=0;}
 public ReactorCore.Status status(){return ReactorCore.Status.values()[Math.clamp(stat(5),0,ReactorCore.Status.values().length-1)];}
 public int rods(){return Math.clamp(stat(6),0,100);}public boolean enabled(){return stat(7)!=0;}public boolean tripped(){return stat(8)!=0;}
 public String fuelId(){int index=stat(9);return index<0||index>=ReactorFuels.ALL.size()?"":ReactorFuels.ALL.get(index).id();}
 public int tab(){return stat(10);}public void previewTab(int tab){if(tab>=0&&tab<=1)stat(10,tab);}
 public int maximum(int axis){return Math.max(5,stat(12+axis));}
 public int water(){return Math.clamp(stat(16),0,ReactorSteam.MAX_TANK);}public int returnedWater(){return Math.clamp(stat(17),0,8000);}
 public int waterInputPorts(){return Math.max(0,stat(18));}public int steamOutputPorts(){return Math.max(0,stat(19));}
 public int controlColumnCount(){return Math.max(0,stat(24));}
 public boolean activeCooling(){return stat(20)!=0;}public int steam(){return Math.max(0,stat(21));}public int fluidCapacity(){return Math.max(1,stat(22));}public boolean canSwitchMode(){return stat(23)!=0;}public long production(){return amount(8);}
 public long fuelUnits(){return amount(0);}public long wasteUnits(){return amount(1);}public long energy(){return amount(2);}public long heatUnits(){return amount(3);}
 public long fuelCapacityUnits(){return amount(4);}public long wasteCapacityUnits(){return amount(5);}public long energyCapacity(){return amount(6);}public long heatCapacityUnits(){return amount(7);}
 public Component issue(){int length=Math.clamp(stat(15),0,TEXT_LIMIT);var text=new StringBuilder(length);for(int i=0;i<length;i++)text.append((char)(data.get(TEXT_BASE+i)&65535));return Component.literal(text.toString());}
 public BlockPos controllerPosition(){return controller;}
 private Direction facing(){
  if(tile!=null){var state=tile.getBlockState();if(state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))return state.getValue(BlockStateProperties.HORIZONTAL_FACING);if(state.hasProperty(BlockStateProperties.FACING)){var value=state.getValue(BlockStateProperties.FACING);if(value.getAxis().isHorizontal())return value;}}
  var face=Direction.values()[Math.clamp(stat(11),0,5)];return face.getAxis().isHorizontal()?face:Direction.NORTH;
 }
 private boolean inventoryAvailable(){return tile==null||tile.available();}
 private void refreshSlots(){if(tile!=null)for(int i=0;i<MACHINE_SLOTS;i++)((FuelSlot)slots.get(i)).refresh();}
 @Override public void broadcastChanges(){
  if(tile!=null){var core=tile.core();var rules=tile.rules();stat(0,tile.width());stat(1,tile.depth());stat(2,tile.height());stat(3,tile.fuelSegments());stat(4,tile.formed()?1:0);stat(5,tile.statusCode());stat(6,core.rods());stat(7,core.enabled()?1:0);stat(8,core.tripped()?1:0);
   int fuel=-1;for(int i=0;i<ReactorFuels.ALL.size();i++)if(ReactorFuels.ALL.get(i).id().equals(core.fuelId()))fuel=i;stat(9,fuel);
   var state=tile.getBlockState();var face=state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)?state.getValue(BlockStateProperties.HORIZONTAL_FACING):state.hasProperty(BlockStateProperties.FACING)?state.getValue(BlockStateProperties.FACING):Direction.NORTH;stat(11,face.ordinal());
   stat(12,ReactorConfig.maxWidth());stat(13,ReactorConfig.maxDepth());stat(14,ReactorConfig.maxHeight());
   stat(16,tile.water());stat(17,tile.returnedWater());stat(18,tile.waterInputPorts());stat(19,tile.steamOutputPorts());stat(20,tile.activeCooling()?1:0);stat(21,tile.steam());stat(22,tile.fluidCapacity());stat(23,tile.canSwitchMode()?1:0);stat(24,tile.controlColumnCount());amount(8,tile.production());
   amount(0,core.fuelUnits());amount(1,core.wasteUnits());amount(2,core.energy());amount(3,core.heatUnits());amount(4,rules.fuelCapacityUnits());amount(5,rules.wasteCapacityUnits());amount(6,rules.energyCapacity());amount(7,rules.heatCapacityUnits());
   String issue=tile.issue().getString();int length=Math.min(issue.length(),TEXT_LIMIT);if(length>0&&Character.isHighSurrogate(issue.charAt(length-1)))length--;stat(15,length);for(int i=0;i<TEXT_LIMIT;i++)data.set(TEXT_BASE+i,i<length?issue.charAt(i):0);
   refreshSlots();
  }super.broadcastChanges();
 }
 @Override public boolean clickMenuButton(Player player,int button){
  if(tile==null||!stillValid(player)||!player.mayBuild())return false;
  if(button==21&&player instanceof net.minecraft.server.level.ServerPlayer server){com.neolena.essenceindustry.blueprint.BlueprintStorage.open(server,tile);return true;}if(button==20)previewTab(0);
  else if(button>=0&&button<=7)tile.control(button);
  else if(button>=30&&button<=35)tile.adjustSize((button-30)/2,(button%2==0)?-1:1);
  else return false;
  broadcastChanges();return true;
 }
 @Override public boolean stillValid(Player player){
  if(tile!=null&&tile.isRemoved())return false;
  return access.evaluate((level,pos)->{if(!(level.getBlockEntity(pos) instanceof ReactorTile actual)||tile!=null&&actual!=tile)return false;
   int width=tile==null?width():tile.width(),depth=tile==null?depth():tile.depth(),height=tile==null?height():tile.height();
   var anchor=level.getBlockEntity(pos) instanceof ReactorTile r?r.layoutAnchor():pos;var a=NuclearLayout.pos(anchor,facing(),width,0,0,0);var b=NuclearLayout.pos(anchor,facing(),width,width-1,height-1,depth-1);var min=NuclearLayout.min(a,b);var max=NuclearLayout.max(a,b);
   double x=Math.clamp(player.getX(),min.getX(),max.getX()+1),y=Math.clamp(player.getY(),min.getY(),max.getY()+1),z=Math.clamp(player.getZ(),min.getZ(),max.getZ()+1);return player.distanceToSqr(x,y,z)<=64;
  },true);
 }
 @Override public void clicked(int slot,int button,ContainerInput input,Player player){if(!stillValid(player)||!player.mayBuild()||!inventoryAvailable()||slot>=0&&slot<slots.size()&&!slots.get(slot).isActive())return;refreshSlots();super.clicked(slot,button,input,player);}
 @Override public ItemStack quickMoveStack(Player player,int index){
  if(!stillValid(player)||!player.mayBuild()||!inventoryAvailable()||tab()!=0||index<0||index>=slots.size())return ItemStack.EMPTY;refreshSlots();var slot=slots.get(index);if(!slot.mayPickup(player)||!slot.hasItem())return ItemStack.EMPTY;
  var stack=slot.getItem();var before=stack.copy();if(!(index<MACHINE_SLOTS?moveItemStackTo(stack,PLAYER_START,PLAYER_END,true):moveItemStackTo(stack,0,4,false)))return ItemStack.EMPTY;
  if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return before;
 }
}
