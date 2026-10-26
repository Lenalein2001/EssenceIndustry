package com.neolena.essenceindustry.construction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
/** Immutable, dimension-bound construction origin and frozen layout basis. */
public record BuilderAnchor(GlobalPos origin,Direction face,Direction heading) {
 public BuilderAnchor {if(heading.getAxis()==Direction.Axis.Y)throw new IllegalArgumentException("Anchor heading must be horizontal");}
 public static final Codec<BuilderAnchor> CODEC=RecordCodecBuilder.create(i->i.group(GlobalPos.CODEC.fieldOf("origin").forGetter(BuilderAnchor::origin),Direction.CODEC.fieldOf("face").forGetter(BuilderAnchor::face),Direction.CODEC.fieldOf("heading").forGetter(BuilderAnchor::heading)).apply(i,BuilderAnchor::new));
}
