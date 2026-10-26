package com.neolena.essenceindustry.construction;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
/** Persistent item history; lossless palette/delta wire encoding also preserves inventory and AE2 key equality. */
public record BuilderJournal(Optional<GlobalPos> origin,List<Placed> blocks,Optional<ItemStackTemplate> material,String owner,boolean paid) {
 public record Placed(BlockPos pos,BlockState state,Optional<GlobalPos> network,Optional<String> data) {
  public Placed(BlockPos pos,BlockState state,Optional<GlobalPos> network){this(pos,state,network,Optional.empty());}
  static final Codec<String> DATA=Codec.STRING.validate(s->s.matches("[0-9a-f]{64}")?DataResult.success(s):DataResult.error(()->"Invalid builder fingerprint"));
  static final Codec<Placed> CODEC=RecordCodecBuilder.create(i->i.group(BlockPos.CODEC.fieldOf("pos").forGetter(Placed::pos),BlockState.CODEC.fieldOf("state").forGetter(Placed::state),GlobalPos.CODEC.optionalFieldOf("network").forGetter(Placed::network),DATA.optionalFieldOf("data").forGetter(Placed::data)).apply(i,Placed::new));
 }
 public static final BuilderJournal EMPTY=new BuilderJournal(Optional.empty(),List.of(),Optional.empty(),"",false);
 private static final Codec<BuilderJournal> LEGACY=RecordCodecBuilder.create(i->i.group(GlobalPos.CODEC.optionalFieldOf("origin").forGetter(BuilderJournal::origin),Placed.CODEC.sizeLimitedListOf(4096).fieldOf("blocks").forGetter(BuilderJournal::blocks),ItemStackTemplate.CODEC.optionalFieldOf("material").forGetter(BuilderJournal::material),Codec.STRING.fieldOf("owner").forGetter(BuilderJournal::owner),Codec.BOOL.fieldOf("paid").forGetter(BuilderJournal::paid)).apply(i,BuilderJournal::new));
 public static final Codec<BuilderJournal> CODEC=Codec.either(BuilderJournalStorage.CODEC,LEGACY).xmap(e->e.map(java.util.function.Function.identity(),java.util.function.Function.identity()),com.mojang.datafixers.util.Either::left);
 public ItemStack materialStack(){return material.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);}
 public BuilderJournal {blocks=List.copyOf(blocks);material=Objects.requireNonNull(material);}
}
