package com.neolena.essenceindustry.compat.ae2.bulk;
import appeng.api.stacks.AEItemKey;
import com.mojang.serialization.Codec;
import com.neolena.essenceindustry.library.SeedDataSafety;
import java.math.BigInteger;
import java.util.Objects;
import net.minecraft.nbt.*;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.CustomData;
/** Immutable counters and encoded keys; transfers never serialize item components. */
public final class BulkCellData {
 /** Legacy capacity and bounded AE2 listing window, not the new storage capacity. */
 public static final long CAPACITY = 1_000_000_000_000L;
 public static final BigInteger MAX_QUANTITY = BigInteger.ONE.shiftLeft(4096).subtract(BigInteger.ONE);
 private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
 public static final BulkCellData EMPTY = new BulkCellData(null,null,null,null,BigInteger.ZERO,null);
 public static final Codec<BulkCellData> CODEC = CustomData.CODEC.xmap(BulkCellData::decode,BulkCellData::encode);
 public static final StreamCodec<io.netty.buffer.ByteBuf,BulkCellData> STREAM_CODEC = CustomData.STREAM_CODEC.map(BulkCellData::decode,BulkCellData::encode);
 private final AEItemKey filter, stored;
 private final CustomData filterTag, storedTag, recovery;
 private final BigInteger quantity;
 private BulkCellData(AEItemKey filter,CustomData filterTag,AEItemKey stored,CustomData storedTag,BigInteger quantity,CustomData recovery) {
  this.filter=filter;this.filterTag=filterTag;this.stored=stored;this.storedTag=storedTag;this.quantity=quantity;this.recovery=recovery;
 }
 public AEItemKey key() { return stored!=null?stored:filter; }
 public AEItemKey filter() { return filter; }
 public AEItemKey storedKey() { return stored; }
 public BigInteger quantity() { return quantity; }
 public long count() { return quantity.min(LONG_MAX).longValueExact(); }
 public long listedCount() { return quantity.min(BigInteger.valueOf(CAPACITY)).longValueExact(); }
 public boolean valid() { return recovery==null; }
 public boolean accepts(AEItemKey key) { return valid() && key.equals(filter) && (stored==null || key.equals(stored)); }
 public boolean mismatched() { return stored!=null && !stored.equals(filter); }
 public BulkCellData withCount(long count) { return withQuantity(BigInteger.valueOf(count)); }
 public BulkCellData withQuantity(BigInteger count) {
  if(!valid() || count.signum()<0 || count.compareTo(MAX_QUANTITY)>0 || (count.signum()>0 && key()==null))throw new IllegalArgumentException("Invalid bulk count");
  return new BulkCellData(filter,filterTag,count.signum()==0?null:key(),count.signum()==0?null:(stored!=null?storedTag:filterTag),count,null);
 }
 public BulkCellData withFilter(AEItemKey key) {
  if(!valid())return null;
  var selected=select(key);if(selected==null)return null;
  return new BulkCellData(key,selected.filterTag,stored,storedTag,quantity,null);
 }
 public static BulkCellData select(AEItemKey key) {
  if(key==null)return EMPTY;
  try {
   var ops=SeedDataSafety.ops(SeedDataSafety.builtinLookup());var encoded=AEItemKey.CODEC.encodeStart(ops,key);
   if(encoded.error().isPresent() || !(encoded.result().orElseThrow() instanceof CompoundTag tag) || SeedDataSafety.tagWeight(tag)>65_536)return null;
   if(!key.equals(parseKey(tag)))return null;
   return new BulkCellData(key,CustomData.of(tag),null,null,BigInteger.ZERO,null);
  }catch(RuntimeException error){return null;}
 }
 private static AEItemKey parseKey(CompoundTag tag) {
  if(SeedDataSafety.tagWeight(tag)>65_536)throw new IllegalArgumentException();
  return AEItemKey.CODEC.parse(SeedDataSafety.ops(SeedDataSafety.builtinLookup()),tag).getOrThrow();
 }
 private static BulkCellData decode(CustomData raw) {
  try {
   var tag=raw.copyTag();if(!(tag.get("version") instanceof IntTag))throw new IllegalArgumentException();
   int version=tag.getIntOr("version",0);BigInteger count;
   if(version==1) {
    if(!(tag.get("count") instanceof LongTag) || !java.util.Set.of("version","count","item").containsAll(tag.keySet()))throw new IllegalArgumentException();
    long legacy=tag.getLongOr("count",-1);if(legacy<0 || legacy>CAPACITY)throw new IllegalArgumentException();count=BigInteger.valueOf(legacy);
   }else if(version==2) {
    if(!(tag.get("count") instanceof ByteArrayTag bytes) || !java.util.Set.of("version","count","item","filter").containsAll(tag.keySet()))throw new IllegalArgumentException();
    byte[] value=bytes.getAsByteArray();if(value.length==0 || value.length>513)throw new IllegalArgumentException();
    count=new BigInteger(value);if(count.signum()<0 || count.compareTo(MAX_QUANTITY)>0 || !java.util.Arrays.equals(value,count.toByteArray()))throw new IllegalArgumentException();
   }else throw new IllegalArgumentException();
   CompoundTag item=tag.contains("item")?tag.getCompound("item").orElseThrow():null;
   CompoundTag selected=version==1?item:(tag.contains("filter")?tag.getCompound("filter").orElseThrow():null);
   if(count.signum()>0 && item==null || version==2 && count.signum()==0 && item!=null)throw new IllegalArgumentException();
   var stored=item==null?null:parseKey(item);var filter=selected==null?null:parseKey(selected);
   return new BulkCellData(filter,filter==null?null:encodeKey(filter),count.signum()>0?stored:null,count.signum()>0?encodeKey(stored):null,count,null);
  }catch(RuntimeException error){return new BulkCellData(null,null,null,null,BigInteger.ZERO,raw);}
 }
 private static CustomData encodeKey(AEItemKey key) {
  return CustomData.of((CompoundTag)AEItemKey.CODEC.encodeStart(SeedDataSafety.ops(SeedDataSafety.builtinLookup()),key).getOrThrow());
 }
 private CustomData encode() {
  if(recovery!=null)return recovery;
  var tag=new CompoundTag();tag.putInt("version",2);tag.putByteArray("count",quantity.toByteArray());
  if(filterTag!=null)tag.put("filter",filterTag.copyTag());if(storedTag!=null)tag.put("item",storedTag.copyTag());return CustomData.of(tag);
 }
 @Override public boolean equals(Object other) {return other instanceof BulkCellData data && quantity.equals(data.quantity) && Objects.equals(filterTag,data.filterTag) && Objects.equals(storedTag,data.storedTag) && Objects.equals(recovery,data.recovery);}
 @Override public int hashCode(){return Objects.hash(filterTag,storedTag,quantity,recovery);}
}
