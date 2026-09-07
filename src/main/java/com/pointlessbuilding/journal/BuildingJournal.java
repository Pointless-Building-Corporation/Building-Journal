package com.pointlessbuilding.journal;

import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.client.ClientSetup;
import com.pointlessbuilding.journal.commission.Commission;
import com.pointlessbuilding.journal.commission.CommissionLoader;
import com.pointlessbuilding.journal.datagen.DataGeneration;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.List;

import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BuildingJournal.MODID)
public class BuildingJournal
{
    public static final String MODID = "buildingjournal";
    public static final String VERSION = "1.20.1-1.0.0-beta.5";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BuildingJournal(IEventBus modEventBus, ModContainer modContainer)
    {
        modContainer.registerConfig(ModConfig.Type.COMMON, BuildingJournalConfig.SPEC);

        Registration.init(modEventBus); 

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(DataGeneration::generate);

        if(FMLEnvironment.dist.isClient()) ClientSetup.registerConfigScreen(modContainer);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        CommissionLoader.setup();
        List<Commission> loaded = CommissionLoader.loadCommissions();
        LOGGER.info("Loaded {} commissions", loaded.size());
        for(Commission c : loaded) LOGGER.info(" - {} ({})", c.title(), c.id());
    }

}
