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

public class CommissionDetailPacket implements CustomPacketPayload{
    
    private final String commissionId;
    private final int commissionPage;

    public CommissionDetailPacket(String commissionId, int commissionPage) {
        this.commissionId = commissionId;
        this.commissionPage = commissionPage;
    }

    public static final Type<CommissionDetailPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "commission_detail_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, CommissionDetailPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, CommissionDetailPacket::getId,
        ByteBufCodecs.VAR_INT, CommissionDetailPacket::getPage,
        CommissionDetailPacket::new
    );

    public String getId() {
        return commissionId;
    }

    public int getPage() {
        return commissionPage;
    }

    public static void handle(CommissionDetailPacket packet, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        if (player == null) return;
        CommissionLoader.sendCommissionDetailData(player, packet.commissionId, packet.commissionPage);
    }

}
