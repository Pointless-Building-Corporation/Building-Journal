package com.pointlessbuilding.journal.commission.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

import net.minecraft.resources.ResourceLocation;

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
    public String getTitle() {
        return title;
    }

    @Override
    public String describeFailure(EvaluationResult result) {
        return failureDescription;
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
            jsonTitle = json.get("failureDescription").getAsString();
        }

        if(json.has("blocks")) {
            JsonArray blockArray = json.get("blocks").getAsJsonArray();
            for (JsonElement block : blockArray) {
                jsonBlocks.add(ResourceLocation.tryParse(block.getAsString()));
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
