package com.neolena.essenceindustry.compat.mysticalagriculture;

import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.MysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropModels;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalagriculture.api.lib.PluginConfig;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@MysticalAgriculturePlugin
public final class EssenceIndustryPlugin implements IMysticalAgriculturePlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(EssenceIndustryPlugin.class);
    private Set<String> configuredCropNames = Set.of();
    private static final CropTier TIER_SIX = new CropTier(EssenceIndustry.id("6"), 6, 0x5E2A8A, ChatFormatting.DARK_PURPLE);

    private static final Crop PROSPERITY = resourceCrop("prosperity", CropTier.TWO, new CropModels(CropModels.FLOWER_ROCK_BLANK, CropModels.ESSENCE_ROCK_BLANK), 0xDDB646, "mysticalagriculture:prosperity_shard");
    private static final Crop HONEY_TREAT = resourceCrop("honey_treat", CropTier.THREE, new CropModels(CropModels.FLOWER_DUST_BLANK, CropModels.ESSENCE_DUST_BLANK), 0xE5A525, "productivebees:honey_treat")
            .setEnabled(modLoaded("productivebees"));
    private static final Crop GHOSTLY_COMB = resourceCrop("ghostly_comb", CropTier.FOUR, new CropModels(CropModels.FLOWER_ROCK_BLANK, CropModels.ESSENCE_ROCK_BLANK), 0xA3EAF5, "productivebees:comb_ghostly")
            .setEnabled(modLoaded("productivebees"));
    private static final Crop MILKY_COMB = resourceCrop("milky_comb", CropTier.FOUR, new CropModels(CropModels.FLOWER_ROCK_BLANK, CropModels.ESSENCE_ROCK_BLANK), 0xEFDDBF, "productivebees:comb_milky")
            .setEnabled(modLoaded("productivebees"));
    private static final Crop POWDERY_COMB = resourceCrop("powdery_comb", CropTier.FOUR, new CropModels(CropModels.FLOWER_DUST_BLANK, CropModels.ESSENCE_DUST_BLANK), 0xD6D6D6, "productivebees:comb_powdery")
            .setEnabled(modLoaded("productivebees"));
    private static final Crop AMBER = resourceCrop("amber", CropTier.FOUR, new CropModels(CropModels.FLOWER_ROCK_BLANK, CropModels.ESSENCE_GEM_BLANK), 0xE9802E, "productivebees:amber")
            .setEnabled(modLoaded("productivebees"));
    private static final Crop DRACONIC_CHUNK = resourceCrop("draconic_chunk", CropTier.FIVE, new CropModels(CropModels.FLOWER_ROCK_BLANK, CropModels.ESSENCE_GEM_BLANK), 0x8A1436, "productivebees:draconic_chunk")
            .setEnabled(modLoaded("productivebees"));
    private static final Crop ANGLESITE = resourceCrop("anglesite", CropTier.FOUR, new CropModels(CropModels.FLOWER_ROCK_BLANK, CropModels.ESSENCE_GEM_BLANK), 0xD8AA28, "bigreactors:anglesite_crystal")
            .setEnabled(modLoaded("bigreactors"));
    private static final Crop BENITOITE = resourceCrop("benitoite", CropTier.FOUR, new CropModels(CropModels.FLOWER_ROCK_BLANK, CropModels.ESSENCE_GEM_BLANK), 0x55B7D4, "bigreactors:benitoite_crystal")
            .setEnabled(modLoaded("bigreactors"));
    private static final Crop MAGENTITE = resourceCrop("magentite", CropTier.FIVE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xD936D9, "bigreactors:magentite_ingot")
            .setEnabled(modLoaded("bigreactors"));
    private static final Crop RIDICULITE = resourceCrop("ridiculite", TIER_SIX, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xB390B8, "bigreactors:ridiculite_ingot")
            .setEnabled(modLoaded("bigreactors"));
    private static final Crop INANITE = resourceCrop("inanite", TIER_SIX, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xD51C52, "bigreactors:inanite_ingot")
            .setEnabled(modLoaded("bigreactors"));
    private static final Crop INSANITE = resourceCrop("insanite", TIER_SIX, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x7435A7, "bigreactors:insanite_ingot")
            .setEnabled(modLoaded("bigreactors"));
    private static final Crop LUDICRITE = resourceCrop("ludicrite", CropTier.FIVE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x0FD7C2, "bigreactors:ludicrite_ingot")
            .setEnabled(modLoaded("bigreactors"));
    private static final Crop BLUTONIUM = resourceCrop("blutonium", CropTier.FIVE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x1B4BA8, "bigreactors:blutonium_ingot")
            .setEnabled(modLoaded("bigreactors"));

    private static final Crop ALLTHEMODIUM = resourceCrop("allthemodium", TIER_SIX, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xF0BA2D, "allthemodium:allthemodium_ingot")
            .setEnabled(modLoaded("allthemodium"));
    private static final Crop VIBRANIUM = resourceCrop("vibranium", TIER_SIX, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x2CC0F7, "allthemodium:vibranium_ingot")
            .setEnabled(modLoaded("allthemodium"));
    private static final Crop UNOBTAINIUM = resourceCrop("unobtainium", TIER_SIX, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x8A23D9, "allthemodium:unobtainium_ingot")
            .setEnabled(modLoaded("allthemodium"));

    private static final Crop CHAOS_FRAGMENT = resourceCrop("chaos_fragment", TIER_SIX, new CropModels(CropModels.FLOWER_ROCK_BLANK, CropModels.ESSENCE_GEM_BLANK), 0x6D1645, "draconicevolution:small_chaos_frag")
            .setEnabled(modLoaded("draconicevolution"));
    private static final Crop DRAGON_HEART = resourceCrop("dragon_heart", TIER_SIX, new CropModels(CropModels.FLOWER_ROCK_BLANK, CropModels.ESSENCE_GEM_BLANK), 0xB81E2A, "draconicevolution:dragon_heart")
            .setEnabled(modLoaded("draconicevolution"));

    private static final Crop PLASTIC = resourceCrop("plastic", CropTier.THREE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xD7E0E3, "oritech:plastic_sheet")
                .setEnabled(modLoaded("oritech"));
    private static final Crop FLUXITE = resourceCrop("fluxite", CropTier.FIVE, new CropModels(CropModels.FLOWER_ROCK_BLANK, CropModels.ESSENCE_GEM_BLANK), 0x54D9D2, "oritech:fluxite")
                .setEnabled(modLoaded("oritech"));
    private static final Crop OIL = resourceCrop("oil", CropTier.FOUR, new CropModels(CropModels.FLOWER_DUST_BLANK, CropModels.ESSENCE_DUST_BLANK), 0x25221F, "oritech:polymer_resin")
                .setEnabled(modLoaded("oritech"));

    private static final Crop FERRICORE = resourceCrop("ferricore", CropTier.THREE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x69767A, "justdirethings:raw_ferricore")
                .setEnabled(modLoaded("justdirethings"));
    private static final Crop BLAZEGOLD = resourceCrop("blazegold", CropTier.FOUR, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xE88220, "justdirethings:raw_blazegold")
                .setEnabled(modLoaded("justdirethings"));
    private static final Crop CELESTIGEM = resourceCrop("celestigem", CropTier.FIVE, new CropModels(CropModels.FLOWER_ROCK_BLANK, CropModels.ESSENCE_GEM_BLANK), 0x69DDE1, "justdirethings:celestigem")
                .setEnabled(modLoaded("justdirethings"));
    private static final Crop ECLIPSE_ALLOY = resourceCrop("eclipse_alloy", TIER_SIX, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x49345F, "justdirethings:raw_eclipsealloy")
                .setEnabled(modLoaded("justdirethings"));

    private static final Crop EDELWOOD = resourceCrop("edelwood", CropTier.FOUR, new CropModels(CropModels.FLOWER_ROCK_BLANK, CropModels.ESSENCE_ROCK_BLANK), 0x72543D, "forbidden_arcanus:edelwood_log")
                .setEnabled(modLoaded("forbidden_arcanus"));

    private static final Crop ANTIMONY = resourceCrop("antimony", CropTier.THREE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xB8BBC8, "ftbmaterials:antimony_ingot")
            .setEnabled(modLoaded("ftbmaterials"));
    private static final Crop TITANIUM = resourceCrop("titanium", CropTier.FOUR, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xBBC9DA, "ftbmaterials:titanium_ingot")
            .setEnabled(modLoaded("ftbmaterials"));
    private static final Crop TUNGSTEN = resourceCrop("tungsten", CropTier.FIVE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x505766, "ftbmaterials:tungsten_ingot")
            .setEnabled(modLoaded("ftbmaterials"));
    private static final Crop BAUXITE = resourceCrop("bauxite", CropTier.THREE, new CropModels(CropModels.FLOWER_DUST_BLANK, CropModels.ESSENCE_DUST_BLANK), 0xB96846, "ftbmaterials:bauxite_dust")
            .setEnabled(modLoaded("ftbmaterials"));
    private static final Crop POLYMER_RESIN = resourceCrop("polymer_resin", CropTier.THREE, new CropModels(CropModels.FLOWER_DUST_BLANK, CropModels.ESSENCE_DUST_BLANK), 0xD9B46A, "oritech:polymer_resin")
            .setEnabled(modLoaded("oritech"));
    private static final Crop CARBON_FIBRE = resourceCrop("carbon_fibre", CropTier.THREE, new CropModels(CropModels.FLOWER_DUST_BLANK, CropModels.ESSENCE_DUST_BLANK), 0x515662, "oritech:carbon_fibre_strands")
            .setEnabled(modLoaded("oritech"));
    private static final Crop STICKY_RESIN = resourceCrop("sticky_resin", CropTier.TWO, new CropModels(CropModels.FLOWER_DUST_BLANK, CropModels.ESSENCE_DUST_BLANK), 0xD79B3E, "ftbic:sticky_resin")
            .setEnabled(modLoaded("ftbic"));

    private static final Crop STAINLESS_STEEL = resourceCrop("stainless_steel", CropTier.FOUR, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xC6CDD4, "ftbmaterials:stainless_steel_ingot").setEnabled(modLoaded("ftbmaterials"));
    private static final Crop PLUTONIUM = resourceCrop("plutonium", CropTier.FIVE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x62AC58, "ftbmaterials:plutonium_ingot").setEnabled(modLoaded("ftbmaterials"));
    private static final Crop ADAMANT = resourceCrop("adamant", CropTier.FIVE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xCD5BA8, "oritech:adamant_ingot").setEnabled(modLoaded("oritech"));
    private static final Crop DURATIUM = resourceCrop("duratium", CropTier.FIVE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xADADC9, "oritech:duratium_ingot").setEnabled(modLoaded("oritech"));
    private static final Crop BIOSTEEL = resourceCrop("biosteel", CropTier.FOUR, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x63AD91, "oritech:biosteel_ingot").setEnabled(modLoaded("oritech"));
    private static final Crop ENERGITE = resourceCrop("energite", CropTier.FIVE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xDE6946, "oritech:energite_ingot").setEnabled(modLoaded("oritech"));
    private static final Crop BORON = resourceCrop("boron", CropTier.THREE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x719598, "colossal_reactors:boron_ingot").setEnabled(modLoaded("colossal_reactors"));
    private static final Crop DIMENSIONAL_SHARD = resourceCrop("dimensional_shard", CropTier.FOUR, new CropModels(CropModels.FLOWER_ROCK_BLANK, CropModels.ESSENCE_GEM_BLANK), 0x60BCD6, "ftbmaterials:dimensional_shard_gem").setEnabled(modLoaded("ftbmaterials"));

    private static final Crop DEPLETED_URANIUM = resourceCrop("depleted_uranium", CropTier.FOUR, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x78816B, "essenceindustry:depleted_uranium");
    private static final Crop THORIUM_FUEL = resourceCrop("thorium_fuel", CropTier.FOUR, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x70A9A2, "essenceindustry:thorium_fuel");
    private static final Crop ENRICHED_URANIUM = resourceCrop("enriched_uranium", CropTier.FIVE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xB2C667, "essenceindustry:enriched_uranium_fuel");

    private static final Crop ADAMANTITE = resourceCrop("adamantite", CropTier.FOUR, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xD45B62, "ftbarmory:adamantite_ingot").setEnabled(modLoaded("ftbarmory"));
    private static final Crop AETERNIUM = resourceCrop("aeternium", CropTier.FIVE, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0x5DD9B5, "ftbarmory:aeternium_ingot").setEnabled(modLoaded("ftbarmory"));
    private static final Crop AURICHALCUM = resourceCrop("aurichalcum", TIER_SIX, new CropModels(CropModels.FLOWER_INGOT_BLANK, CropModels.ESSENCE_INGOT_BLANK), 0xE6B65A, "ftbarmory:aurichalcum_ingot").setEnabled(modLoaded("ftbarmory"));

    @Override
    public void configure(PluginConfig config) {
        config.setModId(EssenceIndustry.MOD_ID);
    }

    @Override
    public void onRegisterCrops(ICropRegistry registry) {
        this.configuredCropNames = findConfiguredCropNames();
        registry.registerTier(TIER_SIX);

        registerIfAvailable(registry, PROSPERITY);
        registerIfAvailable(registry, HONEY_TREAT);
        registerIfAvailable(registry, GHOSTLY_COMB);
        registerIfAvailable(registry, MILKY_COMB);
        registerIfAvailable(registry, POWDERY_COMB);
        registerIfAvailable(registry, AMBER);
        registerIfAvailable(registry, DRACONIC_CHUNK);
        registerIfAvailable(registry, ANGLESITE);
        registerIfAvailable(registry, BENITOITE);
        registerIfAvailable(registry, MAGENTITE);
        registerIfAvailable(registry, RIDICULITE);
        registerIfAvailable(registry, INANITE);
        registerIfAvailable(registry, INSANITE);
        registerIfAvailable(registry, LUDICRITE);
        registerIfAvailable(registry, BLUTONIUM);
        registerIfAvailable(registry, ALLTHEMODIUM);
        registerIfAvailable(registry, VIBRANIUM);
        registerIfAvailable(registry, UNOBTAINIUM);
        registerIfAvailable(registry, CHAOS_FRAGMENT);
        registerIfAvailable(registry, DRAGON_HEART);
        registerIfAvailable(registry, PLASTIC);
        registerIfAvailable(registry, FLUXITE);
        registerIfAvailable(registry, OIL);
        registerIfAvailable(registry, FERRICORE);
        registerIfAvailable(registry, BLAZEGOLD);
        registerIfAvailable(registry, CELESTIGEM);
        registerIfAvailable(registry, ECLIPSE_ALLOY);
        registerIfAvailable(registry, EDELWOOD);
        registerIfAvailable(registry, ANTIMONY);
        registerIfAvailable(registry, TITANIUM);
        registerIfAvailable(registry, TUNGSTEN);
        registerIfAvailable(registry, BAUXITE);
        registerIfAvailable(registry, POLYMER_RESIN);
        registerIfAvailable(registry, CARBON_FIBRE);
        registerIfAvailable(registry, STICKY_RESIN);
        registerIfAvailable(registry, STAINLESS_STEEL);
        registerIfAvailable(registry, PLUTONIUM);
        registerIfAvailable(registry, ADAMANT);
        registerIfAvailable(registry, DURATIUM);
        registerIfAvailable(registry, BIOSTEEL);
        registerIfAvailable(registry, ADAMANTITE);
        registerIfAvailable(registry, AETERNIUM);
        registerIfAvailable(registry, AURICHALCUM);
        registerIfAvailable(registry, ENERGITE);
        registerIfAvailable(registry, BORON);
        registerIfAvailable(registry, DIMENSIONAL_SHARD);
        registerIfAvailable(registry, DEPLETED_URANIUM);
        registerIfAvailable(registry, THORIUM_FUEL);
        registerIfAvailable(registry, ENRICHED_URANIUM);
    }

    @Override
    public void onPostRegisterCrops(ICropRegistry registry) {
        TIER_SIX
                .setFarmlandBlock(() -> findBlock("mysticalagradditions:insanium_farmland", "mysticalagriculture:supremium_farmland", Blocks.FARMLAND))
                .setEssenceItem(() -> findItem("mysticalagradditions:insanium_essence", "mysticalagriculture:supremium_essence", Items.AMETHYST_SHARD));
    }

    private static Crop resourceCrop(String name, CropTier tier, CropModels models, int color, String materialId) {
        Crop crop = new Crop(
                EssenceIndustry.id(name),
                tier,
                CropType.RESOURCE,
                models,
                LazyIngredient.item(materialId)
        );
        return crop
            .setCropBlock(memoizedCropBlock(crop), true)
            .setColor(color);
    }

    private static Supplier<CropBlock> memoizedCropBlock(Crop crop) {
        return new Supplier<>() {
            private CropBlock block;

            @Override
            public synchronized CropBlock get() {
                if (this.block == null) {
                    this.block = new LightIndependentMysticalCropBlock(crop);
                }

                return this.block;
            }
        };
    }

    private void registerIfAvailable(ICropRegistry registry, Crop crop) {
        // MA registers seeds, essence and blocks even for disabled crops.
        // Skip the crop entirely when its optional provider mod is absent.
        if (crop.isEnabled()) {
            // All crops share MA item/block names, regardless of their plugin namespace.
            // Check configuration as well as the registry so plugin order cannot override pack crops.
            if (this.configuredCropNames.contains(crop.getName()) || registry.getCropByName(crop.getName()) != null) {
                LOGGER.info("Keeping existing crop {}, skipping addon registration", crop.getName());
                return;
            }
            registry.register(crop);
        }
    }

    private static Set<String> findConfiguredCropNames() {
        if (!modLoaded("mysticalcustomization")) {
            return Set.of();
        }
        var directory = FMLPaths.CONFIGDIR.get().resolve("mysticalcustomization/crops");
        if (!Files.isDirectory(directory)) {
            return Set.of();
        }
        try (var files = Files.walk(directory)) {
            return files.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".json"))
                    .map(name -> name.substring(0, name.length() - 5))
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot inspect Mystical Customization crop definitions", exception);
        }
    }

    private static boolean modLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    private static Item findItem(String primary, String secondary, Item fallback) {
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(primary));
        if (item != null && item != Items.AIR) {
            return item;
        }

        item = BuiltInRegistries.ITEM.getValue(Identifier.parse(secondary));
        if (item != null && item != Items.AIR) {
            return item;
        }

        return fallback;
    }

    private static Block findBlock(String primary, String secondary, Block fallback) {
        Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(primary));
        if (block != null && block != Blocks.AIR) {
            return block;
        }

        block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(secondary));
        if (block != null && block != Blocks.AIR) {
            return block;
        }

        return fallback;
    }
}


