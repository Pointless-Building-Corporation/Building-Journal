package com.pointlessbuilding.journal.commission;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CommissionProgress{
    
    public static final String TAG_COMPLETED = "CompletedCommisions";
    public static final String TAG_COMPLETION_COUNT = "CompletionCount";
    public static final String TAG_CURR_STREAK = "CurrentStreak";
    public static final String TAG_MAX_STREAK = "MaxStreak";
    public static final String TAG_LAST_COMPLETION_DAY = "LastCompletionDay";

    private Set<String> completedCommissions = new HashSet<>();
    private int completionCount = 0;
    private int currentStreak = 0;
    private int maxStreak = 0;
    private long lastCompletionDay = 0;

    public CommissionProgress() {}

    public CommissionProgress(Set<String> completedCommissions, int completionCount, int currentStreak, int maxStreak, long lastCompletionDay) {
        this.completedCommissions = completedCommissions;
        this.completionCount = completionCount;
        this.currentStreak = currentStreak;
        this.maxStreak = maxStreak;
        this.lastCompletionDay = lastCompletionDay;
    }

    public boolean isCompleted(String commissionId) {
        return completedCommissions.contains(commissionId);
    }   
    public void markCompleted(String commissionId) {
        completedCommissions.add(commissionId);
        completionCount++;
    }
    public void markIncomplete(String commissionId) {
        if(completedCommissions.contains(commissionId)) completionCount--;
        completedCommissions.remove(commissionId);
    }
    public void markAllIncomplete() {
        completedCommissions.clear();
        completionCount = 0;
    }
    public Set<String> getCompletedCommissions() {
        // Maybe unmodifiable, idk
        return completedCommissions;
    }

    public int getCompletionCount() {
        return completionCount;
    }

    public int getCurrentStreak(){
        return currentStreak;
    }

    // Call this when a player requests this capability, use above for simply copying
    public int getCurrentStreak(long today) {
        cleanDirtyStreak(today);
        return currentStreak;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public long getLastCompletionDay() {
        return lastCompletionDay;
    }
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
    
    public void resetStreak(boolean isHardReset) {
        currentStreak = 0;
        if(isHardReset) maxStreak = 0;
        lastCompletionDay = -1;
    }

    private void cleanDirtyStreak(long today) {
        if (today - lastCompletionDay > 1) {
            currentStreak = 0;
        }
    }

    private static final Codec<Set<String>> COMPLETED_COMMS_CODEC = Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf);

    public static final Codec<CommissionProgress> CODEC = RecordCodecBuilder.create(instance -> 
        instance.group(
            COMPLETED_COMMS_CODEC.fieldOf(TAG_COMPLETED).forGetter(CommissionProgress::getCompletedCommissions),
            Codec.INT.fieldOf(TAG_COMPLETION_COUNT).forGetter(CommissionProgress::getCompletionCount),
            Codec.INT.fieldOf(TAG_CURR_STREAK).forGetter(CommissionProgress::getCurrentStreak),
            Codec.INT.fieldOf(TAG_MAX_STREAK).forGetter(CommissionProgress::getMaxStreak),
            Codec.LONG.fieldOf(TAG_LAST_COMPLETION_DAY).forGetter(CommissionProgress::getLastCompletionDay)
        ).apply(instance, CommissionProgress::new)
    );

}
