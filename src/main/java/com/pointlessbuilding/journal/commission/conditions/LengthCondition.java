package com.pointlessbuilding.journal.commission.conditions;

import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

public class LengthCondition implements CommissionCondition{

    public enum Operator { LESS_THAN, GREATER_THAN, EQUAL }

    private final long threshold;
    private final Operator operator;

    public LengthCondition(long threshold, Operator operator) {
        this.threshold = threshold;
        this.operator = operator;
    }

    @Override
    public boolean test(EvaluationResult result) {
        int minX = result.boxes().stream()
            .mapToInt(b -> Math.min(b.firstPos().getX(), b.secondPos().getX()))
            .min().orElseThrow();
        int maxX = result.boxes().stream()
            .mapToInt(b -> Math.max(b.firstPos().getX(), b.secondPos().getX()))
            .max().orElseThrow();
        int minZ = result.boxes().stream()
            .mapToInt(b -> Math.min(b.firstPos().getZ(), b.secondPos().getZ()))
            .min().orElseThrow();
        int maxZ = result.boxes().stream()
            .mapToInt(b -> Math.max(b.firstPos().getZ(), b.secondPos().getZ()))
            .max().orElseThrow();

        long lengthX = maxX - minX + 1;
        long lengthZ = maxZ - minZ + 1;

        return switch (operator) {
            case LESS_THAN -> Math.min(lengthX, lengthZ) > threshold;
            case GREATER_THAN -> Math.max(lengthX, lengthZ) < threshold;
            case EQUAL -> (lengthX == threshold) || (lengthZ == threshold);
        };
    }

    @Override
    public String describeFailure(EvaluationResult result) {
        return "";
    }
    
}
