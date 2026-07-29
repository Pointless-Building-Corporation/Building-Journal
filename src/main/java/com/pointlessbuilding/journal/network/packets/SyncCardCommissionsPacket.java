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
    private final long nextResetEpochMillis;
    private int currentStreak;
    private int maxStreak;
    private int completionCount;

    public SyncCardCommissionsPacket(List<CommissionCardData> cards, long nextResetEpochMillis, int currentStreak, int maxStreak, int completionCount) {
        this.cards = cards;
        this.nextResetEpochMillis = nextResetEpochMillis;
        this.currentStreak = currentStreak;
        this.maxStreak = maxStreak;
        this.completionCount = completionCount;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(cards.size());
        for(CommissionCardData card : cards) {
            buf.writeUtf(card.id());
            buf.writeUtf(card.title());
            buf.writeByteArray(card.thumbnailBytes());
            buf.writeEnum(card.state());
        }
        buf.writeLong(nextResetEpochMillis);
        buf.writeInt(currentStreak);
        buf.writeInt(maxStreak);
        buf.writeInt(completionCount);
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
        long nextResetEpochMillis = buf.readLong();
        int currentStreak = buf.readInt();
        int maxStreak = buf.readInt();
        int completionCount = buf.readInt();
        return new SyncCardCommissionsPacket(cards, nextResetEpochMillis, currentStreak, maxStreak, completionCount);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientCommonEvents.updateCards(this.cards);
            ClientCommonEvents.updateNextResetTime(this.nextResetEpochMillis);
            ClientCommonEvents.updateStats(currentStreak, maxStreak, completionCount);

            if(Minecraft.getInstance().screen instanceof JournalUI ui) {
                ui.refreshCards();
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
