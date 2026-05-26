package com.pointlessbuilding.journal.items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.pointlessbuilding.journal.Registration;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class Blueprint extends Item{

    public static final String TAG_NAME = "BlueprintName";
    public static final String TAG_DIMENSION = "Dimension";
    public static final String TAG_BOXES = "Boxes";
    public static final String TAG_FIRSTPOS = "FirstPos";
    public static final String TAG_SECONDPOS = "SecondPos";
    public static final String TAG_BLOCK_COUNTS = "BlockCounts";
    public static final String TAG_BLOCK = "Block";
    public static final String TAG_ADDED = "Added";
    public static final String TAG_REMOVED = "Removed";
    public static final String TAG_UNION_VOLUME = "UnionVolume";
    public static final String TAG_UUID = "UUID";

    public Blueprint(Properties properties) {
        super(properties);
    }
    
    public static ItemStack create(String name, String dimension, ListTag boxes, ListTag blockCounts, long unionVolume) {
        ItemStack stack = new ItemStack(Registration.BLUEPRINT.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_NAME, name);
        tag.putString(TAG_DIMENSION, dimension);
        tag.put(TAG_BOXES, boxes);
        tag.put(TAG_BLOCK_COUNTS, blockCounts);
        tag.putLong(TAG_UNION_VOLUME, unionVolume);
        tag.put(TAG_UUID, NbtUtils.createUUID(UUID.randomUUID()));
        return stack;
    }

    public static String getBlueprintName(ItemStack stack) {
        if (!stack.hasTag()) return "";
        return stack.getTag().getString(TAG_NAME);
    }

    public static String getDimension(ItemStack stack) {
        if (!stack.hasTag()) return "";
        return stack.getTag().getString(TAG_DIMENSION);
    }

    public static ListTag getBoxes(ItemStack stack) {
        if (!stack.hasTag()) return new ListTag();
        return stack.getTag().getList(TAG_BOXES, Tag.TAG_COMPOUND);
    }

    public static Map<String, long[]> getBlockCounts(ItemStack stack) {
        Map<String, long[]> result = new HashMap<>();
        if(!stack.hasTag()) return result;
        ListTag list = stack.getTag().getList(TAG_BLOCK_COUNTS, Tag.TAG_COMPOUND);
        for(int i = 0; i < list.size(); i++) {
            CompoundTag block = list.getCompound(i);
            result.put(block.getString(TAG_BLOCK), new long[] {
                block.getLong(TAG_ADDED),
                block.getLong(TAG_REMOVED)
            });
        }
        return result;
    }

    public static long getUnionVolume(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        return stack.getTag().getLong(TAG_UNION_VOLUME);
    }

    public static UUID getUUID(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_UUID)) return null;
        return NbtUtils.loadUUID(stack.getTag().get(TAG_UUID));
    }

}
