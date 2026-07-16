package com.pointlessbuilding.journal.commission.conditions;

import java.util.Map;

import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

public class ElevationCondition implements CommissionCondition{

    public enum Operator { LESS_THAN, GREATER_THAN }

    private static Map<String, Operator> operatorMap = Map.of(
        ">", Operator.GREATER_THAN,
        "<", Operator.LESS_THAN
    );

    private final String title;
    private final String failureDescription;
    private final long threshold;
    private final Operator operator;

    public ElevationCondition(String title, String failureDescription, long threshold, Operator operator) {
        this.title = title;
        this.failureDescription = failureDescription;
        this.threshold = threshold;
        this.operator = operator;
    }

    @Override
    public boolean test(EvaluationResult result) {
        int minY = result.boxes().stream()
            .mapToInt(b -> Math.min(b.firstPos().getY(), b.secondPos().getY()))
            .min().orElseThrow();
        int maxY = result.boxes().stream()
            .mapToInt(b -> Math.max(b.firstPos().getY(), b.secondPos().getY()))
            .max().orElseThrow();

        return switch (operator) {
            case LESS_THAN -> maxY < threshold;
            case GREATER_THAN -> minY > threshold;
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
        Operator jsonOperator;
        long jsonThreshold;

        if(json.has("title")) {
            jsonTitle = json.get("title").getAsString();
        }

        if(json.has("failureDescription")) {
            jsonTitle = json.get("failureDescription").getAsString();
        }

        if(json.has("operator")) {
            jsonOperator = operatorMap.get(json.get("operator").getAsString());
        }
        else {
            BuildingJournal.LOGGER.warn("Missing operator field in condition {}", jsonTitle != null ? jsonTitle : "ElevationCondition");
            return null;
        }
        
        if(json.has("threshold")) {
            jsonThreshold = json.get("threshold").getAsLong();
        }
        else {
            BuildingJournal.LOGGER.warn("Missing threshold field in condition {}", jsonTitle != null ? jsonTitle : "ElevationCondition");
            return null;
        }

        return new ElevationCondition(jsonTitle, jsonFailure, jsonThreshold, jsonOperator);
    }
}
