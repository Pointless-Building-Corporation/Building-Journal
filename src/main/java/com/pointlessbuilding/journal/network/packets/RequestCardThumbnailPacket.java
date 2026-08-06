package com.pointlessbuilding.journal.network.packets;

import java.util.function.Supplier;

import com.pointlessbuilding.journal.commission.CommissionLoader;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class RequestCardThumbnailPacket {
    
    private String commission_id;

    public RequestCardThumbnailPacket(String commission_id) {
        this.commission_id = commission_id;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(commission_id);
    }

    public static RequestCardThumbnailPacket decode(FriendlyByteBuf buf) {
        String commission_id = buf.readUtf();
        return new RequestCardThumbnailPacket(commission_id);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            CommissionLoader.sendCommissionThumbnail(player, commission_id);
        });
        ctx.get().setPacketHandled(true);
    }

}
