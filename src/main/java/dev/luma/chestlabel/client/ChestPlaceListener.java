package dev.luma.chestlabel.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "chestlabel", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ChestPlaceListener {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        var block = event.getPlacedBlock().getBlock();
        if (!(block instanceof ChestBlock) && !(block instanceof BarrelBlock)) {
            return;
        }
        BlockPos pos = event.getPos();
        var dimKey = ((net.minecraft.world.level.Level) event.getLevel()).dimension().location();
        ChestLabelData.setLabel(dimKey, pos, "");
        ChestLabelData.setLogoItem(dimKey, pos, net.minecraft.world.item.ItemStack.EMPTY);
    }
}