package com.pointlessbuilding.journal;

import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.commission.Commission;
import com.pointlessbuilding.journal.commission.CommissionSetup;
import com.pointlessbuilding.journal.datagen.DataGeneration;
import com.pointlessbuilding.journal.gui.ConfigUI;
import com.pointlessbuilding.journal.network.Network;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;

import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BuildingJournal.MODID)
public class BuildingJournal
{
    public static final String MODID = "buildingjournal";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BuildingJournal()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BuildingJournalConfig.SPEC);
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                (mc, screen) -> new ConfigUI(screen)
            ));

        Registration.init(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(DataGeneration::generate);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        Network.init();
        CommissionSetup.setup();
        List<Commission> loaded = CommissionSetup.loadCommissions();
        LOGGER.info("Loaded {} commissions", loaded.size());
        for(Commission c : loaded) LOGGER.info(" - {} ({})", c.title(), c.id());
    }

}
