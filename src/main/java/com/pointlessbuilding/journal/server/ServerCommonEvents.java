package com.pointlessbuilding.journal.server;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionProgress;
import com.pointlessbuilding.journal.network.Network;
import com.pointlessbuilding.journal.network.packets.JournalToastPacket;
import com.pointlessbuilding.journal.server.commands.BlueprintCommand;
import com.pointlessbuilding.journal.server.commands.CommissionCommand;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("removal")
@Mod.EventBusSubscriber(modid = BuildingJournal.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerCommonEvents {
    
    public static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        // Check if compass is unlocked
        ResourceLocation compass_recipe = new ResourceLocation(BuildingJournal.MODID, "recipes/tools/builders_compass");

        if (event.getAdvancement().getId().equals(compass_recipe)) {
            Network.sendToClient(new JournalToastPacket(), (ServerPlayer) event.getEntity());
        }

    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(BuildingJournal.MODID, "commission_progress"), new CommissionProgress());
        }
    }

    @SubscribeEvent
    public static void OnPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(CommissionProgress.COMMISSION_PROGRESS).ifPresent(oldCap -> {
            CompoundTag tag = oldCap.serializeNBT();
            event.getEntity().getCapability(CommissionProgress.COMMISSION_PROGRESS).ifPresent(newCap -> {
                newCap.deserializeNBT(tag);
            });
        });
    }

    @SubscribeEvent
    public static void OnCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(CommissionCommand.register());
        event.getDispatcher().register(BlueprintCommand.register());
    }

}
