package com.neolena.essenceindustry.weather;
import net.minecraft.util.StringRepresentable;
/** Stable serialized names: saved blocks and dropped items keep their selection. */
public enum CelestialMode implements StringRepresentable {
 SUNRISE(0), NOON(6000), SUNSET(12000), NIGHT(13000), MIDNIGHT(18000), DAWN(22500), CLEAR(-1), RAIN(-1), THUNDER(-1);
 private final int time;
 CelestialMode(int time){this.time=time;}
 public int time(){return time;}
 @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
}
