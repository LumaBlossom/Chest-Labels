package dev.luma.chestlabel.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "chestlabel", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ShulkerHoverRenderer {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.screen != null) {
            return;
        }
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) {
            return;
        }
        BlockPos pos = blockHit.getBlockPos();
        var state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }

        var dimKey = mc.level.dimension().location();
        String label = ChestLabelData.getLabel(dimKey, pos);
        ItemStack logoItem = ChestLabelData.getLogoItem(dimKey, pos);

        if (label.isEmpty() && logoItem.isEmpty()) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int textWidth = label.isEmpty() ? 0 : mc.font.width(label);
        int iconWidth = logoItem.isEmpty() ? 0 : 18;
        int spacing = (textWidth > 0 && iconWidth > 0) ? 4 : 0;
        int totalWidth = textWidth + spacing + iconWidth;

        int centerX = screenWidth / 2;
        int baseY = screenHeight / 2 - 30;

        int startX = centerX - totalWidth / 2;

        int padding = 4;
        graphics.fill(startX - padding, baseY - padding, startX + totalWidth + padding, baseY + 16 + padding, 0x90000000);

        int cursorX = startX;
        if (!logoItem.isEmpty()) {
            graphics.renderItem(logoItem, cursorX, baseY - 1);
            cursorX += iconWidth + spacing;
        }
        if (!label.isEmpty()) {
            graphics.drawString(mc.font, label, cursorX, baseY + 4, 0xFFFFFF, true);
        }
    }
}