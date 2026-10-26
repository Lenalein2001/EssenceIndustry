package com.neolena.essenceindustry.blueprint;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.neolena.essenceindustry.nuclear.*;
import com.neolena.essenceindustry.array.*;
import com.neolena.essenceindustry.construction.*;
/** One persistent supply bank per controller, shared by every viewing player. */
public final class BlueprintStorage {
 private static final Map<BlockEntity,ItemStack> CACHE=new WeakHashMap<>();
 /** An unresolved construction transfer must remain attached to its controller. */
 public static boolean safeToMove(BlockEntity tile){
  if(tile.getLevel()==null)return false;if(tile.getLevel().isClientSide())return true;
  if(BlueprintRepair.pending(tile))return false;
  if(!CACHE.containsKey(tile)&&!tile.getPersistentData().contains("constructionPlan"))return true;
  try{return !get(tile).has(QuantumBuilder.RECOVERY.get());}catch(RuntimeException ex){return false;}
 }
 public static ItemStack get(BlockEntity tile){return CACHE.computeIfAbsent(tile,t->{var raw=t.getPersistentData().get("constructionPlan");ItemStack stack;
  if(raw!=null)stack=ItemStack.CODEC.parse(t.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE),raw).getOrThrow();
  else {stack=new ItemStack(Items.PAPER);var s=BlueprintSettings.DEFAULT;if(t instanceof ReactorTile r)s=s.type(0,r.width(),r.depth(),r.height());else if(t instanceof TurbineTile turbine)s=turbine.layout();else if(t instanceof FuelProcessorTile f){int k=switch(f.machine()){case "fuel_enricher"->2;case "fuel_reprocessor"->3;default->4;};var l=f.layout();s=s.type(k,l.width(),l.depth(),l.height());}else if(net.neoforged.fml.ModList.get().isLoaded("ae2")&&t instanceof ArrayTile a)s=s.type(1,a.width(),a.depth(),a.height());else throw new IllegalArgumentException("Not a multiblock controller");stack.set(MultiblockPlanner.SETTINGS.get(),s);}
  stack.set(MultiblockPlanner.TARGET.get(),GlobalPos.of(t.getLevel().dimension(),t.getBlockPos()));return stack;});}
 public static void save(BlockEntity tile){var s=CACHE.get(tile);if(s!=null&&tile.getLevel()!=null){tile.getPersistentData().put("constructionPlan",ItemStack.CODEC.encodeStart(tile.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE),s).getOrThrow());tile.setChanged();}}
 public static void open(ServerPlayer player,BlockEntity tile){if(!player.mayBuild()||tile.isRemoved())return;try{get(tile);if(net.neoforged.fml.ModList.get().isLoaded("ae2")&&tile instanceof ArrayTile a)a.builder().migrateTo(get(tile));save(tile);player.openMenu(new SimpleMenuProvider((id,inv,p)->new BlueprintMenu(id,inv,tile.getBlockPos()),net.minecraft.network.chat.Component.translatable("gui.essenceindustry.planner.title")),buf->{buf.writeVarInt(-1);buf.writeBlockPos(tile.getBlockPos());});}catch(RuntimeException ex){player.sendOverlayMessage(net.minecraft.network.chat.Component.literal("Construction data could not be read; stored materials were preserved."));org.slf4j.LoggerFactory.getLogger(BlueprintStorage.class).error("Cannot open construction bank at {}",tile.getBlockPos(),ex);}}
 public static void drop(BlockEntity tile){if(tile.getLevel()==null||tile.getLevel().isClientSide())return;var raw=tile.getPersistentData().get("constructionPlan");if(raw==null&&!CACHE.containsKey(tile))return;var stack=get(tile);var entries=stack.getOrDefault(QuantumBuilder.BANK.get(),BuilderStock.EMPTY).entries();stack.set(QuantumBuilder.BANK.get(),BuilderStock.EMPTY);save(tile);for(var entry:entries){var p=tile.getBlockPos();Containers.dropItemStack(tile.getLevel(),p.getX()+.5,p.getY()+.5,p.getZ()+.5,entry.item().create());}}
 private BlueprintStorage(){}
}
