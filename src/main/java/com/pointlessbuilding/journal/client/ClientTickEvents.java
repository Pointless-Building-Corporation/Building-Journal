package com.pointlessbuilding.journal.client;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.gui.JournalUI;

import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = BuildingJournal.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientTickEvents {

    public static int currentJournalPage = 0;
    public static int currentJournalCommissionPage = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            while(ClientSetup.JOURNAL_KEYMAP.get().consumeClick()) {
                Minecraft.getInstance().setScreen(new JournalUI(currentJournalPage, currentJournalCommissionPage));
            }
        }
    }

}
