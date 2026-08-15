package com.pointlessbuilding.journal.api;

import java.time.LocalDate;

import com.pointlessbuilding.journal.commission.DailyCommission;
import com.pointlessbuilding.journal.commission.DailyCommissionFactory;

import net.minecraft.server.MinecraftServer;

public final class BuildingJournalAPI {

    private static DailyCommissionFactory dailyFactory = DailyCommission::new;

    public static void setDailyCommissionFactory(DailyCommissionFactory factory) {
        dailyFactory = factory;
    }

    public static DailyCommission createDailyCommission(LocalDate date, MinecraftServer server) {
        return dailyFactory.create(date, server);
    }

}
