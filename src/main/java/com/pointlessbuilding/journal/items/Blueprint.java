package com.pointlessbuilding.journal.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.pointlessbuilding.journal.Registration;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class Blueprint extends Item{

    public static final String TAG_NAME = "BlueprintName";
    public static final String TAG_DIMENSION = "Dimension";
    public static final String TAG_BIOME = "Biome";
    public static final String TAG_BOXES = "Boxes";
    public static final String TAG_FIRSTPOS = "FirstPos";
    public static final String TAG_SECONDPOS = "SecondPos";
    public static final String TAG_BLOCK_COUNTS = "BlockCounts";
    public static final String TAG_BLOCK = "Block";
    public static final String TAG_ADDED = "Added";
    public static final String TAG_REMOVED = "Removed";
    public static final String TAG_MODIFIED = "ModifiedCount";
    public static final String TAG_UNION_VOLUME = "UnionVolume";
    public static final String TAG_UUID = "UUID";

    public Blueprint(Properties properties) {
        super(properties);
    }
    
    public static ItemStack create(String name, String dimension, List<String> biome, List<BoxData> boxes, Map<String, long[]> blockCounts, long modifiedCount, long unionVolume) {
        ItemStack stack = new ItemStack(Registration.BLUEPRINT.get());
        stack.set(Registration.BLUEPRINT_DATA.get(), 
            new BlueprintData(name, dimension, biome, boxes, blockCounts, modifiedCount, unionVolume, UUID.randomUUID()));
        return stack;
    }

    public static String getBlueprintName(ItemStack stack) {
        BlueprintData data = stack.get(Registration.BLUEPRINT_DATA.get());
        return data == null ? "" : data.name();
    }

    public static String getDimension(ItemStack stack) {
        BlueprintData data = stack.get(Registration.BLUEPRINT_DATA.get());
        return data == null ? "" : data.dimension();
    }

    public static List<String> getBiome(ItemStack stack) {
        BlueprintData data = stack.get(Registration.BLUEPRINT_DATA.get());
        return data == null ? List.of() : data.biomes();
    }

    public static List<BoxData> getBoxes(ItemStack stack) {
        BlueprintData data = stack.get(Registration.BLUEPRINT_DATA.get());
        return data == null ? List.of() : data.boxes();
    }

    public static Map<String, long[]> getBlockCounts(ItemStack stack) {
        BlueprintData data = stack.get(Registration.BLUEPRINT_DATA.get());
        return data == null ? Map.of() : data.blockCounts();
    }

    public static long getModifiedCount(ItemStack stack) {
        BlueprintData data = stack.get(Registration.BLUEPRINT_DATA.get());
        return data == null ? 0 : data.modifiedCount();
    }

    public static long getUnionVolume(ItemStack stack) {
        BlueprintData data = stack.get(Registration.BLUEPRINT_DATA.get());
        return data == null ? 0 : data.unionVolume();
    }

    public static UUID getUUID(ItemStack stack) {
        BlueprintData data = stack.get(Registration.BLUEPRINT_DATA.get());
        return data == null ? null: data.uuid();
    }

}
