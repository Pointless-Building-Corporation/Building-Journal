package com.pointlessbuilding.journal.blocks;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ComplexAnchorBlock extends Block implements EntityBlock {
    
    public ComplexAnchorBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(0.0f, 9f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.BONE_BLOCK)
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ComplexAnchorBlockEntity(pos, state);
    }

    // An Entity block can have this function to check events per tick. Functions run on the Entity class side.
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide) return null;
        else {
            return (lvl, pos, st, blockEntity) -> {
                if(blockEntity instanceof ComplexAnchorBlockEntity be) {
                    be.tickServer();
                }
            };
        }
    }

}
