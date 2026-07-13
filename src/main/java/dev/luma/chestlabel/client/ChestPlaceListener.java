package dev.luma.chestlabel.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "chestlabel", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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