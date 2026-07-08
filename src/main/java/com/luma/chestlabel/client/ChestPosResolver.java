package com.luma.chestlabel.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;

public class ChestPosResolver {

    public static BlockPos resolveAnchor(BlockGetter level, BlockPos clicked) {
        BlockState state = level.getBlockState(clicked);
        if (!(state.getBlock() instanceof ChestBlock) || !state.hasProperty(ChestBlock.TYPE)) {
            return clicked;
        }
        ChestType type = state.getValue(ChestBlock.TYPE);
        if (type == ChestType.SINGLE) {
            return clicked;
        }
        Direction dir = ChestBlock.getConnectedDirection(state);
        BlockPos neighbor = clicked.relative(dir);
        if (type == ChestType.RIGHT) {
            return neighbor;
        }
        return clicked;
    }

    public static Vec3 resolveRenderCenter(BlockGetter level, BlockPos anchor) {
        BlockState state = level.getBlockState(anchor);
        double x = anchor.getX() + 0.5;
        double y = anchor.getY();
        double z = anchor.getZ() + 0.5;
        if (state.getBlock() instanceof ChestBlock && state.hasProperty(ChestBlock.TYPE)) {
            ChestType type = state.getValue(ChestBlock.TYPE);
            if (type != ChestType.SINGLE) {
                Direction dir = ChestBlock.getConnectedDirection(state);
                BlockPos neighbor = anchor.relative(dir);
                x = (anchor.getX() + neighbor.getX()) / 2.0 + 0.5;
                z = (anchor.getZ() + neighbor.getZ()) / 2.0 + 0.5;
            }
        }
        return new Vec3(x, y, z);
    }
}