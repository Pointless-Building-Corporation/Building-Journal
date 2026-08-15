package com.pointlessbuilding.journal.network.packets;

import java.time.LocalDate;
import java.util.function.Supplier;

import com.pointlessbuilding.journal.commission.CommissionCompleteTrigger;
import com.pointlessbuilding.journal.commission.CommissionProgress;
import com.pointlessbuilding.journal.commission.CommissionUnlock;
import com.pointlessbuilding.journal.menu.CommissionContainer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

public class CommissionSubmitPacket {
    
    private final String commissionId;

    public CommissionSubmitPacket(String commissionId) {
        this.commissionId = commissionId;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(commissionId);
    }

    public static CommissionSubmitPacket decode(FriendlyByteBuf buf) {
        return new CommissionSubmitPacket(buf.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if(player == null) return;
            if(!(player.containerMenu instanceof CommissionContainer container)) return;
            if(!container.getId().equals(commissionId)) return;
            if(!container.isSubmitActive()) return;

            boolean isDaily = commissionId.startsWith("daily_");

            player.getCapability(CommissionProgress.COMMISSION_PROGRESS).ifPresent(progress -> {
                if (isDaily) {
                    long today = LocalDate.now().toEpochDay();
                    if (progress.getLastCompletionDay() == today) return;
                    progress.checkStreakExtension(today);
                }

                if (progress.isCompleted(commissionId)) return;
                progress.markCompleted(commissionId);

                CommissionCompleteTrigger.INSTANCE.trigger(player);

                for (CommissionUnlock unlock : container.getUnlocks()) {
                    unlock.apply(player);
                }

                player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1f);
                player.closeContainer();
            });
        });
        ctx.get().setPacketHandled(true);
    }

}
