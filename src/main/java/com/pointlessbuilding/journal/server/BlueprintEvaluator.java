package com.pointlessbuilding.journal.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.blocks.DraftingTableEntity;
import com.pointlessbuilding.journal.items.Blueprint;
import com.pointlessbuilding.journal.network.Network;
import com.pointlessbuilding.journal.network.packets.BlueprintCompletePacket;
import com.pointlessbuilding.journal.utility.BoundaryMath;
import com.pointlessbuilding.journal.utility.SandboxWorldGenRegion;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.registries.ForgeRegistries;

public class BlueprintEvaluator {
    
    private static final Logger LOGGER = LogUtils.getLogger();

    // Baseline Cache - store individual chunks in cache and reaccess them on repeat calls
    // private static final int MAX_CACHE_SIZE = 200;
    // private static final Map<ChunkPos, ChunkAccess> BASELINE_CACHE = Collections.synchronizedMap(
    //     new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
    //         @Override
    //         protected boolean removeEldestEntry(Map.Entry<ChunkPos, ChunkAccess> eldest) {
    //             return size() > MAX_CACHE_SIZE;
    //         }
    //     }
    // );

    public static void evaluate(ServerPlayer player, BlockPos pos, String name) {
        LOGGER.debug("evaluate() called for {} at {}", name, pos);

        // Validation step: check whether the current state is valid
        if(!validate(player, pos)) {
            LOGGER.warn("Drafting Table Validation failed for " + name + "! at " + pos.toString());
            return;
        }
        //LOGGER.info("Validation passed");

        // Filter the boxes in the compass NBT to the current dimension and get the valid chunks area
        ServerLevel level = player.serverLevel();
        String dimension = level.dimension().location().toString();
        DraftingTableEntity tableEntity = ((DraftingTableEntity) level.getBlockEntity(pos));
        ItemStack compass = tableEntity.getItems().getStackInSlot(DraftingTableEntity.COMPASS_SLOT);
        tableEntity.setProcessing(true);

        List<CompoundTag> boxes = filterBoxes(compass, dimension);
        if(boxes.isEmpty()) {
            LOGGER.warn("No boxes found for dimension " + dimension);
            Network.sendToClient(new BlueprintCompletePacket(pos), player);
            return;
        }
        //LOGGER.info("Boxes filtered: {} boxes", boxes.size());

        // Get the set of chunks required to load (with a border of 8 because of seed generation) and the actual chunks to be diffed
        Set<ChunkPos> diffChunks = getRequiredChunks(boxes, 0);
        Set<ChunkPos> loadChunks = getRequiredChunks(boxes, 8);
        //LOGGER.info("diffChunks: {}, loadChunks: {}", diffChunks.size(), loadChunks.size());

        Set<ChunkPos> forcedChunks = new HashSet<>();
        List<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> futures = new ArrayList<>();
        for(ChunkPos chunk : loadChunks) {
            if (!level.hasChunk(chunk.x, chunk.z)) {
                forcedChunks.add(chunk);
                level.setChunkForced(chunk.x, chunk.z, true);
            }
            futures.add(level.getChunkSource().getChunkFuture(chunk.x, chunk.z, ChunkStatus.FULL, true));
        }

        // Background thread: compute the diff
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenRunAsync(() -> {
            //LOGGER.info("Background thread started");

            Map<ChunkPos, ChunkAccess> chunkCache = new HashMap<>();
            for (ChunkPos cp : loadChunks) {
                ChunkAccess chunk = level.getChunk(cp.x, cp.z, ChunkStatus.FULL, false);
                if (chunk != null) chunkCache.put(cp, chunk);
            }

            DiffResult diffResult = null;

            try {
                diffResult = computeDiff(level, boxes, diffChunks, chunkCache);
            } finally {
                level.getServer().execute(() -> unloadChunks(level, forcedChunks));
            }
            if (diffResult != null) {
                final Map<String, long[]> finalCounts = diffResult.counts();
                List<int[]> finalBoxMins = diffResult.boxMins();
                List<int[]> finalBoxMaxs = diffResult.boxMaxs();
                final long finalModifiedCount = diffResult.modifiedCount();
                level.getServer().execute(() -> {
                    //LOGGER.info("Back on main thread, writing blueprint");

                    // After diff calculation, take all the data and put it into the blueprint item
                    ListTag blockCounts = new ListTag();
                    for (Map.Entry<String, long[]> entry : finalCounts.entrySet()) {
                        CompoundTag entryTag = new CompoundTag();
                        entryTag.putString(Blueprint.TAG_BLOCK, entry.getKey());
                        entryTag.putLong(Blueprint.TAG_ADDED, entry.getValue()[0]);
                        entryTag.putLong(Blueprint.TAG_REMOVED, entry.getValue()[1]);
                        blockCounts.add(entryTag);
                    }

                    ListTag boxesTag = new ListTag();
                    for(int i = 0; i < finalBoxMins.size(); i++) {
                        int[] min = finalBoxMins.get(i);
                        int[] max = finalBoxMaxs.get(i);
                        CompoundTag boxTag = new CompoundTag();
                        boxTag.putIntArray("FirstPos", new int[]{min[0], min[1], min[2]});
                        boxTag.putIntArray("SecondPos", new int[]{max[0], max[1], max[2]});
                        boxesTag.add(boxTag);
                    }

                    long unionVolume = BoundaryMath.unionVolume(finalBoxMins, finalBoxMaxs);

                    ItemStack blueprintStack = Blueprint.create(name, dimension, boxesTag, blockCounts, finalModifiedCount, unionVolume);
                    if(!(name.equals("Blueprint"))) blueprintStack.setHoverName(Component.literal(name));

                    if(!(level.getBlockEntity(pos) instanceof DraftingTableEntity table)) {
                        LOGGER.warn("DraftingTableEntity no longer exists at " + pos);
                        return;
                    }
                    table.getItems().setStackInSlot(DraftingTableEntity.BLUEPRINT_SLOT, blueprintStack);
                    level.playSound(null, table.getBlockPos(), SoundEvents.VILLAGER_WORK_LIBRARIAN, SoundSource.BLOCKS, 5f, 1f);
                    table.setProcessing(false);

                    Network.sendToClient(new BlueprintCompletePacket(pos), player);
                });
            } 
        }, Util.backgroundExecutor());

    }

