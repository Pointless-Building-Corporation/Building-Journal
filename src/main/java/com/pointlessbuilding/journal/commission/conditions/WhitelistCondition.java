package com.pointlessbuilding.journal.commission.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

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

    public List<ResourceLocation> getBlocks() {
        return blocks;
    }

    public Boolean getIsBlacklist() {
        return isBlacklist;
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
            String generatedTitle = "The following blocks ";
            if(isBlacklist) generatedTitle += " must not be included in the build: ";
            else generatedTitle += " must be included in the build: ";
            List<String> blockStrings = new ArrayList<>();
            for(ResourceLocation block : blocks) {
                blockStrings.add(block.toString());
            }
            generatedTitle += String.join(", ", blockStrings);
            
            return generatedTitle;
        }
        return title;
    }

    @Override
    public String describeFailure(EvaluationResult result) {
        if(failureDescription == null) {
            String generatedDesc = "The blocks in the build do not follow the ";
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
                String blockString = block.getAsString();
                if(blockString.startsWith("#")) {   // Tag
                    ResourceLocation tagId = ResourceLocation.tryParse(blockString.substring(1));
                    if(tagId == null) {
                        BuildingJournal.LOGGER.error("Invalid tag resource location in commission blocks list of {}", jsonTitle != null ? jsonTitle : "WhitelistCondition");
                        continue;
                    }
                    TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagId);
                    ForgeRegistries.BLOCKS.tags().getTag(tagKey).forEach(b -> jsonBlocks.add(ForgeRegistries.BLOCKS.getKey(b)));
                }
                else if(blockString.startsWith("regex:")) { // Regex
                    Pattern pattern = Pattern.compile(blockString.substring("regex:".length()));
                    for (ResourceLocation blockId : ForgeRegistries.BLOCKS.getKeys()) {
                        if(pattern.matcher(blockId.toString()).matches()) {
                            jsonBlocks.add(blockId);
                        }
                    }
                }
                else {  // Block
                    ResourceLocation parsed = ResourceLocation.tryParse(blockString);
                    if(parsed == null) {
                        BuildingJournal.LOGGER.error("Invalid block resource location in commission blocks list of {}", jsonTitle != null ? jsonTitle : "WhitelistCondition");
                        continue;
                    }
                    jsonBlocks.add(parsed);
                }
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
