package com.pointlessbuilding.journal.commission.unlocks;

import java.util.Map;

import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionUnlock;

import net.minecraft.server.level.ServerPlayer;

public class ExpRewardUnlock  implements CommissionUnlock{

    public static enum ExpType { POINTS, LEVELS };

    private static Map<String, ExpType> expTypeMap = Map.of(
        "Points", ExpType.POINTS,
        "Levels", ExpType.LEVELS
    );

    private final String title;
    private final int expAmount;
    private final ExpType expType;

    public ExpRewardUnlock(String title, int expAmount, ExpType expType) {
        this.title = title;
        this.expAmount = expAmount;
        this.expType = expType;
    }

    @Override
    public void apply(ServerPlayer player) {
        if (expType == ExpType.POINTS) player.giveExperiencePoints(expAmount);
        else player.giveExperienceLevels(expAmount);
    }
    
    @Override
    public String getTitle() {
        return title;
    }

    public static CommissionUnlock fromJson(JsonObject json) {
        String jsonTitle = null;
        ExpType jsonExpType;
        int jsonAmount;

        if(json.has("title")) {
            jsonTitle = json.get("title").getAsString();
        }

        if(json.has("expType")) {
            jsonExpType = expTypeMap.get(json.get("expType").getAsString());
        }
        else {
            BuildingJournal.LOGGER.warn("Missing expType field in unlock {}", jsonTitle != null ? jsonTitle : "ExpRewardUnlock");
            return null;
        }

        if(json.has("expAmount")) {
            jsonAmount = json.get("expAmount").getAsInt();
        }
        else {
            BuildingJournal.LOGGER.warn("Missing expAmount field in unlock {}", jsonTitle != null ? jsonTitle : "ExpRewardUnlock");
            return null;
        }

        return new ExpRewardUnlock(jsonTitle, jsonAmount, jsonExpType);
    }

}
