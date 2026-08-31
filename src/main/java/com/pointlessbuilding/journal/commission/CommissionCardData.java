package com.pointlessbuilding.journal.commission;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CommissionCardData(
    String id,
    String title,
    CommissionState state
) {
    public static final StreamCodec<RegistryFriendlyByteBuf, CommissionCardData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, CommissionCardData::id,
        ByteBufCodecs.STRING_UTF8, CommissionCardData::title,
        CommissionState.STREAM_CODEC, CommissionCardData::state,
        CommissionCardData::new
    );
}
