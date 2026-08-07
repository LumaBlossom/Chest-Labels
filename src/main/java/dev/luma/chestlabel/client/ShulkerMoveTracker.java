package dev.luma.chestlabel.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayDeque;
import java.util.Deque;

@EventBusSubscriber(modid = "chestlabel", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ShulkerMoveTracker {

    private record PendingLabel(String label, ItemStack icon) {
    }

    private static final Deque<PendingLabel> PENDING = new ArrayDeque<>();
    private static final int MAX_PENDING = 8;

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getState().getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }
        var dimKey = ((net.minecraft.world.level.Level) event.getLevel()).dimension().location();
        BlockPos pos = event.getPos();

        String label = ChestLabelData.getLabel(dimKey, pos);
        ItemStack icon = ChestLabelData.getLogoItem(dimKey, pos);

        if (label.isEmpty() && icon.isEmpty()) {
            return;
        }

        if (PENDING.size() >= MAX_PENDING) {
            PENDING.removeFirst();
        }
        PENDING.addLast(new PendingLabel(label, icon));

        ChestLabelData.setLabel(dimKey, pos, "");
        ChestLabelData.setLogoItem(dimKey, pos, ItemStack.EMPTY);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getPlacedBlock().getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }
        if (PENDING.isEmpty()) {
            return;
        }

        PendingLabel pending = PENDING.removeFirst();
        var dimKey = ((net.minecraft.world.level.Level) event.getLevel()).dimension().location();
        BlockPos pos = event.getPos();

        if (!pending.label().isEmpty()) {
            ChestLabelData.setLabel(dimKey, pos, pending.label());
        }
        if (!pending.icon().isEmpty()) {
            ChestLabelData.setLogoItem(dimKey, pos, pending.icon());
        }
    }
}