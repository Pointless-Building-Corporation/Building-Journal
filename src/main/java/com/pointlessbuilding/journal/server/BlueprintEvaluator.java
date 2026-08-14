package com.pointlessbuilding.journal.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.blocks.DraftingTableEntity;
import com.pointlessbuilding.journal.items.Blueprint;
import com.pointlessbuilding.journal.network.Network;
import com.pointlessbuilding.journal.network.packets.BlueprintCompletePacket;
import com.pointlessbuilding.journal.utility.BoundaryMath;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.registries.ForgeRegistries;

public class BlueprintEvaluator {
    
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void evaluate(ServerPlayer player, BlockPos pos, String name) {
        LOGGER.debug("evaluate() called for {} at {}", name, pos);

        // Validation step: check whether the current state is valid
        if(!validate(player, pos)) {
            LOGGER.warn("Drafting Table Validation failed for " + name + "! at " + pos.toString());
            return;
        }
        // LOGGER.info("Validation passed");

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
        // LOGGER.info("Boxes filtered: {} boxes", boxes.size());

        Set<ChunkPos> diffChunks = getRequiredChunks(boxes, 0);

        // Force load these chunks in the real level
        Set<ChunkPos> forcedChunks = new HashSet<>();
        List<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> futures = new ArrayList<>();
        for(ChunkPos chunk : diffChunks) {
            if (!level.hasChunk(chunk.x, chunk.z)) {
                forcedChunks.add(chunk);
                level.setChunkForced(chunk.x, chunk.z, true);
            }
            futures.add(level.getChunkSource().getChunkFuture(chunk.x, chunk.z, ChunkStatus.FULL, true));
        }

        // Create the evil fake level of this and force load the chunks there too
        ServerLevel fakeLevel = FakeDimension.getOrCreateFakeLevel(
            level.getServer(),
            FakeDimension.fakeKeyFor(level.dimension()),
            FakeDimension.fakeFactoryFor(level)
        );

        Set<ChunkPos> forcedFakeChunks = new HashSet<>();
        List<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> fakeFutures = new ArrayList<>();
        for (ChunkPos chunk : diffChunks) {
            if (!fakeLevel.hasChunk(chunk.x, chunk.z)) {
                forcedFakeChunks.add(chunk);
                fakeLevel.setChunkForced(chunk.x, chunk.z, true);
            }
            fakeFutures.add(fakeLevel.getChunkSource().getChunkFuture(chunk.x, chunk.z, ChunkStatus.FULL, true));
        }

        // Combine both the futures
        List<CompletableFuture<?>> allFutures = new ArrayList<>();
        allFutures.addAll(futures);
        allFutures.addAll(fakeFutures);

        // Background thread: compute the diff
        CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]))
        .thenRunAsync(() -> {
            // LOGGER.info("Background thread started");

            Map<ChunkPos, ChunkAccess> chunkCache = new HashMap<>();
            for (ChunkPos cp : diffChunks) {
                ChunkAccess chunk = level.getChunk(cp.x, cp.z, ChunkStatus.FULL, false);
                if (chunk != null) chunkCache.put(cp, chunk);
            }

            DiffResult diffResult = null;

            try {
                diffResult = computeDiff(level, fakeLevel, boxes, diffChunks, chunkCache);
            } finally {
                level.getServer().execute(() -> {
                    unloadChunks(level, forcedChunks);
                    unloadChunks(fakeLevel, forcedFakeChunks);
                });
            }
            if (diffResult != null) {
                final Map<String, long[]> finalCounts = diffResult.counts();
                List<int[]> finalBoxMins = diffResult.boxMins();
                List<int[]> finalBoxMaxs = diffResult.boxMaxs();
                final long finalModifiedCount = diffResult.modifiedCount();
                List<String> finalBiomes = diffResult.biomes().stream().map(key -> key.location().toString()).toList();
                level.getServer().execute(() -> {
                    // LOGGER.info("Back on main thread, writing blueprint");

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

                    ListTag biomesTag = new ListTag();
                    for(String biome : finalBiomes) {
                        biomesTag.add(StringTag.valueOf(biome));
                    }

                    long unionVolume = BoundaryMath.unionVolume(finalBoxMins, finalBoxMaxs);

                    ItemStack blueprintStack = Blueprint.create(name, dimension, biomesTag, boxesTag, blockCounts, finalModifiedCount, unionVolume);
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

    private record DiffResult(Map<String, long[]> counts, List<int[]> boxMins, List<int[]> boxMaxs, long modifiedCount, Set<ResourceKey<Biome>> biomes) {}

    private static DiffResult computeDiff(ServerLevel level, ServerLevel fakeLevel, List<CompoundTag> boxes, Set<ChunkPos> diffChunks, Map<ChunkPos, ChunkAccess> chunkCache) {

        List<int[]> mins = new ArrayList<>();
        List<int[]> maxs = new ArrayList<>();

        List<int[]> diffMins = new ArrayList<>();
        List<int[]> diffMaxs = new ArrayList<>();

        Set<ResourceKey<Biome>> biomes = new HashSet<>();

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

        for(ChunkPos chunk : diffChunks) {

            ChunkAccess liveChunk = chunkCache.get(chunk);

            ChunkAccess baselineChunk = fakeLevel.getChunk(chunk.x, chunk.z, ChunkStatus.FULL, false);
            if (baselineChunk == null) {
                LOGGER.warn("Fake evil chunk {} unexpectedly null after force-load completed, skipping in diff", chunk);
                continue;
            }

            MutableBlockPos bp = new BlockPos.MutableBlockPos();

            for (int x = chunk.x * 16; x < chunk.x * 16 + 16; x++) {
                for (int z = chunk.z * 16; z < chunk.z * 16 + 16; z++) {
                    List<int[]> yIntervals = BoundaryMath.mergeYIntervals(x,z, mins, maxs);
                    for (int[] interval : yIntervals) {

                        // Biomes
                        for (int y = interval[0]; y <= interval[1]; y += 4) {
                            biomes.add(level.getBiome(bp.set(x, y, z)).unwrapKey().orElseThrow());
                        }
                        if ((interval[1] - interval[0]) % 4 != 0) {
                            biomes.add(level.getBiome(bp.set(x, interval[1], z)).unwrapKey().orElseThrow());
                        }

    
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

        List<int[]> resultMins = new ArrayList<>();
        List<int[]> resultMaxs = new ArrayList<>();
        for (int i = 0; i < diffMins.size(); i++) {
            if (diffMins.get(i) != null) {
                resultMins.add(diffMins.get(i));
                resultMaxs.add(diffMaxs.get(i));
            }
        }

        return new DiffResult(counts, resultMins, resultMaxs, modifiedCount, biomes);
    }

}
