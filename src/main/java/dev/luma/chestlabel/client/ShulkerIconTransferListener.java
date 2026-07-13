package dev.luma.chestlabel.client;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "chestlabel")
public class ShulkerIconTransferListener {

    private static final String ICON_TAG_KEY = "chestlabel_icon_carry";

    private static ItemStack lastPlacingStack = ItemStack.EMPTY;

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ShulkerBoxBlock) {
            lastPlacingStack = stack.copy();
        } else {
            lastPlacingStack = ItemStack.EMPTY;
        }
    }

    @SubscribeEvent
    public static void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        if (!(event.getState().getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockEntity be = event.getLevel().getBlockEntity(pos);
        if (be == null) {
            return;
        }
        ItemStack icon = ShulkerLabelStorage.getLogoItem(be);
        if (icon.isEmpty()) {
            return;
        }
        for (ItemStack drop : event.getDrops()) {
            if (drop.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                CompoundTag iconTag = new CompoundTag();
                icon.save(iconTag);
                drop.getOrCreateTag().put(ICON_TAG_KEY, iconTag);
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getPlacedBlock().getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }
        if (lastPlacingStack.isEmpty() || !lastPlacingStack.hasTag()) {
            lastPlacingStack = ItemStack.EMPTY;
            return;
        }
        CompoundTag tag = lastPlacingStack.getTag();
        if (tag == null || !tag.contains(ICON_TAG_KEY)) {
            lastPlacingStack = ItemStack.EMPTY;
            return;
        }
        ItemStack icon = ItemStack.of(tag.getCompound(ICON_TAG_KEY));
        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (be != null) {
            ShulkerLabelStorage.setLogoItem(be, icon);
        }
        lastPlacingStack = ItemStack.EMPTY;
    }
}