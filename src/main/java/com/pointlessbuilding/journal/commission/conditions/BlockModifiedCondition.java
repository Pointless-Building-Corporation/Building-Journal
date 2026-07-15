package com.pointlessbuilding.journal.commission.conditions;

import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

public class BlockModifiedCondition implements CommissionCondition{
    
    public enum Operator { LESS_THAN, GREATER_THAN, EQUAL }

    private final long threshold;
    private final Operator operator;

    public BlockModifiedCondition(long threshold, Operator operator) {
        this.threshold = threshold;
        this.operator = operator;
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
    public String describeFailure(EvaluationResult result) {
        return "";
    }

}
