package com.luma.chestlabel.client;

import com.luma.chestlabel.client.ChestLabelData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "chestlabel", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChestLabelRenderer {

    private static final double MAX_RENDER_DISTANCE_SQ = 64 * 64;
    private static final float TEXT_SCALE = 0.025F;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        var dimKey = level.dimension().location();
        iterateLoadedChests(level, camPos, dimKey, poseStack, buffer, mc);
        buffer.endBatch();
    }

    private static void iterateLoadedChests(ClientLevel level, Vec3 camPos, net.minecraft.resources.ResourceLocation dimKey, PoseStack poseStack, MultiBufferSource.BufferSource buffer, Minecraft mc) {
        int radius = 6;
        BlockPos playerPos = mc.player.blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);
                    if (!ChestLabelData.hasData(dimKey, pos)) {
                        continue;
                    }
                    BlockEntity be = level.getBlockEntity(pos);
                    if (!(be instanceof ChestBlockEntity)) {
                        continue;
                    }
                    renderLabelAt(level, pos, dimKey, camPos, poseStack, buffer, mc);
                }
            }
        }
    }

    private static void renderLabelAt(ClientLevel level, BlockPos anchor, net.minecraft.resources.ResourceLocation dimKey, Vec3 camPos, PoseStack poseStack, MultiBufferSource.BufferSource buffer, Minecraft mc) {
        Vec3 center = ChestPosResolver.resolveRenderCenter(level, anchor);
        double distSq = center.distanceToSqr(camPos);
        if (distSq > MAX_RENDER_DISTANCE_SQ) {
            return;
        }
        String label = ChestLabelData.getLabel(dimKey, anchor);
        ItemStack logoItem = ChestLabelData.getLogoItem(dimKey, anchor);
        if (label.isEmpty() && logoItem.isEmpty()) {
            return;
        }

        Vec3 abovePos = new Vec3(center.x, anchor.getY() + 1.4, center.z);
        Vec3 targetPos = abovePos;

        if (isOccluded(level, camPos, abovePos)) {
            BlockState state = level.getBlockState(anchor);
            Direction facing = state.hasProperty(ChestBlock.FACING) ? state.getValue(ChestBlock.FACING) : Direction.NORTH;
            Direction left = facing.getCounterClockWise();
            Direction right = facing.getClockWise();
            Direction back = facing.getOpposite();

            Vec3 frontPos = new Vec3(center.x + facing.getStepX() * 0.65, anchor.getY() + 0.5, center.z + facing.getStepZ() * 0.65);
            Vec3 leftPos = new Vec3(center.x + left.getStepX() * 0.65, anchor.getY() + 0.5, center.z + left.getStepZ() * 0.65);
            Vec3 rightPos = new Vec3(center.x + right.getStepX() * 0.65, anchor.getY() + 0.5, center.z + right.getStepZ() * 0.65);
            Vec3 backPos = new Vec3(center.x + back.getStepX() * 0.65, anchor.getY() + 0.5, center.z + back.getStepZ() * 0.65);

            if (!isOccluded(level, camPos, frontPos)) {
                targetPos = frontPos;
            } else if (!isOccluded(level, camPos, leftPos)) {
                targetPos = leftPos;
            } else if (!isOccluded(level, camPos, rightPos)) {
                targetPos = rightPos;
            } else if (!isOccluded(level, camPos, backPos)) {
                targetPos = backPos;
            } else {
                targetPos = backPos;
            }
        }

        double x = targetPos.x - camPos.x;
        double y = targetPos.y - camPos.y;
        double z = targetPos.z - camPos.z;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());

        Font font = mc.font;
        float textWidth = label.isEmpty() ? 0 : font.width(label);
        float worldTextWidth = textWidth * TEXT_SCALE;

        if (!label.isEmpty()) {
            poseStack.pushPose();
            poseStack.scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
            float bgX = -textWidth / 2f;
            font.drawInBatch(label, bgX, 0, 0xFFFFFF, false, poseStack.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, 15728880);
            poseStack.popPose();
        }

        if (!logoItem.isEmpty()) {
            poseStack.pushPose();
            float itemOffsetX = label.isEmpty() ? 0 : (worldTextWidth / 2f + 0.16f);
            poseStack.translate(itemOffsetX, -0.08, 0);
            poseStack.scale(0.4F, 0.4F, 0.4F);
            mc.getItemRenderer().renderStatic(logoItem, ItemDisplayContext.FIXED, 15728880, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, poseStack, buffer, mc.level, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static boolean isOccluded(ClientLevel level, Vec3 from, Vec3 to) {
        ClipContext context = new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, null);
        HitResult result = level.clip(context);
        return result.getType() != HitResult.Type.MISS;
    }
}