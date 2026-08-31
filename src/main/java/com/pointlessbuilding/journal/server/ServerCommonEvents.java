package com.pointlessbuilding.journal.server;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.network.Network;
import com.pointlessbuilding.journal.network.packets.JournalToastPacket;
import com.pointlessbuilding.journal.server.commands.BlueprintCommand;
import com.pointlessbuilding.journal.server.commands.CommissionCommand;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = BuildingJournal.MODID)
public class ServerCommonEvents {
    
    public static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        // Check if compass is unlocked
        ResourceLocation compass_recipe = ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "recipes/tools/builders_compass");

        if (event.getAdvancement().id().equals(compass_recipe)) {
            Network.sendToClient(JournalToastPacket.INSTANCE, (ServerPlayer) event.getEntity());
        }

    }

    @SubscribeEvent
    public static void OnCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(CommissionCommand.register());
        event.getDispatcher().register(BlueprintCommand.register());
    }

}
