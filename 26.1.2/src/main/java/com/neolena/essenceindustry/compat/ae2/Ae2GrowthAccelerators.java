package com.neolena.essenceindustry.compat.ae2;

import appeng.api.AECapabilities;
import appeng.block.AEBaseEntityBlock;
import appeng.api.ids.AECreativeTabIds;
import com.neolena.essenceindustry.EssenceIndustry;
import com.neolena.essenceindustry.compat.ae2.client.Ae2ClientRegistration;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.concurrent.atomic.AtomicReference;

public final class Ae2GrowthAccelerators {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EssenceIndustry.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EssenceIndustry.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(
        Registries.BLOCK_ENTITY_TYPE,
        EssenceIndustry.MOD_ID
    );
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(
        Registries.MENU,
        EssenceIndustry.MOD_ID
    );

    public static final DeferredHolder<Block, TieredGrowthAcceleratorBlock> TIER_2 = registerBlock(
        "growth_accelerator_tier_2",
        2
    );
    public static final DeferredHolder<Block, TieredGrowthAcceleratorBlock> TIER_3 = registerBlock(
        "growth_accelerator_tier_3",
        4
    );
    public static final DeferredHolder<Block, TieredGrowthAcceleratorBlock> TIER_4 = registerBlock(
        "growth_accelerator_tier_4",
        8
    );
    public static final DeferredHolder<Block, TieredGrowthAcceleratorBlock> TIER_5 = registerBlock(
        "growth_accelerator_tier_5",
        16
    );
    public static final DeferredHolder<Block, BlockAcceleratorBlock> BLOCK_ACCELERATOR = BLOCKS.registerBlock(
        "block_accelerator",
        BlockAcceleratorBlock::new
    );

    public static final DeferredHolder<Item, BlockItem> TIER_2_ITEM = registerBlockItem(
        "growth_accelerator_tier_2",
        TIER_2
    );
    public static final DeferredHolder<Item, BlockItem> TIER_3_ITEM = registerBlockItem(
        "growth_accelerator_tier_3",
        TIER_3
    );
    public static final DeferredHolder<Item, BlockItem> TIER_4_ITEM = registerBlockItem(
        "growth_accelerator_tier_4",
        TIER_4
    );
    public static final DeferredHolder<Item, BlockItem> TIER_5_ITEM = registerBlockItem(
        "growth_accelerator_tier_5",
        TIER_5
    );
    public static final DeferredHolder<Item, BlockItem> BLOCK_ACCELERATOR_ITEM = ITEMS.registerItem(
        "block_accelerator",
        properties -> new BlockItem(BLOCK_ACCELERATOR.get(), properties.useBlockDescriptionPrefix())
    );
    public static final DeferredHolder<Item, appeng.items.parts.PartItem<BlockAcceleratorPart>> BLOCK_ACCELERATOR_PART = ITEMS.registerItem(
        "block_accelerator_part", properties -> new appeng.items.parts.PartItem<>(properties, BlockAcceleratorPart.class, BlockAcceleratorPart::new));
    public static final DeferredHolder<MenuType<?>, MenuType<BlockAcceleratorMenu>> BLOCK_ACCELERATOR_MENU =
        MENU_TYPES.register(
            "block_accelerator",
            () -> IMenuTypeExtension.create(BlockAcceleratorMenu::new)
        );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TieredGrowthAcceleratorBlockEntity>>
        BLOCK_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("tiered_growth_accelerator", () -> {
            AtomicReference<BlockEntityType<TieredGrowthAcceleratorBlockEntity>> typeReference =
                new AtomicReference<>();
            BlockEntityType<TieredGrowthAcceleratorBlockEntity> type = new BlockEntityType<>(
                (pos, state) -> new TieredGrowthAcceleratorBlockEntity(typeReference.get(), pos, state),
                TIER_2.get(),
                TIER_3.get(),
                TIER_4.get(),
                TIER_5.get()
            );
            typeReference.setPlain(type);

            bindBlockEntity(TIER_2.get(), type);
            bindBlockEntity(TIER_3.get(), type);
            bindBlockEntity(TIER_4.get(), type);
            bindBlockEntity(TIER_5.get(), type);
            return type;
        });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockAcceleratorBlockEntity>>
        BLOCK_ACCELERATOR_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("block_accelerator", () -> {
            AtomicReference<BlockEntityType<BlockAcceleratorBlockEntity>> typeReference = new AtomicReference<>();
            BlockEntityType<BlockAcceleratorBlockEntity> type = new BlockEntityType<>(
                (pos, state) -> new BlockAcceleratorBlockEntity(typeReference.get(), pos, state),
                BLOCK_ACCELERATOR.get()
            );
            typeReference.setPlain(type);
            bindBlockAcceleratorEntity(BLOCK_ACCELERATOR.get(), type);
            return type;
        });

    private Ae2GrowthAccelerators() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        MENU_TYPES.register(modBus);
        modBus.addListener(Ae2GrowthAccelerators::addCreativeTabItems);
        modBus.addListener(Ae2GrowthAccelerators::registerCapabilities);
        if (FMLEnvironment.getDist().isClient()) {
            Ae2ClientRegistration.register(modBus);
        }
    }

    private static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == AECreativeTabIds.MAIN) {
            event.accept(TIER_2_ITEM.get());
            event.accept(TIER_3_ITEM.get());
            event.accept(TIER_4_ITEM.get());
            event.accept(TIER_5_ITEM.get());
            event.accept(BLOCK_ACCELERATOR_ITEM.get());
            event.accept(BLOCK_ACCELERATOR_PART.get());
        }
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            AECapabilities.IN_WORLD_GRID_NODE_HOST,
            BLOCK_ENTITY_TYPE.get(),
            (blockEntity, context) -> blockEntity
        );
        event.registerBlockEntity(
            AECapabilities.CRANKABLE,
            BLOCK_ENTITY_TYPE.get(),
            TieredGrowthAcceleratorBlockEntity::getCrankable
        );
        event.registerBlockEntity(
            AECapabilities.IN_WORLD_GRID_NODE_HOST,
            BLOCK_ACCELERATOR_ENTITY_TYPE.get(),
            (blockEntity, context) -> blockEntity
        );
    }

    private static DeferredHolder<Block, TieredGrowthAcceleratorBlock> registerBlock(String name, int multiplier) {
        return BLOCKS.registerBlock(name, properties -> new TieredGrowthAcceleratorBlock(properties, multiplier));
    }

    private static DeferredHolder<Item, BlockItem> registerBlockItem(
        String name,
        DeferredHolder<Block, TieredGrowthAcceleratorBlock> block
    ) {
        return ITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void bindBlockEntity(
        TieredGrowthAcceleratorBlock block,
        BlockEntityType<TieredGrowthAcceleratorBlockEntity> type
    ) {
        ((AEBaseEntityBlock) block).setBlockEntity(
            TieredGrowthAcceleratorBlockEntity.class,
            type,
            null,
            null
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void bindBlockAcceleratorEntity(
        BlockAcceleratorBlock block,
        BlockEntityType<BlockAcceleratorBlockEntity> type
    ) {
        ((AEBaseEntityBlock) block).setBlockEntity(
            BlockAcceleratorBlockEntity.class,
            type,
            null,
            null
        );
    }
}
