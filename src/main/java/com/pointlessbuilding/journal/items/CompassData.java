package com.pointlessbuilding.journal.items;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CompassData(
    boolean active,
    BlockPos firstPos,
    List<BoundaryData> storedBoxes
) {
    public static final Codec<CompassData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("Active").forGetter(CompassData::active),
        BlockPos.CODEC.optionalFieldOf("FirstPos", BlockPos.ZERO).forGetter(CompassData::firstPos),
        BoundaryData.CODEC.listOf().fieldOf("StoredBoxes").forGetter(CompassData::storedBoxes)
    ).apply(instance, CompassData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CompassData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, CompassData::active,
        BlockPos.STREAM_CODEC, CompassData::firstPos,
        BoundaryData.STREAM_CODEC.apply(ByteBufCodecs.list()), CompassData::storedBoxes,
        CompassData::new
    );

    public static final CompassData EMPTY = new CompassData(false, BlockPos.ZERO, List.of());
}
