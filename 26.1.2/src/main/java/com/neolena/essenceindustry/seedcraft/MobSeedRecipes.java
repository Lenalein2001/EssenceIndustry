package com.neolena.essenceindustry.seedcraft;
import com.blakebr0.cucumber.event.RecipeManagerLoadingEvent;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.blakebr0.mysticalagriculture.crafting.recipe.InfusionRecipe;
import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import java.util.*;
/** Additional recipes only: keep MA's original soul recipes and current crop tier/base seed. */
public final class MobSeedRecipes {
 public record Spec(String crop,String drop,String material){}
 public static final List<Spec> SPECS=List.of(
  new Spec("zombie","rotten_flesh","bone_block"),new Spec("skeleton","bone","iron_block"),
  new Spec("creeper","gunpowder","sandstone"),new Spec("spider","spider_eye","string"),
  new Spec("slime","slime_ball","slime_block"),new Spec("cow","leather","hay_block"),
  new Spec("chicken","feather","hay_block"),new Spec("pig","porkchop","hay_block"),
  new Spec("sheep","mutton","hay_block"),new Spec("squid","ink_sac","prismarine"),
  new Spec("fish","cod","dried_kelp_block"),new Spec("rabbit","rabbit_hide","carrot"),
  new Spec("turtle","turtle_scute","seagrass"),new Spec("blaze","blaze_rod","nether_bricks"),
  new Spec("ghast","ghast_tear","soul_sand"),new Spec("enderman","ender_pearl","obsidian"),
  new Spec("wither_skeleton","wither_skeleton_skull","bone_block"),new Spec("wither","nether_star","netherite_scrap")
 );
 private static final String MOD=EssenceIndustry.MOD_ID;
 public static void register(IEventBus bus){net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,MobSeedRecipes::recipes);}
 private static void recipes(RecipeManagerLoadingEvent event){
  for(var spec:SPECS){
   var crop=CropRegistry.getInstance().getCropById(Identifier.fromNamespaceAndPath("mysticalagriculture",spec.crop()));
   if(crop==null || !crop.isEnabled() || !crop.getRecipeConfig().isSeedInfusionRecipeEnabled())continue;
   var essence=crop.getTier().getEssenceItem();var seed=crop.getType().getCraftingSeedItem();if(essence==null || seed==null || crop.getSeedsItem()==null)continue;
   var drop=BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(spec.drop()));var material=BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(spec.material()));if(drop==Items.AIR || material==Items.AIR)continue;
   // Four material positions: three mob drops and one associated ordinary material.
   // No soul jar, offering token or separate filling/crafting step.
   var inputs=new ArrayList<Ingredient>();
   for(int i=0;i<4;i++){inputs.add(Ingredient.of(i<3?drop:material));inputs.add(Ingredient.of(essence));}
   add(event,"mob_seeds/"+spec.crop(),new InfusionRecipe(Ingredient.of(seed),inputs,new ItemStackTemplate(crop.getSeedsItem()),false));
  }
 }
 private static void add(RecipeManagerLoadingEvent event,String path,Recipe<?> recipe){event.addRecipe(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE,Identifier.fromNamespaceAndPath(MOD,path)),recipe));}
 private MobSeedRecipes(){}
}
