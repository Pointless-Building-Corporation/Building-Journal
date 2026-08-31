package com.pointlessbuilding.journal.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BoundaryData(
    BlockPos firstPos,
    BlockPos secondPos,
    String dimension
) {
    public static final Codec<BoundaryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("FirstPos").forGetter(BoundaryData::firstPos),
        BlockPos.CODEC.fieldOf("SecondPos").forGetter(BoundaryData::secondPos),
        Codec.STRING.fieldOf("Dimension").forGetter(BoundaryData::dimension)
    ).apply(instance, BoundaryData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoundaryData> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, BoundaryData::firstPos,
        BlockPos.STREAM_CODEC, BoundaryData::secondPos,
        ByteBufCodecs.STRING_UTF8, BoundaryData::dimension,
        BoundaryData::new
    );
}