package com.neolena.essenceindustry.guide;
import com.neolena.essenceindustry.EssenceIndustry;
import guideme.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;

public final class FieldGuideClient {
 public static final net.minecraft.resources.Identifier ID=EssenceIndustry.id("field_guide");
 // Register before the initial resource reload so GuideME can build navigation and search together.
 public static void register(IEventBus bus){Guide.builder(ID).folder("fieldguide").extension(guideme.compiler.TagCompiler.EXTENSION_POINT,new AvailableFuelGuideTag()).startPage(EssenceIndustry.id("index.md")).build();}
 public static void open(Player player){GuidesCommon.openGuide(player,ID);}
 private FieldGuideClient(){}
}
