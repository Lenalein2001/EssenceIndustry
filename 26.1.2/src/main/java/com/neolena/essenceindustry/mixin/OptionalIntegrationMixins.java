package com.neolena.essenceindustry.mixin;
import org.spongepowered.asm.mixin.extensibility.*;
import org.objectweb.asm.tree.ClassNode;
import java.util.*;
/** Do not even inspect third-party targets when their optional mod is absent. */
public final class OptionalIntegrationMixins implements IMixinConfigPlugin {
 public void onLoad(String p){} public String getRefMapperConfig(){return null;}
 public boolean shouldApplyMixin(String target,String mixin){
  if(mixin.substring(mixin.lastIndexOf('.')+1).startsWith("BuilderWireless"))return net.neoforged.fml.loading.LoadingModList.get().getModFileById("ae2")!=null;
  if(mixin.endsWith("QuantumApothicCriticalMixin"))return net.neoforged.fml.loading.LoadingModList.get().getModFileById("apothic_attributes")!=null;
  if(mixin.endsWith("ExtendedCreativeItemsMixin"))return net.neoforged.fml.loading.LoadingModList.get().getModFileById("extendedae")!=null;
  if(mixin.endsWith("EssencePatternExecutionMixin"))return net.neoforged.fml.loading.LoadingModList.get().getModFileById("ae2")!=null;
  if(mixin.endsWith("SimulationGeneticsMixin"))return net.neoforged.fml.loading.LoadingModList.get().getModFileById("productivefarming")!=null;
  if(!mixin.substring(mixin.lastIndexOf('.')+1).startsWith("Hnn"))return true;
  var mods=net.neoforged.fml.loading.LoadingModList.get();
  if(mixin.endsWith("HnnAdvancedAeBusMixin"))return mods!=null && mods.getModFileById("hostilenetworks")!=null && mods.getModFileById("ae2")!=null && mods.getModFileById("advanced_ae")!=null;
  return mods!=null && mods.getModFileById("hostilenetworks")!=null && (!mixin.endsWith("HnnAe2ExportMixin") || mods.getModFileById("ae2")!=null);
 }
 public void acceptTargets(Set<String>a,Set<String>b){} public List<String> getMixins(){return null;}
 public void preApply(String t,ClassNode c,String m,IMixinInfo i){} public void postApply(String t,ClassNode c,String m,IMixinInfo i){}
}
