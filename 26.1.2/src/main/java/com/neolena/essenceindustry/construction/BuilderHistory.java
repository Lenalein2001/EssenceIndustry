package com.neolena.essenceindustry.construction;
import com.mojang.serialization.*;
import java.util.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import io.netty.handler.codec.DecoderException;
/** Older operations, newest first. Only Undo records expire; buffered materials are separate. */
public record BuilderHistory(List<BuilderJournal> operations) {
 public static final int MAX_OPERATIONS=32,MAX_BLOCKS=8192;
 public static final BuilderHistory EMPTY=new BuilderHistory(List.of());
 public BuilderHistory {operations=List.copyOf(operations);if(operations.size()>=MAX_OPERATIONS||operations.stream().anyMatch(j->j.blocks().isEmpty())||operations.stream().mapToInt(j->j.blocks().size()).sum()>MAX_BLOCKS)throw new IllegalArgumentException("Invalid builder history bounds");}
 private static final Codec<List<BuilderJournal>> LIST=BuilderJournal.CODEC.sizeLimitedListOf(MAX_OPERATIONS-1).validate(list->list.stream().noneMatch(j->j.blocks().isEmpty())&&list.stream().mapToInt(j->j.blocks().size()).sum()<=MAX_BLOCKS?DataResult.success(list):DataResult.error(()->"Invalid builder history bounds"));
 public static final Codec<BuilderHistory> CODEC=LIST.xmap(BuilderHistory::new,BuilderHistory::operations);
 public static final StreamCodec<RegistryFriendlyByteBuf,BuilderHistory> WIRE=new StreamCodec<>(){
  public void encode(RegistryFriendlyByteBuf b,BuilderHistory history){b.writeVarInt(history.operations.size());for(var j:history.operations)BuilderJournalWire.CODEC.encode(b,j);}
  public BuilderHistory decode(RegistryFriendlyByteBuf b){int count=b.readVarInt();if(count<0||count>=MAX_OPERATIONS)throw new DecoderException("Oversized builder history");var journals=new ArrayList<BuilderJournal>();int blocks=0;for(int n=0;n<count;n++){var j=BuilderJournalWire.CODEC.decode(b);blocks+=j.blocks().size();if(j.blocks().isEmpty()||blocks>MAX_BLOCKS)throw new DecoderException("Oversized/empty builder history");journals.add(j);}return new BuilderHistory(journals);}
 };
}
