package com.neolena.essenceindustry.blueprint;

import java.util.*;
import appeng.api.AECapabilities;
import appeng.api.networking.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.*;
import net.neoforged.bus.api.IEventBus;

/** Optional AE2 endpoint shared by reactor and fuel-plant controllers. */
public final class BlueprintNode implements IInWorldGridNodeHost {
 private static final Map<BlockEntity,BlueprintNode> NODES=new IdentityHashMap<>();
 private final BlockEntity host;private IManagedGridNode managed;private boolean queued,created;
 private BlueprintNode(BlockEntity host){this.host=host;}
 public static void register(IEventBus bus){bus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e)->{
  e.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST,com.neolena.essenceindustry.nuclear.NuclearReactor.TILE.get(),(t,c)->endpoint(t));
  e.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST,com.neolena.essenceindustry.nuclear.NuclearFuels.TILE.get(),(t,c)->endpoint(t));
  e.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST,com.neolena.essenceindustry.nuclear.SteamTurbine.TILE.get(),(t,c)->endpoint(t));
 });}
 private static BlueprintNode endpoint(BlockEntity t){if(t.getLevel()==null||t.getLevel().isClientSide())return new BlueprintNode(t);return NODES.computeIfAbsent(t,BlueprintNode::new);}
 public static void start(BlockEntity host){if(!(host instanceof com.neolena.essenceindustry.nuclear.NuclearHost)||host.getLevel()==null||host.getLevel().isClientSide())return;endpoint(host).start();}
 private void start(){if(queued||created)return;
  managed=GridHelper.createManagedNode(host,new IGridNodeListener<BlockEntity>(){public void onSaveChanges(BlockEntity h,IGridNode n){save();}}).setTagName("constructionNode").setInWorldNode(true).setVisualRepresentation(host.getBlockState().getBlock()).setIdlePowerUsage(.25).setFlags(GridFlags.REQUIRE_CHANNEL);
  managed.deserialize(TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,host.getLevel().registryAccess(),host.getPersistentData()));queued=true;var scheduled=managed;
  GridHelper.onFirstTick(host,h->{if(managed!=scheduled)return;queued=false;if(!h.isRemoved()&&h.getLevel()!=null&&h.getLevel().getBlockEntity(h.getBlockPos())==h){created=true;managed.create(h.getLevel(),h.getBlockPos());h.getLevel().invalidateCapabilities(h.getBlockPos());}});
 }
 private void save(){if(managed==null||!created||host.getLevel()==null)return;var out=TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,host.getLevel().registryAccess());managed.serialize(out);out.buildResult().getCompound("constructionNode").ifPresent(tag->host.getPersistentData().put("constructionNode",tag));host.setChanged();}
 public static void stop(BlockEntity host){var n=NODES.remove(host);if(n!=null){n.save();if(n.managed!=null)n.managed.destroy();n.managed=null;n.created=n.queued=false;}}
 public IGridNode getGridNode(Direction side){return managed==null?null:managed.getNode();}
 public appeng.api.util.AECableType getCableConnectionType(Direction side){return appeng.api.util.AECableType.COVERED;}
}
