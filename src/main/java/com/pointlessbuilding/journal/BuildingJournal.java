package com.pointlessbuilding.journal;

import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.commission.Commission;
import com.pointlessbuilding.journal.commission.CommissionCompleteTrigger;
import com.pointlessbuilding.journal.commission.CommissionLoader;
import com.pointlessbuilding.journal.datagen.DataGeneration;
import com.pointlessbuilding.journal.network.Network;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;

import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@SuppressWarnings("removal")
@Mod(BuildingJournal.MODID)
public class BuildingJournal
{
    public static final String MODID = "buildingjournal";
    public static final String VERSION = "1.20.1-0.1.0";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BuildingJournal()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BuildingJournalConfig.SPEC);

        Registration.init(modEventBus); 

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(DataGeneration::generate);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        Network.init();

        event.enqueueWork(() ->  {
            CriteriaTriggers.register(CommissionCompleteTrigger.INSTANCE);
        });

        CommissionLoader.setup();
        List<Commission> loaded = CommissionLoader.loadCommissions();
        LOGGER.info("Loaded {} commissions", loaded.size());
        for(Commission c : loaded) LOGGER.info(" - {} ({})", c.title(), c.id());
    }

}
