package com.pointlessbuilding.journal.commission.conditions;

import java.util.Map;

import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

public class BlockModifiedCondition implements CommissionCondition{
    
    public enum Operator { LESS_THAN, GREATER_THAN, EQUAL }

    private static Map<String, Operator> operatorMap = Map.of(
    ">", Operator.GREATER_THAN,
    "<", Operator.LESS_THAN,
    "==", Operator.EQUAL
    );

    private final String title;
    private final String failureDescription;

    private final long threshold;
    private final Operator operator;

    public BlockModifiedCondition(String title, String failureDescription, long threshold, Operator operator) {
        this.title = title;
        this.failureDescription = failureDescription;
        this.threshold = threshold;
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }

    public long getThreshold() {
        return threshold;
    }

    @Override
    public boolean test(EvaluationResult result) {
        long blocksModified = result.modifiedCount();

        return switch (operator) {
            case LESS_THAN -> blocksModified < threshold;
            case GREATER_THAN -> blocksModified > threshold;
            case EQUAL -> blocksModified == threshold;
        };
    }

    @Override
    public String getTitle() {
        if(title == null) {
            String generatedTitle = "Modified blocks";
            switch (operator) {
                case LESS_THAN -> generatedTitle += " < ";
                case GREATER_THAN -> generatedTitle += " > ";
                case EQUAL -> generatedTitle += " = ";
            };
            generatedTitle += threshold;
            return generatedTitle;
        }
        return title;
    }

    @Override
    public String describeFailure(EvaluationResult result) {
        if(failureDescription == null) {
            String generatedDesc = "Modified blocks not ";
             switch (operator) {
                case LESS_THAN -> generatedDesc += "less than ";
                case GREATER_THAN -> generatedDesc += "greater than ";
                case EQUAL -> generatedDesc += "equal to ";
            };
            generatedDesc += threshold + "!";
            return generatedDesc;
        }
        else {
            long blocksModified = result.modifiedCount();
            return failureDescription.replace("{value}", Long.toString(blocksModified));
        }
    }

    public static CommissionCondition fromJson(JsonObject json) {
        String jsonTitle = null;
        String jsonFailure = null;
        Operator jsonOperator;
        long jsonThreshold;

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
            BuildingJournal.LOGGER.warn("Missing operator field in condition {}", jsonTitle != null ? jsonTitle : "BlockModifiedCondition");
            return null;
        }
        
        if(json.has("threshold")) {
            jsonThreshold = json.get("threshold").getAsLong();
        }
        else {
            BuildingJournal.LOGGER.warn("Missing threshold field in condition {}", jsonTitle != null ? jsonTitle : "BlockModifiedCondition");
            return null;
        }

        return new BlockModifiedCondition(jsonTitle, jsonFailure, jsonThreshold, jsonOperator);
    }

}
