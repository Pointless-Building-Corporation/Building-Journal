package com.pointlessbuilding.journal.commission;

import java.time.LocalDate;

import net.minecraft.server.MinecraftServer;

public interface DailyCommissionFactory {
    DailyCommission create(LocalDate date, MinecraftServer server);
}
