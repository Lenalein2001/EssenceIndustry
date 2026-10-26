package com.neolena.essenceindustry.guide;

import com.neolena.essenceindustry.EssenceIndustry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.*;

/** A craftable entry point for the GuideME reader. */
public final class FieldGuide {
 private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(EssenceIndustry.MOD_ID);
 public static final DeferredItem<Item> BOOK=ITEMS.registerItem("field_guide",GuideBook::new,p->p.stacksTo(1));
 public static void register(IEventBus bus){
  ITEMS.register(bus);bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e)->{if(e.getTabKey()==CreativeModeTabs.TOOLS_AND_UTILITIES)e.accept(BOOK.get());});
  if(FMLEnvironment.getDist().isClient())FieldGuideClient.register(bus);
 }
 private static final class GuideBook extends Item {
  GuideBook(Item.Properties properties){super(properties);}
  @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
   if(level.isClientSide())FieldGuideClient.open(player);
   return InteractionResult.SUCCESS;
  }
 }
 private FieldGuide(){}
}
