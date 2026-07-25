package com.pointlessbuilding.journal.network.packets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.pointlessbuilding.journal.client.ClientCommonEvents;
import com.pointlessbuilding.journal.commission.CommissionCardData;
import com.pointlessbuilding.journal.commission.CommissionState;
import com.pointlessbuilding.journal.gui.JournalUI;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SyncCardCommissionsPacket {
    
    private final List<CommissionCardData> cards;

    public SyncCardCommissionsPacket(List<CommissionCardData> cards) {
        this.cards = cards;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(cards.size());
        for(CommissionCardData card : cards) {
            buf.writeUtf(card.id());
            buf.writeUtf(card.title());
            buf.writeByteArray(card.thumbnailBytes());
            buf.writeEnum(card.state());
        }
    }

    public static SyncCardCommissionsPacket decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<CommissionCardData> cards = new ArrayList<>();
        for(int i = 0; i < count; i++) {
            String id = buf.readUtf();
            String title = buf.readUtf();
            byte[] thumbnailBytes = buf.readByteArray();
            CommissionState state = buf.readEnum(CommissionState.class);
            cards.add(new CommissionCardData(id, title, thumbnailBytes, state));
        }
        return new SyncCardCommissionsPacket(cards);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientCommonEvents.updateCards(this.cards);

            if(Minecraft.getInstance().screen instanceof JournalUI ui) {
                ui.refreshCards();
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
