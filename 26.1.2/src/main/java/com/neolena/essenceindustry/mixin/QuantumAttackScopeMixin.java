package com.neolena.essenceindustry.mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.neolena.essenceindustry.quantum.QuantumCombat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(Player.class)
public abstract class QuantumAttackScopeMixin {
 @WrapMethod(method="attack")
 private void essenceindustry$attackScope(Entity target,Operation<Void> original){QuantumCombat.attackScope((Player)(Object)this,target,()->original.call(target));}
}
