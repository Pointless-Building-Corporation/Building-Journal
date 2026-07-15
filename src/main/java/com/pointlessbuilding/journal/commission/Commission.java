package com.pointlessbuilding.journal.commission;

import java.nio.file.Path;
import java.util.List;

public record Commission(
    String id,
    String title,
    Path thumbnailPath,
    List<CommissionCondition> conditions,
    List<CommissionUnlock> unlocks,
    List<String> prerequisites
) {}
