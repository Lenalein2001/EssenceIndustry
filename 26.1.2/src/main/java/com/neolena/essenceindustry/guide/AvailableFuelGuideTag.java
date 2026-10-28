package com.neolena.essenceindustry.guide;
/** Optional fuel guidance is compiled only when the current pack supplies its source material. */
final class AvailableFuelGuideTag implements guideme.compiler.TagCompiler {
 public java.util.Set<String> getTagNames(){return java.util.Set.of("ThoriumAvailable");}
 public void compileBlockContext(guideme.compiler.PageCompiler compiler,guideme.document.block.LytBlockContainer parent,guideme.libs.mdast.mdx.model.MdxJsxFlowElement element){
  if(com.neolena.essenceindustry.nuclear.FuelCycle.thoriumAvailable())compiler.compileBlockContext(element,parent);
 }
}
