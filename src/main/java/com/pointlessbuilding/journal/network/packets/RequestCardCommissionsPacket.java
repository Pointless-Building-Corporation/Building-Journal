package com.pointlessbuilding.journal.network.packets;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionLoader;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestCardCommissionsPacket implements CustomPacketPayload{
    
    public static final RequestCardCommissionsPacket INSTANCE = new RequestCardCommissionsPacket();

    public RequestCardCommissionsPacket() {
        // nothing
    }

    public static final Type<RequestCardCommissionsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "request_card_commissions_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestCardCommissionsPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handle(RequestCardCommissionsPacket packet, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        CommissionLoader.sendCommissionCardData(player);
    }

}
