package com.pointlessbuilding.journal.commission.conditions;

import java.util.Map;

import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

public class DensityCondition implements CommissionCondition{
    
    public enum Operator { LESS_THAN, GREATER_THAN, EQUAL }

    private static Map<String, Operator> operatorMap = Map.of(
        ">", Operator.GREATER_THAN,
        "<", Operator.LESS_THAN,
        "==", Operator.EQUAL
    );

    private final String title;
    private final String failureDescription;

    private final float threshold;
    private final Operator operator;

    public DensityCondition(String title, String failureDescription, float threshold, Operator operator) {
        this.title = title;
        this.failureDescription = failureDescription;
        this.threshold = threshold;
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }

    public float getThreshold() {
        return threshold;
    }

    @Override
    public boolean test(EvaluationResult result) {
        float density = result.modifiedCount() / result.unionVolume();

        return switch (operator) {
            case LESS_THAN -> density < threshold;
            case GREATER_THAN -> density > threshold;
            case EQUAL -> density == threshold;
        };
    }

    @Override
    public String getTitle() {
        if(title == null) {
            String generatedTitle = "The density of the build (ratio of blocks to air) ";

            String eq = "";
            switch (operator) {
                case LESS_THAN -> eq += "must be less than ";
                case GREATER_THAN -> eq += "must exceed ";
                case EQUAL -> eq += "must be equal to exactly ";
            };

            generatedTitle += eq;
            generatedTitle += threshold;

            return generatedTitle;
        }
        return title;
    }

    @Override
    public String describeFailure(EvaluationResult result) {
        if(failureDescription == null) {
            String generatedDesc = "The density of the build is not ";
             switch (operator) {
                case LESS_THAN -> generatedDesc += "less than ";
                case GREATER_THAN -> generatedDesc += "greater than ";
                case EQUAL -> generatedDesc += "equal to ";
            };
            generatedDesc += threshold + "!";
            return generatedDesc;
        }
        else {
            float density = result.modifiedCount() / result.unionVolume();
            return failureDescription.replace("{value}", Float.toString(density));
        }
    }

    public static CommissionCondition fromJson(JsonObject json) {
        String jsonTitle = null;
        String jsonFailure = null;
        Operator jsonOperator;
        float jsonThreshold;

        if(json.has("title")) {
            jsonTitle = json.get("title").getAsString();
        }

        if(json.has("failureDescription")) {
            jsonFailure = json.get("failureDescription").getAsString();
        }

        if(json.has("operator")) {
            jsonOperator = operatorMap.get(json.get("operator").getAsString());
        }
        else {
            BuildingJournal.LOGGER.warn("Missing operator field in condition {}", jsonTitle != null ? jsonTitle : "DensityCondition");
            return null;
        }
        
        if(json.has("threshold")) {
            jsonThreshold = json.get("threshold").getAsFloat();
        }
        else {
            BuildingJournal.LOGGER.warn("Missing threshold field in condition {}", jsonTitle != null ? jsonTitle : "DensityCondition");
            return null;
        }

        return new DensityCondition(jsonTitle, jsonFailure, jsonThreshold, jsonOperator);
    }

}
