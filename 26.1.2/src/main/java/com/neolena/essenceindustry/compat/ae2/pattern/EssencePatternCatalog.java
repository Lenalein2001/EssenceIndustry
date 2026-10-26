package com.neolena.essenceindustry.compat.ae2.pattern;

import appeng.api.crafting.*;
import appeng.api.stacks.*;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.blakebr0.cucumber.crafting.recipe.ShapedTagRecipe;
import com.blakebr0.cucumber.crafting.recipe.ShapelessTagRecipe;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;

/** Shared immutable recipe generation. Discovery never runs in a crafting planner thread. */
public final class EssencePatternCatalog {
    public record Entry(String id, IPatternDetails pattern, Map<AEKey, Long> inputs, AEItemKey output, long amount) {}
    public record Snapshot(Object recipes, List<Entry> entries, Map<AEItemKey, Entry> byDefinition) {}
    private static final Map<MinecraftServer, Snapshot> CACHE = new WeakHashMap<>();
    public static synchronized void clear() { CACHE.clear(); }
    public static synchronized Snapshot get(MinecraftServer server) {
        var recipes = server.getRecipeManager().recipeMap();
        var cached = CACHE.get(server);
        if (cached != null && cached.recipes() == recipes) return cached;
        if (!server.isSameThread()) throw new IllegalStateException("Recipe discovery requires the server thread");
        long started = System.nanoTime();
        var essences = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());
        for (var crop : CropRegistry.getInstance().getCrops()) if (crop.isEnabled() && crop.getEssenceItem() != null && crop.getEssenceItem() != Items.AIR) essences.add(crop.getEssenceItem());
        var entries = new ArrayList<Entry>();
        var definitions = new LinkedHashMap<AEItemKey, Entry>();
        // Keep existing vanilla-backed IDs when a newly supported tag recipe encodes
        // the same conversion, so saved disabled choices do not silently change IDs.
        var order = Comparator.<RecipeHolder<?>>comparingInt(r -> r.value().getClass() == ShapedTagRecipe.class || r.value().getClass() == ShapelessTagRecipe.class ? 1 : 0)
                .thenComparing(r -> r.id().identifier().toString());
        for (var holder : server.getRecipeManager().getRecipes().stream().sorted(order).toList()) {
            if (holder.id().identifier().toString().length() > 256) continue;
            if (entries.size() >= 4096) { org.slf4j.LoggerFactory.getLogger(EssencePatternCatalog.class).warn("Essence pattern safety limit reached (4096 unique conversions)"); break; }
            var recipe = holder.value();
            // Exact classes exclude dynamic/custom subclasses even when they impersonate a vanilla serializer.
            if (recipe.getClass() != ShapedRecipe.class && recipe.getClass() != ShapelessRecipe.class
                    && recipe.getClass() != ShapedTagRecipe.class && recipe.getClass() != ShapelessTagRecipe.class) continue;
            var crafting = (CraftingRecipe)recipe;
            var ingredients = crafting.placementInfo().ingredients();
            if (ingredients.isEmpty() || ingredients.size() > 9) continue;
            var stacks = new ArrayList<ItemStack>();
            boolean supported = true;
            for (var ingredient : ingredients) {
                if (ingredient.isCustom() || ingredient.getValues().size() != 1) { supported = false; break; }
                var item = ingredient.getValues().get(0).value();
                if (!essences.contains(item)) { supported = false; break; }
                stacks.add(new ItemStack(item));
            }
            if (!supported) continue;
            List<ItemStack> grid;
            int width, height;
            if (recipe instanceof ShapedRecipe || recipe instanceof ShapedTagRecipe) {
                width = recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : ((ShapedTagRecipe)recipe).getWidth();
                height = recipe instanceof ShapedRecipe shaped ? shaped.getHeight() : ((ShapedTagRecipe)recipe).getHeight();
                grid = new ArrayList<>();
                for (int index : crafting.placementInfo().slotsToIngredientIndex()) grid.add(index < 0 ? ItemStack.EMPTY : new ItemStack(ingredients.get(index).getValues().get(0).value()));
            } else { width = Math.min(3, stacks.size()); height = (stacks.size() + width - 1) / width; grid = new ArrayList<>(stacks); while (grid.size() < width * height) grid.add(ItemStack.EMPTY); }
            if (width > 3 || height > 3) continue;
            var input = CraftingInput.of(width, height, grid);
            if (!crafting.matches(input, server.overworld()) || crafting.getRemainingItems(input).stream().anyMatch(s -> !s.isEmpty())) continue;
            var output = crafting.assemble(input);
            if (output.isEmpty() || essences.contains(output.getItem()) || output.getCount() > output.getMaxStackSize()) continue;
            // Initial scope excludes component-bearing results and tier/compression cycles.
            if (!ItemStack.isSameItemSameComponents(output, new ItemStack(output.getItem()))) continue;
            var amounts = new LinkedHashMap<AEKey, Long>();
            for (var stack : grid) if (!stack.isEmpty()) amounts.merge(AEItemKey.of(stack), 1L, Math::addExact);
            var in = amounts.entrySet().stream().map(e -> new GenericStack(e.getKey(), e.getValue())).toList();
            var out = new GenericStack(AEItemKey.of(output), output.getCount());
            var encoded = PatternDetailsHelper.encodeProcessingPattern(in, List.of(out));
            var pattern = PatternDetailsHelper.decodePattern(encoded, server.overworld());
            if (pattern == null) continue;
            var entry = new Entry(holder.id().identifier().toString(), pattern, Map.copyOf(amounts), AEItemKey.of(output), output.getCount());
            if (definitions.putIfAbsent(pattern.getDefinition(), entry) == null) entries.add(entry);
        }
        var result = new Snapshot(recipes, List.copyOf(entries), Map.copyOf(definitions));
        CACHE.put(server, result);
        org.slf4j.LoggerFactory.getLogger(EssencePatternCatalog.class).info("Essence pattern catalog: {} exact ordinary essence conversions, discovery={}ms", entries.size(), (System.nanoTime() - started) / 1_000_000.0);
        return result;
    }
    private EssencePatternCatalog() {}
}
