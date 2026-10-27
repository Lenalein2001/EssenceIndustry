package com.neolena.essenceindustry.compat.jei;
import com.neolena.essenceindustry.compat.hnn.HnnUtilities;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
/** The source model is retained; only the blank framework and Quantum Circuit are consumed. */
final class ModelCopyCategory implements IRecipeCategory<ItemStack> {
 static final RecipeType<ItemStack> TYPE=RecipeType.create("essenceindustry","model_copying",ItemStack.class);
 final IDrawable icon;ModelCopyCategory(IDrawable icon){this.icon=icon;}
 public RecipeType<ItemStack> getRecipeType(){return TYPE;}public Component getTitle(){return Component.translatable("block.essenceindustry.model_copier");}public int getWidth(){return 180;}public int getHeight(){return 82;}public IDrawable getIcon(){return icon;}
 public void setRecipe(IRecipeLayoutBuilder b,ItemStack source,IFocusGroup focus){
  var displayed=focusedModel(source,focus);
  b.addSlot(RecipeIngredientRole.INPUT,4,23).setSlotName("retained_source").setStandardSlotBackground().addItemStack(displayed.copy())
   .addRichTooltipCallback((slot,tooltip)->tooltip.add(Component.translatable("jei.essenceindustry.copy_source")));
  b.addInputSlot(38,23).setSlotName("blank_model").setStandardSlotBackground().addItemStack(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("hostilenetworks:blank_data_model"))));
  b.addInputSlot(60,23).setSlotName("quantum_circuit").setStandardSlotBackground().addItemStack(new ItemStack(com.neolena.essenceindustry.compat.hnn.ModelCopy.catalyst()));
  b.addOutputSlot(145,23).setSlotName("copied_model").setOutputSlotBackground().addItemStack(displayed.copy())
   .addRichTooltipCallback((slot,tooltip)->tooltip.add(Component.translatable("jei.essenceindustry.copy_data")));
 }
 private static ItemStack focusedModel(ItemStack source,IFocusGroup focus){
  var model=dev.shadowsoffire.hostilenetworks.item.DataModelItem.getStoredModel(source).getId();
  // HNN deliberately indexes JEI subtypes by mob, not training. Preserve the focused
  // model's exact training and other components instead of showing a generic copy.
  return focus.getItemStackFocuses().map(f->f.getTypedValue().getIngredient())
   .filter(s->s.getItem()==source.getItem()&&HnnUtilities.isModel(s)&&dev.shadowsoffire.hostilenetworks.item.DataModelItem.getStoredModel(s).getId().equals(model))
   .findFirst().orElse(source).copyWithCount(1);
 }
 public void draw(ItemStack source,IRecipeSlotsView slots,GuiGraphicsExtractor g,double x,double y){var f=Minecraft.getInstance().font;g.text(f,Component.translatable("jei.essenceindustry.retained"),2,6,0xFF303E48,false);g.fill(100,30,131,33,0xFF28647D);for(int i=0;i<5;i++)g.fill(131+i,27+i,132+i,36-i,0xFF28647D);g.text(f,Component.translatable("jei.essenceindustry.copy_data"),2,52,0xFF303E48,false);g.text(f,Component.translatable("jei.essenceindustry.copy_source"),2,67,0xFF46545E,false);}
}
