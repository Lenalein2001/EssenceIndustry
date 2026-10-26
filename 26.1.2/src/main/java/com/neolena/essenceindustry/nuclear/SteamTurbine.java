package com.neolena.essenceindustry.nuclear;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
/** Shared reactor shell and configurable ports, with a single ticking rotor controller. */
public final class SteamTurbine {
 static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks("essenceindustry");
 static final DeferredRegister.Items ITEMS=DeferredRegister.createItems("essenceindustry");
 static final DeferredRegister<BlockEntityType<?>> TILES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,"essenceindustry");
 static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,"essenceindustry");
 public static final Map<String,DeferredBlock<Part>> PARTS=new LinkedHashMap<>();
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<TurbineTile>> TILE;
 public static final DeferredHolder<MenuType<?>,MenuType<TurbineMenu>> MENU=MENUS.register("turbine",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(TurbineMenu::new));
 static{for(String k:List.of("controller","shaft","blade","bearing","condenser","foot","light","coil_copper","coil_uranium","coil_enriched","coil_mox","coil_plutonium","coil_advanced")){var b=BLOCKS.registerBlock("turbine_"+k,p->k.equals("controller")?new Controller(k,p):new Part(k,p));PARTS.put(k,b);ITEMS.registerSimpleBlockItem(b);}TILE=TILES.register("turbine",()->new BlockEntityType<>(TurbineTile::new,block("controller")));}
 public static Part block(String k){return PARTS.get(k).get();}
 public static String kind(BlockState s){return s.getBlock() instanceof Part p?p.part:"";}
 public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);TILES.register(bus);MENUS.register(bus);bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS)PARTS.values().forEach(b->e.accept(b.get()));});if(net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())TurbineClient.register(bus);}
 public static class Part extends NuclearPart {
  public static final BooleanProperty VERTICAL=BooleanProperty.create("vertical");
  final String part;public Part(String k,Properties p){super("turbine_"+k,k.equals("light")?p.lightLevel(s->s.getValue(POWERED)?12:0):p);part=k;registerDefaultState(defaultBlockState().setValue(VERTICAL,false));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(VERTICAL);}
  @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return super.getStateForPlacement(c).setValue(VERTICAL,c.getNearestLookingDirection().getAxis()==Direction.Axis.Y);}
 }
 public static final class Controller extends Part implements EntityBlock{
  Controller(String k,Properties p){super(k,p);}
  public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new TurbineTile(p,s);}
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level l,BlockState s,BlockEntityType<T> t){return !l.isClientSide()&&t==TILE.get()?(w,p,state,tile)->((TurbineTile)tile).tick():null;}
  @Override public boolean onDestroyedByPlayer(BlockState s,net.minecraft.world.level.Level l,BlockPos p,Player player,ItemStack tool,boolean harvest,net.minecraft.world.level.material.FluidState fluid){if(l.getBlockEntity(p) instanceof TurbineTile t&&!t.safeToMove())return false;return super.onDestroyedByPlayer(s,l,p,player,tool,harvest,fluid);}
  @Override public BlockState playerWillDestroy(net.minecraft.world.level.Level l,BlockPos p,BlockState s,Player player){if(!l.isClientSide()&&player.isCreative()&&l instanceof net.minecraft.server.level.ServerLevel server&&l.getBlockEntity(p) instanceof TurbineTile t&&t.hasContents()&&t.safeToMove())for(var drop:Block.getDrops(s,server,p,t,player,player.getMainHandItem()))Block.popResource(l,p,drop);return super.playerWillDestroy(l,p,s,player);}
  protected InteractionResult useWithoutItem(BlockState s,net.minecraft.world.level.Level l,BlockPos p,Player who,BlockHitResult hit){if(l.getBlockEntity(p) instanceof TurbineTile t){if(!l.isClientSide())t.open(who);return InteractionResult.SUCCESS;}return InteractionResult.PASS;}
 }
 private SteamTurbine(){}
}
