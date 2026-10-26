package com.neolena.essenceindustry.blueprint;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
/** Cached native block-model preview of the selected construction plan. */
final class BlueprintPreview {
 record Part(BlockModelRenderState model,int x,int y,int z){}
 record View(int x0,int y0,int x1,int y1,float scale,ScreenRectangle scissorArea,ScreenRectangle bounds,List<Part> parts,int w,int d,int h,float yaw) implements PictureInPictureRenderState{}
 static void register(IEventBus bus){bus.addListener((net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent e)->e.register(View.class,Renderer::new));}
 private List<BlueprintPlan.Cell> source;private boolean cut;private List<Part> parts=List.of();
 void draw(GuiGraphicsExtractor graphics,int x,int y,int width,int height,BlueprintSettings settings,List<BlueprintPlan.Cell> cells,float yaw,boolean cutaway){
  if(source!=cells||cut!=cutaway){source=cells;cut=cutaway;var resolver=new BlockModelResolver(Minecraft.getInstance().getModelManager());var models=new HashMap<BlockState,BlockModelRenderState>();var result=new ArrayList<Part>();
   for(var c:cells){var pos=c.local();if(c.state().isAir()||cutaway&&(pos.getY()==settings.height()-1||pos.getZ()==0||pos.getX()==settings.width()-1))continue;
    var model=models.computeIfAbsent(c.state(),s->{var m=new BlockModelRenderState();resolver.update(m,s,BlockDisplayContext.create());return m;});result.add(new Part(model,pos.getX(),pos.getY(),pos.getZ()));
   }parts=List.copyOf(result);
  }
  var bounds=new ScreenRectangle(x,y,width,height);float scale=Math.min(width/(float)(settings.width()+settings.depth()),height/(settings.height()+(settings.width()+settings.depth())*.3f))*1.25f;
  var state=new View(x,y,x+width,y+height,scale,bounds,bounds,parts,settings.width(),settings.depth(),settings.height(),yaw);
  ((com.neolena.essenceindustry.mixin.ArrayGuiAccessor)(Object)graphics).essenceindustry$renderState().addPicturesInPictureState(state);
 }
 private static final class Renderer extends PictureInPictureRenderer<View>{
  Renderer(MultiBufferSource.BufferSource buffers){super(buffers);}
  public Class<View> getRenderStateClass(){return View.class;}
  protected float getTranslateY(int height,int scale){return height/2f;}
  protected String getTextureLabel(){return "construction_preview";}
  protected void renderToTexture(View v,PoseStack pose){var mc=Minecraft.getInstance();mc.gameRenderer.getLighting().setupFor(com.mojang.blaze3d.platform.Lighting.Entry.ENTITY_IN_UI);
   pose.mulPose(Axis.XP.rotationDegrees(25));pose.mulPose(Axis.YP.rotationDegrees(v.yaw));pose.scale(1,-1,-1);pose.translate(-v.w/2d,-v.h/2d,-v.d/2d);
   var features=mc.gameRenderer.getFeatureRenderDispatcher();var collector=features.getSubmitNodeStorage();for(var p:v.parts){pose.pushPose();pose.translate(p.x+.001,p.y+.001,p.z+.001);pose.scale(.998f,.998f,.998f);p.model.submit(pose,collector,15728880,OverlayTexture.NO_OVERLAY,0);pose.popPose();}features.renderAllFeatures();
  }
 }
}




