package com.luma.chestlabel;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import com.luma.chestlabel.client.ChestLabelData;

@Mod(ChestLabelMod.MODID)
public class ChestLabelMod {
    public static final String MODID = "chestlabel";

    public ChestLabelMod() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ChestLabelData.load();
        }
    }
}

