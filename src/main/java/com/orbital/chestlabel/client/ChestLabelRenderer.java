package com.orbital.chestlabel.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = "chestlabel", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChestLabelRenderer {

    private static final double MAX_RENDER_DISTANCE_SQ = 64 * 64;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
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
                    renderLabelAt(pos, dimKey, camPos, poseStack, buffer, mc);
                }
            }
        }
    }

    private static void renderLabelAt(BlockPos pos, net.minecraft.resources.ResourceLocation dimKey, Vec3 camPos, PoseStack poseStack, MultiBufferSource.BufferSource buffer, Minecraft mc) {
        double distSq = pos.distToCenterSqr(camPos.x, camPos.y, camPos.z);
        if (distSq > MAX_RENDER_DISTANCE_SQ) {
            return;
        }
        String label = ChestLabelData.getLabel(dimKey, pos);
        ItemStack logoItem = ChestLabelData.getLogoItem(dimKey, pos);
        if (label.isEmpty() && logoItem.isEmpty()) {
            return;
        }

        double x = pos.getX() + 0.5 - camPos.x;
        double y = pos.getY() + 1.4 - camPos.y;
        double z = pos.getZ() + 0.5 - camPos.z;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        Font font = mc.font;
        float textWidth = label.isEmpty() ? 0 : font.width(label);
        float bgX = -textWidth / 2f;

        if (!label.isEmpty()) {
            poseStack.pushPose();
            font.drawInBatch(label, bgX, 0, 0xFFFFFF, false, poseStack.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, 15728880);
            poseStack.popPose();
        }

        if (!logoItem.isEmpty()) {
            poseStack.pushPose();
            float itemOffsetX = label.isEmpty() ? 0 : (textWidth / 2f + 10);
            poseStack.translate(itemOffsetX, -8, 0);
            poseStack.scale(16, 16, 1);
            mc.getItemRenderer().renderStatic(logoItem, net.minecraft.world.item.ItemDisplayContext.FIXED, 15728880, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, poseStack, buffer, mc.level, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}