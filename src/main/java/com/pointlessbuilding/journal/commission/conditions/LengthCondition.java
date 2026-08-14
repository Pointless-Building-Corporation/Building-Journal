package com.pointlessbuilding.journal.commission.conditions;

import java.util.Map;

import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

public class LengthCondition implements CommissionCondition{

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

    public LengthCondition(String title, String failureDescription, long threshold, Operator operator) {
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
        int minX = result.boxes().stream()
            .mapToInt(b -> Math.min(b.firstPos().getX(), b.secondPos().getX()))
            .min().orElse(0);
        int maxX = result.boxes().stream()
            .mapToInt(b -> Math.max(b.firstPos().getX(), b.secondPos().getX()))
            .max().orElse(0);
        int minZ = result.boxes().stream()
            .mapToInt(b -> Math.min(b.firstPos().getZ(), b.secondPos().getZ()))
            .min().orElse(0);
        int maxZ = result.boxes().stream()
            .mapToInt(b -> Math.max(b.firstPos().getZ(), b.secondPos().getZ()))
            .max().orElse(0);

        long lengthX = Math.abs(maxX - minX) + 1;
        long lengthZ = Math.abs(maxZ - minZ) + 1;

        return switch (operator) {
            case LESS_THAN -> Math.min(lengthX, lengthZ) < threshold;
            case GREATER_THAN -> Math.max(lengthX, lengthZ) > threshold;
            case EQUAL -> (lengthX == threshold) || (lengthZ == threshold);
        };
    }

    @Override
    public String getTitle(boolean getDefault) {
        if(title == null || getDefault) {
            String generatedTitle = "One of the lengths (On the X or Z axis) of the build ";
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
            String generatedDesc = "The length of build is not ";
             switch (operator) {
                case LESS_THAN -> generatedDesc += "less than ";
                case GREATER_THAN -> generatedDesc += "greater than ";
                case EQUAL -> generatedDesc += "equal to ";
            };
            generatedDesc += threshold + "!";
            return generatedDesc;
        }
        else {
            int minX = result.boxes().stream()
            .mapToInt(b -> Math.min(b.firstPos().getX(), b.secondPos().getX()))
            .min().orElse(0);
            int maxX = result.boxes().stream()
                .mapToInt(b -> Math.max(b.firstPos().getX(), b.secondPos().getX()))
                .max().orElse(0);
            int minZ = result.boxes().stream()
                .mapToInt(b -> Math.min(b.firstPos().getZ(), b.secondPos().getZ()))
                .min().orElse(0);
            int maxZ = result.boxes().stream()
                .mapToInt(b -> Math.max(b.firstPos().getZ(), b.secondPos().getZ()))
                .max().orElse(0);

            long lengthX = Math.abs(maxX - minX) + 1;
            long lengthZ = Math.abs(maxZ - minZ) + 1;
            long length;
            if (operator == Operator.LESS_THAN) length = Math.min(lengthX, lengthZ);
            else length = Math.max(lengthX, lengthZ);
            return failureDescription.replace("{value}", Long.toString(length));
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
            BuildingJournal.LOGGER.warn("Missing operator field in condition {}", jsonTitle != null ? jsonTitle : "LengthCondition");
            return null;
        }
        
        if(json.has("threshold")) {
            jsonThreshold = json.get("threshold").getAsLong();
        }
        else {
            BuildingJournal.LOGGER.warn("Missing threshold field in condition {}", jsonTitle != null ? jsonTitle : "LengthCondition");
            return null;
        }

        return new LengthCondition(jsonTitle, jsonFailure, jsonThreshold, jsonOperator);
    }
    
}
