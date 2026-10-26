package com.neolena.essenceindustry.array;
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
/** Native offscreen 3D view. It shares formed block models and gantry rendering with the world. */
final class ArrayScenePreview {
 record Part(BlockModelRenderState model,float x,float y,float z,float sx,float sz){}
 record View(int x0,int y0,int x1,int y1,float scale,ScreenRectangle scissorArea,ScreenRectangle bounds,ArrayRenderer.State scene,List<Part> parts,float yaw) implements PictureInPictureRenderState{}
 static void register(IEventBus bus){bus.addListener((net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent e)->e.register(View.class,Renderer::new));}
 static void draw(GuiGraphicsExtractor graphics,int x,int y,int width,int height,int w,int d,int h,int installed,int active,ItemStack machine,float yaw,boolean cutaway,List<net.minecraft.core.BlockPos> racks,List<net.minecraft.core.BlockPos> windows){
  int originalW=w,originalD=d,originalH=h;
  int sourceColumns=ArrayBayLayout.columns(w),sourceRows=ArrayBayLayout.columns(d);
  h=Math.min(9,Math.max(5,h));var mc=Minecraft.getInstance();w=Math.min(9,Math.max(5,w));d=Math.min(9,Math.max(5,d));var scene=new ArrayRenderer.State();scene.preview=true;scene.detailed=true;scene.cutaway=cutaway;scene.height=h;scene.width=w;scene.depth=d;scene.installed=installed;scene.active=active;scene.face=Direction.NORTH;scene.formed=true;scene.lightCoords=15728880;scene.time=mc.level==null?0:mc.level.getGameTime();
  var shownMask=new BitSet();int cols=ArrayBayLayout.columns(w),rows=ArrayBayLayout.columns(d);
  for(var rack:racks){int col=Math.round((rack.getX()/2-1)*(cols-1f)/Math.max(1,sourceColumns-1)),row=Math.round((rack.getZ()/2-1)*(rows-1f)/Math.max(1,sourceRows-1));int level=Math.min((h-5)/2,(rack.getY()-1)/2);shownMask.set(level*rows*cols+row*cols+col);}
  scene.windows=(originalW==w&&originalD==d&&originalH==h)?windows:List.of();
  scene.racks=ArrayBayLayout.decode(shownMask.toLongArray(),w,d,h);scene.installed=Math.min(installed,scene.racks.size()*8);scene.bays=ArrayBayLayout.allocate(scene.racks,scene.installed);
  mc.getItemModelResolver().updateForTopItem(scene.machine,machine,ItemDisplayContext.NONE,mc.level,null,0);
  var resolver=new BlockModelResolver(mc.getModelManager());var models=new HashMap<BlockState,BlockModelRenderState>();var parts=new ArrayList<Part>();
  java.util.function.Function<BlockState,BlockModelRenderState> model=state->models.computeIfAbsent(state,value->{var result=new BlockModelRenderState();resolver.update(result,value,BlockDisplayContext.create());return result;});
  if(machine.getItem() instanceof net.minecraft.world.item.BlockItem block){scene.hasMachineBlock=true;resolver.update(scene.machineBlock,block.getBlock().defaultBlockState(),BlockDisplayContext.create());}
  var rackState=ProcessingArray.RACK.get().defaultBlockState().setValue(ArrayLayout.FORMED,true).setValue(ArrayLayout.PART,3);
  for(var rack:scene.racks)for(int level=0;level<2;level++)parts.add(new Part(model.apply(rackState.setValue(ArrayLayout.PART,rack.getY()==1&&level==0?3:6)),rack.getX(),rack.getY()-1+level,rack.getZ(),1,1));
  parts.add(new Part(model.apply(ProcessingArray.CONTROLLER.get().defaultBlockState().setValue(ArrayLayout.FORMED,true)),0,0,0,1,1));
  var bounds=new ScreenRectangle(x,y,width,height);float scale=Math.min(width/(float)(w+d),height/(h+(w+d)*.25f))*.85f;
  var state=new View(x,y,x+width,y+height,scale,bounds,bounds,scene,List.copyOf(parts),yaw);
  ((com.neolena.essenceindustry.mixin.ArrayGuiAccessor)(Object)graphics).essenceindustry$renderState().addPicturesInPictureState(state);
 }
 private static final class Renderer extends PictureInPictureRenderer<View>{
  Renderer(MultiBufferSource.BufferSource buffers){super(buffers);}
  @Override public Class<View> getRenderStateClass(){return View.class;}
  @Override protected float getTranslateY(int height,int scale){return height/2f;}
  @Override protected String getTextureLabel(){return "processing_array";}
  @Override protected void renderToTexture(View view,PoseStack pose){
   var mc=Minecraft.getInstance();mc.gameRenderer.getLighting().setupFor(com.mojang.blaze3d.platform.Lighting.Entry.ENTITY_IN_UI);
   pose.mulPose(Axis.XP.rotationDegrees(22));pose.mulPose(Axis.YP.rotationDegrees(view.yaw));pose.scale(1,-1,1);pose.translate(-view.scene.width/2d,-1.5,-view.scene.depth/2d);
   var features=mc.gameRenderer.getFeatureRenderDispatcher();var collector=features.getSubmitNodeStorage();
   for(var part:view.parts){pose.pushPose();pose.translate(part.x,part.y,part.z);pose.scale(part.sx,1,part.sz);part.model.submit(pose,collector,15728880,OverlayTexture.NO_OVERLAY,0);pose.popPose();}
   ArrayRenderer.submitScene(view.scene,pose,collector);features.renderAllFeatures();
  }
 }
 private ArrayScenePreview(){}
}
