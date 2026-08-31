package com.pointlessbuilding.journal.network.packets;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.client.ClientCommonEvents;
import com.pointlessbuilding.journal.gui.JournalUI;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncCardThumbnailPacket implements CustomPacketPayload{
    
    private String commissionId;
    private byte[] thumbnailBytes;

    public SyncCardThumbnailPacket(String commissionId, byte[] thumbnailBytes) {
        this.commissionId = commissionId;
        this.thumbnailBytes = thumbnailBytes;
    }

    public static final Type<SyncCardThumbnailPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "sync_card_thumbnail_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCardThumbnailPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, SyncCardThumbnailPacket::getId,
        ByteBufCodecs.BYTE_ARRAY, SyncCardThumbnailPacket::getThumbnailBytes,
        SyncCardThumbnailPacket::new
    );

    public String getId() {
        return commissionId;
    }

    public byte[] getThumbnailBytes() {
        return thumbnailBytes;
    }

    public static void handle(SyncCardThumbnailPacket packet, IPayloadContext ctx) {
        ClientCommonEvents.updateThumbnail(packet.commissionId, packet.thumbnailBytes);
        if(Minecraft.getInstance().screen instanceof JournalUI ui) {
            ui.refreshCardThumbnail(packet.commissionId);
        }
    }

}
