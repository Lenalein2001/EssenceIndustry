package com.neolena.essenceindustry.nuclear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

/** Approved Cherenkov flow bands, driven only by synchronized running reactors. */
public final class ReactorEffects {
    private static final Identifier FLOW = Identifier.parse("essenceindustry:textures/block/reactor/flow.png");
    private static final ContextKey<View> VIEW = new ContextKey<>(Identifier.parse("essenceindustry:reactor_flow"));
    private static final int MAX_COLUMNS = 8;
    private static final int SEGMENTS = 12;
    private static final int FULLBRIGHT = 0xF000F0;
    private static final double RANGE_SQUARED = 48 * 48;
    private static final double RADIUS = .46;
    private static final double[] SIN = new double[SEGMENTS + 1];
    private static final double[] COS = new double[SEGMENTS + 1];
    private static ClientLevel cachedLevel;
    private static List<Column> columns = List.of();
    private static long lastSnapshot = Long.MIN_VALUE;
    private static Vec3 snapshotCamera = Vec3.ZERO;
    private static boolean registered;

    /** Top is exclusive. Owning tile is checked every frame for immediate stop/disassembly. */
    private record Column(ReactorTile owner, BlockPos base, int height) {
        int x() { return base.getX(); }
        int z() { return base.getZ(); }
        int bottom() { return base.getY(); }
        int top() { return bottom() + height; }
        AABB bounds() { return new AABB(x() + .02, bottom(), z() + .02, x() + .98, top(), z() + .98); }
    }
    private record View(List<Column> columns, Vec3 camera, double time) {}

    static {
        for (int i = 0; i <= SEGMENTS; i++) {
            double angle = i * Math.PI * 2 / SEGMENTS;
            SIN[i] = Math.sin(angle);
            COS[i] = Math.cos(angle);
        }
    }

    public static void register() {
        if (registered) return;
        registered = true;
        NeoForge.EVENT_BUS.addListener(ReactorEffects::extract);
        NeoForge.EVENT_BUS.addListener(ReactorEffects::submit);
        NeoForge.EVENT_BUS.addListener(ReactorEffects::tick);
    }

    private static void tick(ClientTickEvent.Post event) {
        // Release old tile/world references even when there is no next render-level event.
        var level = Minecraft.getInstance().level;
        if (level != cachedLevel) reset(level);
    }

    private static void reset(ClientLevel level) {
        cachedLevel = level;
        columns = List.of();
        lastSnapshot = Long.MIN_VALUE;
        snapshotCamera = Vec3.ZERO;
    }

    private static void extract(ExtractLevelRenderStateEvent event) {
        var level = event.getLevel();
        var camera = event.getCamera().position();
        double time = level.getGameTime() + event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        event.getRenderState().setRenderData(VIEW, new View(List.of(), camera, time));
        if (cachedLevel != level) reset(level);
        long tick = level.getGameTime();
        if (lastSnapshot == Long.MIN_VALUE || tick < lastSnapshot || tick - lastSnapshot >= 10
                || snapshotCamera.distanceToSqr(camera) > 4) {
            snapshot(level, camera);
            lastSnapshot = tick;
            snapshotCamera = camera;
        }
        var visible = new ArrayList<Column>(MAX_COLUMNS);
        // At most eight synchronized owners and chunk checks; no block-volume scan per frame.
        for (var column : columns) {
            if (column.owner.isRemoved() || column.owner.getLevel() != level || !column.owner.running()
                    || !level.hasChunkAt(column.base) || distanceSquared(column, camera) > RANGE_SQUARED) continue;
            if (event.getFrustum().isVisible(column.bounds())) visible.add(column);
        }
        visible.sort(Comparator.comparingDouble((Column column) -> distanceSquared(column, camera)).reversed());
        event.getRenderState().setRenderData(VIEW, new View(List.copyOf(visible), camera, time));
    }

    private static void snapshot(ClientLevel level, Vec3 camera) {
        // Client tiles already contain compact synchronized column bases. The priority queue
        // stays bounded even for many dense reactors; no server world or neighbor reads.
        var nearest = new PriorityQueue<Column>(MAX_COLUMNS,
                Comparator.comparingDouble((Column column) -> distanceSquared(column, camera)).reversed());
        var seen = new HashSet<BlockPos>();
        for (var tile : ReactorTile.clientTiles(level)) {
            if (tile.isRemoved() || !tile.running()) continue;
            int height = tile.height() - 2;
            if (height < 1) continue;
            for (var base : tile.visualFuelColumns()) {
                if (!seen.add(base)) continue;
                var column = new Column(tile, base.immutable(), height);
                double distance = distanceSquared(column, camera);
                if (distance > RANGE_SQUARED) continue;
                if (nearest.size() < MAX_COLUMNS) nearest.add(column);
                else if (distance < distanceSquared(nearest.peek(), camera)) {
                    nearest.remove();
                    nearest.add(column);
                }
            }
        }
        columns = List.copyOf(nearest);
    }

