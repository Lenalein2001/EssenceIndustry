package com.neolena.essenceindustry.construction;
import java.util.*;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
/** Lossless public item data: preserve vanilla inventory hashes and AE2 item keys without repeating state NBT per block. */
public final class BuilderJournalWire {
 public static final StreamCodec<RegistryFriendlyByteBuf,BuilderJournal> CODEC=new StreamCodec<>() {
  public void encode(RegistryFriendlyByteBuf b,BuilderJournal journal){
   b.writeBoolean(journal.origin().isPresent());journal.origin().ifPresent(p->GlobalPos.STREAM_CODEC.encode(b,p));
   b.writeBoolean(journal.material().isPresent());journal.material().ifPresent(m->ItemStackTemplate.STREAM_CODEC.encode(b,m));
   b.writeUtf(journal.owner(),64);b.writeBoolean(journal.paid());
   var states=new LinkedHashMap<BlockState,Integer>();var networks=new LinkedHashMap<Optional<GlobalPos>,Integer>();
   for(var entry:journal.blocks()){states.computeIfAbsent(entry.state(),s->states.size());networks.computeIfAbsent(entry.network(),s->networks.size());}
   if(journal.blocks().size()>4096||states.size()>64||networks.size()>8)throw new IllegalArgumentException("Builder history exceeds wire bounds");
   b.writeVarInt(states.size());for(var state:states.keySet())b.writeVarInt(Block.getId(state));
   b.writeVarInt(networks.size());for(var point:networks.keySet()){b.writeBoolean(point.isPresent());point.ifPresent(p->GlobalPos.STREAM_CODEC.encode(b,p));}
   var base=journal.origin().map(GlobalPos::pos).orElse(BlockPos.ZERO);boolean compact=true;
   for(var entry:journal.blocks()){var p=entry.pos();if(p.getX()-base.getX()< -128||p.getX()-base.getX()>127||p.getY()-base.getY()< -128||p.getY()-base.getY()>127||p.getZ()-base.getZ()< -128||p.getZ()-base.getZ()>127){compact=false;break;}}
   var data=new LinkedHashMap<String,Integer>();for(var entry:journal.blocks())entry.data().ifPresent(value->data.computeIfAbsent(value,k->data.size()+1));
   b.writeVarInt(data.size());for(var value:data.keySet())b.writeUtf(value,64);
   b.writeBoolean(compact);b.writeVarInt(journal.blocks().size());
   for(var entry:journal.blocks()){
    var p=entry.pos();if(compact){b.writeByte(p.getX()-base.getX());b.writeByte(p.getY()-base.getY());b.writeByte(p.getZ()-base.getZ());}else b.writeBlockPos(p);
    if(!data.isEmpty())b.writeVarInt(entry.data().map(data::get).orElse(0));
    if(states.size()>1)b.writeVarInt(states.get(entry.state()));if(networks.size()>1)b.writeVarInt(networks.get(entry.network()));
   }
  }
  public BuilderJournal decode(RegistryFriendlyByteBuf b){
   var origin=b.readBoolean()?Optional.of(GlobalPos.STREAM_CODEC.decode(b)):Optional.<GlobalPos>empty();
   var material=b.readBoolean()?Optional.of(ItemStackTemplate.STREAM_CODEC.decode(b)):Optional.<ItemStackTemplate>empty();var owner=b.readUtf(64);boolean paid=b.readBoolean();
   int stateCount=bounded(b.readVarInt(),64);var states=new ArrayList<BlockState>();for(int n=0;n<stateCount;n++){int id=b.readVarInt();var state=Block.stateById(id);if(id<0||Block.getId(state)!=id)throw new DecoderException("Invalid builder state");states.add(state);}
   int networkCount=bounded(b.readVarInt(),8);var networks=new ArrayList<Optional<GlobalPos>>();for(int n=0;n<networkCount;n++)networks.add(b.readBoolean()?Optional.of(GlobalPos.STREAM_CODEC.decode(b)):Optional.empty());
   int dataCount=bounded(b.readVarInt(),4096);var data=new ArrayList<String>();for(int n=0;n<dataCount;n++){var value=b.readUtf(64);if(!value.matches("[0-9a-f]{64}"))throw new DecoderException("Invalid builder fingerprint");data.add(value);}
   boolean compact=b.readBoolean();int count=bounded(b.readVarInt(),4096);if(count>0&&(states.isEmpty()||networks.isEmpty()))throw new DecoderException("Missing builder palette");
   var base=origin.map(GlobalPos::pos).orElse(BlockPos.ZERO);var blocks=new ArrayList<BuilderJournal.Placed>();
   for(int n=0;n<count;n++){var pos=compact?base.offset(b.readByte(),b.readByte(),b.readByte()):b.readBlockPos();int di=dataCount>0?bounded(b.readVarInt(),dataCount):0;int si=stateCount>1?b.readVarInt():0,ni=networkCount>1?b.readVarInt():0;if(si<0||si>=stateCount||ni<0||ni>=networkCount)throw new DecoderException("Invalid builder palette index");blocks.add(new BuilderJournal.Placed(pos,states.get(si),networks.get(ni),di==0?Optional.empty():Optional.of(data.get(di-1))));}
   return new BuilderJournal(origin,blocks,material,owner,paid);
  }
 };
 private static int bounded(int value,int max){if(value<0||value>max)throw new DecoderException("Oversized builder palette/history");return value;}
 private BuilderJournalWire(){}
}
