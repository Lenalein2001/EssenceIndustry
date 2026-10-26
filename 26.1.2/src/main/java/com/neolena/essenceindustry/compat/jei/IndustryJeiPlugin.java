package com.neolena.essenceindustry.compat.jei;
import com.neolena.essenceindustry.nuclear.*;
import mezz.jei.api.*;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import java.util.*;
/** Optional JEI integration: recipes are derived from the same fuel process definitions as gameplay. */
@JeiPlugin
public final class IndustryJeiPlugin implements IModPlugin {
 public static final Map<String,RecipeType<FuelCycle.Process>> TYPES=new LinkedHashMap<>();
 static {for(String m:List.of("fuel_enricher","fuel_reprocessor","fuel_fabricator","reactor"))TYPES.put(m,RecipeType.create("essenceindustry",m,FuelCycle.Process.class));}
 public Identifier getPluginUid(){return Identifier.parse("essenceindustry:machines");}
 public void registerCategories(IRecipeCategoryRegistration r){if(net.neoforged.fml.ModList.get().isLoaded("hostilenetworks"))r.addRecipeCategories(new ModelCopyCategory(r.getJeiHelpers().getGuiHelper().createDrawableItemLike(com.neolena.essenceindustry.compat.hnn.HnnUtilities.BLOCK.get())));for(var e:TYPES.entrySet())r.addRecipeCategories(new FuelCategory(e.getKey(),e.getValue(),r.getJeiHelpers().getGuiHelper().createDrawableItemLike(station(e.getKey()))));}
 public void registerRecipes(IRecipeRegistration r){if(net.neoforged.fml.ModList.get().isLoaded("hostilenetworks"))r.addRecipes(ModelCopyCategory.TYPE,r.getIngredientManager().getAllItemStacks().stream().filter(com.neolena.essenceindustry.compat.hnn.HnnUtilities::isModel).map(ItemStack::copy).toList());for(var e:TYPES.entrySet())r.addRecipes(e.getValue(),processes().stream().filter(p->p.machine().equals(e.getKey())).toList());}
 public void registerRecipeCatalysts(IRecipeCatalystRegistration r){if(net.neoforged.fml.ModList.get().isLoaded("hostilenetworks"))r.addCraftingStation(ModelCopyCategory.TYPE,com.neolena.essenceindustry.compat.hnn.HnnUtilities.BLOCK.get());r.addCraftingStation(com.blakebr0.mysticalagriculture.compat.jei.category.InfusionCategory.RECIPE_TYPE,com.neolena.essenceindustry.seedcraft.SeedCrafter.BLOCK.get());for(var e:TYPES.entrySet())r.addCraftingStation(e.getValue(),station(e.getKey()));}
 public static net.minecraft.world.level.ItemLike station(String machine){return machine.equals("reactor")?NuclearReactor.block("controller"):NuclearFuels.MACHINES.get(machine).get();}
 public static List<FuelCycle.Process> processes(){var list=new ArrayList<>(FuelCycle.PROCESSES);for(var fuel:ReactorFuels.ALL)list.add(new FuelCycle.Process("reactor",0,List.of(new FuelCycle.Input(fuel.id().equals("uranium")?"#c:ingots/uranium":fuel.preparedItem().substring("essenceindustry:".length()),1)),List.of(new FuelCycle.Output("spent_"+fuel.id()+"_fuel",1))));list.add(new FuelCycle.Process("reactor",0,List.of(new FuelCycle.Input("#c:storage_blocks/uranium",1)),List.of(new FuelCycle.Output("spent_uranium_fuel",9))));return List.copyOf(list);}
 public static List<ItemStack> inputs(FuelCycle.Input input){return BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(input::matches).map(s->s.copyWithCount(input.count())).toList();}
 static final class FuelCategory implements IRecipeCategory<FuelCycle.Process> {
  final String machine;final RecipeType<FuelCycle.Process> type;final IDrawable icon;
  FuelCategory(String m,RecipeType<FuelCycle.Process> t,IDrawable i){machine=m;type=t;icon=i;}
  public RecipeType<FuelCycle.Process> getRecipeType(){return type;}
  public Component getTitle(){return Component.translatable("block.essenceindustry."+(machine.equals("reactor")?"reactor_controller":machine));}
  public int getWidth(){return 180;}public int getHeight(){return 86;}public IDrawable getIcon(){return icon;}
  public void setRecipe(IRecipeLayoutBuilder b,FuelCycle.Process p,IFocusGroup focus){int i=0;for(var in:p.inputs())b.addInputSlot(4+22*i++,24).setStandardSlotBackground().addItemStacks(inputs(in));i=0;for(var out:p.outputs())b.addOutputSlot(132+24*i++,24).setOutputSlotBackground().addItemStack(NuclearFuels.stack(out.item(),out.count()));}
  public void draw(FuelCycle.Process p,IRecipeSlotsView slots,GuiGraphicsExtractor g,double mx,double my){var font=Minecraft.getInstance().font;g.text(font,Component.translatable("jei.essenceindustry.ingredients"),3,5,0xFF303E48,false);g.text(font,Component.translatable("jei.essenceindustry.products"),130,5,0xFF303E48,false);g.fill(82,31,113,34,0xFF28647D);for(int i=0;i<5;i++)g.fill(113+i,28+i,114+i,37-i,0xFF28647D);g.text(font,(machine.equals("reactor")?Component.translatable("jei.essenceindustry.burn"):Component.literal(String.format(Locale.ROOT,"%,d FE  |  %.1f s",p.energy(),FuelProcessorTile.duration(p)/20d))),3,52,0xFF222222,false);g.text(font,Component.translatable("jei.essenceindustry.multiblock"),3,69,0xFF46545E,false);}
  public Identifier getRegistryName(FuelCycle.Process p){return Identifier.parse("essenceindustry:"+machine+"/"+p.inputs().getFirst().ingredient().replace('#','_').replace(':','_').replace('/','_')+"_"+p.outputs().getFirst().item());}
 }
}
