package com.neolena.essenceindustry.array;

import appeng.api.crafting.*;
import appeng.api.stacks.*;
import appeng.recipes.handlers.*;
import java.util.*;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.fml.ModList;

/** Explicit adapters only; pattern outputs cannot authorize an invented transformation. */
public final class ArrayRecipes {
 public record Demand(Predicate<AEKey> test,long amount){public Demand{if(amount<1||amount>ArrayLedger.MAX_AMOUNT)throw new IllegalArgumentException();}}
 public record Native(String id,String pool,int ticks,long power,List<Demand> inputs,List<GenericStack> outputs,List<Demand> tools){}
 public record Match(ArrayLedger.Plan plan,int operations,Map<AEKey,Long> expected){}
 public static Demand item(Ingredient i,long n){return new Demand(k->k instanceof AEItemKey a&&i.test(a.toStack()),n);}
 public static List<GenericStack> output(ItemStack stack){return stack.isEmpty()?List.of():List.of(new GenericStack(AEItemKey.of(stack),stack.getCount()));}
 /** Ordinary item input deliberately starts with vanilla's single-input cooking types.
  * Use the native selector and actual component-bearing input, not a guessed output. */
 public static ArrayLedger.Plan cookingPlan(ServerLevel level,String pool,ItemStack input){
  return switch(cookingFamily(pool)){
   case "minecraft:furnace"->cookingPlan(level,pool,input,RecipeType.SMELTING);
   case "minecraft:blast_furnace"->cookingPlan(level,pool,input,RecipeType.BLASTING);
   case "minecraft:smoker"->cookingPlan(level,pool,input,RecipeType.SMOKING);
   default->null;
  };
 }
 private static <T extends AbstractCookingRecipe> ArrayLedger.Plan cookingPlan(ServerLevel level,String pool,ItemStack input,RecipeType<T> type){
  var original=AEItemKey.of(input.copyWithCount(1));var single=new SingleRecipeInput(input.copyWithCount(1));var holder=level.getServer().getRecipeManager().getRecipeFor(type,single,level).orElse(null);if(holder==null)return null;
  var recipe=holder.value();var result=recipe.assemble(single);if(result.isEmpty())return null;
  return new ArrayLedger.Plan(holder.id().identifier().toString(),pool,Math.max(1,recipe.cookingTime()),20,List.of(new GenericStack(original,1)),output(result),List.of());
 }
 public static boolean plain(ItemStack stack){return !stack.isEmpty()&&ItemStack.isSameItemSameComponents(stack,new ItemStack(stack.getItem()));}
 private static net.minecraft.world.level.block.Block block(String item){var value=BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(item));return value instanceof BlockItem bi?bi.getBlock():net.minecraft.world.level.block.Blocks.AIR;}
 public static String cookingFamily(String item){var block=block(item);if(block instanceof net.minecraft.world.level.block.BlastFurnaceBlock)return "minecraft:blast_furnace";if(block instanceof net.minecraft.world.level.block.SmokerBlock)return "minecraft:smoker";return block instanceof net.minecraft.world.level.block.FurnaceBlock?"minecraft:furnace":"";}
 private static boolean inscriber(String item){return block(item) instanceof appeng.block.misc.InscriberBlock;}
 public static boolean machine(ItemStack stack){
  if(!plain(stack)||!(stack.getItem() instanceof BlockItem))return false;String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
  if(!cookingFamily(id).isEmpty()||inscriber(id))return true;
  if(ModList.get().isLoaded("extendedae")&&ArrayExtendedRecipes.machine(stack))return true;
  if(ModList.get().isLoaded("advanced_ae")&&ArrayAdvancedRecipes.machine(stack))return true;
  if(ModList.get().isLoaded("oritech")&&ArrayOritechRecipes.machine(stack))return true;
  if(ModList.get().isLoaded("enderio")&&ArrayEnderRecipes.machine(stack))return true;
  return ModList.get().isLoaded("ftbic")&&ArrayIndustrialRecipes.machine(stack);
 }
 public static List<Native> catalog(ServerLevel level,Set<String> installed){
  if(installed.isEmpty())return List.of();
  if(ModList.get().isLoaded("ftbic"))ArrayIndustrialRecipes.prepare(installed);
  var result=new ArrayList<Native>();for(var holder:level.getServer().getRecipeManager().getRecipes()){
   var recipe=holder.value();String id=holder.id().identifier().toString();
   for(String machine:installed){if(recipe instanceof InscriberRecipe r&&inscriber(machine)){
    var inputs=new ArrayList<Demand>();var tools=new ArrayList<Demand>();inputs.add(item(r.getMiddleInput(),1));boolean retain=r.getProcessType()==InscriberProcessType.INSCRIBE;
    if(!retain){r.getTopOptional().ifPresent(i->inputs.add(item(i,1)));r.getBottomOptional().ifPresent(i->inputs.add(item(i,1)));}
    result.add(new Native(id,machine,200,20,inputs,output(r.result().create()),tools));
   }else if(recipe instanceof AbstractCookingRecipe r){
    String pool=r.getType()==RecipeType.SMELTING?"minecraft:furnace":r.getType()==RecipeType.BLASTING?"minecraft:blast_furnace":r.getType()==RecipeType.SMOKING?"minecraft:smoker":"";
    if(!pool.isEmpty()&&cookingFamily(machine).equals(pool))result.add(new Native(id,machine,Math.max(1,r.cookingTime()),20,List.of(item(r.input(),1)),output(r.assemble(new SingleRecipeInput(ItemStack.EMPTY))),List.of()));
   }
   }
   if(ModList.get().isLoaded("extendedae"))ArrayExtendedRecipes.add(holder,installed,result);
   if(ModList.get().isLoaded("advanced_ae"))ArrayAdvancedRecipes.add(id,recipe,installed,result);
   if(ModList.get().isLoaded("oritech"))ArrayOritechRecipes.add(id,recipe,installed,result);
   if(ModList.get().isLoaded("ftbic"))ArrayIndustrialRecipes.add(id,recipe,installed,result);
   if(ModList.get().isLoaded("enderio"))ArrayEnderRecipes.add(id,recipe,installed,result);
  }return List.copyOf(result);
 }
 public static Map<AEKey,Long> patternInputs(IPatternDetails pattern){
  var result=new LinkedHashMap<AEKey,Long>();if(pattern.getInputs().length<1||pattern.getInputs().length>32)return null;
  for(var input:pattern.getInputs()){
   var possible=input.getPossibleInputs();if(possible.length!=1||input.getMultiplier()<1)return null;
   var g=possible[0];if(g.amount()<1)return null;result.merge(g.what(),Math.multiplyExact(g.amount(),input.getMultiplier()),Math::addExact);
  }return result;
 }
 public static Match match(IPatternDetails pattern,List<Native> recipes,Map<AEKey,Long> tools){
  var inputs=patternInputs(pattern);if(inputs==null)return null;var wanted=ArrayLedger.map(pattern.getOutputs());Match found=null;
  for(var r:recipes){
   var outputs=ArrayLedger.map(r.outputs());if(outputs.isEmpty()||!outputs.keySet().equals(wanted.keySet()))continue;
   var first=outputs.entrySet().iterator().next();long quotient=wanted.get(first.getKey())/first.getValue();
   if(quotient<1||quotient>ArrayLedger.MAX_OPERATIONS||wanted.get(first.getKey())%first.getValue()!=0)continue;int n=(int)quotient;
   if(!ArrayLedger.multiply(r.outputs(),n).equals(wanted)||!allocate(r.inputs(),inputs,n,true))continue;
   var perInput=new LinkedHashMap<AEKey,Long>();boolean exact=true;for(var e:inputs.entrySet()){if(e.getValue()%n!=0){exact=false;break;}perInput.put(e.getKey(),e.getValue()/n);}if(!exact)continue;
   // Fix concrete retained-tool variants for this plan; no copying one press into multiple lanes.
   var selected=new LinkedHashMap<AEKey,Long>();var left=new HashMap<>(tools);boolean enough=true;
   for(var need:r.tools()){
    var key=left.keySet().stream().filter(k->need.test().test(k)&&left.get(k)>=need.amount()).findFirst().orElse(null);
    if(key==null){enough=false;break;}selected.merge(key,need.amount(),Math::addExact);left.merge(key,-need.amount(),Long::sum);
   }if(!enough)continue;
   var plan=new ArrayLedger.Plan(r.id(),r.pool(),r.ticks(),r.power(),ArrayLedger.list(perInput),r.outputs(),ArrayLedger.list(selected));
   var candidate=new Match(plan,n,Map.copyOf(inputs));
   // Both installed Inscribers and Crystal Assemblers can satisfy a processor pattern.
   // Prefer the complete native bulk recipe, then native cost and a stable pool/id tie-break.
   // Every candidate already consumed exactly the same packet and produced its exact outputs.
   if(found==null||compare(candidate,found)<0)found=candidate;
  }return found;
 }
 private static int compare(Match a,Match b){int c=Integer.compare(a.operations(),b.operations());if(c!=0)return c;long ac=Math.multiplyExact(Math.multiplyExact(a.plan().power(),a.plan().ticks()),a.operations());long bc=Math.multiplyExact(Math.multiplyExact(b.plan().power(),b.plan().ticks()),b.operations());c=Long.compare(ac,bc);if(c!=0)return c;return(a.plan().pool()+"/"+a.plan().recipe()).compareTo(b.plan().pool()+"/"+b.plan().recipe());}
 /** Capacitated bipartite allocation: shared tags never double-count the same input. */
 public static boolean allocate(List<Demand> needs,Map<AEKey,Long> stock,int factor,boolean exact){
  if(needs.size()>32||stock.size()>32||factor<1)return false;var keys=new ArrayList<>(stock.keySet());int nk=keys.size(),nd=needs.size(),sink=nk+nd+1,size=sink+1;long[][] flow=new long[size][size];long total=0,available=0;
  for(int k=0;k<nk;k++){long n=stock.get(keys.get(k));if(n<1||n>ArrayLedger.MAX_AMOUNT)return false;flow[0][k+1]=n;available=Math.addExact(available,n);}
  for(int d=0;d<nd;d++){long n=Math.multiplyExact(needs.get(d).amount(),factor);flow[nk+d+1][sink]=n;total=Math.addExact(total,n);for(int k=0;k<nk;k++)if(needs.get(d).test().test(keys.get(k)))flow[k+1][nk+d+1]=n;}
  if(exact&&available!=total)return false;long moved=0;
  while(moved<total){int[] previous=new int[size];Arrays.fill(previous,-1);previous[0]=0;var q=new ArrayDeque<Integer>();q.add(0);while(!q.isEmpty()&&previous[sink]<0){int u=q.remove();for(int v=1;v<size;v++)if(previous[v]<0&&flow[u][v]>0){previous[v]=u;q.add(v);}}
   if(previous[sink]<0)return false;long n=Long.MAX_VALUE;for(int v=sink;v!=0;v=previous[v])n=Math.min(n,flow[previous[v]][v]);for(int v=sink;v!=0;v=previous[v]){flow[previous[v]][v]-=n;flow[v][previous[v]]+=n;}moved+=n;
  }return true;
 }
 private ArrayRecipes(){}
}
