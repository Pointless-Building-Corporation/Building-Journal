package com.pointlessbuilding.journal;

import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.datagen.DataGeneration;
import com.pointlessbuilding.journal.network.Network;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BuildingJournal.MODID)
public class BuildingJournal
{
    public static final String MODID = "buildingjournal";
    public static final String VERSION = "1.0.0";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BuildingJournal()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BuildingJournalConfig.SPEC);
        Registration.init(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(DataGeneration::generate);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Inside commonSetup!");
        Network.init();
    }

}
