package com.neolena.essenceindustry.nuclear;

/** Steam work becomes stored rotor energy, then electricity. No energy is created by RPM itself. */
public final class TurbineCore {
 public static final int DESIGN_RPM=1800,MAX_RPM=2700,TANK=16_000_000;
 public static final long CAPACITY=4_000_000_000L;
 public static final String[] COILS={"copper","uranium","enriched","mox","plutonium","advanced"};
 public static final long[] COIL_FE={1000,4000,16000,64000,256000,2_000_000};
 public record State(double kinetic,long energy,int steam,int water) {
  public static final com.mojang.serialization.Codec<State> CODEC=com.mojang.serialization.codecs.RecordCodecBuilder.create(i->i.group(com.mojang.serialization.Codec.doubleRange(0,1e15).fieldOf("kinetic").forGetter(State::kinetic),com.mojang.serialization.Codec.LONG.validate(n->n>=0&&n<=CAPACITY?com.mojang.serialization.DataResult.success(n):com.mojang.serialization.DataResult.error(()->"Invalid turbine energy")).fieldOf("energy").forGetter(State::energy),com.mojang.serialization.Codec.intRange(0,TANK).fieldOf("steam").forGetter(State::steam),com.mojang.serialization.Codec.intRange(0,TANK).fieldOf("water").forGetter(State::water)).apply(i,State::new));
  public State {if(!Double.isFinite(kinetic)||kinetic<0||energy<0||energy>CAPACITY||steam<0||steam>TANK||water<0||water>TANK)throw new IllegalArgumentException("Invalid turbine ledger");}
  public static State empty(){return new State(0,0,0,0);}
 }
 public record Rules(int blades,int width,int coils,long rating){
  public Rules {if(blades<1||blades>8||(width!=5&&width!=7)||coils<8||coils>32||rating<0||rating>64_000_000)throw new IllegalArgumentException("Invalid rotor");}
  public double inertia(){double r=(width-3)/2d;return Math.max(.03*(blades*8*r*r+coils*.5)+rating*120d/(DESIGN_RPM*DESIGN_RPM),flow()*(double)ReactorSteam.WORK_PER_MB*600/(DESIGN_RPM*DESIGN_RPM));}
  public int flow(){return blades*(width==5?32000:128000);}
 }
 public record Result(State state,double rpm,long generated,int consumed,double loss){}
 public static double rpm(State s,Rules r){return Math.sqrt(s.kinetic/r.inertia());}
 public static Result tick(State s,Rules r,boolean formed,boolean enabled,boolean engaged,int target,int throttle){
  double k=s.kinetic;int consumed=0;long generated=0;
  if(formed&&enabled){double wanted=r.inertia()*Math.pow(Math.clamp(target,300,MAX_RPM),2);long work=(long)Math.max(0,wanted-k);consumed=(int)Math.min(s.steam,Math.min(r.flow()*(long)Math.clamp(throttle,0,100)/100,work/ReactorSteam.WORK_PER_MB));k+=consumed*(double)ReactorSteam.WORK_PER_MB;}
  double loss=Math.min(k,Math.max(.05,k*.000002));k-=loss;
  // Generation removes the same amount of mechanical energy. Coasting therefore cannot make free FE.
  if(formed&&engaged){double speed=Math.sqrt(k/r.inertia());long offer=(long)Math.min(k,r.rating*Math.min(speed/DESIGN_RPM,1.5));generated=Math.max(0,Math.min(offer,CAPACITY-s.energy));k-=generated;}
  if(k<.01)k=0;
  var next=new State(k,s.energy+generated,s.steam-consumed,Math.min(TANK,s.water+consumed));return new Result(next,rpm(next,r),generated,consumed,loss);
 }
 /** Degrees per game tick; 60 RPM is exactly one revolution per second at 20 TPS. */
 public static double degreesPerTick(double rpm){return rpm*.3;}
 private TurbineCore(){}
}
