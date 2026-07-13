package dev.luma.chestlabel.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ShulkerLabelStorage {

    private static final String NAME_KEY = "chestlabel_name";
    private static final String ICON_KEY = "chestlabel_icon";

    public static String getLabel(BlockEntity be) {
        CompoundTag tag = be.getPersistentData();
        return tag.contains(NAME_KEY) ? tag.getString(NAME_KEY) : "";
    }

    public static void setLabel(BlockEntity be, String label) {
        CompoundTag tag = be.getPersistentData();
        if (label.isEmpty()) {
            tag.remove(NAME_KEY);
        } else {
            tag.putString(NAME_KEY, label);
        }
        be.setChanged();
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
        CompoundTag tag = be.getPersistentData();
        return tag.contains(NAME_KEY) || tag.contains(ICON_KEY);
    }
}