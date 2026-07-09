package com.pointlessbuilding.journal.server;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.network.JournalToastPacket;
import com.pointlessbuilding.journal.network.Network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BuildingJournal.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerCommonEvents {
    
    public static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {

        // Check if compass is unlocked
        ResourceLocation compass_recipe = new ResourceLocation("buildingjournal:recipes/tools/builders_compass");

        if(event.getAdvancement().getId().equals(compass_recipe)) {
            Network.sendToClient(new JournalToastPacket(), (ServerPlayer) event.getEntity());
        }

    }

}
