package dev.luma.chestlabel.client;

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
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    private static boolean isSupportedBlock(Block block) {
        return block instanceof ChestBlock || block instanceof BarrelBlock || block instanceof ShulkerBoxBlock;
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

                    BlockState state = level.getBlockState(pos);

                    if (!isSupportedBlock(state.getBlock())) {
                        continue;
                    }

                    renderLabelAt(level, pos, dimKey, camPos, poseStack, buffer, mc);
                }
            }
        }
    }

    private static void renderLabelAt(ClientLevel level, BlockPos anchor, net.minecraft.resources.ResourceLocation dimKey, Vec3 camPos, PoseStack poseStack, MultiBufferSource.BufferSource buffer, Minecraft mc) {
        Vec3 center = ChestPosResolver.resolveRenderCenter(level, anchor);

        if (center.distanceToSqr(camPos) > MAX_RENDER_DISTANCE_SQ) {
            return;
        }

        String label = ChestLabelData.getLabel(dimKey, anchor);
        ItemStack logoItem = ChestLabelData.getLogoItem(dimKey, anchor);

        if (label.isEmpty() && logoItem.isEmpty()) {
            return;
        }

        Vec3 targetPos = new Vec3(center.x, anchor.getY() + 1.4, center.z);

        if (isOccluded(level, camPos, targetPos)) {
            BlockState state = level.getBlockState(anchor);

            Direction facing = resolveFacing(state);

            Direction left = facing.getCounterClockWise();
            Direction right = facing.getClockWise();
            Direction back = facing.getOpposite();

            double frontOffset = 0.75;
            double sideOffset = 1.2;

            List<FaceCandidate> faces = new ArrayList<>();

            addFace(faces, center, anchor, facing, frontOffset, camPos);
            addFace(faces, center, anchor, left, sideOffset, camPos);
            addFace(faces, center, anchor, right, sideOffset, camPos);
            addFace(faces, center, anchor, back, frontOffset, camPos);

            faces.sort(Comparator.comparingDouble(FaceCandidate::score).reversed());

            for (FaceCandidate face : faces) {
                if (!isOccluded(level, camPos, face.position())) {
                    targetPos = face.position();
                    break;
                }
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

            float xOffset = -textWidth / 2.0F;

            font.drawInBatch(
                    label,
                    xOffset,
                    0,
                    0xFFFFFF,
                    false,
                    poseStack.last().pose(),
                    buffer,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    15728880
            );

            poseStack.popPose();
        }

        if (!logoItem.isEmpty()) {
            poseStack.pushPose();

            float itemOffset = label.isEmpty() ? 0.0F : (worldTextWidth / 2.0F + 0.16F);

            poseStack.translate(itemOffset, -0.08, 0);
            poseStack.scale(0.4F, 0.4F, 0.4F);

            mc.getItemRenderer().renderStatic(
                    logoItem,
                    ItemDisplayContext.FIXED,
                    15728880,
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    mc.level,
                    0
            );

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static Direction resolveFacing(BlockState state) {
        if (state.hasProperty(ChestBlock.FACING)) {
            return state.getValue(ChestBlock.FACING);
        }
        if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)) {
            Direction dir = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
            return dir.getAxis() == Direction.Axis.Y ? Direction.NORTH : dir;
        }
        if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
        }
        return Direction.NORTH;
    }

    private static void addFace(List<FaceCandidate> faces, Vec3 center, BlockPos anchor, Direction dir, double offset, Vec3 cameraPos) {
        Vec3 pos = new Vec3(
                center.x + dir.getStepX() * offset,
                anchor.getY() + 0.5,
                center.z + dir.getStepZ() * offset
        );

        Vec3 normal = new Vec3(dir.getStepX(), 0, dir.getStepZ());
        Vec3 toCamera = cameraPos.subtract(center).normalize();

        double score = normal.dot(toCamera);

        faces.add(new FaceCandidate(pos, score));
    }

    private static boolean isOccluded(ClientLevel level, Vec3 from, Vec3 to) {
        ClipContext context = new ClipContext(
                from,
                to,
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE,
                null
        );

        HitResult result = level.clip(context);

        return result.getType() != HitResult.Type.MISS;
    }

    private record FaceCandidate(Vec3 position, double score) {
    }
}