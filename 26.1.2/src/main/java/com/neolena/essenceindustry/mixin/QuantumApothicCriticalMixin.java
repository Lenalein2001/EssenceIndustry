package com.neolena.essenceindustry.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.neolena.essenceindustry.quantum.QuantumCombat;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
/** Guarantees the ground gesture through Apothic's own single critical calculation, without mutating attributes. */
@Mixin(targets="dev.shadowsoffire.apothic_attributes.impl.AttributeEvents")
public abstract class QuantumApothicCriticalMixin {
 @WrapOperation(method="apothCriticalStrike",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
 private double essenceindustry$groundCritical(LivingEntity owner,Holder<Attribute> attribute,Operation<Double> original){return QuantumCombat.groundCriticalChance(owner,attribute,original.call(owner,attribute));}
}
