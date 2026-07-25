package com.pointlessbuilding.journal.commission.unlocks;

import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionUnlock;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class CommissionRewardUnlock implements CommissionUnlock{

    private final String title;
    private final String commissionId;
    private static final ResourceLocation commissionIcon = new ResourceLocation(BuildingJournal.MODID, "textures/gui/unlocks/commission_unlock.png");

    public CommissionRewardUnlock(String title, String commissionId) {
        this.title = title;
        this.commissionId = commissionId;
    }

    @Override
    public void apply(ServerPlayer player) {
        // This should actually do absolutely nothing since prerequisites already unlocks the commission.
        // It's still good to have this unlock for visual purposes but there could be more uses to this.
    }
    
    @Override
    public String getTitle() {
        if(title == null) {
            String generatedTitle = "Unlock " + commissionId;
            return generatedTitle;
        }
        return title;
    }

    @Override
    public void renderIcon(GuiGraphics guiGraphics, int x, int y, int size) {
        guiGraphics.blit(commissionIcon, x, y, size, size, 0, 0, 16, 16, 16, 16);
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
