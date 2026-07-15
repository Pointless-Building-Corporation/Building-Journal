package com.pointlessbuilding.journal.commission.unlocks;

import com.pointlessbuilding.journal.commission.CommissionUnlock;

import net.minecraft.server.level.ServerPlayer;

public class ExpRewardUnlock  implements CommissionUnlock{

    public static enum ExpType { POINTS, LEVELS };

    private final int expAmount;
    private final ExpType expType;

    public ExpRewardUnlock(int expAmount, ExpType expType) {
        this.expAmount = expAmount;
        this.expType = expType;
    }

    @Override
    public void apply(ServerPlayer player) {
        if (expType == ExpType.POINTS) player.giveExperiencePoints(expAmount);
        else player.giveExperienceLevels(expAmount);
    }
    
}
