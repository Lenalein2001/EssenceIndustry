package com.neolena.essenceindustry.blueprint;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
/** Layout settings are saved on the planner, independently of supplied materials. */
public record BlueprintSettings(int kind,int width,int depth,int height,int pattern,int glass,int filler,int steam,int hatches,int coil,int blades,int rings) {
 public static final BlueprintSettings DEFAULT=new BlueprintSettings(0,5,5,5,0,1,0,0,1,0,2,2);
 public static final Codec<BlueprintSettings> CODEC=RecordCodecBuilder.create(i->i.group(
  Codec.intRange(0,5).fieldOf("kind").forGetter(BlueprintSettings::kind),Codec.intRange(5,64).fieldOf("width").forGetter(BlueprintSettings::width),Codec.intRange(5,64).fieldOf("depth").forGetter(BlueprintSettings::depth),Codec.intRange(3,64).fieldOf("height").forGetter(BlueprintSettings::height),
  Codec.intRange(0,2).fieldOf("pattern").forGetter(BlueprintSettings::pattern),Codec.intRange(0,7).fieldOf("glass").forGetter(BlueprintSettings::glass),Codec.intRange(0,2).fieldOf("filler").forGetter(BlueprintSettings::filler),Codec.intRange(0,1).fieldOf("steam").forGetter(BlueprintSettings::steam),Codec.intRange(1,8).fieldOf("hatches").forGetter(BlueprintSettings::hatches),Codec.intRange(0,5).fieldOf("coil").forGetter(BlueprintSettings::coil),Codec.intRange(1,8).fieldOf("blades").forGetter(BlueprintSettings::blades),Codec.intRange(1,4).fieldOf("rings").forGetter(BlueprintSettings::rings)).apply(i,BlueprintSettings::new));
 public int optionCount(int action){return switch(action){case 16->kind==1?2:kind==0?3:0;case 17->kind==0?3:0;case 18->8;case 19->kind==0?2:0;case 20->kind==1?8:0;case 21->kind==5?6:0;case 22->kind==5?8:0;case 23,24->kind==5?4:0;default->0;};}
 public int option(int action){return switch(action){case 16->pattern;case 17->filler;case 18->glass;case 19->steam;case 20->hatches-1;case 21->coil;case 22->blades-1;case 23->rings-1;case 24->(vertical()?2:0)+(width==7?1:0);default->-1;};}
 public boolean vertical(){return kind==5&&pattern==1;}
 public boolean fixed(){return kind>=2&&kind<=4;}
 public BlueprintSettings size(int w,int d,int h){return new BlueprintSettings(kind,w,d,h,pattern,glass,filler,steam,hatches,coil,blades,rings);}
 public BlueprintSettings type(int k,int w,int d,int h){return new BlueprintSettings(k,w,d,h,0,1,0,0,1,coil,blades,rings);}
 public BlueprintSettings change(int action){
  int w=width,d=depth,h=height,p=pattern,g=glass,f=filler,s=steam,a=hatches,c=coil,b=blades,r=rings;
  if(action>=10&&action<=15&&!fixed()&&kind!=5){int axis=(action-10)/2,step=kind==1?2:1,delta=action%2==0?-step:step;if(axis==0)w=Math.clamp(w+delta,5,BlueprintPlan.maximum(kind,0));if(axis==1)d=Math.clamp(d+delta,5,BlueprintPlan.maximum(kind,1));if(axis==2)h=Math.clamp(h+delta,5,BlueprintPlan.maximum(kind,2));}
  if(action==16)p=(p+1)%(kind==1?2:3);if(action==17)f=(f+1)%3;if(action==18)g=(g+1)%8;if(action==19)s=1-s;if(action==20)a=a%8+1;
  if(action==21)c=(c+1)%6;if(action==22)b=b%8+1;if(action==23)r=r%4+1;if(action==24){int v=(option(24)+1)%4;w=v%2==0?5:7;p=v/2;}
  if(kind==5){int length=Math.max(9,b+r+4);d=p==1?w:length;h=p==1?length:w;}
  return new BlueprintSettings(kind,w,d,h,p,g,f,s,a,c,b,r);
 }
}