    private static boolean validate(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        // Is the block a drafting table?
        if(!(level.getBlockEntity(pos) instanceof DraftingTableEntity table)) return false;
        // Does the table have a compass?
        if(table.getItems().getStackInSlot(DraftingTableEntity.COMPASS_SLOT).isEmpty()) return false;
        // Is the blueprint slot empty?
        if(!table.getItems().getStackInSlot(DraftingTableEntity.BLUEPRINT_SLOT).isEmpty()) return false;
        // Is the player close enough?
        if(player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) return false;
        return true;
    }

    private static List<CompoundTag> filterBoxes(ItemStack compass, String dimension) {
        ListTag boxes = compass.getOrCreateTag().getList("StoredBoxes", Tag.TAG_COMPOUND);
        List<CompoundTag> filtered = new ArrayList<>();
        for(int i = 0; i < boxes.size(); i++) {
            CompoundTag box = boxes.getCompound(i);
            if(box.getString("Dimension").equals(dimension)) filtered.add(box);
        }
        return filtered;
    }

    private static Set<ChunkPos> getRequiredChunks(List<CompoundTag> boxes, int border) {
        Set<ChunkPos> loaded = new HashSet<>();
        
        for(CompoundTag box : boxes) {
            int[] first = box.getIntArray("FirstPos");
            int[] second = box.getIntArray("SecondPos");

            // Get chunk coord
            int minX = (Math.min(first[0], second[0]) >> 4) - border;
            int minZ = (Math.min(first[2], second[2]) >> 4) - border;
            int maxX = (Math.max(first[0], second[0]) >> 4) + border;
            int maxZ = (Math.max(first[2], second[2]) >> 4) + border;
            
            for(int cx = minX; cx <= maxX; cx++) {
                for(int cz = minZ; cz <= maxZ; cz++) {
                    loaded.add(new ChunkPos(cx, cz));
                }
            }
        }

        return loaded;
    }

    private static void unloadChunks(ServerLevel level, Set<ChunkPos> forcedChunks) {
        for(ChunkPos chunk : forcedChunks) {
            level.setChunkForced(chunk.x, chunk.z, false);
        }
    }

    private record DiffResult(Map<String, long[]> counts, List<int[]> boxMins, List<int[]> boxMaxs, long modifiedCount) {}

