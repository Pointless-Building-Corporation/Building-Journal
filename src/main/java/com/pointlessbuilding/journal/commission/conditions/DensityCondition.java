package com.pointlessbuilding.journal.commission.conditions;

import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

public class DensityCondition implements CommissionCondition{
    
    public enum Operator { LESS_THAN, GREATER_THAN, EQUAL }

    private final float threshold;
    private final Operator operator;

    public DensityCondition(float threshold, Operator operator) {
        this.threshold = threshold;
        this.operator = operator;
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
    public String describeFailure(EvaluationResult result) {
        return "";
    }

}
