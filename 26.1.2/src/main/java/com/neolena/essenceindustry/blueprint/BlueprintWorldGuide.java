package com.neolena.essenceindustry.blueprint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neolena.essenceindustry.EssenceIndustry;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;

/** Local read-only construction guide. World data is sampled in bounded batches, never requested. */
public final class BlueprintWorldGuide {
 private static final int SCAN_BUDGET=256,DRAW_BUDGET=512;
 private static final net.minecraft.world.phys.shapes.VoxelShape MISSING_SHAPE=Shapes.box(.012,.012,.012,.988,.988,.988),WRONG_SHAPE=Shapes.box(-.008,-.008,-.008,1.008,1.008,1.008);
 private static final KeyMapping.Category CATEGORY=new KeyMapping.Category(EssenceIndustry.id("construction"));
 private static final KeyMapping TOGGLE=new KeyMapping("key.essenceindustry.world_guide_toggle",-1,CATEGORY);
 private static final KeyMapping NEXT=new KeyMapping("key.essenceindustry.world_guide_next",-1,CATEGORY);
 private static final KeyMapping PREVIOUS=new KeyMapping("key.essenceindustry.world_guide_previous",-1,CATEGORY);
 private record Cell(BlockPos pos,BlockState expected,int layer){}
 private record Mark(Cell cell,boolean wrong){}
 private record View(List<Mark> cells,Vec3 camera){}
 private static final ContextKey<View> VIEW=new ContextKey<>(EssenceIndustry.id("world_build_guide"));
 private static net.minecraft.client.multiplayer.ClientLevel world;
 private static BlockPos controller;private static BlockState owner;
 private static List<Cell> plan=List.of(),slice=List.of();private static final Map<BlockPos,Mark> marks=new HashMap<>();
 private static List<Mark> visible=List.of();private static Mark aimed;private static int layer,height,cursor,unloaded;private static boolean enabled;
 private BlueprintWorldGuide(){}
 public static void register(IEventBus bus){
  bus.addListener((RegisterKeyMappingsEvent e)->{e.registerCategory(CATEGORY);e.register(TOGGLE);e.register(NEXT);e.register(PREVIOUS);});
  NeoForge.EVENT_BUS.addListener(BlueprintWorldGuide::tick);NeoForge.EVENT_BUS.addListener(BlueprintWorldGuide::extract);NeoForge.EVENT_BUS.addListener(BlueprintWorldGuide::submit);NeoForge.EVENT_BUS.addListener(BlueprintWorldGuide::hud);
 }
 public static boolean activeFor(BlockPos pos){return enabled&&Objects.equals(controller,pos)&&world==Minecraft.getInstance().level;}
 public static void toggle(BlueprintMenu menu,int layer){if(activeFor(menu.controllerPosition())){enabled=false;visible=List.of();}else show(menu,layer);}
 public static void show(BlueprintMenu menu,int selectedLayer){
  var mc=Minecraft.getInstance();if(mc.level==null||!menu.controllerMode()||menu.stat(16)==0)return;
  var s=menu.settings();if((long)s.width()*s.depth()*s.height()>64L*64*64)return;
  var actual=menu.controllerPosition();if(!mc.level.hasChunkAt(actual))return;
  var anchor=menu.worldGuideAnchor();var facing=menu.worldGuideFacing();var hostState=mc.level.getBlockState(actual);
  var cells=new ArrayList<Cell>();
  try{for(var cell:BlueprintPlan.create(s,facing,menu.coreDesign())){
   var p=BlueprintPlan.position(anchor,facing,s,cell.local());if(p.equals(actual))continue;var state=cell.state();
   // A manually moved controller replaces a wall block; its old planned slot is casing.
   if(!anchor.equals(actual)&&state.getBlock()==hostState.getBlock())state=(s.kind()==1?com.neolena.essenceindustry.array.ProcessingArray.CASING.get():com.neolena.essenceindustry.nuclear.NuclearReactor.block(s.fixed()?"pressure_panel":"casing")).defaultBlockState();
   cells.add(new Cell(p,state,cell.local().getY()));
  }}catch(IllegalArgumentException ex){return;}
  world=mc.level;controller=actual.immutable();owner=hostState;plan=List.copyOf(cells);height=s.height();enabled=true;setLayer(selectedLayer);
 }
 public static void setLayer(int value){layer=Math.floorMod(value,Math.max(1,height));slice=plan.stream().filter(c->c.layer==layer).toList();marks.clear();visible=List.of();aimed=null;cursor=0;unloaded=0;}
 public static int selectedLayer(){return layer;}
 public static int renderedCount(){return visible.size();}
 public static int mismatchCount(){return marks.size();}
 public static void clear(){world=null;controller=null;owner=null;plan=slice=List.of();marks.clear();visible=List.of();aimed=null;enabled=false;}
 private static void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();
  if(mc.level==null||mc.player==null||world!=null&&mc.level!=world){clear();return;}
  while(TOGGLE.consumeClick())if(mc.screen==null&&controller!=null){enabled=!enabled;visible=List.of();}
  while(NEXT.consumeClick())if(mc.screen==null&&enabled)setLayer(layer+1);
  while(PREVIOUS.consumeClick())if(mc.screen==null&&enabled)setLayer(layer-1);
  if(!enabled||mc.screen!=null||controller==null){visible=List.of();return;}
  if(world.hasChunkAt(controller)&&world.getBlockState(controller).getBlock()!=owner.getBlock()){clear();return;}
  if(controller.distToCenterSqr(mc.player.position())>128*128){visible=List.of();return;}
  for(int n=0;n<Math.min(SCAN_BUDGET,slice.size());n++){
   if(cursor>=slice.size()){cursor=0;unloaded=0;}var cell=slice.get(cursor++);
   if(!world.hasChunkAt(cell.pos)){marks.remove(cell.pos);unloaded++;continue;}
   var actual=world.getBlockState(cell.pos);if(BlueprintWork.matches(actual,cell.expected))marks.remove(cell.pos);else marks.put(cell.pos,new Mark(cell,!actual.isAir()));
  }
  var eye=mc.player.getEyePosition();var end=eye.add(mc.player.getLookAngle().scale(12));
  visible=marks.values().stream().filter(m->m.cell.pos.distToCenterSqr(eye)<48*48).sorted(Comparator.comparingDouble(m->m.cell.pos.distToCenterSqr(eye))).limit(DRAW_BUDGET).toList();
  aimed=null;double best=Double.MAX_VALUE;for(var mark:visible){var hit=new AABB(mark.cell.pos).clip(eye,end);if(hit.isPresent()){double distance=eye.distanceToSqr(hit.get());if(distance<best){best=distance;aimed=mark;}}}
 }
 private static void extract(ExtractLevelRenderStateEvent e){if(!visible.isEmpty())e.getRenderState().setRenderData(VIEW,new View(visible,e.getCamera().position()));}
 private static void submit(SubmitCustomGeometryEvent e){var view=e.getLevelRenderState().getRenderData(VIEW);if(view==null)return;var stack=e.getPoseStack();stack.pushPose();stack.translate(-view.camera.x,-view.camera.y,-view.camera.z);
  e.getSubmitNodeCollector().submitCustomGeometry(stack,RenderTypes.lines(),(pose,vertices)->{var local=new PoseStack();local.last().pose().set(pose.pose());local.last().normal().set(pose.normal());for(var mark:view.cells){var p=mark.cell.pos;ShapeRenderer.renderShape(local,vertices,mark.wrong?WRONG_SHAPE:MISSING_SHAPE,p.getX(),p.getY(),p.getZ(),mark.wrong?0xD8EE9566:0xC871C8E5,1.5f);}});stack.popPose();
 }
 private static Component text(String key,Object... args){return Component.translatable("gui.essenceindustry.planner."+key,args);}
 private static void hud(RenderGuiEvent.Post event){var mc=Minecraft.getInstance();if(!enabled||mc.screen!=null||mc.options.hideGui||world!=mc.level||controller==null)return;var g=event.getGuiGraphics();int x=8,y=38;
  g.fill(x-3,y-3,x+238,y+41,0xC0101923);g.text(mc.font,text("world_layer",layer+1,height),x,y,0xFFD5E2E8,false);
  g.text(mc.font,text("world_legend"),x,y+12,0xFF8FA4B4,false);
  if(aimed!=null){var state=aimed.cell.expected;var name=state.isAir()?text("world_clear"):state.getBlock().getName();if(!state.isAir())g.item(new ItemStack(state.getBlock()),x,y+24);g.text(mc.font,mc.font.plainSubstrByWidth(name.getString(),214),x+20,y+27,aimed.wrong?0xFFEE9566:0xFF71C8E5,false);}
  else g.text(mc.font,text(unloaded>0?"world_unloaded":"world_aim"),x,y+27,0xFF8FA4B4,false);
 }
}
