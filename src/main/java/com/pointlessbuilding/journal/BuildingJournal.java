package com.pointlessbuilding.journal;

import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.commission.Commission;
import com.pointlessbuilding.journal.commission.CommissionCompleteTrigger;
import com.pointlessbuilding.journal.commission.CommissionLoader;
import com.pointlessbuilding.journal.datagen.DataGeneration;
import com.pointlessbuilding.journal.network.Network;

import net.minecraft.advancements.CriteriaTriggers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@SuppressWarnings("removal")
@Mod(BuildingJournal.MODID)
public class BuildingJournal
{
    public static final String MODID = "buildingjournal";
    public static final String VERSION = "1.20.1-1.0.0-beta.5";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BuildingJournal()
    {
        IEventBus modEventBus = NeoForge.EVENT_BUS;

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BuildingJournalConfig.SPEC);

        Registration.init(modEventBus); 

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(DataGeneration::generate);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        Network.init();

        CommissionLoader.setup();
        List<Commission> loaded = CommissionLoader.loadCommissions();
        LOGGER.info("Loaded {} commissions", loaded.size());
        for(Commission c : loaded) LOGGER.info(" - {} ({})", c.title(), c.id());
    }

}
