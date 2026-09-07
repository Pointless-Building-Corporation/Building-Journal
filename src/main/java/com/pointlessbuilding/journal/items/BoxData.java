package com.pointlessbuilding.journal.items;

import java.util.stream.IntStream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BoxData(
    int[] firstPos,
    int[] secondPos
) {
    public static final Codec<BoxData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT_STREAM.xmap(IntStream::toArray, IntStream::of).fieldOf("FirstPos").forGetter(BoxData::firstPos),
        Codec.INT_STREAM.xmap(IntStream::toArray, IntStream::of).fieldOf("SecondPos").forGetter(BoxData::secondPos)
    ).apply(instance, BoxData::new));
}
