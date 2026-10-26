package com.neolena.essenceindustry.construction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStackTemplate;
public record BuilderTransfer(ItemStackTemplate material,GlobalPos network,boolean exporting) {
 // Transfer journals may cover thousands of items; vanilla stack codecs only accept 1..99.
 private record Saved(ItemStackTemplate material,GlobalPos network,boolean exporting,int amount){}
 public static final Codec<BuilderTransfer> CODEC=RecordCodecBuilder.<Saved>create(i->i.group(ItemStackTemplate.CODEC.fieldOf("material").forGetter(Saved::material),GlobalPos.CODEC.fieldOf("network").forGetter(Saved::network),Codec.BOOL.fieldOf("exporting").forGetter(Saved::exporting),Codec.intRange(0,1_000_000).optionalFieldOf("amount",0).forGetter(Saved::amount)).apply(i,Saved::new)).xmap(s->new BuilderTransfer(s.material.withCount(s.amount==0?s.material.count():s.amount),s.network,s.exporting),t->new Saved(t.material.withCount(1),t.network,t.exporting,t.material.count()));
}
