package com.pointlessbuilding.journal.server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import com.mojang.serialization.Lifecycle;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;

public class FakeDimension {

    // Get the evil fake level copy of the given level
    public static ResourceKey<Level> fakeKeyFor(ResourceKey<Level> sourceKey) {
        return ResourceKey.create(Registries.DIMENSION, sourceKey.location().withPrefix("fake_"));
    }

    // Get the evil fake dimension data copy of the given level
    public static Supplier<LevelStem> fakeFactoryFor(ServerLevel sourceLevel) {
        return () -> new LevelStem(sourceLevel.dimensionTypeRegistration(), sourceLevel.getChunkSource().getGenerator());
    }

    @SuppressWarnings("deprecation")
    public static ServerLevel getOrCreateFakeLevel(MinecraftServer server, ResourceKey<Level> levelKey, Supplier<LevelStem> dimensionFactory) {
        
        // Our dimension factory will be a near exact copy of whatever dimension is being called in evaluate()

        Map<ResourceKey<Level>, ServerLevel> map = server.forgeGetWorldMap();
        ServerLevel existingLevel = map.get(levelKey);
        if(existingLevel != null) {
            return existingLevel;
        }

        ResourceKey<LevelStem> dimensionKey = ResourceKey.create(Registries.LEVEL_STEM, levelKey.location());
		LevelStem dimension = dimensionFactory.get();

        Registry<LevelStem> dimensionRegistry = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
        if (dimensionRegistry instanceof MappedRegistry<LevelStem> writableRegistry) {
            writableRegistry.unfreeze();
            writableRegistry.register(dimensionKey, dimension, Lifecycle.stable());
        } else {
            throw new IllegalStateException("Unable to register fake dimension " + dimensionKey.location() + ", registry not writable");
        }

        // I could use any of the three vanilla levels, they all have the same seed.
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        // loadLevel(), which vanilla uses, hardcodes 11 as the input here. Not touching it
        ChunkProgressListener chunkProgressListener = server.progressListenerFactory.create(11);
        Executor executor = server.executor;
        LevelStorageSource.LevelStorageAccess anvilConverter = server.storageSource;
        WorldData worldData = server.getWorldData();
        DerivedLevelData derivedLevelData = new DerivedLevelData(worldData, worldData.overworldData());

        ServerLevel newFakeLevel = new ServerLevel(
            server, executor, anvilConverter, derivedLevelData,
            levelKey, dimension, chunkProgressListener,
            worldData.isDebugWorld(), overworld.getSeed(),  // Seed must exactly match
            List.of(),  // We don't want anything to spawn, much less custom spawns
            false, null
        );

        map.put(levelKey, newFakeLevel);
        server.markWorldsDirty();

        return newFakeLevel;
    }

}