    private static DiffResult computeDiff(ServerLevel level, List<CompoundTag> boxes, Set<ChunkPos> diffChunks, Map<ChunkPos, ChunkAccess> chunkCache) {

        List<int[]> mins = new ArrayList<>();
        List<int[]> maxs = new ArrayList<>();

        List<int[]> diffMins = new ArrayList<>();
        List<int[]> diffMaxs = new ArrayList<>();

        long modifiedCount = 0;

        for(CompoundTag box : boxes) {
            int[] first = box.getIntArray("FirstPos");
            int[] second = box.getIntArray("SecondPos");
            mins.add(new int[]{Math.min(first[0],second[0]), Math.min(first[1],second[1]), Math.min(first[2],second[2])});
            maxs.add(new int[]{Math.max(first[0],second[0]), Math.max(first[1],second[1]), Math.max(first[2],second[2])});

            diffMins.add(null);
            diffMaxs.add(null);
        }

        Map<String, long[]> counts = new HashMap<>();

        ExecutorService chunkGenExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<Future<?>> generatedFutures = new ArrayList<>();
        Map<ChunkPos, ChunkAccess> results = new ConcurrentHashMap<>();

        for (ChunkPos chunk: diffChunks) {
            generatedFutures.add(chunkGenExecutor.submit(() -> {
                    ChunkAccess genChunk = generateBaselineChunk(level, chunk, level.getChunkSource().getGenerator(), chunkCache);
                    results.put(chunk, genChunk);
                }
            ));
        }

        for (Future<?> f : generatedFutures) {
           try {
            f.get();
           }
            catch (Exception e) {
                throw new RuntimeException("Chunk generation failed", e.getCause());
            }
        }

        for(ChunkPos chunk : diffChunks) {

            ChunkAccess liveChunk = chunkCache.get(chunk);

            ChunkAccess baselineChunk = results.get(chunk);

            MutableBlockPos bp = new BlockPos.MutableBlockPos();

            for (int x = chunk.x * 16; x < chunk.x * 16 + 16; x++) {
                for (int z = chunk.z * 16; z < chunk.z * 16 + 16; z++) {
                    List<int[]> yIntervals = BoundaryMath.mergeYIntervals(x,z, mins, maxs);
                    for (int[] interval : yIntervals) {
                        for (int y = interval[0]; y <= interval[1]; y++) {
                            //BlockPos bp = new BlockPos(x,y,z);
                            bp.set(x,y,z);
                            BlockState live = liveChunk.getBlockState(bp);
                            BlockState baseline = baselineChunk.getBlockState(bp);

                            boolean liveAir = live.isAir();
                            boolean baseAir = baseline.isAir();
                            boolean isModified = false;

                            if(!liveAir && baseAir) {   // Added block
                                isModified = true;
                                String key = ForgeRegistries.BLOCKS.getKey(live.getBlock()).toString();
                                counts.computeIfAbsent(key, k -> new long[2])[0]++;
                            }
                            else if(liveAir && !baseAir) {  //Removed block
                                isModified = true;
                                String key = ForgeRegistries.BLOCKS.getKey(baseline.getBlock()).toString();
                                counts.computeIfAbsent(key, k -> new long[2])[1]++;
                            }
                            else if(!liveAir && !baseAir && live.getBlock() != baseline.getBlock()) {   //Replaced block
                                isModified = true;
                                String liveKey = ForgeRegistries.BLOCKS.getKey(live.getBlock()).toString();
                                String baseKey = ForgeRegistries.BLOCKS.getKey(baseline.getBlock()).toString();
                                counts.computeIfAbsent(liveKey, k -> new long[2])[0]++;
                                counts.computeIfAbsent(baseKey, k -> new long[2])[1]++;
                            }

                            if(isModified) {
                                modifiedCount++;
                                // Check if position is in which box
                                for (int i = 0; i < diffMins.size(); i++) {
                                    
                                    int[] boxMin = mins.get(i);
                                    int[] boxMax = maxs.get(i);
                                    if (x < boxMin[0] || x > boxMax[0]) continue;
                                    if (y < boxMin[1] || y > boxMax[1]) continue;
                                    if (z < boxMin[2] || z > boxMax[2]) continue;

                                    if(diffMins.get(i) == null) {
                                        diffMins.set(i, new int[]{x,y,z});
                                        diffMaxs.set(i, new int[]{x,y,z});
                                    }
                                    else {
                                        int[] dMin = diffMins.get(i);
                                        int[] dMax = diffMaxs.get(i);
                                        dMin[0] = Math.min(dMin[0], x);
                                        dMin[1] = Math.min(dMin[1], y);
                                        dMin[2] = Math.min(dMin[2], z);
                                        dMax[0] = Math.max(dMax[0], x);
                                        dMax[1] = Math.max(dMax[1], y);
                                        dMax[2] = Math.max(dMax[2], z);
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        chunkGenExecutor.shutdown();
        //LOGGER.info("Active threads: {}", Thread.activeCount());

        List<int[]> resultMins = new ArrayList<>();
        List<int[]> resultMaxs = new ArrayList<>();
        for (int i = 0; i < diffMins.size(); i++) {
            if (diffMins.get(i) != null) {
                resultMins.add(diffMins.get(i));
                resultMaxs.add(diffMaxs.get(i));
            }
        }

        return new DiffResult(counts, resultMins, resultMaxs, modifiedCount);
    }

    private static ChunkAccess generateBaselineChunk(ServerLevel level, ChunkPos chunkPos, ChunkGenerator gen, Map<ChunkPos, ChunkAccess> chunkCache) {

        ProtoChunk proto = new ProtoChunk(chunkPos, UpgradeData.EMPTY, level, level.registryAccess().registryOrThrow(Registries.BIOME), null);

        gen.fillFromNoise(Util.backgroundExecutor(), Blender.empty(), level.getChunkSource().randomState(), level.structureManager(), proto).join();
        //LOGGER.info("fillFromNoise complete");

        List<ChunkAccess> regionChunks = new ArrayList<>();
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                if (dx == 0 && dz == 0) regionChunks.add(proto);
                else {
                    ChunkAccess neighbor = chunkCache.get(new ChunkPos(chunkPos.x + dx, chunkPos.z + dz));
                    if (neighbor != null) regionChunks.add(neighbor);
                }
            }
        }
        if(regionChunks.size() != 17*17) {
            LOGGER.warn("Expected 289 chunks, got {}. Skipping baseline region for {}.", regionChunks.size(), chunkPos);
            return proto;
        }
        SandboxWorldGenRegion region = new SandboxWorldGenRegion(level, regionChunks, ChunkStatus.SURFACE, 0);
        //LOGGER.info("WorldGenRegion created.");

        if (gen instanceof NoiseBasedChunkGenerator noiseGen) {
            //LOGGER.info("NoiseBasedChunkGenerator buildSurface called...");
            WorldGenerationContext context = new WorldGenerationContext(noiseGen, region);
            noiseGen.buildSurface(proto, context, level.getChunkSource().randomState(), 
                level.structureManager(), level.getBiomeManager(), 
                level.registryAccess().registryOrThrow(Registries.BIOME), Blender.empty());
        } else {
            gen.buildSurface(region, level.structureManager(), level.getChunkSource().randomState(), proto);
        }
        //LOGGER.info("buildSurface complete");

        gen.applyCarvers(region, level.getSeed(), level.getChunkSource().randomState(), level.getBiomeManager(), level.structureManager(), proto, GenerationStep.Carving.AIR);
        gen.applyCarvers(region, level.getSeed(), level.getChunkSource().randomState(), level.getBiomeManager(), level.structureManager(), proto, GenerationStep.Carving.LIQUID);
        //LOGGER.info("applyCarvers complete");

        gen.createStructures(level.registryAccess(), level.getChunkSource().getGeneratorState(), level.structureManager(), proto, level.getStructureManager());
        //LOGGER.info("createStructures complete");

        BoundingBox chunkBox = BoundingBox.fromCorners(
            new Vec3i(chunkPos.getMinBlockX(), level.getMinBuildHeight(), chunkPos.getMinBlockZ()),
            new Vec3i(chunkPos.getMaxBlockX(), level.getMaxBuildHeight(), chunkPos.getMaxBlockZ())
        );

        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(level.getSeed() ^ chunkPos.toLong()));

        SectionPos sectionPos = SectionPos.bottomOf(proto);
        level.registryAccess().registryOrThrow(Registries.STRUCTURE).stream().forEach(structure -> {
            level.structureManager().startsForStructure(sectionPos, structure).forEach(start -> {
                start.placeInChunk(region, level.structureManager(), gen, random, chunkBox, chunkPos);
            });
        });
        //LOGGER.info("Structure placeInChunk complete");
        
        return proto;
    }

}
