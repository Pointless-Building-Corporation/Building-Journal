package com.pointlessbuilding.journal.network.packets;

import java.time.LocalDate;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.commission.CommissionCompleteTrigger;
import com.pointlessbuilding.journal.commission.CommissionProgress;
import com.pointlessbuilding.journal.commission.CommissionUnlock;
import com.pointlessbuilding.journal.event.CommissionCompletedEvent;
import com.pointlessbuilding.journal.menu.CommissionContainer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CommissionSubmitPacket implements CustomPacketPayload{
    
    private final String commissionId;

    public CommissionSubmitPacket(String commissionId) {
        this.commissionId = commissionId;
    }

    public static final Type<CommissionSubmitPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "commission_submit_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, CommissionSubmitPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, CommissionSubmitPacket::getId,
        CommissionSubmitPacket::new
    );

    public String getId() {
        return commissionId;
    }

    public static void handle(CommissionSubmitPacket packet, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();
        if(player == null) return;
        if(!(player.containerMenu instanceof CommissionContainer container)) return;
        if(!container.getId().equals(packet.commissionId)) return;
        if(!container.isSubmitActive()) return;

        boolean isDaily = packet.commissionId.startsWith("daily_");

        CommissionProgress progress = player.getData(Registration.COMMISSION_PROGRESS);
        if (isDaily) {
            long today = LocalDate.now().toEpochDay();
            if (progress.getLastCompletionDay() == today) return;
            progress.checkStreakExtension(today);
        }

        if (progress.isCompleted(packet.commissionId)) return;
        progress.markCompleted(packet.commissionId);

        CommissionCompleteTrigger.INSTANCE.trigger(player);

        for (CommissionUnlock unlock : container.getUnlocks()) {
            unlock.apply(player);
        }

        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1f);
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Commission Completed!").withStyle(ChatFormatting.GREEN)));
        NeoForge.EVENT_BUS.post(new CommissionCompletedEvent(player, packet.commissionId));

        player.closeContainer();
    }

}
