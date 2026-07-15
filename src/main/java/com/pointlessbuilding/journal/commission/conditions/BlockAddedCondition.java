package com.pointlessbuilding.journal.commission.conditions;

import java.util.List;

import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

import net.minecraft.resources.ResourceLocation;

public class BlockAddedCondition implements CommissionCondition{

    public enum Operator { LESS_THAN, GREATER_THAN, EQUAL }

    private final List<ResourceLocation> blocks;
    private final Operator operator;
    private final long threshold;

    public BlockAddedCondition(List<ResourceLocation> blocks, Operator operator, long threshold) {
        this.blocks = blocks;
        this.operator = operator;
        this.threshold = threshold;
    }

    @Override
    public boolean test(EvaluationResult result) {
        long total;
        if (blocks.isEmpty()) {
            total = result.blockData().values().stream()
                .mapToLong(EvaluationResult.BlockCounts::added)
                .sum();
        }
        else {
            total = 0;
            for (ResourceLocation block : blocks) {
                EvaluationResult.BlockCounts counts = result.blockData().get(block);
                if(counts != null) total += counts.added();
            }
        }

        return switch (operator) {
            case LESS_THAN -> total < threshold;
            case GREATER_THAN -> total > threshold;
            case EQUAL -> total == threshold;
        };
    }

    @Override
    public String describeFailure(EvaluationResult result) {
        return "";
    }
}
