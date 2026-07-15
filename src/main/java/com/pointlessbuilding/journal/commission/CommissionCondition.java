package com.pointlessbuilding.journal.commission;

public interface CommissionCondition {
    
    boolean test(EvaluationResult result);
    String describeFailure(EvaluationResult result);
    
}
