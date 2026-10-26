package com.neolena.essenceindustry.array;

import appeng.api.stacks.*;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import java.util.function.LongPredicate;

/** Immutable job/accounting snapshots. No native machine entities or per-item loops. */
public final class ArrayLedger {
 public static final int MAX_JOBS=64,MAX_TYPES=128,MAX_OPERATIONS=16384,MAX_LANES=1024;
 public static final long MAX_AMOUNT=1_000_000_000L;
 public record Plan(String recipe,String pool,int ticks,long power,List<GenericStack> inputs,List<GenericStack> outputs,List<GenericStack> tools){
  public static final Codec<Plan> CODEC=RecordCodecBuilder.create(i->i.group(Codec.STRING.fieldOf("recipe").forGetter(Plan::recipe),Codec.STRING.fieldOf("pool").forGetter(Plan::pool),Codec.intRange(1,1_000_000).fieldOf("ticks").forGetter(Plan::ticks),Codec.LONG.fieldOf("power").forGetter(Plan::power),GenericStack.CODEC.listOf(1,32).fieldOf("inputs").forGetter(Plan::inputs),GenericStack.CODEC.listOf(1,32).fieldOf("outputs").forGetter(Plan::outputs),GenericStack.CODEC.listOf(0,4).fieldOf("tools").forGetter(Plan::tools)).apply(i,Plan::new));
  public Plan{recipe=com.neolena.essenceindustry.migration.LegacyCompatibility.canonicalId(recipe);pool=com.neolena.essenceindustry.migration.LegacyCompatibility.canonicalId(pool);inputs=List.copyOf(inputs);outputs=List.copyOf(outputs);tools=List.copyOf(tools);if(recipe.length()>256||pool.length()>256||ticks<1||ticks>1_000_000||power<1||power>100_000_000)throw new IllegalArgumentException("Invalid plan");valid(inputs);valid(outputs);valid(tools);}
 }
 public record Job(long id,Plan plan,int remaining,int active,int progress){
  public static final Codec<Job> CODEC=RecordCodecBuilder.create(i->i.group(Codec.LONG.fieldOf("id").forGetter(Job::id),Plan.CODEC.fieldOf("plan").forGetter(Job::plan),Codec.intRange(1,MAX_OPERATIONS).fieldOf("remaining").forGetter(Job::remaining),Codec.intRange(0,MAX_LANES).fieldOf("active").forGetter(Job::active),Codec.intRange(0,1_000_000).fieldOf("progress").forGetter(Job::progress)).apply(i,Job::new));
  public Job{if(id<1||remaining<1||remaining>MAX_OPERATIONS||active<0||active>remaining||active>MAX_LANES||progress<0||progress>=plan.ticks()||(active==0&&progress!=0))throw new IllegalArgumentException("Invalid job");}
 }
 public record State(long next,List<Job> jobs,List<GenericStack> output,boolean locked){
  public static final Codec<State> CODEC=RecordCodecBuilder.create(i->i.group(Codec.LONG.fieldOf("next").forGetter(State::next),Job.CODEC.listOf(0,MAX_JOBS).fieldOf("jobs").forGetter(State::jobs),GenericStack.CODEC.listOf(0,MAX_TYPES).fieldOf("output").forGetter(State::output),Codec.BOOL.fieldOf("locked").forGetter(State::locked)).apply(i,State::new));
  public State{jobs=List.copyOf(jobs);output=List.copyOf(output);if(next<1||jobs.size()>MAX_JOBS)throw new IllegalArgumentException("Invalid ledger");valid(output);var ids=new HashSet<Long>();for(var j:jobs)if(!ids.add(j.id())||j.id()>=next)throw new IllegalArgumentException("Invalid job identity");if(!fits(jobs,output))throw new IllegalArgumentException("Ledger exceeds reservation limits");}
  public static State empty(){return new State(1,List.of(),List.of(),false);}
 }
 private static void valid(List<GenericStack> values){for(var s:values)if(s==null||s.what()==null||s.amount()<=0||s.amount()>MAX_AMOUNT||(!(s.what() instanceof AEItemKey)&&!(s.what() instanceof AEFluidKey)))throw new IllegalArgumentException("Invalid resource");}
 public static Map<AEKey,Long> map(List<GenericStack> values){var out=new LinkedHashMap<AEKey,Long>();for(var s:values)out.merge(s.what(),s.amount(),Math::addExact);return out;}
 public static List<GenericStack> list(Map<AEKey,Long> values){return values.entrySet().stream().filter(e->e.getValue()>0).map(e->new GenericStack(e.getKey(),e.getValue())).toList();}
 public static Map<AEKey,Long> multiply(List<GenericStack> values,int count){var out=new LinkedHashMap<AEKey,Long>();for(var s:values)out.merge(s.what(),Math.multiplyExact(s.amount(),count),Math::addExact);return out;}
 private static void add(Map<AEKey,Long> into,Map<AEKey,Long> values){values.forEach((k,n)->into.merge(k,n,Math::addExact));}
 /** Reserve BOTH possible refunds and results, including every variant, before accepting input. */
 public static boolean fits(List<Job> jobs,List<GenericStack> output){try{var reserved=map(output);for(var j:jobs){add(reserved,multiply(j.plan().inputs(),j.remaining()));add(reserved,multiply(j.plan().outputs(),j.remaining()));}return reserved.size()<=MAX_TYPES&&reserved.values().stream().allMatch(n->n>0&&n<=MAX_AMOUNT);}catch(ArithmeticException e){return false;}}
 public static State admit(State before,Plan plan,int operations){
  if(before.locked()||operations<1||operations>MAX_OPERATIONS||before.next()==Long.MAX_VALUE)return null;
  var jobs=new ArrayList<>(before.jobs());boolean merged=false;
  for(int i=0;i<jobs.size();i++){var j=jobs.get(i);if(j.active()==0&&j.plan().equals(plan)&&j.remaining()<=MAX_OPERATIONS-operations){jobs.set(i,new Job(j.id(),plan,j.remaining()+operations,0,0));merged=true;break;}}
  if(!merged){if(jobs.size()>=MAX_JOBS)return null;jobs.add(new Job(before.next(),plan,operations,0,0));}if(!fits(jobs,before.output()))return null;
  return new State(before.next()+1,jobs,before.output(),false);
 }
 /** Existing waves keep their lanes; new waves start only with complete compatible tools. */
 public static State dispatch(State state,Map<String,Integer> capacity,Map<AEKey,Long> tools){
  if(state.locked())return state;var free=new HashMap<>(capacity);var available=new HashMap<>(tools);for(var j:state.jobs())if(j.active()>0){free.merge(j.plan().pool(),-j.active(),Integer::sum);for(var t:j.plan().tools())available.merge(t.what(),-Math.multiplyExact(t.amount(),j.active()),Long::sum);}
  var jobs=new ArrayList<Job>();for(var j:state.jobs()){
   int n=j.active();if(n==0){n=Math.max(0,Math.min(j.remaining(),free.getOrDefault(j.plan().pool(),0)));for(var t:j.plan().tools())n=(int)Math.min(n,Math.max(0,available.getOrDefault(t.what(),0L)/t.amount()));if(n>0){free.merge(j.plan().pool(),-n,Integer::sum);for(var t:j.plan().tools())available.merge(t.what(),-Math.multiplyExact(t.amount(),n),Long::sum);}}
   jobs.add(n==j.active()?j:new Job(j.id(),j.plan(),j.remaining(),n,0));
  }return jobs.equals(state.jobs())?state:new State(state.next(),jobs,state.output(),false);
 }
 public static long demand(State s,int speed){long cost=0;for(var j:s.jobs())if(j.active()>0)cost=Math.addExact(cost,Math.multiplyExact(Math.multiplyExact(j.plan().power(),j.active()),(long)speed*speed));return cost;}
 public static State advance(State state,int speed,LongPredicate pay){
  if(state.locked()||speed<1||speed>ArrayUpgrades.MAX_SPEED)return state;long cost=demand(state,speed);if(cost==0||!pay.test(cost))return state;
  var jobs=new ArrayList<Job>();var nextWaves=new ArrayList<Job>();var out=map(state.output());for(var j:state.jobs()){
   if(j.active()==0){jobs.add(j);continue;}int progress=j.progress()+speed;
   if(progress<j.plan().ticks())jobs.add(new Job(j.id(),j.plan(),j.remaining(),j.active(),progress));
   else{add(out,multiply(j.plan().outputs(),j.active()));int left=j.remaining()-j.active();if(left>0)nextWaves.add(new Job(j.id(),j.plan(),left,0,0));}
  }jobs.addAll(nextWaves);return new State(state.next(),jobs,list(out),false);
 }
 /** Recover only remaining input operations. Completed outputs are retained exactly once. */
 public static State refund(State state){if(state.locked())return state;var output=map(state.output());for(var j:state.jobs())add(output,multiply(j.plan().inputs(),j.remaining()));return new State(state.next(),List.of(),list(output),false);}
 public static State extract(State s,AEKey key,long amount){var output=map(s.output());long count=output.getOrDefault(key,0L);if(amount<=0||amount>count)throw new IllegalArgumentException("Invalid extraction");if(amount==count)output.remove(key);else output.put(key,count-amount);return new State(s.next(),s.jobs(),list(output),s.locked());}
 private ArrayLedger(){}
}
