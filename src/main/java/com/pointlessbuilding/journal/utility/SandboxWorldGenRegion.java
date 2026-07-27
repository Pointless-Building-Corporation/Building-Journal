package com.pointlessbuilding.journal.utility;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

public class SandboxWorldGenRegion extends WorldGenRegion{

    private final ChunkPos writeableChunk;

    public SandboxWorldGenRegion(ServerLevel level, List<ChunkAccess> cache, ChunkStatus generatingStatus, int writeRadiusCutoff, ChunkPos writeableChunk) {
        super(level, cache, generatingStatus, writeRadiusCutoff);
        this.writeableChunk = writeableChunk;
    }

    private boolean isWriteable(BlockPos pos) {
        return new ChunkPos(pos).equals(writeableChunk);
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        if (!isWriteable(pos)) return false;
        return super.setBlock(pos, state, flags, recursionLeft);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
        if (!isWriteable(pos)) return false;
        return super.destroyBlock(pos, dropBlock, entity, recursionLeft);
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        if (!isWriteable(pos)) return false;
        return super.removeBlock(pos, isMoving);
    }

    @Override
    public void addFreshEntityWithPassengers(Entity entity) {
        return;
    }
    
    @Override
    public boolean addFreshEntity(Entity entity) {
        return false;
    }

}
