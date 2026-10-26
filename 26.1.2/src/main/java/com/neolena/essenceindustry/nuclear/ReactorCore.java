package com.neolena.essenceindustry.nuclear;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;

/** Pure, immutable reactor accounting. Controllers commit each candidate with their other transfers. */
public final class ReactorCore {
 public static final long UNITS_PER_MB=1_000_000L,UNITS_PER_SOLID=1000*UNITS_PER_MB,MAX_AMOUNT=1_000_000_000_000_000L;
 public static final int MAX_SEGMENTS=262_144;
 public enum Status { RUNNING,OFF,UNFORMED,NO_FUEL,RODS_INSERTED,WASTE_FULL,ENERGY_FULL,THERMAL_TRIP,CONFIG_BLOCKED,REDSTONE_PAUSED,RECOVERY_REQUIRED,COOLANT_MISSING,STEAM_FULL,COOLING_PATH_MISSING,LEGACY_COOLANT,WATER_INPUT_MISSING,STEAM_OUTPUT_MISSING,CONTROL_COVERAGE_MISSING,ACTUATOR_MISSING }
 public record Rules(long fuelCapacityMb,long wasteCapacityMb,long energyCapacity,long heatCapacityUnits,long passiveCoolingUnitsPerSegment,Map<String,ReactorFuels.Rate> rates){
  public Rules {
   rates=Map.copyOf(rates);
   if(fuelCapacityMb<1000||fuelCapacityMb>1_000_000_000||wasteCapacityMb<1000||wasteCapacityMb>1_000_000_000||energyCapacity<1||energyCapacity>MAX_AMOUNT||heatCapacityUnits<1||heatCapacityUnits>MAX_AMOUNT||passiveCoolingUnitsPerSegment<0||passiveCoolingUnitsPerSegment>1_000_000_000)throw new IllegalArgumentException("Invalid reactor limits");
   if(rates.size()!=ReactorFuels.ALL.size())throw new IllegalArgumentException("Every reactor fuel needs one rate");
   for(var fuel:ReactorFuels.ALL)if(!rates.containsKey(fuel.id()))throw new IllegalArgumentException("Missing reactor fuel rate: "+fuel.id());
  }
  public long fuelCapacityUnits(){return fuelCapacityMb*UNITS_PER_MB;}
  public long wasteCapacityUnits(){return wasteCapacityMb*UNITS_PER_MB;}
 }
 private record Stored(String fuelId,long fuelUnits,long wasteUnits,long energy,long energyRemainder,long heatUnits,int rateRemainder,boolean enabled,int rods,boolean tripped){}
 private static final Codec<Stored> STORED_CODEC=RecordCodecBuilder.create(i->i.group(
  Codec.STRING.fieldOf("fuelId").forGetter(Stored::fuelId),Codec.LONG.fieldOf("fuelUnits").forGetter(Stored::fuelUnits),Codec.LONG.fieldOf("wasteUnits").forGetter(Stored::wasteUnits),Codec.LONG.fieldOf("energy").forGetter(Stored::energy),Codec.LONG.fieldOf("energyRemainder").forGetter(Stored::energyRemainder),Codec.LONG.fieldOf("heatUnits").forGetter(Stored::heatUnits),Codec.INT.fieldOf("rateRemainder").forGetter(Stored::rateRemainder),Codec.BOOL.fieldOf("enabled").forGetter(Stored::enabled),Codec.INT.fieldOf("rods").forGetter(Stored::rods),Codec.BOOL.fieldOf("tripped").forGetter(Stored::tripped)).apply(i,Stored::new));
 public record State(String fuelId,long fuelUnits,long wasteUnits,long energy,long energyRemainder,long heatUnits,int rateRemainder,boolean enabled,int rods,boolean tripped){
  /** Invalid data returns an error. The controller must preserve the original payload for recovery. */
  public static final Codec<State> CODEC=STORED_CODEC.flatXmap(raw->{try{return DataResult.success(new State(raw.fuelId,raw.fuelUnits,raw.wasteUnits,raw.energy,raw.energyRemainder,raw.heatUnits,raw.rateRemainder,raw.enabled,raw.rods,raw.tripped));}catch(IllegalArgumentException e){return DataResult.error(()->"Invalid reactor ledger: "+e.getMessage());}},state->DataResult.success(new Stored(state.fuelId,state.fuelUnits,state.wasteUnits,state.energy,state.energyRemainder,state.heatUnits,state.rateRemainder,state.enabled,state.rods,state.tripped)));
  public State {
   Objects.requireNonNull(fuelId,"fuelId");
   if(fuelUnits<0||fuelUnits>MAX_AMOUNT||wasteUnits<0||wasteUnits>MAX_AMOUNT||energy<0||energy>MAX_AMOUNT||heatUnits<0||heatUnits>MAX_AMOUNT||energyRemainder<0||energyRemainder>=UNITS_PER_MB||rateRemainder<0||rateRemainder>=100||rods<0||rods>100)throw new IllegalArgumentException("Out-of-range stored amount or control");
   if(fuelUnits==0&&wasteUnits==0?!fuelId.isEmpty():!ReactorFuels.known(fuelId))throw new IllegalArgumentException("Fuel identity does not match retained material");
   if(tripped&&enabled)throw new IllegalArgumentException("Tripped reactor cannot be enabled");
  }
  public static State empty(){return new State("",0,0,0,0,0,0,false,50,false);}
  public double fuelMb(){return fuelUnits/(double)UNITS_PER_MB;}
  public double wasteMb(){return wasteUnits/(double)UNITS_PER_MB;}
  public double heat(){return heatUnits/(double)UNITS_PER_MB;}
 }
 public record WasteOffer(String itemId,int count){}
 public record Tick(State state,Status status,long burnedUnits,long generatedFE,long assistedCoolingUnits){public boolean working(){return status==Status.RUNNING;}}
 public static State controls(State s,boolean enabled,int rods){
  if(rods<0||rods>100)throw new IllegalArgumentException("Rod insertion must be 0-100 percent");
  return copy(s,s.fuelId,s.fuelUnits,s.wasteUnits,s.energy,s.energyRemainder,s.heatUnits,s.rateRemainder,enabled&&!s.tripped,rods,s.tripped);
 }
 /** Refusal returns null and never changes the caller's snapshot or item source. */
 public static State insertFuel(State s,ReactorFuels.Charge charge,int count,Rules rules){
  if(charge==null||count<1||!s.fuelId.isEmpty()&&!s.fuelId.equals(charge.fuelId())||s.fuelUnits>rules.fuelCapacityUnits())return null;
  if(count>(rules.fuelCapacityUnits()-s.fuelUnits)/charge.units())return null;
  return copy(s,charge.fuelId(),s.fuelUnits+charge.units()*count,s.wasteUnits,s.energy,s.energyRemainder,s.heatUnits,s.rateRemainder,s.enabled,s.rods,s.tripped);
 }
 public static WasteOffer wasteOffer(State s){return new WasteOffer(s.wasteUnits==0?"":ReactorFuels.definition(s.fuelId).spentItem(),(int)Math.min(Integer.MAX_VALUE,s.wasteUnits/UNITS_PER_SOLID));}
 public static State ejectWaste(State s,int count){
  if(count<1||count>s.wasteUnits/UNITS_PER_SOLID)return null;long waste=s.wasteUnits-count*UNITS_PER_SOLID;
  return copy(s,s.fuelUnits==0&&waste==0?"":s.fuelId,s.fuelUnits,waste,s.energy,s.energyRemainder,s.heatUnits,s.rateRemainder,s.enabled,s.rods,s.tripped);
 }
 public static State extractEnergy(State s,long amount){
  if(amount<=0||amount>s.energy)return null;return copy(s,s.fuelId,s.fuelUnits,s.wasteUnits,s.energy-amount,s.energyRemainder,s.heatUnits,s.rateRemainder,s.enabled,s.rods,s.tripped);
 }
 /** A reset never starts the reactor. Cooling, then Reset and Start, is the default safe recovery. */
 public static State resetTrip(State s,Rules rules){
  if(!s.tripped)return s;if(s.heatUnits>rules.heatCapacityUnits()*3/4)return null;
  return copy(s,s.fuelId,s.fuelUnits,s.wasteUnits,s.energy,s.energyRemainder,s.heatUnits,s.rateRemainder,false,s.rods,false);
 }
 public static Tick tick(State s,Rules rules,int fuelSegments,boolean formed,long extraCoolingUnits){return tick(s,rules,fuelSegments,formed,false,extraCoolingUnits);}
 /** External waste blockage and coolant reservations are supplied by the controller's atomic transaction. */
 public static Tick tick(State s,Rules rules,int fuelSegments,boolean formed,boolean wasteOutputBlocked,long extraCoolingUnits){
  if(fuelSegments<0||fuelSegments>MAX_SEGMENTS||extraCoolingUnits<0||extraCoolingUnits>MAX_AMOUNT)throw new IllegalArgumentException("Invalid reactor tick inputs");
  long passive=Math.min(s.heatUnits,rules.passiveCoolingUnitsPerSegment()*fuelSegments);
  long assisted=Math.min(s.heatUnits-passive,extraCoolingUnits),heat=s.heatUnits-passive-assisted;
  var cooled=copy(s,s.fuelId,s.fuelUnits,s.wasteUnits,s.energy,s.energyRemainder,heat,s.rateRemainder,s.enabled,s.rods,s.tripped);
  if(s.tripped||s.heatUnits>=rules.heatCapacityUnits())return stopped(trip(cooled),Status.THERMAL_TRIP,assisted);
  if(!formed||fuelSegments==0)return stopped(cooled,Status.UNFORMED,assisted);
  if(s.fuelUnits>rules.fuelCapacityUnits()||s.wasteUnits>rules.wasteCapacityUnits()||s.energy>rules.energyCapacity())return stopped(cooled,Status.CONFIG_BLOCKED,assisted);
  if(!s.enabled)return stopped(cooled,Status.OFF,assisted);
  if(s.rods==100)return stopped(cooled,Status.RODS_INSERTED,assisted);
  if(s.fuelUnits==0)return stopped(cooled,Status.NO_FUEL,assisted);
  if(wasteOutputBlocked||s.wasteUnits==rules.wasteCapacityUnits())return stopped(cooled,Status.WASTE_FULL,assisted);
  if(s.energy==rules.energyCapacity())return stopped(cooled,Status.ENERGY_FULL,assisted);
  var rate=rules.rates().get(s.fuelId);
  long requested=rate.burnUnitsPerSegmentTick()*fuelSegments*(100-s.rods)+s.rateRemainder;
  // Finish a partial solid at the tank boundary. Refusing this smaller burn would strand
  // less than 1000mB forever when the normal tick quantity does not divide the capacity.
  long burn=Math.min(Math.min(s.fuelUnits,requested/100),rules.wasteCapacityUnits()-s.wasteUnits);int remainder=(int)(requested%100);
  // Divide before multiplying so even maximum configured rates stay within signed long arithmetic.
  long fractional=(burn%UNITS_PER_MB)*rate.fePerMb()+s.energyRemainder;
  long generated=(burn/UNITS_PER_MB)*rate.fePerMb()+fractional/UNITS_PER_MB;
  long headroom=rules.energyCapacity()-s.energy;
  if(generated>headroom){
   // Largest fixed-point burn whose earned whole FE fit. Decomposition avoids headroom*1e6
   // overflowing, while this branch guarantees the quotient is below the requested burn.
   burn=(headroom/rate.fePerMb())*UNITS_PER_MB
       +((headroom%rate.fePerMb())*UNITS_PER_MB+UNITS_PER_MB-1-s.energyRemainder)/rate.fePerMb();
   if(burn==0)return stopped(cooled,Status.ENERGY_FULL,assisted);
   fractional=(burn%UNITS_PER_MB)*rate.fePerMb()+s.energyRemainder;
   generated=(burn/UNITS_PER_MB)*rate.fePerMb()+fractional/UNITS_PER_MB;
  }
  long addedHeat=burn*rate.heatPerMb();
  if(addedHeat>rules.heatCapacityUnits()-heat)return stopped(trip(cooled),Status.THERMAL_TRIP,assisted);
  var next=copy(s,s.fuelId,s.fuelUnits-burn,s.wasteUnits+burn,s.energy+generated,fractional%UNITS_PER_MB,heat+addedHeat,remainder,s.enabled,s.rods,false);
  return new Tick(next,Status.RUNNING,burn,generated,assisted);
 }
 private static Tick stopped(State s,Status status,long assisted){return new Tick(s,status,0,0,assisted);}
 private static State trip(State s){return copy(s,s.fuelId,s.fuelUnits,s.wasteUnits,s.energy,s.energyRemainder,s.heatUnits,s.rateRemainder,false,s.rods,true);}
 private static State copy(State old,String id,long fuel,long waste,long energy,long energyRemainder,long heat,int rateRemainder,boolean enabled,int rods,boolean tripped){
  if(old.fuelId.equals(id)&&old.fuelUnits==fuel&&old.wasteUnits==waste&&old.energy==energy&&old.energyRemainder==energyRemainder&&old.heatUnits==heat&&old.rateRemainder==rateRemainder&&old.enabled==enabled&&old.rods==rods&&old.tripped==tripped)return old;
  return new State(id,fuel,waste,energy,energyRemainder,heat,rateRemainder,enabled,rods,tripped);
 }
 private ReactorCore(){}
}
