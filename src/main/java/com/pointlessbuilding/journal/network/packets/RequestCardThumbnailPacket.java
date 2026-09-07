package com.pointlessbuilding.journal.network.packets;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionLoader;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestCardThumbnailPacket implements CustomPacketPayload{
    
    private String commissionId;

    public RequestCardThumbnailPacket(String commissionId) {
        this.commissionId = commissionId;
    }

    public static final Type<RequestCardThumbnailPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "request_card_thumbnail_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestCardThumbnailPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, RequestCardThumbnailPacket::getId,
        RequestCardThumbnailPacket::new
    );

    public String getId() {
        return commissionId;
    }

    public static void handle(RequestCardThumbnailPacket packet, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        CommissionLoader.sendCommissionThumbnail(player, packet.commissionId);
    }

}
