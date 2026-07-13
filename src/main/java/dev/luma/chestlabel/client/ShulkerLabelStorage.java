package dev.luma.chestlabel.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ShulkerLabelStorage {

    private static final String ICON_KEY = "chestlabel_icon";

    public static String getLabel(BlockEntity be) {
        if (be instanceof BaseContainerBlockEntity container) {
            Component name = container.getCustomName();
            return name != null ? name.getString() : "";
        }
        return "";
    }

    public static void setLabel(BlockEntity be, String label) {
        if (be instanceof BaseContainerBlockEntity container) {
            container.setCustomName(label.isEmpty() ? null : Component.literal(label));
            be.setChanged();
        }
    }

    public static ItemStack getLogoItem(BlockEntity be) {
        CompoundTag tag = be.getPersistentData();
        if (!tag.contains(ICON_KEY)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(tag.getCompound(ICON_KEY));
    }

    public static void setLogoItem(BlockEntity be, ItemStack stack) {
        CompoundTag tag = be.getPersistentData();
        if (stack.isEmpty()) {
            tag.remove(ICON_KEY);
        } else {
            CompoundTag itemTag = new CompoundTag();
            stack.save(itemTag);
            tag.put(ICON_KEY, itemTag);
        }
        be.setChanged();
    }

    public static boolean hasData(BlockEntity be) {
        return !getLabel(be).isEmpty() || be.getPersistentData().contains(ICON_KEY);
    }
}