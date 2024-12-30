package dev.tr3ymix.cfm_circuit_manager.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mrcrayfish.furniture.refurbished.Config;
import com.mrcrayfish.furniture.refurbished.client.DeferredElectricRenderer;
import com.mrcrayfish.furniture.refurbished.client.ExtraModels;
import com.mrcrayfish.furniture.refurbished.client.LinkHandler;
import com.mrcrayfish.furniture.refurbished.electricity.Connection;
import com.mrcrayfish.furniture.refurbished.electricity.IElectricityNode;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CircuitManagerBlockEntityRenderer<T extends BlockEntity & IElectricityNode> implements BlockEntityRenderer<T> {
    private static final Set<Connection> DRAWN_CONNECTIONS = new HashSet<>();
    private static final int DEFAULT_COLOR = -1;
    private static final int POWERED_COLOR = -9652;
    private static final int CROSSING_ZONE_COLOR = -3983818;
    private static final float POWER_NODE_SCALE = 1.5F;

    public CircuitManagerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }
    public void render(@NotNull T node, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int light, int overlay) {
        drawNodeAndConnection(node);

    }

    public static void drawNodeAndConnection(IElectricityNode node){
        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null && LinkHandler.isHoldingWrench()){
            DeferredElectricRenderer renderer = DeferredElectricRenderer.get();
            LinkHandler handler = LinkHandler.get();
            boolean isLookingAt = handler.isTargetNode(node);
            if(isLookingAt && !handler.isLinking() && !node.isNodeConnectionLimitReached() || handler.isLinkingNode(node) ||
                    handler.canLinkToNode(node.getNodeLevel(), node) && handler.isTargetNode(node)){
                AABB box = node.getNodeInteractBox();
                int color = handler.getLinkColour(node.getNodeLevel());
                renderer.deferDraw(((poseStack, vertexConsumer) -> {
                    poseStack.pushPose();
                    BlockPos pos = node.getNodePosition();
                    poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    Matrix4f matrix = poseStack.last().pose();
                    renderer.drawInvertedColouredBox(matrix, vertexConsumer, box.inflate(0.03125), color, 0.7F);
                    poseStack.popPose();
                }));
            }

            for (Connection connection : node.getNodeConnections()) {
                if (!DRAWN_CONNECTIONS.contains(connection)) {
                    DRAWN_CONNECTIONS.add(connection);
                    renderer.deferDraw((pose, consumer) -> {
                        pose.pushPose();
                        BlockPos pos = node.getNodePosition();
                        pose.translate((float) pos.getX(), (float) pos.getY(), (float) pos.getZ());
                        pose.translate(0.5, 0.5, 0.5);
                        Vec3 delta = Vec3.atLowerCornerOf(connection.getPosB().subtract(connection.getPosA()));
                        double yaw = Math.atan2(-delta.z, delta.x) + Math.PI;
                        double pitch = Math.atan2(delta.horizontalDistance(), delta.y) + 1.5707963705062866;
                        boolean selected = !handler.isLinking() && connection.equals(handler.getTargetConnection());
                        int color = getConnectionColour(connection, node.getNodeLevel());
                        float offset = (float) (Math.sin((double) Util.getMillis() / 500.0) + 1.0) / 2.0F * 0.2F;
                        AABB box = new AABB(0.0, -0.03125, -0.03125, delta.length(), 0.03125, 0.03125);
                        pose.mulPose(Axis.YP.rotation((float) yaw));
                        pose.mulPose(Axis.ZP.rotation((float) pitch));
                        Matrix4f matrix = pose.last().pose();
                        renderer.drawColouredBox(matrix, consumer, box, color, 0.7F + offset);
                        renderer.drawColouredBox(matrix, consumer, box.inflate(0.03125), color, 0.5F + offset);
                        if (selected) {
                            renderer.drawColouredBox(matrix, consumer, box.inflate(0.03125), -1, 0.8F);
                        }

                        pose.popPose();
                    });
                }
            }

            renderer.deferDraw((pose, consumer) -> {
                pose.pushPose();
                BlockPos pos = node.getNodePosition();
                pose.translate((float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
                Matrix4f matrix = pose.last().pose();
                renderer.drawTexturedBox(matrix, consumer, node.getNodeInteractBox(), 0.0F, 0.0F, 0.25F, 0.25F);
                pose.popPose();
            });

        }
    }

    private static int getConnectionColour(Connection connection, Level level) {
        if (connection.isCrossingPowerableZone(level)) {
            return CROSSING_ZONE_COLOR;
        } else {
            return connection.isPowered(level) ? POWERED_COLOR : DEFAULT_COLOR;
        }
    }

    private static BakedModel getNodeModel(IElectricityNode node) {
        if (node.isNodeConnectionLimitReached()) {
            return ExtraModels.ELECTRIC_NODE_ERROR.getModel();
        } else {
            LinkHandler handler = LinkHandler.get();
            if (handler.isLinking() && !handler.isLinkingNode(node)) {
                return handler.canLinkToNode(node.getNodeLevel(), node) ? ExtraModels.ELECTRIC_NODE_SUCCESS.getModel() : ExtraModels.ELECTRIC_NODE_ERROR.getModel();
            } else {
                return node.isNodePowered() ? ExtraModels.ELECTRIC_NODE_NEUTRAL.getModel() : ExtraModels.ELECTRIC_NODE_POWER.getModel();
            }
        }
    }

    public boolean shouldRenderOffScreen(@NotNull T node) {
        return true;
    }

    public int getViewDistance() {
        return Config.CLIENT.electricityViewDistance.get();
    }

    public @NotNull AABB getRenderBoundingBox(@NotNull T node) {
        return (new AABB(node.getNodePosition())).inflate((double) Config.CLIENT.electricityViewDistance.get());
    }

    public static void clearDrawn() {
        DRAWN_CONNECTIONS.clear();
    }

    public static Set<Connection> getDrawnConnections() {
        return DRAWN_CONNECTIONS;
    }
}
