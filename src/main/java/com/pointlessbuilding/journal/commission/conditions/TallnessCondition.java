package com.pointlessbuilding.journal.commission.conditions;

import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

public class TallnessCondition implements CommissionCondition{
    
    public enum Operator { LESS_THAN, GREATER_THAN, EQUAL }

    private final long threshold;
    private final Operator operator;

    public TallnessCondition(long threshold, Operator operator) {
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
        long tallness = maxY - minY + 1;

        return switch (operator) {
            case LESS_THAN -> tallness < threshold;
            case GREATER_THAN -> tallness > threshold;
            case EQUAL -> tallness == threshold;
        };
    }

    @Override
    public String describeFailure(EvaluationResult result) {
        return "";
    }

}
