package com.pointlessbuilding.journal.utility;

import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

public class SandboxWorldGenRegion extends WorldGenRegion{

    public SandboxWorldGenRegion(ServerLevel level, List<ChunkAccess> cache, ChunkStatus generatingStatus, int writeRadiusCutoff) {
        super(level, cache, generatingStatus, writeRadiusCutoff);
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
