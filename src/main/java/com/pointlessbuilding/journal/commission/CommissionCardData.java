package com.pointlessbuilding.journal.commission;

public record CommissionCardData(
    String id,
    String title,
    CommissionState state
) {}
