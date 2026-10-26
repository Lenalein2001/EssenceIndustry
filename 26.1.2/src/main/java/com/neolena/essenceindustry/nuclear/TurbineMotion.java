package com.neolena.essenceindustry.nuclear;

/** Client presentation only. Physical RPM and FE accounting remain in TurbineCore.
 * ER 1.21 renders RPM / 10 and accumulates a client-local angle each frame.
 * Our four-paddle rotor uses RPM / 20 to remain readable down to 20 FPS.
 * We use a monotonic clock and an exact easing integral, so packets never reset phase.
 */
public final class TurbineMotion {
 public static final double VISUAL_SCALE=.05,SMOOTH_SECONDS=.15;
 private long previous=Long.MIN_VALUE;private double angle,speed;
 public double angle(){return angle;}
 public double sample(long nanos,double measuredRpm,boolean paused){
  double target=Double.isFinite(measuredRpm)?Math.clamp(measuredRpm,0,100000)*VISUAL_SCALE:0;
  if(previous==Long.MIN_VALUE){previous=nanos;speed=target;return angle;}
  double dt=(nanos-previous)/1e9;previous=nanos;
  // Do not catch up invisible time after a pause, cull or long stalled frame.
  if(paused||dt<=0||dt>.5)return angle;
  double decay=Math.exp(-dt/SMOOTH_SECONDS);
  double turnsPerMinuteSeconds=target*dt+(speed-target)*SMOOTH_SECONDS*(1-decay);
  angle=(angle+6*turnsPerMinuteSeconds)%360;
  speed=target+(speed-target)*decay;if(speed<1e-7)speed=0;
  return angle;
 }
}
