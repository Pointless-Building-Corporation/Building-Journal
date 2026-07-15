package com.pointlessbuilding.journal.commission.conditions;

import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

public class TotalVolumeCondition implements CommissionCondition{

    public enum Operator { LESS_THAN, GREATER_THAN, EQUAL }

    private final long threshold;
    private final Operator operator;

    public TotalVolumeCondition(long threshold, Operator operator) {
        this.threshold = threshold;
        this.operator = operator;
    }

    @Override
    public boolean test(EvaluationResult result) {
        long volume = result.unionVolume();

        return switch (operator) {
            case LESS_THAN -> volume < threshold;
            case GREATER_THAN -> volume > threshold;
            case EQUAL -> volume == threshold;
        };
    }

    @Override
    public String describeFailure(EvaluationResult result) {
        return "";
    }
    
}
