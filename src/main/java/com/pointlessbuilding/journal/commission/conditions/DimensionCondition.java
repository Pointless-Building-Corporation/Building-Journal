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

    public List<String> getDimensions() {
        return dimensions;
    }

    @Override
    public boolean test(EvaluationResult result) {
        return dimensions.contains(result.dimension());
    }
    
    @Override
    public String getTitle() {
        if(title == null) {
            String generatedTitle = "Dimension is one of ";
            generatedTitle += dimensions;
            return generatedTitle;
        }
        return title;
    }

    @Override
    public String describeFailure(EvaluationResult result) {
        if(failureDescription == null) {
            String generatedDesc = "Build dimension not satisfied!";
            return generatedDesc;
        }
        else {
            String dimension = result.dimension();
            return failureDescription.replace("{value}", dimension);
        }
    }

    public static CommissionCondition fromJson(JsonObject json) {
        String jsonTitle = null;
        String jsonFailure = null;
        List<String> jsonDimensions = new ArrayList<>();

        if(json.has("title")) {
            jsonTitle = json.get("title").getAsString();
        }

        if(json.has("failureDescription")) {
            jsonFailure = json.get("failureDescription").getAsString();
        }

        if(json.has("dimensions")) {
            JsonArray dimensionArray = json.get("dimensions").getAsJsonArray();
            if(dimensionArray.size() == 0) {
                BuildingJournal.LOGGER.warn("Too few dimensions in condition {}", jsonTitle != null ? jsonTitle : "DimensionCondition");
                return null;
            }
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
