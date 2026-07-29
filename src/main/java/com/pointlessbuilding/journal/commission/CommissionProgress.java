package com.pointlessbuilding.journal.commission;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class CommissionProgress implements ICommissionProgress, ICapabilityProvider, INBTSerializable<CompoundTag>{
    
    public static final String TAG_COMPLETED = "CompletedCommisions";
    public static final String TAG_COMPLETION_COUNT = "CompletionCount";
    public static final String TAG_CURR_STREAK = "CurrentStreak";
    public static final String TAG_MAX_STREAK = "MaxStreak";
    public static final String TAG_LAST_COMPLETION_DAY = "LastCompletionDay";

    private final Set<String> completedCommissions = new HashSet<>();
    private int completionCount;
    private int currentStreak;
    private int maxStreak;
    private long lastCompletionDay;

    @Override
    public boolean isCompleted(String commissionId) {
        return completedCommissions.contains(commissionId);
    }   
    @Override
    public void markCompleted(String commissionId) {
        completedCommissions.add(commissionId);
        completionCount++;
    }
    @Override
    public void markIncomplete(String commissionId) {
        if(completedCommissions.contains(commissionId)) completionCount--;
        completedCommissions.remove(commissionId);
    }
    @Override
    public void markAllIncomplete() {
        completedCommissions.clear();
        completionCount = 0;
    }
    @Override
    public Set<String> getCompletedCommissions() {
        // Maybe unmodifiable, idk
        return completedCommissions;
    }

    @Override
    public int getCompletionCount() {
        return completionCount;
    }

    @Override
    public int getCurrentStreak() {
        return currentStreak;
    }

    @Override
    public int getMaxStreak() {
        return maxStreak;
    }

    @Override
    public long getLastCompletionDay() {
        return lastCompletionDay;
    }
    @Override
    public void checkStreakExtension(long dayEpoch) {
        if(dayEpoch - lastCompletionDay == 0) return;

        if (dayEpoch - lastCompletionDay > 1) {
            currentStreak = 1;
        }
        else {
            currentStreak++;
        }
        
        if(currentStreak > maxStreak) maxStreak = currentStreak;
        lastCompletionDay = dayEpoch;
    }
    
    @Override
    public void resetStreak(boolean isHardReset) {
        currentStreak = 0;
        if(isHardReset) maxStreak = 0;
        lastCompletionDay = -1;
    }


    // Capability Stuff

    public static final Capability<ICommissionProgress> COMMISSION_PROGRESS = CapabilityManager.get(new CapabilityToken<>() {});

    private final LazyOptional<ICommissionProgress> optional = LazyOptional.of(() -> this);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == COMMISSION_PROGRESS ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() { 
        CompoundTag tag = new CompoundTag();
        ListTag completedTag = new ListTag();
        for (String id: completedCommissions) completedTag.add(StringTag.valueOf(id));

        tag.put(TAG_COMPLETED, completedTag);
        tag.putInt(TAG_COMPLETION_COUNT, completionCount);
        tag.putInt(TAG_CURR_STREAK, currentStreak);
        tag.putInt(TAG_MAX_STREAK, maxStreak);
        tag.putLong(TAG_LAST_COMPLETION_DAY, lastCompletionDay);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        completedCommissions.clear();
        ListTag completedTag = tag.getList(TAG_COMPLETED, Tag.TAG_STRING);
        for(int i = 0; i < completedTag.size(); i++) completedCommissions.add(completedTag.getString(i));

        completionCount = tag.getInt(TAG_COMPLETION_COUNT);
        currentStreak = tag.getInt(TAG_CURR_STREAK);
        maxStreak = tag.getInt(TAG_MAX_STREAK);
        lastCompletionDay = tag.getLong(TAG_LAST_COMPLETION_DAY);
    }

}
