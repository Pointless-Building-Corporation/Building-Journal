package com.pointlessbuilding.journal.commission.conditions;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

import net.minecraft.resources.ResourceLocation;

public class WhitelistCondition implements CommissionCondition{

    private final String title;
    private final String failureDescription;

    private final List<ResourceLocation> blocks;
    private final Boolean isBlacklist;

    public WhitelistCondition(String title, String failureDescription, List<ResourceLocation> blocks, Boolean isBlacklist){
        this.title = title;
        this.failureDescription = failureDescription;
        this.blocks = blocks;
        this.isBlacklist = isBlacklist;
    }

    @Override
    public boolean test(EvaluationResult result) {

        boolean isSatisfied = true;

        if(isBlacklist) {
            for (ResourceLocation block : blocks) {
                if(result.blockData().containsKey(block)) isSatisfied = false;
            }
        }
        else {
            for (ResourceLocation block : blocks) {
                if(!result.blockData().containsKey(block)) isSatisfied = false;
            }
        }

        return isSatisfied;
    }

    @Override
    public String getTitle() {
        if(title == null) {
            String generatedTitle = "";
            List<String> blockStrings = new ArrayList<>();
            for(ResourceLocation block : blocks) {
                blockStrings.add(block.toString());
            }
            generatedTitle += blockStrings;
            if(isBlacklist) generatedTitle += " must not be included";
            else generatedTitle += " must be included";
            return generatedTitle;
        }
        return title;
    }

    @Override
    public String describeFailure(EvaluationResult result) {
        if(failureDescription == null) {
            String generatedDesc = "Blocks in build does not follow the ";
            if(isBlacklist) generatedDesc += "blacklist!";
            else generatedDesc += "whitelist!";
            return generatedDesc;
        }
        else {
            String firstFailedBlock = "";

            if(isBlacklist) {
                for (ResourceLocation block : blocks) {
                    if(result.blockData().containsKey(block)) firstFailedBlock = block.toString();
                }
            }
            else {
                for (ResourceLocation block : blocks) {
                    if(!result.blockData().containsKey(block)) firstFailedBlock = block.toString();
                }
            }

            return failureDescription.replace("{value}", firstFailedBlock);
        }
    }
    
    public static CommissionCondition fromJson(JsonObject json) {
        String jsonTitle = null;
        String jsonFailure = null;
        List<ResourceLocation> jsonBlocks = new ArrayList<>();
        boolean jsonIsBlacklist = false;

        if(json.has("title")) {
            jsonTitle = json.get("title").getAsString();
        }

        if(json.has("failureDescription")) {
            jsonFailure = json.get("failureDescription").getAsString();
        }

        if(json.has("blocks")) {
            JsonArray blockArray = json.get("blocks").getAsJsonArray();
            if(blockArray.size() == 0) {
                BuildingJournal.LOGGER.warn("Too few items in block array in condition {}", jsonTitle != null ? jsonTitle : "WhitelistCondition");
                return null;
            }
            for (JsonElement block : blockArray) {
                jsonBlocks.add(ResourceLocation.tryParse(block.getAsString()));
            }
        }
        else {
            BuildingJournal.LOGGER.warn("Missing blocks field in condition {}", jsonTitle != null ? jsonTitle : "WhitelistCondition");
            return null;
        }

        if(json.has("isBlacklist")) {
            jsonIsBlacklist = json.get("isBlacklist").getAsBoolean();
        }

        return new WhitelistCondition(jsonTitle, jsonFailure, jsonBlocks, jsonIsBlacklist);
    }

}
