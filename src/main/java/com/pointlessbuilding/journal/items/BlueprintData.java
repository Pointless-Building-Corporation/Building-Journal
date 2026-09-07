package com.pointlessbuilding.journal.items;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.LongStream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;

public record BlueprintData(
    String name,
    String dimension,
    List<String> biomes,
    List<BoxData> boxes,
    Map<String, long[]> blockCounts,
    long modifiedCount,
    long unionVolume,
    UUID uuid
) {
    
    public static final Codec<BlueprintData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf(Blueprint.TAG_NAME).forGetter(BlueprintData::name),
        Codec.STRING.fieldOf(Blueprint.TAG_DIMENSION).forGetter(BlueprintData::dimension),
        Codec.STRING.listOf().fieldOf(Blueprint.TAG_BIOME).forGetter(BlueprintData::biomes),
        BoxData.CODEC.listOf().fieldOf(Blueprint.TAG_BOXES).forGetter(BlueprintData::boxes),
        Codec.unboundedMap(Codec.STRING, Codec.LONG_STREAM.xmap(LongStream::toArray, LongStream::of))
            .fieldOf(Blueprint.TAG_BLOCK_COUNTS).forGetter(BlueprintData::blockCounts),
        Codec.LONG.fieldOf(Blueprint.TAG_MODIFIED).forGetter(BlueprintData::modifiedCount),
        Codec.LONG.fieldOf(Blueprint.TAG_UNION_VOLUME).forGetter(BlueprintData::unionVolume),
        UUIDUtil.CODEC.fieldOf(Blueprint.TAG_UUID).forGetter(BlueprintData::uuid)
    ).apply(instance, BlueprintData::new));

}
