package com.neolena.essenceindustry.construction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStackTemplate;
/** Exact item plus its original ME access point; empty origin denotes player inventory. */
public record BuilderStock(List<Entry> entries) {
 public record Entry(ItemStackTemplate item,Optional<GlobalPos> network) {
  public static final Codec<Entry> CODEC=RecordCodecBuilder.create(i->i.group(ItemStackTemplate.CODEC.fieldOf("item").forGetter(Entry::item),GlobalPos.CODEC.optionalFieldOf("network").forGetter(Entry::network)).apply(i,Entry::new));
 }
 public static final BuilderStock EMPTY=new BuilderStock(List.of());
 public static final Codec<BuilderStock> CODEC=Entry.CODEC.sizeLimitedListOf(128).xmap(BuilderStock::new,BuilderStock::entries);
 public BuilderStock{entries=List.copyOf(entries);}
}
