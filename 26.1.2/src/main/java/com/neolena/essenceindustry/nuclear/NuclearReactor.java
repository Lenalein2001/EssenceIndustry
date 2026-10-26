package com.neolena.essenceindustry.nuclear;
import com.neolena.essenceindustry.EssenceIndustry;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.capabilities.*;
public final class NuclearReactor {
 private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(EssenceIndustry.MOD_ID);
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(EssenceIndustry.MOD_ID);
 private static final DeferredRegister<BlockEntityType<?>> TILES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,EssenceIndustry.MOD_ID);
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,EssenceIndustry.MOD_ID);
 public static final List<String> KINDS=List.of("casing","pressure_panel","frame","glass","controller","fuel_input","waste_output","coolant_in","coolant_out","energy_out","moderator","reflector","fuel_assembly","actuator","cooling_channel","centrifuge","treatment_vessel","fabrication_bed","fabrication_head","roof_window");
 public static final Set<String> PORTS=Set.of("fuel_input","waste_output","coolant_in","coolant_out","energy_out");
 private static final Map<String,DeferredBlock<NuclearPart>> PARTS=new LinkedHashMap<>();
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ReactorTile>> TILE;
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<NuclearPortTile>> PORT_TILE;
 public static final DeferredHolder<MenuType<?>,MenuType<ReactorMenu>> MENU=MENUS.register("reactor",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(ReactorMenu::new));
 public static final DeferredHolder<MenuType<?>,MenuType<NuclearPortMenu>> PORT_MENU=MENUS.register("reactor_port",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(NuclearPortMenu::new));
 static {for(String k:KINDS){var b=BLOCKS.registerBlock("reactor_"+k,p->switch(k){case "controller"->new NuclearPart.Controller(k,p);case "frame"->new NuclearPart.Frame(k,p);case "casing","pressure_panel"->new NuclearPart.Shell(k,p);case "glass"->new NuclearPart.Glass(k,p);case "roof_window"->new NuclearPart.Roof(k,p);case "fuel_assembly"->new NuclearPart.Assembly(k,p);case "centrifuge","treatment_vessel"->new NuclearPart.Vessel(k,p);case "fabrication_bed"->new NuclearPart.Conveyor(k,p);case "cooling_channel"->new NuclearPart.Channel(k,p);default->PORTS.contains(k)?new NuclearPart.Port(k,p):new NuclearPart(k,p);});PARTS.put(k,b);ITEMS.registerSimpleBlockItem(b);}TILE=TILES.register("reactor",()->new BlockEntityType<>(ReactorTile::new,block("controller")));PORT_TILE=TILES.register("reactor_port",()->new BlockEntityType<>(NuclearPortTile::new,PORTS.stream().map(NuclearReactor::block).toArray(Block[]::new)));}
 public static NuclearPart block(String kind){return PARTS.get(switch(kind){case "pressure_panel","frame"->"casing";case "roof_window"->"glass";default->kind;}).get();}
 public static String kind(BlockState s){return s.getBlock() instanceof NuclearPart p?p.kind:"";}
 public static String baseKind(String kind){return switch(kind){case "waste_output"->"fuel_input";case "coolant_out"->"coolant_in";default->kind;};}
 public static String role(BlockState s){String k=kind(s);if(s.hasProperty(NuclearPart.OUTPUT)&&s.getValue(NuclearPart.OUTPUT)){if(k.equals("fuel_input"))return "waste_output";if(k.equals("coolant_in"))return "coolant_out";}return k;}
 public static BlockState portState(String role){var s=block(baseKind(role)).defaultBlockState();return s.hasProperty(NuclearPart.OUTPUT)?s.setValue(NuclearPart.OUTPUT,role.equals("waste_output")||role.equals("coolant_out")):s;}
 public static void register(IEventBus bus){StructureValidationQueue.register();SteamTurbine.register(bus);NuclearSteam.register(bus);ReactorConfig.register(bus);NuclearVisualUpdates.register();BLOCKS.register(bus);ITEMS.register(bus);TILES.register(bus);MENUS.register(bus);bus.addListener((RegisterCapabilitiesEvent e)->{e.registerBlockEntity(Capabilities.Item.BLOCK,PORT_TILE.get(),(t,s)->t.side(s)&&Set.of("fuel_input","waste_output").contains(kind(t.getBlockState()))?t.items:null);e.registerBlockEntity(Capabilities.Energy.BLOCK,PORT_TILE.get(),(t,s)->t.side(s)&&kind(t.getBlockState()).equals("energy_out")?t.energy:null);e.registerBlockEntity(Capabilities.Fluid.BLOCK,PORT_TILE.get(),(t,s)->t.side(s)&&Set.of("coolant_in","coolant_out").contains(kind(t.getBlockState()))?t.fluids:null);});bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS)PARTS.entrySet().stream().filter(entry->!Set.of("waste_output","coolant_out","pressure_panel","frame","roof_window").contains(entry.getKey())).forEach(entry->e.accept(entry.getValue().get()));});net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent e)->{if(e.getLevel() instanceof net.minecraft.world.level.Level l&&!l.isClientSide())NuclearOwners.changed(l,e.getPos());});if(net.neoforged.fml.loading.FMLEnvironment.getDist().isClient())ReactorClient.register(bus);}
 private NuclearReactor(){}
}
