package com.neolena.essenceindustry.nuclear;

import java.util.*;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-authoritative game balance. Capacity reductions retain contents and pause processing. */
public final class ReactorConfig {
 public static final ModConfigSpec SPEC;
 public static final int HARD_MAX_SIZE=64;
 private static final ModConfigSpec.IntValue WIDTH,DEPTH,HEIGHT,REVISION;
 public static final long DEFAULT_ENERGY=1_000_000_000L,DEFAULT_HEAT=2_777_800_000_000L,DEFAULT_COOLING=16_666_800L;
 private static final ModConfigSpec.DoubleValue OUTPUT_MULTIPLIER,EFFICIENCY_MULTIPLIER;
 private static final ModConfigSpec.LongValue FUEL,WASTE,ENERGY,HEAT,COOLING;
 private record RateValues(ModConfigSpec.LongValue burn,ModConfigSpec.LongValue fe,ModConfigSpec.LongValue heat){}
 private static final Map<String,RateValues> RATES=new LinkedHashMap<>();
 static {
  var b=new ModConfigSpec.Builder();b.push("reactor");
  REVISION=b.comment("Balance migration revision. Only untouched earlier default presets are upgraded; custom fuel presets are preserved.").defineInRange("balanceRevision",1,1,3);
  OUTPUT_MULTIPLIER=b.comment("Reactor FE and steam output multiplier. Does not increase fuel consumption. 2 = twice the output. Turbine coil ratings are unchanged.").defineInRange("energyProductionMultiplier",1d,.25d,8d);
  EFFICIENCY_MULTIPLIER=b.comment("Fuel lifetime multiplier relative to the new 32x-efficient defaults. 2 = half the fuel consumption at the same output. Heat per tick is preserved. Fixed-point rounding and per-fuel safety limits apply.").defineInRange("fuelEfficiencyMultiplier",1d,.25d,8d);
  WIDTH=b.comment("Maximum outer width. Existing larger structures pause without losing stored contents.").defineInRange("maxWidth",32,5,HARD_MAX_SIZE);
  DEPTH=b.comment("Maximum outer depth, including the shell.").defineInRange("maxDepth",32,5,HARD_MAX_SIZE);
  HEIGHT=b.comment("Maximum outer height, including base and roof.").defineInRange("maxHeight",48,5,HARD_MAX_SIZE);
  FUEL=b.comment("Liquid-equivalent fuel capacity in mB. One tagged uranium ingot is 1000 mB; one tagged storage block is 9000 mB.").defineInRange("fuelCapacityMb",64_000L,1000L,1_000_000_000L);
  WASTE=b.comment("Spent-fuel capacity in mB; each 1000 mB ejects as one matching solid, retaining the remainder.").defineInRange("wasteCapacityMb",64_000L,1000L,1_000_000_000L);
  ENERGY=b.comment("Internal FE capacity. A full buffer pauses fuel consumption.").defineInRange("energyCapacity",DEFAULT_ENERGY,1L,ReactorCore.MAX_AMOUNT);
  HEAT=b.comment("Safe thermal-trip threshold in fixed-point heat units; 1000000 units = one displayed heat unit. Trips require cooling and manual reset; no explosions.").defineInRange("heatCapacityUnits",DEFAULT_HEAT,1L,ReactorCore.MAX_AMOUNT);
  COOLING=b.comment("Passive heat removal per installed fuel segment per tick, in fixed-point heat units. Basic uranium can operate passively at the default rate.").defineInRange("passiveCoolingUnitsPerSegment",DEFAULT_COOLING,0L,1_000_000_000L);
  b.push("fuels");
  for(var fuel:ReactorFuels.ALL){b.push(fuel.id());var rate=fuel.defaults();
   var burn=b.comment("Fuel consumption per fuel segment per tick at 0% rod insertion. 1000000 fixed-point units = 1 mB.").defineInRange("burnUnitsPerSegmentTick",rate.burnUnitsPerSegmentTick(),1L,1_000_000L);
   var fe=b.comment("FE generated for each mB actually consumed; fractional FE is retained.").defineInRange("fePerMb",rate.fePerMb(),1L,1_000_000_000L);
   var heat=b.comment("Displayed heat added per mB actually consumed.").defineInRange("heatPerMb",rate.heatPerMb(),0L,1_000_000L);
   RATES.put(fuel.id(),new RateValues(burn,fe,heat));b.pop();
  }b.pop();b.pop();SPEC=b.build();
 }
 public static ReactorCore.Rules rules(){
  var rates=new LinkedHashMap<String,ReactorFuels.Rate>();RATES.forEach((id,values)->rates.put(id,scaledRate(new ReactorFuels.Rate(values.burn.get(),values.fe.get(),values.heat.get()),OUTPUT_MULTIPLIER.get(),EFFICIENCY_MULTIPLIER.get())));
  return new ReactorCore.Rules(FUEL.get(),WASTE.get(),ENERGY.get(),HEAT.get(),COOLING.get(),rates);
 }
 public static ReactorFuels.Rate scaledRate(ReactorFuels.Rate rate,double output,double efficiency){
  if(!Double.isFinite(output)||!Double.isFinite(efficiency)||output<.25||output>8||efficiency<.25||efficiency>8)throw new IllegalArgumentException("Invalid reactor multipliers");
  long burn=Math.clamp(Math.round(rate.burnUnitsPerSegmentTick()/efficiency),1,1_000_000);
  double ratio=rate.burnUnitsPerSegmentTick()/(double)burn;
  return new ReactorFuels.Rate(burn,Math.clamp(Math.round(rate.fePerMb()*ratio*output),1,100_000_000_000L),Math.clamp(Math.round(rate.heatPerMb()*ratio),0,1_000_000));
 }
 public static int maxWidth(){return WIDTH.get();}
 public static int maxDepth(){return DEPTH.get();}
 public static int maxHeight(){return HEIGHT.get();}
 public static ReactorCore.Rules defaults(){return new ReactorCore.Rules(64_000,64_000,DEFAULT_ENERGY,DEFAULT_HEAT,DEFAULT_COOLING,ReactorFuels.defaults());}
 public static boolean legacyPreset(int index,long burn,long fe,long heat){long[][] old={{1000,100000,500},{1000,400000,900},{1250,800000,1600},{900,600000,1000},{600,150000,300},{1500,1600000,2400}};return index>=0&&index<old.length&&Arrays.equals(old[index],new long[]{burn,fe,heat});}
 public static boolean betaPreset(int index,long burn,long fe,long heat){long[][] old={{27778,600000,500},{27778,2400000,900},{34722,4800000,1600},{25000,3600000,1000},{16667,900000,300},{41667,9600000,2400}};return index>=0&&index<old.length&&Arrays.equals(old[index],new long[]{burn,fe,heat});}
 public static void register(net.neoforged.bus.api.IEventBus bus){bus.addListener((net.neoforged.fml.event.config.ModConfigEvent.Loading e)->migrate(e.getConfig()));bus.addListener((net.neoforged.fml.event.config.ModConfigEvent.Reloading e)->migrate(e.getConfig()));}
 private static void migrate(net.neoforged.fml.config.ModConfig config){if(config.getSpec()!=SPEC||REVISION.get()>=3)return;
  int index=0;for(var fuel:ReactorFuels.ALL){var r=RATES.get(fuel.id());if((REVISION.get()==1&&legacyPreset(index,r.burn.get(),r.fe.get(),r.heat.get()))||betaPreset(index,r.burn.get(),r.fe.get(),r.heat.get())){r.burn.set(fuel.defaults().burnUnitsPerSegmentTick());r.fe.set(fuel.defaults().fePerMb());r.heat.set(fuel.defaults().heatPerMb());}index++;}
  if(REVISION.get()==1&&ENERGY.get()==100_000_000L)ENERGY.set(DEFAULT_ENERGY);if(REVISION.get()==1&&HEAT.get()==100_000_000_000L)HEAT.set(DEFAULT_HEAT);if(REVISION.get()==1&&COOLING.get()==600_000L)COOLING.set(DEFAULT_COOLING);
  REVISION.set(3);SPEC.save();org.slf4j.LoggerFactory.getLogger(ReactorConfig.class).info("Applied reactor balance revision 3 (32x fuel efficiency) to earlier default presets; customized fuel presets retained");
 }
 private ReactorConfig(){}
}
