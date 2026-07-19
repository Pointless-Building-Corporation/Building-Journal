package com.pointlessbuilding.journal.network.packets;

import java.util.function.Supplier;

import com.pointlessbuilding.journal.commission.CommissionLoader;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class CommissionDetailPacket {
    
    private final String commissionId;
    private final int commissionPage;

    public CommissionDetailPacket(String commissionId, int commissionPage) {
        this.commissionId = commissionId;
        this.commissionPage = commissionPage;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(commissionId);
        buf.writeInt(commissionPage);
    }

    public static CommissionDetailPacket decode(FriendlyByteBuf buf) {
        String bufId = buf.readUtf();
        int bufPage = buf.readInt();
        return new CommissionDetailPacket(bufId, bufPage);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            CommissionLoader.sendCommissionDetailData(player, commissionId, commissionPage);
        });
        ctx.get().setPacketHandled(true);
    }

}
