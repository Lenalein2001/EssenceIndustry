package com.neolena.essenceindustry.construction;
import java.io.*;
import java.security.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
/** Immutable, bounded Undo fingerprints. Source block data is never copied into a new placement. */
public final class BuilderBlockData {
 public static Optional<String> fingerprint(ServerLevel level,BlockPos pos){
  var entity=level.getBlockEntity(pos);if(entity==null)return Optional.empty();
  if(entity instanceof net.minecraft.world.Container container&&!container.isEmpty())return Optional.empty();
  try{
   var tag=entity.saveWithFullMetadata(level.registryAccess());tag.remove("x");tag.remove("y");tag.remove("z");if(tag.sizeInBytes()>1_048_576)return Optional.empty();
   var digest=MessageDigest.getInstance("SHA-256");
   try(var out=new DataOutputStream(new DigestOutputStream(OutputStream.nullOutputStream(),digest))){write(tag,out,0);}
   return Optional.of(HexFormat.of().formatHex(digest.digest()));
  }catch(IOException|GeneralSecurityException|RuntimeException ex){return Optional.empty();}
 }
 private static void write(Tag tag,DataOutput out,int depth)throws IOException{
  if(depth>64)throw new IOException("Builder block data nesting limit");
  if(tag instanceof CompoundTag compound){out.writeByte(10);var keys=new TreeSet<>(compound.keySet());out.writeInt(keys.size());for(var key:keys){out.writeUTF(key);write(compound.get(key),out,depth+1);}}
  else if(tag instanceof ListTag list){out.writeByte(9);out.writeInt(list.size());for(var child:list)write(child,out,depth+1);}
  else NbtIo.writeUnnamedTag(tag,out);
 }
 public static boolean unchanged(ServerLevel level,BuilderJournal.Placed entry){
  if(entry.state().hasBlockEntity())return entry.data().isPresent()&&entry.data().equals(fingerprint(level,entry.pos()));
  return level.getBlockEntity(entry.pos())==null;
 }
 private BuilderBlockData(){}
}
