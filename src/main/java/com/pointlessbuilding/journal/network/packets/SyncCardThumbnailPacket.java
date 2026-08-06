package com.pointlessbuilding.journal.network.packets;

import java.util.function.Supplier;

import com.pointlessbuilding.journal.client.ClientCommonEvents;
import com.pointlessbuilding.journal.gui.JournalUI;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SyncCardThumbnailPacket {
    
    private String commission_id;
    private byte[] thumbnailBytes;

    public SyncCardThumbnailPacket(String commission_id, byte[] thumbnailBytes) {
        this.commission_id = commission_id;
        this.thumbnailBytes = thumbnailBytes;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(commission_id);
        buf.writeByteArray(thumbnailBytes);
    }

    public static SyncCardThumbnailPacket decode(FriendlyByteBuf buf) {
        String commission_id = buf.readUtf();
        byte[] thumbnailBytes = buf.readByteArray();
        return new SyncCardThumbnailPacket(commission_id, thumbnailBytes);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientCommonEvents.updateThumbnail(this.commission_id, this.thumbnailBytes);
            if(Minecraft.getInstance().screen instanceof JournalUI ui) {
                ui.refreshCardThumbnail(this.commission_id);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
