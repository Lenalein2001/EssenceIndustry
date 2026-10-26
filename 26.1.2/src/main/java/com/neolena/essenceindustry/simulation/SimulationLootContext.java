package com.neolena.essenceindustry.simulation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
/** Scoped virtual crop inputs. Never writes blocks, chunks or crop attachments. */
public final class SimulationLootContext {
 private static final ThreadLocal<Context> ACTIVE=new ThreadLocal<>();
 private static final class Context {final ItemStack seed;final Level level;final BlockPos pos;final BlockState soil,crux;boolean read;Context(ItemStack s,Level l,BlockPos p,BlockState f,BlockState c){seed=s;level=l;pos=p;soil=f;crux=c;}}
 public static void begin(ItemStack seed,Level level,BlockPos pos,BlockState soil,BlockState crux){if(ACTIVE.get()!=null)throw new IllegalStateException("Nested crop analysis");ACTIVE.set(new Context(seed,level,pos,soil,crux));}
 public static ItemStack seed(Level level,BlockPos pos){var c=ACTIVE.get();return c!=null && c.level==level && c.pos.equals(pos)?c.seed:null;}
 public static BlockState block(Level level,BlockPos pos){var c=ACTIVE.get();if(c==null || c.level!=level)return null;return pos.equals(c.pos.below())?c.soil:pos.equals(c.pos.below(2))?c.crux:null;}
 public static void handled(){var c=ACTIVE.get();if(c!=null)c.read=true;}
 public static boolean genesHandled(){var c=ACTIVE.get();return c!=null && c.read;}
 public static void end(){ACTIVE.remove();}
 private SimulationLootContext(){}
}
