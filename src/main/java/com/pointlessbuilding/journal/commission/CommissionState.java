package com.pointlessbuilding.journal.commission;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum CommissionState {
    AVAILABLE,
    UNAVAILABLE,
    COMPLETED;

    public static final StreamCodec<ByteBuf, CommissionState> STREAM_CODEC = ByteBufCodecs.idMapper(id -> 
        CommissionState.values()[id], CommissionState::ordinal
    );
}