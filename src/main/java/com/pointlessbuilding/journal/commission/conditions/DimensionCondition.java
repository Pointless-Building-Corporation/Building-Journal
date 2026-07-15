package com.pointlessbuilding.journal.commission.conditions;

import java.util.List;

import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

public class DimensionCondition implements CommissionCondition{

    private final List<String> dimensions;

    public DimensionCondition(List<String> dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public boolean test(EvaluationResult result) {
        return dimensions.contains(result.dimension());
    }
    
    @Override
    public String describeFailure(EvaluationResult result) {
        return "";
    }

}