    private static double distanceSquared(Column column, Vec3 camera) {
        double dx = Math.max(Math.abs(camera.x - column.x() - .5) - .5, 0);
        double dz = Math.max(Math.abs(camera.z - column.z() - .5) - .5, 0);
        double dy = Math.max(Math.max(column.bottom() - camera.y, camera.y - column.top()), 0);
        return dx * dx + dy * dy + dz * dz;
    }

    private static void submit(SubmitCustomGeometryEvent event) {
        var view = event.getLevelRenderState().getRenderData(VIEW);
        if (view == null || view.columns.isEmpty()) return;
        var stack = event.getPoseStack();
        stack.pushPose();
        stack.translate(-view.camera.x, -view.camera.y, -view.camera.z);
        event.getSubmitNodeCollector().submitCustomGeometry(stack, RenderTypes.entityTranslucent(FLOW),
                (pose, vertices) -> draw(view, pose, vertices));
        stack.popPose();
    }

    private static void draw(View view, PoseStack.Pose pose, VertexConsumer vertices) {
        var normal = new Vector3f();
        for (var column : view.columns) {
            double cameraX = view.camera.x - column.x() - .5;
            double cameraZ = view.camera.z - column.z() - .5;
            double cameraLength = Math.sqrt(cameraX * cameraX + cameraZ * cameraZ);
            if (cameraLength < .00001) { cameraX = 0; cameraZ = -1; }
            else { cameraX /= cameraLength; cameraZ /= cameraLength; }
            double span = column.height - .4;
            double phase = view.time * .035 / span + (column.x() + column.z()) * .035;
            int bands = column.height >= 6 ? 2 : 1;
            for (int band = 0; band < bands; band++) {
                double progress = phase + (double) band / bands;
                progress -= Math.floor(progress);
                double halfHeight = Math.min(.7, column.height * .14);
                double centerY = column.bottom() + .2 + progress * span;
                double fade = Math.min(1, Math.min(progress, 1 - progress) * span / (halfHeight * 1.5));
                fade = fade * fade * (3 - 2 * fade);
                int alpha = (int) Math.round(112 * fade);
                if (alpha < 1) continue;
                double bottom = Math.max(column.bottom() + .2, centerY - halfHeight);
                double top = Math.min(column.top() - .2, centerY + halfHeight);
                float vBottom = (float) ((bottom - centerY + halfHeight) / (2 * halfHeight));
                float vTop = (float) ((top - centerY + halfHeight) / (2 * halfHeight));
                int color = (alpha << 24) | 0x91DFFF;
                for (int segment = 0; segment < SEGMENTS; segment++) {
                    double midX = (COS[segment] + COS[segment + 1]) * .5;
                    double midZ = (SIN[segment] + SIN[segment + 1]) * .5;
                    if (midX * cameraX + midZ * cameraZ < 0) continue;
                    double length = Math.sqrt(midX * midX + midZ * midZ);
                    pose.transformNormal((float) (midX / length), 0, (float) (midZ / length), normal);
                    float x1 = (float) (column.x() + .5 + RADIUS * COS[segment]);
                    float z1 = (float) (column.z() + .5 + RADIUS * SIN[segment]);
                    float x2 = (float) (column.x() + .5 + RADIUS * COS[segment + 1]);
                    float z2 = (float) (column.z() + .5 + RADIUS * SIN[segment + 1]);
                    vertex(pose, vertices, x1, (float) bottom, z1, .5f, vBottom, color, normal);
                    vertex(pose, vertices, x1, (float) top, z1, .5f, vTop, color, normal);
                    vertex(pose, vertices, x2, (float) top, z2, .5f, vTop, color, normal);
                    vertex(pose, vertices, x2, (float) bottom, z2, .5f, vBottom, color, normal);
                }
            }
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer vertices, float x, float y, float z,
                               float u, float v, int color, Vector3f normal) {
        vertices.addVertex(pose, x, y, z).setColor(color).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULLBRIGHT)
                .setNormal(normal.x, normal.y, normal.z);
    }

    private ReactorEffects() {}
}
