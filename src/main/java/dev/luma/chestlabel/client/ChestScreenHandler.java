package dev.luma.chestlabel.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
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
    private static boolean logoSlotPressActive;

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        lastRightClickedPos = event.getPos();
    }

    private static boolean isSupportedMenu(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return false;
        }
        AbstractContainerMenu menu = containerScreen.getMenu();
        return menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu;
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!isSupportedMenu(event.getScreen())) {
            return;
        }
        AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) event.getScreen();
        BlockPos clickedPos = lastRightClickedPos;
        if (clickedPos == null) {
            return;
        }
        BlockPos pos = ChestPosResolver.resolveAnchor(Minecraft.getInstance().level, clickedPos);
        activePos = pos;
        int guiLeft = containerScreen.getGuiLeft();
        int guiTop = containerScreen.getGuiTop();
        int barY = guiTop - 24;
        int barX = guiLeft;

        String existingLabel = ChestLabelData.getLabel(getDimensionKey(), pos);
        ItemStack existingLogo = ChestLabelData.getLogoItem(getDimensionKey(), pos);

        EditBox editBox = new EditBox(Minecraft.getInstance().font, barX, barY, 118, 18, Component.literal("Chest Label"));
        editBox.setMaxLength(32);
        editBox.setValue(existingLabel);
        editBox.setResponder(value -> ChestLabelData.setLabel(getDimensionKey(), activePos, value));
        event.addListener(editBox);
        activeEditBox = editBox;

        logoSlotX = barX + 122;
        logoSlotY = barY;
        pendingLogoItem = existingLogo;
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!isSupportedMenu(event.getScreen())) {
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
        if (!isSupportedMenu(event.getScreen())) {
            return;
        }
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        if (isOverLogoSlot(mouseX, mouseY)) {
            logoSlotPressActive = true;
            ItemStack carried = Minecraft.getInstance().player.containerMenu.getCarried();
            if (!carried.isEmpty()) {
                ItemStack copy = carried.copy();
                copy.setCount(1);
                pendingLogoItem = copy;
                ChestLabelData.setLogoItem(getDimensionKey(), activePos, copy);
            } else if (!pendingLogoItem.isEmpty()) {
                pendingLogoItem = ItemStack.EMPTY;
                ChestLabelData.setLogoItem(getDimensionKey(), activePos, ItemStack.EMPTY);
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScreenMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (!isSupportedMenu(event.getScreen())) {
            return;
        }
        if (logoSlotPressActive) {
            logoSlotPressActive = false;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScreenMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (!isSupportedMenu(event.getScreen())) {
            return;
        }
        if (logoSlotPressActive) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!isSupportedMenu(event.getScreen())) {
            return;
        }
        if (activeEditBox == null || !activeEditBox.isFocused()) {
            return;
        }
        if (event.getKeyCode() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            return;
        }
        activeEditBox.keyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers());
        event.setCanceled(true);
    }

    private static boolean isOverLogoSlot(double mouseX, double mouseY) {
        return mouseX >= logoSlotX && mouseX <= logoSlotX + 18 && mouseY >= logoSlotY && mouseY <= logoSlotY + 18;
    }

    private static net.minecraft.resources.ResourceLocation getDimensionKey() {
        return Minecraft.getInstance().level.dimension().location();
    }

    public static boolean isLogoSlotActive() {
        return activePos != null;
    }

    public static String getActiveLabel() {
        return activeEditBox != null ? activeEditBox.getValue() : "";
    }

    public static ItemStack getActiveLogoItem() {
        return pendingLogoItem;
    }

    public static int getLogoSlotX() {
        return logoSlotX;
    }

    public static int getLogoSlotY() {
        return logoSlotY;
    }
}