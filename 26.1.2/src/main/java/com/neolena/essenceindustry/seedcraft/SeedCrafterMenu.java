package com.neolena.essenceindustry.seedcraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
public final class SeedCrafterMenu extends AbstractContainerMenu {
 private final SeedCrafterTile tile;private final ContainerLevelAccess access;private final net.minecraft.world.level.block.Block expected;private int status,fuelMode,fuelEnergy;
 private final ContainerData fuelData=new SimpleContainerData(28);
 private final ItemStacksResourceHandler displayInventory;private final Inventory playerInventory;private boolean compactFuelLayout;
 public SeedCrafterMenu(int id,Inventory player,RegistryFriendlyByteBuf buffer){this(id,player,buffer.readBlockPos(),null,net.minecraft.core.registries.BuiltInRegistries.BLOCK.byId(buffer.readVarInt()));}
 public SeedCrafterMenu(int id,Inventory player,SeedCrafterTile tile){this(id,player,tile.getBlockPos(),tile,tile.getBlockState().getBlock());}
 private SeedCrafterMenu(int id,Inventory player,BlockPos pos,SeedCrafterTile tile,net.minecraft.world.level.block.Block menuBlock){
  super(SeedCrafter.MENU.get(),id);this.tile=tile;expected=menuBlock;access=ContainerLevelAccess.create(player.player.level(),pos);
  boolean copier=expected instanceof com.neolena.essenceindustry.compat.hnn.ModelCopierBlock;
  var inv=tile==null?new ItemStacksResourceHandler(SeedCrafterTile.SLOTS){
   @Override public boolean isValid(int slot,net.neoforged.neoforge.transfer.item.ItemResource resource){return super.isValid(slot,resource)&&(slot>=SeedCrafterTile.INPUTS||!copier||resource.isEmpty()||com.neolena.essenceindustry.compat.hnn.ModelCopy.isIngredient(resource.toStack()));}
  }:tile.inventory();displayInventory=inv;playerInventory=player;
  boolean fuel=expected instanceof com.neolena.essenceindustry.nuclear.FuelProcessorBlock;
  for(int i=0;i<9;i++)addSlot(new CrafterSlot(inv,i,fuel?24+i*18:24+(i%3)*18,fuel?153:49+(i/3)*18,tile));
  for(int i=0;i<10;i++)addSlot(new CrafterSlot(inv,9+i,(fuel?204:178)+(i%5)*18,(fuel?153:49)+(i/5)*18,tile));
  for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(player,9+y*9+x,79+x*18,(fuel?196:146)+y*18));
  for(int x=0;x<9;x++)addSlot(new Slot(player,x,79+x*18,fuel?254:204));
  addDataSlot(new DataSlot(){public int get(){return tile instanceof com.neolena.essenceindustry.nuclear.FuelProcessorTile f?(int)(f.energy().getAmountAsLong()/1000):fuelEnergy;}public void set(int value){fuelEnergy=value;}});
  addDataSlot(new DataSlot(){public int get(){return tile instanceof com.neolena.essenceindustry.nuclear.FuelProcessorTile f?f.fabricationMode():fuelMode;}public void set(int value){fuelMode=value;}});
  addDataSlot(new DataSlot(){public int get(){return tile==null?status:tile.status();}public void set(int value){status=value;}});
  for(int i=0;i<28;i++){int part=i;addDataSlot(new DataSlot(){public int get(){if(!(tile instanceof com.neolena.essenceindustry.nuclear.FuelProcessorTile f))return fuelData.get(part);int value=switch(part/2){case 0->f.progress();case 1->f.durationTicks();case 2->f.formed()?1:0;case 3->f.issue().getX();case 4->f.issue().getY();case 5->f.issue().getZ();case 6->f.issueItem();case 7->f.issueFound();case 8->f.issueRequired();case 9->f.currentProductId();case 10->f.nextProductId();case 11->f.currentInputId();case 12->f.nextInputId();default->f.readyBatches();};return part%2==0?value&65535:value>>>16;}public void set(int value){fuelData.set(part,value);}});}

 }
 /** Client coordinates only. Slot IDs and the underlying inventories stay unchanged. */
 public void layoutFuelClient(boolean compact){
  if(tile!=null||!fuelProcessor()||compactFuelLayout==compact)return;
  compactFuelLayout=compact;int shift=compact?50:0;
  for(int i=0;i<9;i++)replaceDisplaySlot(i,new CrafterSlot(displayInventory,i,24+i*18,153-shift,null));
  for(int i=0;i<10;i++)replaceDisplaySlot(9+i,new CrafterSlot(displayInventory,9+i,204+(i%5)*18,153+(i/5)*18-shift,null));
  for(int y=0;y<3;y++)for(int x=0;x<9;x++)replaceDisplaySlot(19+y*9+x,new Slot(playerInventory,9+y*9+x,79+x*18,196+y*18-shift));
  for(int x=0;x<9;x++)replaceDisplaySlot(46+x,new Slot(playerInventory,x,79+x*18,254-shift));
 }
 private void replaceDisplaySlot(int index,Slot slot){slot.index=index;slots.set(index,slot);}
 public String fuelProcess(){return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(expected).getPath();}
 public ItemStack missingComponent(){return new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.byId(fuelStat(6)));}
 public int foundComponents(){return fuelStat(7);}public int requiredComponents(){return fuelStat(8);}
 public int fuelEnergy(){return fuelEnergy;}
 public ItemStack currentProduct(){return displayItem(fuelStat(9));}public ItemStack nextProduct(){return displayItem(fuelStat(10));}
 public ItemStack currentInput(){return displayItem(fuelStat(11));}public ItemStack nextInput(){return displayItem(fuelStat(12));}
 public int readyBatches(){return fuelStat(13);}
 private static ItemStack displayItem(int id){return id<=0?ItemStack.EMPTY:new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.byId(id));}
 public int fuelMode(){return fuelMode;}
 private int fuelStat(int index){return (fuelData.get(index*2)&65535)|(fuelData.get(index*2+1)&65535)<<16;}
 public int fuelProgress(){return fuelStat(0);}
 public int fuelDuration(){return fuelStat(1);}
 public boolean fuelFormed(){return fuelStat(2)!=0;}
 public BlockPos fuelIssue(){return new BlockPos(fuelStat(3),fuelStat(4),fuelStat(5));}
 public boolean fabricator(){return expected==com.neolena.essenceindustry.nuclear.NuclearFuels.MACHINES.get("fuel_fabricator").get();}
 @Override public boolean clickMenuButton(Player player,int button){if(button==1&&stillValid(player)&&player instanceof net.minecraft.server.level.ServerPlayer server&&tile instanceof com.neolena.essenceindustry.nuclear.FuelProcessorTile f){com.neolena.essenceindustry.blueprint.BlueprintStorage.open(server,f);return true;}if(button==0&&fabricator()&&stillValid(player)&&player.mayBuild()&&tile instanceof com.neolena.essenceindustry.nuclear.FuelProcessorTile f){f.cycleMode();return true;}return false;}
 public int status(){return status;}
 public boolean fuelProcessor(){return expected instanceof com.neolena.essenceindustry.nuclear.FuelProcessorBlock;}
 public boolean modelCopier(){return access.evaluate((level,pos)->net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getPath().equals("model_copier"),false); }
 @Override public boolean stillValid(Player player){return (tile==null || tile.available()) && stillValid(access,player,expected);}
 @Override public void removed(Player player){super.removed(player);if(tile!=null)for(var slot:slots)if(slot instanceof CrafterSlot s)tile.unwatch(s);}
 @Override public void clicked(int slot,int button,ContainerInput input,Player player){if(stillValid(player) && player.mayBuild())super.clicked(slot,button,input,player);}
 @Override public ItemStack quickMoveStack(Player player,int index){
  if(!stillValid(player) || !player.mayBuild() || index<0 || index>=slots.size())return ItemStack.EMPTY;
  var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var source=slot.getItem();var before=source.copy();
  if(!(index<SeedCrafterTile.SLOTS?moveItemStackTo(source,SeedCrafterTile.SLOTS,slots.size(),true):moveItemStackTo(source,0,9,false)))return ItemStack.EMPTY;
  if(source.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,source);return before;
 }
}
