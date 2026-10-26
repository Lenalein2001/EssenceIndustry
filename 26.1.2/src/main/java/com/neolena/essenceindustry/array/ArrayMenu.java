package com.neolena.essenceindustry.array;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public final class ArrayMenu extends AbstractContainerMenu {
 public static final int PLAYER_START=71,PLAYER_END=107,UPGRADE_START=107;
 private final ArrayTile tile;
 private final ContainerLevelAccess access;private final BlockPos controllerPos;
 private final net.minecraft.world.SimpleContainer machineIcon=new net.minecraft.world.SimpleContainer(1);
 public BlockPos controllerPosition(){return controllerPos;}
 public ItemStack machineIcon(){return machineIcon.getItem(0);}
 private net.minecraft.core.Direction facing(){if(tile!=null)return ArrayLayout.facing(tile.getBlockState());var value=net.minecraft.core.Direction.values()[Math.clamp(stat(56),0,5)];return value.getAxis().isHorizontal()?value:net.minecraft.core.Direction.NORTH;}
 public BlockPos problemPosition(){return ArrayLayout.position(tile==null?controllerPos:tile.layoutAnchor(),facing(),stat(48),stat(49),stat(50));}
 private final ContainerData data=new SimpleContainerData(256);
 private final net.minecraft.world.SimpleContainer jobIcons=new net.minecraft.world.SimpleContainer(3);
 private int page,currentTab,patternPage;
 private final Inventory playerInventory;
 private final ItemStacksResourceHandler displayInventory,displayUpgrades;
 private final net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> displayPatterns;
 private final net.neoforged.neoforge.transfer.IndexModifier<net.neoforged.neoforge.transfer.item.ItemResource> displayPatternModifier;
 private boolean compactLayout;
 public ArrayMenu(int id,Inventory inv,RegistryFriendlyByteBuf buf){this(id,inv,buf.readBlockPos(),null);}
 public ArrayMenu(int id,Inventory inv,ArrayTile tile){this(id,inv,tile.getBlockPos(),tile);}
 private ArrayMenu(int id,Inventory player,BlockPos pos,ArrayTile tile){
  super(ProcessingArray.MENU.get(),id);controllerPos=pos;this.tile=tile;access=ContainerLevelAccess.create(player.player.level(),pos);addDataSlots(data);
  var inventory=tile==null?new ItemStacksResourceHandler(ArrayTile.SLOTS):tile.inventory();displayInventory=inventory;playerInventory=player;
  // The client keeps one flat page; server slots delegate to the selected persistent bank.
  var clientPatterns=new ItemStacksResourceHandler(36){@Override protected int getCapacity(int slot,net.neoforged.neoforge.transfer.item.ItemResource resource){return Math.min(1,super.getCapacity(slot,resource));}};
  net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> patterns=new net.neoforged.neoforge.transfer.ResourceHandler<>(){
   private ItemStacksResourceHandler bank(){return tile==null?clientPatterns:tile.patternInventory(patternPage);}
   public int size(){return 36;}
   public net.neoforged.neoforge.transfer.item.ItemResource getResource(int i){return bank().getResource(i);}
   public long getAmountAsLong(int i){return bank().getAmountAsLong(i);}
   public long getCapacityAsLong(int i,net.neoforged.neoforge.transfer.item.ItemResource r){return bank().getCapacityAsLong(i,r);}
   public boolean isValid(int i,net.neoforged.neoforge.transfer.item.ItemResource r){return bank().isValid(i,r);}
   public int insert(int i,net.neoforged.neoforge.transfer.item.ItemResource r,int n,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){return bank().insert(i,r,n,tx);}
   public int extract(int i,net.neoforged.neoforge.transfer.item.ItemResource r,int n,net.neoforged.neoforge.transfer.transaction.TransactionContext tx){return bank().extract(i,r,n,tx);}
  };
  displayPatterns=patterns;displayPatternModifier=(index,r,n)->{(tile==null?clientPatterns:tile.patternInventory(patternPage)).set(index,r,n);};
  for(int i=0;i<36;i++)addSlot(new ArraySlot(this,patterns,displayPatternModifier,i,18+i%6*18,70+i/6*18,tile,1,false));
  for(int i=0;i<16;i++)addSlot(new ArraySlot(this,inventory,36+i,18+i%4*18,70+i/4*18,tile,0,true));
  // Reserved network indices from the old press bank; no visible or accessible slots.
  for(int i=0;i<4;i++)addSlot(new Slot(new net.minecraft.world.SimpleContainer(4),i,-10000,-10000){
   @Override public boolean isActive(){return false;}@Override public boolean isFake(){return true;}
   @Override public boolean mayPlace(ItemStack stack){return false;}@Override public boolean mayPickup(Player player){return false;}
  });
  for(int i=0;i<12;i++)addSlot(new ArrayResultSlot(this,i,114+i%6*18,198+i/6*18,tile));
  for(int i=0;i<3;i++)addSlot(jobSlot(i,258+i*42,202));
  for(int y=0;y<3;y++)for(int x=0;x<9;x++)addSlot(new Slot(player,9+y*9+x,119+x*18,246+y*18));
  for(int x=0;x<9;x++)addSlot(new Slot(player,x,119+x*18,306));
  var upgrades=tile==null?new ItemStacksResourceHandler(3):tile.upgrades();displayUpgrades=upgrades;
  for(int i=0;i<3;i++)addSlot(new ArraySlot(this,upgrades,i,298,70+i*40,tile,0,true));
  addSlot(new Slot(machineIcon,0,-10000,-10000){@Override public boolean isActive(){return false;}@Override public boolean isFake(){return true;}@Override public boolean mayPickup(Player player){return false;}@Override public boolean mayPlace(ItemStack item){return false;}});

 }
 private Slot jobSlot(int local,int x,int y){return new Slot(jobIcons,local,x,y){
  @Override public boolean mayPlace(ItemStack s){return false;}
  @Override public boolean mayPickup(Player p){return false;}
  @Override public boolean isFake(){return true;}
  @Override public boolean isActive(){return tab()==0;}
 };}
 /** Client coordinates only; slot indices, handlers, page selection and carried items stay intact. */
 public void layoutClient(boolean compact){
  if(tile!=null||compactLayout==compact)return;
  compactLayout=compact;
  for(int i=0;i<36;i++)replaceDisplaySlot(i,new ArraySlot(this,displayPatterns,displayPatternModifier,i,18+i%(compact?9:6)*18,(compact?62:70)+i/(compact?9:6)*18,null,1,false));
  for(int i=0;i<16;i++)replaceDisplaySlot(36+i,new ArraySlot(this,displayInventory,36+i,(compact?16:18)+i%(compact?8:4)*18,(compact?62:70)+i/(compact?8:4)*18,null,0,true));
  for(int i=0;i<12;i++)replaceDisplaySlot(56+i,((ArrayResultSlot)slots.get(56+i)).atClientPosition((compact?192:114)+i%6*18,(compact?62:198)+i/6*18));
  for(int i=0;i<3;i++)replaceDisplaySlot(68+i,jobSlot(i,(compact?246:258)+i*(compact?20:42),compact?101:202));
  for(int y=0;y<3;y++)for(int x=0;x<9;x++)replaceDisplaySlot(PLAYER_START+y*9+x,new Slot(playerInventory,9+y*9+x,(compact?79:119)+x*18,(compact?158:246)+y*18));
  for(int x=0;x<9;x++)replaceDisplaySlot(PLAYER_START+27+x,new Slot(playerInventory,x,(compact?79:119)+x*18,compact?216:306));
  for(int i=0;i<3;i++)replaceDisplaySlot(UPGRADE_START+i,new ArraySlot(this,displayUpgrades,i,compact?16+i*54:298,compact?114:70+i*40,null,0,true));
 }
 private void replaceDisplaySlot(int index,Slot slot){slot.index=index;slots.set(index,slot);}
 private void stat(int field,int value){data.set(field*2,value&65535);data.set(field*2+1,value>>>16);}
 public int stat(int field){return(data.get(field*2)&65535)|(data.get(field*2+1)&65535)<<16;}
 public long pending(){return Integer.toUnsignedLong(stat(8))|((long)stat(23)<<32);}
 public long demand(){return Integer.toUnsignedLong(stat(42))|((long)stat(43)<<32);}
 public int tab(){return stat(36);}
 public void previewTab(int tab){if(tab>=0&&tab<=3)stat(36,tab);}
 private void selectTab(int tab){currentTab=tab;stat(36,tab);}
 public boolean invalid(int slot){long mask=Integer.toUnsignedLong(stat(20))|((long)stat(21)<<32);return(mask&(1L<<slot))!=0;}
 @Override public void broadcastChanges(){
  if(tile!=null){
   for(int k=0;k<5;k++){stat(64+k,tile.builder().stock(k));stat(69+k,tile.builder().needed(k));}
   stat(74,tile.builder().status().ordinal());stat(75,tile.builder().progress());stat(76,tile.builder().issue().getX());stat(77,tile.builder().issue().getY());stat(78,tile.builder().issue().getZ());
   stat(0,tile.width());stat(1,tile.depth());stat(2,tile.speed());stat(3,tile.racks());stat(4,tile.installed());stat(5,tile.active());stat(6,tile.patterns());stat(7,tile.ledger().jobs().size());
   stat(8,(int)tile.pending());stat(23,(int)(tile.pending()>>>32));stat(9,tile.layer());stat(10,page);
   stat(11,java.util.List.of("unknown","idle","running","shell","interior","rack","no_rack","capacity","unloaded","redstone","locked","mixed","work_error","size").indexOf(tile.diagnostic()));
   stat(12,tile.energy().getAmountAsInt());stat(13,tile.formed()?1:0);
   if(patternPage>=tile.patternPages())selectPatternPage(tile.patternPages()-1);
   stat(59,patternPage);stat(60,tile.patternPages());stat(61,tile.patternCapacity());stat(62,tile.storedPatterns());
   long mask=tile.invalidPatterns(patternPage);stat(20,(int)mask);stat(21,(int)(mask>>>32));stat(22,tile.hatches());
   for(int i=0;i<12;i++){int index=page*12+i;stat(24+i,index<tile.ledger().output().size()?(int)tile.ledger().output().get(index).amount():0);}
   stat(36,currentTab);for(int i=0;i<3;i++)stat(37+i,tile.upgradeTier(i));stat(40,tile.energyCapacity());stat(41,tile.efficiency());
   long demand=tile.demand();stat(42,(int)demand);stat(43,(int)(demand>>>32));stat(44,tile.maximumSpeed());
   stat(48,tile.problemX());stat(49,tile.problemY());stat(50,tile.problemZ());stat(51,ArrayConfig.maxWidth());stat(52,ArrayConfig.maxDepth());
   stat(53,tile.getMainNode().isActive()?1:0);stat(54,tile.scanProgress());stat(55,tile.guide()?1:0);stat(56,ArrayLayout.facing(tile.getBlockState()).ordinal());stat(57,tile.height());stat(58,ArrayConfig.maxHeight());
   var visible=tile.visualMachine();if(!ItemStack.isSameItemSameComponents(machineIcon.getItem(0),visible))machineIcon.setItem(0,visible.copyWithCount(visible.isEmpty()?0:1));
   for(int i=0;i<3;i++){
    var job=i<tile.ledger().jobs().size()?tile.ledger().jobs().get(i):null;
    stat(14+i,job==null?0:job.active());stat(17+i,job==null?0:job.remaining());stat(45+i,tile.progress(i));
    jobIcons.setItem(i,job==null?ItemStack.EMPTY:appeng.api.stacks.GenericStack.wrapInItemStack(new appeng.api.stacks.GenericStack(job.plan().outputs().getFirst().what(),1)));
   }
  }
  super.broadcastChanges();
 }
 private void selectPatternPage(int value){patternPage=value;for(int i=0;i<36;i++)((ArraySlot)slots.get(i)).changed();}
 @Override public boolean clickMenuButton(Player player,int id){
  if(!stillValid(player)||!player.mayBuild()||!(player instanceof ServerPlayer server)||tile==null)return false;
  if(id==22||id==23){com.neolena.essenceindustry.blueprint.BlueprintStorage.open(server,tile);return true;}if(id>=40&&id<=44)return false;
  if(id>=20&&id<=21){selectTab(id-20);if(id==23&&tile.builder().status()==ArrayBuilder.Status.IDLE)tile.builder().calculate();}
  else if(id==9)page=(page+1)%11;
  else if(id==30||id==31)selectPatternPage(Math.clamp(patternPage+(id==30?-1:1),0,tile.patternPages()-1));
  else tile.action(id,server);
  broadcastChanges();return true;
 }
 @Override public boolean stillValid(Player player){return(tile==null||!tile.isRemoved())&&access.evaluate((level,pos)->{
  if(level.getBlockState(pos).getBlock()!=ProcessingArray.CONTROLLER.get())return false;
  int height=tile==null?Math.max(5,stat(57)):tile.height();
  var start=tile==null?pos.below():tile.position(0,0,0);var anchor=tile==null?pos:tile.layoutAnchor();var corner=ArrayLayout.position(anchor,facing(),(tile==null?Math.max(5,stat(0)):tile.width())-1,height-1,(tile==null?Math.max(5,stat(1)):tile.depth())-1);
  double x=Math.clamp(player.getX(),Math.min(start.getX(),corner.getX()),Math.max(start.getX(),corner.getX())+1);
  double y=Math.clamp(player.getY(),start.getY(),start.getY()+height);
  double z=Math.clamp(player.getZ(),Math.min(start.getZ(),corner.getZ()),Math.max(start.getZ(),corner.getZ())+1);
  return player.distanceToSqr(x,y,z)<=64;
 },true);}
 @Override public void clicked(int slot,int button,ContainerInput input,Player player){
  if(!stillValid(player)||!player.mayBuild()||(slot>=0&&slot<slots.size()&&!slots.get(slot).isActive()))return;
  if(tile!=null&&slot>=0&&slot<36&&appeng.api.crafting.PatternDetailsHelper.isEncodedPattern(getCarried())&&!slots.get(slot).mayPlace(getCarried()))player.sendSystemMessage(tile.patternRejection(patternPage,getCarried()));
  super.clicked(slot,button,input,player);
 }
 @Override public void removed(Player player){super.removed(player);if(tile!=null)for(var slot:slots)if(slot instanceof ArraySlot array)tile.unwatch(array);}
 @Override public ItemStack quickMoveStack(Player player,int index){
  if(!stillValid(player)||!player.mayBuild()||index<0||index>=slots.size())return ItemStack.EMPTY;
  var slot=slots.get(index);if(!slot.isActive()||!slot.mayPickup(player)||!slot.hasItem())return ItemStack.EMPTY;
  var source=slot.getItem();var before=source.copy();if(index>=68&&index<71)return ItemStack.EMPTY;
  if(index<PLAYER_START||index>=UPGRADE_START){if(!moveItemStackTo(source,PLAYER_START,PLAYER_END,true))return ItemStack.EMPTY;}
  else{
   int start,end;
   if(appeng.api.crafting.PatternDetailsHelper.isEncodedPattern(source)){
    if(tile==null||!tile.mayEdit())return ItemStack.EMPTY;selectTab(1);
    var resource=net.neoforged.neoforge.transfer.item.ItemResource.of(source);
    if(!tile.canAddPattern(resource)){player.sendSystemMessage(tile.patternRejection(patternPage,source));return ItemStack.EMPTY;}
    int accepted=0,selected=patternPage;
    try(var tx=net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()){
     for(int p=0;p<tile.hatches()&&accepted<source.getCount();p++){var bank=tile.patternInventory(p);for(int i=0;i<36&&accepted<source.getCount();i++){int n=bank.insert(i,resource,source.getCount()-accepted,tx);if(n>0){accepted+=n;selected=p;}}}
     if(accepted==0){player.sendSystemMessage(tile.patternRejection(Math.min(patternPage,Math.max(0,tile.hatches()-1)),source));return ItemStack.EMPTY;}tx.commit();
    }
    source.shrink(accepted);selectPatternPage(selected);if(source.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,source);broadcastChanges();return before;
   }
   else if(source.getItem() instanceof ArrayUpgradeItem upgrade){selectTab(0);start=UPGRADE_START+upgrade.kind.ordinal();end=start+1;}
   else if(ArrayRecipes.machine(source)){selectTab(0);start=36;end=52;}
   else return ItemStack.EMPTY;
   if(!moveItemStackTo(source,start,end,false))return ItemStack.EMPTY;
  }
  if(source.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,source);return before;
 }
}
