package com.pointlessbuilding.journal.commission;

public record CommissionCardData(
    String id,
    String title,
    byte[] thumbnailBytes,
    CommissionState state
) {}
