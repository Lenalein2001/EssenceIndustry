package com.neolena.essenceindustry.nuclear;

import java.util.LinkedHashMap;

/** Pure boiler accounting. Steam replaces direct FE; excess vapor is vented explicitly while fractional work is retained. */
public final class ReactorSteam {
 public static final int WORK_PER_MB=100,MAX_CREDIT=100_100,MAX_TANK=1_000_000_000;
 public record Result(ReactorCore.State core,int steam,int credit,ReactorCore.Status status,long burned,int produced,int vented){}
 public static int capacity(int segments){return (int)Math.min(MAX_TANK,Math.max(64_000L,segments*20_000L));}
 public static Result tick(ReactorCore.State original,ReactorCore.Rules rules,int segments,boolean formed,boolean wasteBlocked,int water,int steam,int credit){
  if(water<0||water>MAX_TANK||steam<0||steam>MAX_TANK||credit<0||credit>MAX_CREDIT)throw new IllegalArgumentException("Invalid steam ledger");
  int room=water;
  var rates=new LinkedHashMap<String,ReactorFuels.Rate>();rules.rates().forEach((id,r)->rates.put(id,new ReactorFuels.Rate(r.burnUnitsPerSegmentTick(),r.fePerMb(),0)));
  // An operating boiler transfers the fuel's heat to its vapor. Available water bounds
  // the burn before commit, so a dry boiler cannot consume fuel or make FE.
  // A single fixed-point fuel unit may yield more than 1 mB of steam. Retain
  // its excess work in the saved credit ledger instead of deadlocking a nearly full tank.
  long quantum=rates.values().stream().mapToLong(r->(r.fePerMb()+ReactorCore.UNITS_PER_MB-1)/ReactorCore.UNITS_PER_MB).max().orElse(1);
  var boilerRules=new ReactorCore.Rules(rules.fuelCapacityMb(),rules.wasteCapacityMb(),Math.max(1,room*(long)WORK_PER_MB+Math.max(WORK_PER_MB-1,quantum-1)-credit),rules.heatCapacityUnits(),rules.passiveCoolingUnitsPerSegment(),rates);
  var result=ReactorCore.tick(energy(original,0),boilerRules,segments,formed&&room>0&&credit<room*(long)WORK_PER_MB,wasteBlocked,0);
  var state=energy(result.state(),original.energy());var status=result.status();
  if(status==ReactorCore.Status.UNFORMED&&formed){status=!original.enabled()?ReactorCore.Status.OFF:original.rods()==100?ReactorCore.Status.RODS_INSERTED:water==0?ReactorCore.Status.COOLANT_MISSING:ReactorCore.Status.STEAM_FULL;}
  long earned=credit+result.generatedFE();int produced=formed?(int)Math.min(room,earned/WORK_PER_MB):0;
  if(produced>room)throw new IllegalStateException("Steam exceeded reserved water/headroom");
  return new Result(state,(int)Math.min(capacity(segments),(long)steam+produced),(int)(earned-produced*(long)WORK_PER_MB),status,result.burnedUnits(),produced,(int)Math.max(0,(long)steam+produced-capacity(segments)));
 }
 public static ReactorCore.State energy(ReactorCore.State s,long value){return new ReactorCore.State(s.fuelId(),s.fuelUnits(),s.wasteUnits(),value,s.energyRemainder(),s.heatUnits(),s.rateRemainder(),s.enabled(),s.rods(),s.tripped());}
 private ReactorSteam(){}
}
