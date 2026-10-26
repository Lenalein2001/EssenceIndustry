package com.neolena.essenceindustry.construction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
/** All values are bounded while decoding and again at the server menu boundary. */
public record BuilderSettings(int mode,int width,int height,int depth,int rotation,int offset,int source,boolean centered) {
 public BuilderSettings {if(mode<0||mode>8||width<1||width>32||height<1||height>32||depth<1||depth>32||rotation<0||rotation>3||offset< -16||offset>16||source<0||source>2)throw new IllegalArgumentException("Builder settings out of bounds");}
 public static final BuilderSettings DEFAULT=new BuilderSettings(0,5,5,5,0,0,0,true);
 public static final Codec<BuilderSettings> CODEC=RecordCodecBuilder.create(i->i.group(
 Codec.intRange(0,8).fieldOf("mode").forGetter(BuilderSettings::mode),Codec.intRange(1,32).fieldOf("width").forGetter(BuilderSettings::width),
 Codec.intRange(1,32).fieldOf("height").forGetter(BuilderSettings::height),Codec.intRange(1,32).fieldOf("depth").forGetter(BuilderSettings::depth),
 Codec.intRange(0,3).fieldOf("rotation").forGetter(BuilderSettings::rotation),Codec.intRange(-16,16).fieldOf("offset").forGetter(BuilderSettings::offset),
 Codec.intRange(0,2).fieldOf("source").forGetter(BuilderSettings::source),Codec.BOOL.fieldOf("centered").forGetter(BuilderSettings::centered)).apply(i,BuilderSettings::new));
 public BuilderSettings range(int size){int n=Math.clamp(size,1,32);return new BuilderSettings(mode,n,n,n,rotation,offset,source,centered);}
 public BuilderSettings resize(int delta){return new BuilderSettings(mode,Math.clamp(width+delta,1,32),Math.clamp(height+delta,1,32),Math.clamp(depth+delta,1,32),rotation,offset,source,centered);}
 public BuilderSettings change(int id){int m=mode,w=width,h=height,d=depth,r=rotation,o=offset,s=source;boolean c=centered;
 switch(id){case 0->m=(m+1)%9;case 1->w--;case 2->w++;case 3->h--;case 4->h++;case 5->d--;case 6->d++;case 7->r=(r+1)%4;case 8->o--;case 9->o++;case 10->s=(s+1)%3;case 11->c=!c;default->{}}
 return new BuilderSettings(m,Math.clamp(w,1,32),Math.clamp(h,1,32),Math.clamp(d,1,32),r,Math.clamp(o,-16,16),s,c);}
}
