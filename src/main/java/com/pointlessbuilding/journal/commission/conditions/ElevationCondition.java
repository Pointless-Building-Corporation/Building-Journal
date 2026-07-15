package com.pointlessbuilding.journal.commission.conditions;

import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

public class ElevationCondition implements CommissionCondition{

    public enum Operator { LESS_THAN, GREATER_THAN }

    private final long threshold;
    private final Operator operator;

    public ElevationCondition(long threshold, Operator operator) {
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
    public String describeFailure(EvaluationResult result) {
        return "";
    }
}
