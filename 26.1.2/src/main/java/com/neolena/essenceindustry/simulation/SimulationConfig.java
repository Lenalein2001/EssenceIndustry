package com.neolena.essenceindustry.simulation;
import net.neoforged.neoforge.common.ModConfigSpec;
/** Server-owned production rules. Per-crop values use registered block IDs, not an item whitelist. */
public final class SimulationConfig {
 public static final ModConfigSpec SPEC;private static final ModConfigSpec.IntValue TICKS,SPEED,YIELD;private static final ModConfigSpec.ConfigValue<String> OVERRIDES;
 private static String last="";private static java.util.Map<String,Integer> cycles=java.util.Map.of();
 static{var b=new ModConfigSpec.Builder();b.push("cropSimulator");TICKS=b.comment("Base virtual growth cycle in game ticks, before seed genetics and speed upgrades. Default 8: 2.5 one-item harvests per second at 20 TPS, delivered in 40-tick batches.").defineInRange("cycleTicks",8,1,1000000);SPEED=b.comment("Multiplicative speed factor per installed speed upgrade. Up to eight upgrades.").defineInRange("speedFactorPerUpgrade",2,1,4);YIELD=b.comment("Multiplicative output factor per yield upgrade. Up to eight upgrades.").defineInRange("yieldFactorPerUpgrade",2,1,4);OVERRIDES=b.comment("Optional per-crop cycles: minecraft:wheat=8;mysticalagriculture:iron_crop=16").define("cycleOverrides","");b.pop();SPEC=b.build();}
 public static int cycle(PlantProfile profile){String value=OVERRIDES.get();if(!value.equals(last)){var next=new java.util.HashMap<String,Integer>();for(var entry:value.split(";")){var pair=entry.trim().split("=");try{if(pair.length==2){int ticks=Integer.parseInt(pair[1]);if(ticks>=1 && ticks<=1000000)next.put(pair[0],ticks);}}catch(NumberFormatException ignored){}}cycles=java.util.Map.copyOf(next);last=value;}return cycles.getOrDefault(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(profile.block()).toString(),TICKS.get());}
 public static int speed(int count){return (int)Math.pow(SPEED.get(),count);}public static int yield(int count){return (int)Math.pow(YIELD.get(),count);}
 private SimulationConfig(){}
}
