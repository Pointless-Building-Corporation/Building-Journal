package com.pointlessbuilding.journal.network.packets;

import java.util.function.Supplier;

import com.pointlessbuilding.journal.commission.CommissionLoader;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class RequestCardCommissionsPacket {
    
    public RequestCardCommissionsPacket() {
        // nothing
    }

    public void encode(FriendlyByteBuf buf) {
        // nothing
    }

    public static RequestCardCommissionsPacket decode(FriendlyByteBuf buf) {
        return new RequestCardCommissionsPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            CommissionLoader.sendCommissionCardData(player);
        });
        ctx.get().setPacketHandled(true);
    }

}
