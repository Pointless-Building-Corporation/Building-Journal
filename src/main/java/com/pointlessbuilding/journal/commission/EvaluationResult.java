package com.pointlessbuilding.journal.commission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.pointlessbuilding.journal.items.Blueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public record EvaluationResult(
    UUID id,
    String name,
    String dimension,
    long unionVolume,
    long modifiedCount,
    List<Box> boxes,
    Map<ResourceLocation, BlockCounts> blockData
) {
    public record Box(BlockPos firstPos, BlockPos secondPos) {}
    public record BlockCounts(long added, long removed) {}

    public static EvaluationResult fromTag(CompoundTag tag) {

        UUID id = tag.getUUID(Blueprint.TAG_UUID);
        String name = tag.getString(Blueprint.TAG_NAME);
        String dimension = tag.getString(Blueprint.TAG_DIMENSION);
        long unionVolume = tag.getLong(Blueprint.TAG_UNION_VOLUME);
        long modifiedCount = tag.getLong(Blueprint.TAG_MODIFIED);

        List<Box> boxes = new ArrayList<>();
        ListTag boxList = tag.getList(Blueprint.TAG_BOXES, Tag.TAG_COMPOUND);
        for (int i = 0; i < boxList.size(); i++) {
            CompoundTag boxTag = boxList.getCompound(i);
            int[] first = boxTag.getIntArray("FirstPos");
            int[] second = boxTag.getIntArray("SecondPos");
            BlockPos firstPos = new BlockPos(first[0], first[1], first[2]);
            BlockPos secondPos = new BlockPos(second[0], second[1], second[2]);
            boxes.add(new Box(firstPos, secondPos));
        }

        Map<ResourceLocation, BlockCounts> blockData = new HashMap<>();
        ListTag countList = tag.getList(Blueprint.TAG_BLOCK_COUNTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < countList.size(); i++) {
            CompoundTag countTag = countList.getCompound(i);
            ResourceLocation blockId = ResourceLocation.tryParse(countTag.getString(Blueprint.TAG_BLOCK));
            long added = countTag.getLong(Blueprint.TAG_ADDED);
            long removed = countTag.getLong(Blueprint.TAG_REMOVED);
            blockData.put(blockId, new BlockCounts(added, removed));
        }

        return new EvaluationResult(id, name, dimension, unionVolume, modifiedCount, boxes, blockData);
    }
}
