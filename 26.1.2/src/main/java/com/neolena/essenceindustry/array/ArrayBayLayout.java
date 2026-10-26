package com.neolena.essenceindustry.array;

import java.util.*;
import net.minecraft.core.BlockPos;

/** Visual placement only; inventories and production capacity are never inferred from this allocation. */
public final class ArrayBayLayout {
 public record Bay(int x,int y,int z,int machines){}
 public static int columns(int size){return Math.max(0,(size-3)/2);}
 public static List<Bay> allocate(List<BlockPos> racks,int installed){
  int remaining=Math.clamp(installed,0,1024);var result=new ArrayList<Bay>();
  for(var rack:racks){if(remaining==0)break;int count=Math.min(8,remaining);result.add(new Bay(rack.getX(),rack.getY(),rack.getZ(),count));remaining-=count;}
  return List.copyOf(result);
 }
 public static long[] encode(List<BlockPos> racks,int width,int depth){return encode(racks,width,depth,5);}
 public static long[] encode(List<BlockPos> racks,int width,int depth,int height){
  int columns=columns(width),rows=columns(depth);var bits=new BitSet(columns*rows);
  for(var rack:racks){int x=rack.getX()/2-1,z=rack.getZ()/2-1;if(x<0||x>=columns||z<0||z>=rows||rack.getX()%2!=0||rack.getZ()%2!=0)throw new IllegalArgumentException("Invalid visual rack position");if(rack.getY()<1||rack.getY()>height-4||rack.getY()%2!=1)throw new IllegalArgumentException("Invalid rack level");bits.set(((rack.getY()-1)/2)*columns*rows+z*columns+x);}
  return bits.toLongArray();
 }
 public static List<BlockPos> decode(long[] words,int width,int depth){return decode(words,width,depth,5);}
 public static List<BlockPos> decode(long[] words,int width,int depth,int height){
  int columns=columns(width),rows=columns(depth);var bits=BitSet.valueOf(words);
  if(bits.length()>columns*rows*((height-3)/2))throw new IllegalArgumentException("Visual rack mask exceeds structure bounds");
  var result=new ArrayList<BlockPos>(bits.cardinality());
  for(int bit=bits.nextSetBit(0);bit>=0;bit=bits.nextSetBit(bit+1))result.add(new BlockPos((bit%columns+1)*2,1+2*(bit/(columns*rows)),((bit/columns)%rows+1)*2));
  return List.copyOf(result);
 }
 private ArrayBayLayout(){}
}
