package com.neolena.essenceindustry.construction;
import com.neolena.essenceindustry.EssenceIndustry;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.*;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
public final class BuilderClient {
 private static final KeyMapping.Category CATEGORY=new KeyMapping.Category(EssenceIndustry.id("tools"));
 private static final KeyMapping SETTINGS=new KeyMapping("key.essenceindustry.builder_settings",-1,CATEGORY);
 private static final java.util.Map<KeyMapping,Integer> ACTIONS=new java.util.LinkedHashMap<>();
 static {String[] names={"range_set_unused","range_down","range_up","anchor","rotate","mode","undo","width_down","width_up","height_down","height_up","depth_down","depth_up"};for(int i=1;i<names.length;i++)ACTIONS.put(new KeyMapping("key.essenceindustry.builder_"+names[i],-1,CATEGORY),i);}
 private record Cell(BlockPos pos,boolean clear){}
 private record View(List<Cell> cells,Vec3 camera){}
 private static final ContextKey<View> VIEW=new ContextKey<>(EssenceIndustry.id("builder_preview"));
 private static List<Cell> cached=List.of();
 public static void register(IEventBus bus){bus.addListener((RegisterKeyMappingsEvent e)->{e.registerCategory(CATEGORY);e.register(SETTINGS);ACTIONS.keySet().forEach(e::register);});NeoForge.EVENT_BUS.addListener(BuilderClient::tick);NeoForge.EVENT_BUS.addListener(BuilderClient::extract);NeoForge.EVENT_BUS.addListener(BuilderClient::submit);}
 private static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();
  while(SETTINGS.consumeClick())if(mc.player!=null&&mc.screen==null)net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new BuilderOpen());
  for(var entry:ACTIONS.entrySet())while(entry.getKey().consumeClick())if(mc.player!=null&&mc.screen==null)net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new BuilderAction(-1,entry.getValue(),0));
  cached=List.of();if(mc.player==null||mc.level==null||mc.screen!=null)return;
  var tool=mc.player.getMainHandItem().getItem() instanceof BuilderItem?mc.player.getMainHandItem():mc.player.getOffhandItem();if(!(tool.getItem() instanceof BuilderItem)||!tool.has(QuantumBuilder.MATERIAL.get()))return;
  var anchor=tool.get(QuantumBuilder.ANCHOR.get());BlockPos clicked;Direction face,heading;
  if(anchor!=null){if(!anchor.origin().dimension().equals(mc.level.dimension())||anchor.origin().pos().distToCenterSqr(mc.player.position())>64*64)return;clicked=anchor.origin().pos();face=anchor.face();heading=anchor.heading();}
  else {if(!(mc.hitResult instanceof BlockHitResult hit)||hit.getType()!=HitResult.Type.BLOCK)return;clicked=hit.getBlockPos();face=hit.getDirection();heading=mc.player.getDirection();}
  var state=tool.get(QuantumBuilder.MATERIAL.get());var result=new ArrayList<Cell>();for(var pos:BuilderPlan.create(mc.level,clicked,face,heading,BuilderItem.settings(tool)))result.add(new Cell(pos,mc.level.hasChunkAt(pos)&&mc.level.getBlockState(pos).isAir()&&state.canSurvive(mc.level,pos)));cached=List.copyOf(result);
 }
 private static void extract(ExtractLevelRenderStateEvent e){if(!cached.isEmpty())e.getRenderState().setRenderData(VIEW,new View(cached,e.getCamera().position()));}
 private static void submit(SubmitCustomGeometryEvent e){var view=e.getLevelRenderState().getRenderData(VIEW);if(view==null)return;
  var stack=e.getPoseStack();stack.pushPose();stack.translate(-view.camera.x,-view.camera.y,-view.camera.z);
  e.getSubmitNodeCollector().submitCustomGeometry(stack,RenderTypes.lines(),(pose,vertices)->{var local=new PoseStack();local.last().pose().set(pose.pose());local.last().normal().set(pose.normal());for(var cell:view.cells){var p=cell.pos;ShapeRenderer.renderShape(local,vertices,Shapes.box(.015,.015,.015,.985,.985,.985),p.getX(),p.getY(),p.getZ(),cell.clear?0xB871C8E5:0xB8EF7878,1.5f);}});stack.popPose();
 }
 private BuilderClient(){}
}
