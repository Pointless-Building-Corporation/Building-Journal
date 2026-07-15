package com.pointlessbuilding.journal.commission.unlocks;

import com.pointlessbuilding.journal.commission.CommissionProgress;
import com.pointlessbuilding.journal.commission.CommissionUnlock;

import net.minecraft.server.level.ServerPlayer;

public class LockedCommissionUnlock implements CommissionUnlock{

    private final String commissionId;

    public LockedCommissionUnlock(String commissionId) {
        this.commissionId = commissionId;
    }

    @Override
    public void apply(ServerPlayer player) {
        player.getCapability(CommissionProgress.COMMISSION_PROGRESS)
            .ifPresent(progress -> progress.markCompleted(commissionId));
    }
    
}
