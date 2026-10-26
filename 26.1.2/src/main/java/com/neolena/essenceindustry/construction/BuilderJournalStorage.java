package com.neolena.essenceindustry.construction;
import java.util.*;
import java.util.stream.*;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.state.BlockState;
/** State/source palettes and primitive position/reference arrays keep saves and vanilla inventory hashes linear and compact. */
public final class BuilderJournalStorage {
 private record Packed(Optional<GlobalPos> origin,List<BlockState> states,List<Optional<GlobalPos>> networks,long[] positions,int[] refs,List<String> data,Optional<ItemStackTemplate> material,String owner,boolean paid){}
 private static final Codec<long[]> LONGS=Codec.LONG_STREAM.xmap(LongStream::toArray,Arrays::stream).validate(a->a.length<=4096?DataResult.success(a):DataResult.error(()->"Oversized builder positions"));
 private static final Codec<int[]> INTS=Codec.INT_STREAM.xmap(IntStream::toArray,Arrays::stream).validate(a->a.length<=4096?DataResult.success(a):DataResult.error(()->"Oversized builder references"));
 private record Point(Optional<GlobalPos> point){}
 private static final Codec<Optional<GlobalPos>> POINT=RecordCodecBuilder.<Point>create(i->i.group(GlobalPos.CODEC.optionalFieldOf("point").forGetter(Point::point)).apply(i,Point::new)).xmap(Point::point,Point::new);
 private static final Codec<Packed> PACKED=RecordCodecBuilder.create(i->i.group(
  GlobalPos.CODEC.optionalFieldOf("origin").forGetter(Packed::origin),BlockState.CODEC.sizeLimitedListOf(64).fieldOf("states").forGetter(Packed::states),POINT.sizeLimitedListOf(8).fieldOf("networks").forGetter(Packed::networks),
  LONGS.fieldOf("positions").forGetter(Packed::positions),INTS.fieldOf("references").forGetter(Packed::refs),Codec.STRING.sizeLimitedListOf(4096).optionalFieldOf("data",List.of()).forGetter(Packed::data),ItemStackTemplate.CODEC.optionalFieldOf("material").forGetter(Packed::material),Codec.STRING.fieldOf("owner").forGetter(Packed::owner),Codec.BOOL.fieldOf("paid").forGetter(Packed::paid)).apply(i,Packed::new));
 // Optional values in a list need a map wrapper: null is not a valid NBT list element.
 public static final Codec<BuilderJournal> CODEC=PACKED.flatXmap(BuilderJournalStorage::unpack,BuilderJournalStorage::pack);
 private static DataResult<BuilderJournal> unpack(Packed p){
  if(p.positions.length!=p.refs.length||!p.data.isEmpty()&&p.data.size()!=p.positions.length)return DataResult.error(()->"Mismatched builder arrays");for(var value:p.data)if(!value.isEmpty()&&!value.matches("[0-9a-f]{64}"))return DataResult.error(()->"Invalid builder fingerprint");var out=new ArrayList<BuilderJournal.Placed>();
  for(int n=0;n<p.positions.length;n++){int ref=p.refs[n],si=ref>>>3,ni=ref&7;if(ref<0||si>=p.states.size()||ni>=p.networks.size())return DataResult.error(()->"Invalid builder reference");out.add(new BuilderJournal.Placed(BlockPos.of(p.positions[n]),p.states.get(si),p.networks.get(ni),p.data.isEmpty()||p.data.get(n).isEmpty()?Optional.empty():Optional.of(p.data.get(n))));}
  return DataResult.success(new BuilderJournal(p.origin,out,p.material,p.owner,p.paid));
 }
 private static DataResult<Packed> pack(BuilderJournal j){
  var states=new LinkedHashMap<BlockState,Integer>();var networks=new LinkedHashMap<Optional<GlobalPos>,Integer>();int count=j.blocks().size();if(count>4096)return DataResult.error(()->"Oversized builder history");var positions=new long[count];var refs=new int[count];
  for(int n=0;n<count;n++){var b=j.blocks().get(n);int state=states.computeIfAbsent(b.state(),s->states.size()),network=networks.computeIfAbsent(b.network(),s->networks.size());if(state>=64||network>=8)return DataResult.error(()->"Oversized builder palette");positions[n]=b.pos().asLong();refs[n]=state<<3|network;}
  return DataResult.success(new Packed(j.origin(),List.copyOf(states.keySet()),List.copyOf(networks.keySet()),positions,refs,j.blocks().stream().anyMatch(b->b.data().isPresent())?j.blocks().stream().map(b->b.data().orElse("")).toList():List.of(),j.material(),j.owner(),j.paid()));
 }
 private BuilderJournalStorage(){}
}
