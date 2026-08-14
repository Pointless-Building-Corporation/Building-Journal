package com.pointlessbuilding.journal.commission;

public interface CommissionCondition {
    
    boolean test(EvaluationResult result);
    String describeFailure(EvaluationResult result);
    String getTitle(boolean getDefault);
    // Wish I could declare static functions here for fromJson(JsonObject json).
}
