package com.pointlessbuilding.journal.network.packets;

import java.util.List;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.client.ClientCommonEvents;
import com.pointlessbuilding.journal.commission.CommissionCardData;
import com.pointlessbuilding.journal.gui.JournalUI;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncCardCommissionsPacket implements CustomPacketPayload{
    
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

    public static final Type<SyncCardCommissionsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "sync_card_commissions_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCardCommissionsPacket> STREAM_CODEC = StreamCodec.composite(
        CommissionCardData.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncCardCommissionsPacket::getCards,
        ByteBufCodecs.VAR_LONG, SyncCardCommissionsPacket::getNextRestEpochMillis,
        ByteBufCodecs.VAR_INT, SyncCardCommissionsPacket::getCurStreak,
        ByteBufCodecs.VAR_INT, SyncCardCommissionsPacket::getMaxStreak,
        ByteBufCodecs.VAR_INT, SyncCardCommissionsPacket::getCompletionCount,
        SyncCardCommissionsPacket::new
    );

    public List<CommissionCardData> getCards() {
        return cards;
    }

    public long getNextRestEpochMillis() {
        return nextResetEpochMillis;
    }

    public int getCurStreak() {
        return currentStreak;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public int getCompletionCount() {
        return completionCount;
    }

    public static void handle(SyncCardCommissionsPacket packet, IPayloadContext ctx) {
        ClientCommonEvents.updateCards(packet.cards);
        ClientCommonEvents.updateNextResetTime(packet.nextResetEpochMillis);
        ClientCommonEvents.updateStats(packet.currentStreak, packet.maxStreak, packet.completionCount);

        if(Minecraft.getInstance().screen instanceof JournalUI ui) {
            ui.refreshCards();
        }
    }

}
