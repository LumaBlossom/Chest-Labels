package dev.luma.chestlabel.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "chestlabel", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ShulkerInventoryHoverRenderer {

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }
        if (!(containerScreen.getMenu() instanceof ShulkerBoxMenu)) {
            return;
        }
        if (!ChestScreenHandler.isLogoSlotActive()) {
            return;
        }

        String label = ChestScreenHandler.getActiveLabel();
        ItemStack logoItem = ChestScreenHandler.getActiveLogoItem();

        if (label.isEmpty() && logoItem.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();

        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();

        int textWidth = label.isEmpty() ? 0 : mc.font.width(label);
        int iconWidth = logoItem.isEmpty() ? 0 : 18;
        int spacing = (textWidth > 0 && iconWidth > 0) ? 4 : 0;
        int totalWidth = textWidth + spacing + iconWidth;

        int boxX = mouseX + 12;
        int boxY = mouseY - 8;
        int padding = 4;

        graphics.fill(boxX - padding, boxY - padding, boxX + totalWidth + padding, boxY + 16 + padding, 0xE0000000);

        int cursorX = boxX;
        if (!logoItem.isEmpty()) {
            graphics.renderItem(logoItem, cursorX, boxY - 1);
            cursorX += iconWidth + spacing;
        }
        if (!label.isEmpty()) {
            graphics.drawString(mc.font, label, cursorX, boxY + 4, 0xFFFFFF, true);
        }
    }
}