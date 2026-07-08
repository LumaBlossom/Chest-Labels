package com.orbital.chestlabel.client;

import com.orbital.chestlabel.client.ChestLabelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "chestlabel", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChestScreenHandler {
    private static EditBox activeEditBox;
    private static BlockPos activePos;
    private static ItemStack pendingLogoItem = ItemStack.EMPTY;
    private static int logoSlotX;
    private static int logoSlotY;
    private static BlockPos lastRightClickedPos;

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        lastRightClickedPos = event.getPos();
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof ContainerScreen ContainerScreen)) {
            return;
        }
        ChestMenu menu = ContainerScreen.getMenu();
        BlockPos pos = locatePosFromMenu(ContainerScreen);
        if (pos == null) {
            return;
        }
        activePos = pos;
        int guiLeft = getLeftPos(ContainerScreen);
        int guiTop = getTopPos(ContainerScreen);
        int barY = guiTop - 24;
        int barX = guiLeft;

        String existingLabel = ChestLabelData.getLabel(getDimensionKey(), pos);
        EditBox editBox = new EditBox(Minecraft.getInstance().font, barX, barY, 118, 18, Component.literal("Chest Label"));
        editBox.setMaxLength(32);
        editBox.setValue(existingLabel);
        editBox.setResponder(value -> ChestLabelData.setLabel(getDimensionKey(), activePos, value));
        event.addListener(editBox);
        activeEditBox = editBox;

        logoSlotX = barX + 122;
        logoSlotY = barY;
        pendingLogoItem = ChestLabelData.getLogoItem(getDimensionKey(), pos);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof ContainerScreen)) {
            return;
        }
        var graphics = event.getGuiGraphics();
        graphics.fill(logoSlotX, logoSlotY, logoSlotX + 18, logoSlotY + 18, 0x8B8B8B8B);
        if (!pendingLogoItem.isEmpty()) {
            graphics.renderItem(pendingLogoItem, logoSlotX + 1, logoSlotY + 1);
        }
    }

    @SubscribeEvent
    public static void onScreenMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof ContainerScreen)) {
            return;
        }
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        if (mouseX >= logoSlotX && mouseX <= logoSlotX + 18 && mouseY >= logoSlotY && mouseY <= logoSlotY + 18) {
            ItemStack carried = Minecraft.getInstance().player.containerMenu.getCarried();
            if (!carried.isEmpty()) {
                ItemStack copy = carried.copy();
                copy.setCount(1);
                pendingLogoItem = copy;
                ChestLabelData.setLogoItem(getDimensionKey(), activePos, copy);
                event.setCanceled(true);
            } else if (!pendingLogoItem.isEmpty()) {
                pendingLogoItem = ItemStack.EMPTY;
                ChestLabelData.setLogoItem(getDimensionKey(), activePos, ItemStack.EMPTY);
                event.setCanceled(true);
            }
        }
    }

    private static net.minecraft.resources.ResourceLocation getDimensionKey() {
        return Minecraft.getInstance().level.dimension().location();
    }

    private static BlockPos locatePosFromMenu(ContainerScreen screen) {
        return lastRightClickedPos;
    }

    private static int getLeftPos(ContainerScreen screen) {
        return screen.getGuiLeft();
    }

    private static int getTopPos(ContainerScreen screen) {
        return screen.getGuiTop();
    }
}