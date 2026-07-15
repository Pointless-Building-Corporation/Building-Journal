package com.pointlessbuilding.journal.commission;

import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface ICommissionProgress {
    
    boolean isCompleted(String commissionId);
    void markCompleted(String commissionId);
    Set<String> getCompletedCommissions();

    int getCompletionCount();

    int getCurrentStreak();

    int getMaxStreak();

    long getLastCompletionDay();
    void checkStreakExtension(long dayEpoch);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag tag);

}
