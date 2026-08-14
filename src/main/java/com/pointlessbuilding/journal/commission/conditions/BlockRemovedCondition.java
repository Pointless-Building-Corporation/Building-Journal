package com.pointlessbuilding.journal.commission.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

public class BlockRemovedCondition implements CommissionCondition{

    public enum Operator { LESS_THAN, GREATER_THAN, EQUAL }

    private static Map<String, Operator> operatorMap = Map.of(
        ">", Operator.GREATER_THAN,
        "<", Operator.LESS_THAN,
        "==", Operator.EQUAL
    );

    private final String title;
    private final String failureDescription;

    private final List<ResourceLocation> blocks;
    private final Operator operator;
    private final long threshold;

    public BlockRemovedCondition(String title, String failureDescription, List<ResourceLocation> blocks, Operator operator, long threshold) {
        this.title = title;
        this.failureDescription = failureDescription;
        this.blocks = blocks;
        this.operator = operator;
        this.threshold = threshold;
    }

    public List<ResourceLocation> getBlocks() {
        return blocks;
    }

    public Operator getOperator() {
        return operator;
    }

    public long getThreshold() {
        return threshold;
    }

    @Override
    public boolean test(EvaluationResult result) {
        long total;
        if (blocks.isEmpty()) {
            total = result.blockData().values().stream()
                .mapToLong(EvaluationResult.BlockCounts::removed)
                .sum();
        }
        else {
            total = 0;
            for (ResourceLocation block : blocks) {
                EvaluationResult.BlockCounts counts = result.blockData().get(block);
                if(counts != null) total += counts.removed();
            }
        }

        return switch (operator) {
            case LESS_THAN -> total < threshold;
            case GREATER_THAN -> total > threshold;
            case EQUAL -> total == threshold;
        };
    }
    
    @Override
    public String getTitle(boolean getDefault) {
        if(title == null || getDefault) {
            String generatedTitle = "The blocks removed from the build ";

            String eq = "";
            switch (operator) {
                case LESS_THAN -> eq += "must be less than ";
                case GREATER_THAN -> eq += "must exceed ";
                case EQUAL -> eq += "must be equal to exactly ";
            };

            List<String> blockStrings = new ArrayList<>();
            for(ResourceLocation block : blocks) {
                blockStrings.add(block.toString());
            }
            if(blockStrings.size() == 0) {
                generatedTitle += "";
            }
            else generatedTitle += "that match the following list ";
            generatedTitle += eq;
            generatedTitle += threshold;

            if(blockStrings.size() != 0) {
                generatedTitle += ": " + String.join(", ", blockStrings);
            }
            return generatedTitle;
        }
        return title;
    }

    @Override
    public String describeFailure(EvaluationResult result) {
        if(failureDescription == null) {
            String generatedDesc = "The number of removed blocks ";
             switch (operator) {
                case LESS_THAN -> generatedDesc += "aren't less than ";
                case GREATER_THAN -> generatedDesc += "don't exceed ";
                case EQUAL -> generatedDesc += "aren't equal to ";
            };
            generatedDesc += threshold + "!";
            return generatedDesc;
        }
        else {
            long total;
            if (blocks.isEmpty()) {
                total = result.blockData().values().stream()
                    .mapToLong(EvaluationResult.BlockCounts::removed)
                    .sum();
            }
            else {
                total = 0;
                for (ResourceLocation block : blocks) {
                    EvaluationResult.BlockCounts counts = result.blockData().get(block);
                    if(counts != null) total += counts.removed();
                }
            }
            return failureDescription.replace("{value}", Long.toString(total));
        }
    }

    public static CommissionCondition fromJson(JsonObject json) {
        String jsonTitle = null;
        String jsonFailure = null;
        List<ResourceLocation> jsonBlocks = new ArrayList<>();
        Operator jsonOperator;
        long jsonThreshold;

        if(json.has("title")) {
            jsonTitle = json.get("title").getAsString();
        }

        if(json.has("failureDescription")) {
            jsonFailure = json.get("failureDescription").getAsString();
        }

        if(json.has("blocks")) {
            JsonArray blockArray = json.get("blocks").getAsJsonArray();
            for (JsonElement block : blockArray) {
                String blockString = block.getAsString();
                if(blockString.startsWith("#")) {   // Tag
                    ResourceLocation tagId = ResourceLocation.tryParse(blockString.substring(1));
                    if(tagId == null) {
                        BuildingJournal.LOGGER.error("Invalid tag resource location in commission blocks list of {}", jsonTitle != null ? jsonTitle : "BlockRemovedCondition");
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
                        BuildingJournal.LOGGER.error("Invalid block resource location in commission blocks list of {}", jsonTitle != null ? jsonTitle : "BlockRemovedCondition");
                        continue;
                    }
                    jsonBlocks.add(parsed);
                }
            }
        }

        if(json.has("operator")) {
            jsonOperator = operatorMap.get(json.get("operator").getAsString());
        }
        else {
            BuildingJournal.LOGGER.warn("Missing operator field in condition {}", jsonTitle != null ? jsonTitle : "BlockRemovedCondition");
            return null;
        }
        
        if(json.has("threshold")) {
            jsonThreshold = json.get("threshold").getAsLong();
        }
        else {
            BuildingJournal.LOGGER.warn("Missing threshold field in condition {}", jsonTitle != null ? jsonTitle : "BlockRemovedCondition");
            return null;
        }

        return new BlockRemovedCondition(jsonTitle, jsonFailure, jsonBlocks, jsonOperator, jsonThreshold);
    }

}
