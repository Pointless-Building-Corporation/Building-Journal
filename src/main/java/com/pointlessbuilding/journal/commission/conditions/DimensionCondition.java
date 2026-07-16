package com.pointlessbuilding.journal.commission.conditions;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

public class DimensionCondition implements CommissionCondition{

    private final String title;
    private final String failureDescription;
    private final List<String> dimensions;

    public DimensionCondition(String title, String failureDescription, List<String> dimensions) {
        this.title = title;
        this.failureDescription = failureDescription;
        this.dimensions = dimensions;
    }

    @Override
    public boolean test(EvaluationResult result) {
        return dimensions.contains(result.dimension());
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
        List<String> jsonDimensions = new ArrayList<>();

        if(json.has("title")) {
            jsonTitle = json.get("title").getAsString();
        }

        if(json.has("failureDescription")) {
            jsonTitle = json.get("failureDescription").getAsString();
        }

        if(json.has("dimensions")) {
            JsonArray dimensionArray = json.get("dimensions").getAsJsonArray();
            for (JsonElement block : dimensionArray) {
                jsonDimensions.add(block.getAsString());
            }
        }
        else {
            BuildingJournal.LOGGER.warn("Missing dimensions field in condition {}", jsonTitle != null ? jsonTitle : "DimensionCondition");
            return null;
        }

        return new DimensionCondition(jsonTitle, jsonFailure, jsonDimensions);
    }

}
