package com.pointlessbuilding.journal.commission;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.pointlessbuilding.journal.items.BlueprintData;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record EvaluationResult(
    UUID id,
    String name,
    String dimension,
    List<String> biomes,
    long unionVolume,
    long modifiedCount,
    List<Box> boxes,
    Map<ResourceLocation, BlockCounts> blockData
) {
    public record Box(BlockPos firstPos, BlockPos secondPos) {}
    public record BlockCounts(long added, long removed) {}

    public static EvaluationResult fromData(BlueprintData data) {

        UUID id = data.uuid();
        String name = data.name();
        String dimension = data.dimension();
        long unionVolume = data.unionVolume();
        long modifiedCount = data.modifiedCount();

        List<String> biomes = data.biomes();

        List<Box> boxes = data.boxes().stream().map(boxData -> {
            int[] first = boxData.firstPos();
            int[] second = boxData.secondPos();
            return new Box(new BlockPos(first[0], first[1], first[2]), new BlockPos(second[0], second[1], second[2]));
        }).toList();

        Map<ResourceLocation, BlockCounts> blockData = data.blockCounts().entrySet().stream()
            .collect(Collectors.toMap(
                entry -> ResourceLocation.tryParse(entry.getKey()),
                entry -> new BlockCounts(entry.getValue()[0], entry.getValue()[1])
            ));

        return new EvaluationResult(id, name, dimension, biomes, unionVolume, modifiedCount, boxes, blockData);
    }
}
