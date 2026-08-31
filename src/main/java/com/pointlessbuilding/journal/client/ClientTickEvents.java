package com.pointlessbuilding.journal.client;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.gui.JournalUI;

import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = BuildingJournal.MODID, value = Dist.CLIENT)
public class ClientTickEvents {

    public static int currentJournalPage = 0;
    public static int currentJournalCommissionPage = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while(ClientSetup.JOURNAL_KEYMAP.get().consumeClick()) {
            Minecraft.getInstance().setScreen(new JournalUI(currentJournalPage, currentJournalCommissionPage));
        }
    }

}
