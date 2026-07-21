package com.pointlessbuilding.journal.commission.unlocks;

import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionProgress;
import com.pointlessbuilding.journal.commission.CommissionUnlock;

import net.minecraft.server.level.ServerPlayer;

public class CommissionRewardUnlock implements CommissionUnlock{

    private final String title;
    private final String commissionId;

    public CommissionRewardUnlock(String title, String commissionId) {
        this.title = title;
        this.commissionId = commissionId;
    }

    @Override
    public void apply(ServerPlayer player) {
        player.getCapability(CommissionProgress.COMMISSION_PROGRESS)
            .ifPresent(progress -> progress.markCompleted(commissionId));
    }
    
    @Override
    public String getTitle() {
        if(title == null) {
            String generatedTitle = "Unlock " + commissionId;
            return generatedTitle;
        }
        return title;
    }

    public static CommissionUnlock fromJson(JsonObject json) {
        String jsonTitle = null;
        String jsonId;

        if(json.has("title")) {
            jsonTitle = json.get("title").getAsString();
        }

        if(json.has("commission")) {
            jsonId = json.get("commission").getAsString();
        }
        else {
            BuildingJournal.LOGGER.warn("Missing ccommission field in unlock {}", jsonTitle != null ? jsonTitle : "CommissionRewardUnlock");
            return null;
        }

        return new CommissionRewardUnlock(jsonTitle, jsonId);
    }

}
