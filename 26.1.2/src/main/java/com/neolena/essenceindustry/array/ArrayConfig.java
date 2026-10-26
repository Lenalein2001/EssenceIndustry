package com.neolena.essenceindustry.array;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-owned size limits. Lowering a limit pauses existing larger arrays; it never resizes them. */
public final class ArrayConfig {
 public static final int HARD_MAX=64;
 public static final ModConfigSpec SPEC;
 private static final ModConfigSpec.IntValue WIDTH,DEPTH,HEIGHT;
 static {
  var builder=new ModConfigSpec.Builder();builder.push("processingArray");
  WIDTH=builder.comment("Maximum outer width, including the shell. Existing larger arrays pause without losing contents.").defineInRange("maxWidth",32,5,HARD_MAX);
  DEPTH=builder.comment("Maximum outer depth, including the shell. 32 blocks is two chunks in length; chunk coverage depends on alignment.").defineInRange("maxDepth",32,5,HARD_MAX);
  HEIGHT=builder.comment("Maximum outer height; two-block rack levels plus a clear gantry space. Existing taller arrays pause without resizing.").defineInRange("maxHeight",17,5,33);
  builder.pop();SPEC=builder.build();
 }
 public static int maxWidth(){return WIDTH.get();}
 public static int maxHeight(){int h=HEIGHT.get();return h%2==0?h-1:h;}
 public static int maxDepth(){return DEPTH.get();}
 public static int nextSize(int current,int maximum,boolean increase){
  if(current>maximum)return maximum;
  return increase?(current>=maximum?5:Math.min(maximum,current+2)):(current<=5?maximum:Math.max(5,current-2));
 }
 private ArrayConfig(){}
}
