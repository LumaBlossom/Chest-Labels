package dev.luma.chestlabel;

import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import dev.luma.chestlabel.client.ChestLabelData;

@Mod(ChestLabelMod.MODID)
public class ChestLabelMod {
    public static final String MODID = "chestlabel";

    public ChestLabelMod() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ChestLabelData.load();
        }
    }
}