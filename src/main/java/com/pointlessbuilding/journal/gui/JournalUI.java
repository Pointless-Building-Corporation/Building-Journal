package com.pointlessbuilding.journal.gui;

import com.pointlessbuilding.journal.client.ClientSetup;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


public class JournalUI extends Screen{

    public JournalUI() {
        super(Component.translatable(ClientSetup.JOURNAL_KEYMAP_STRING));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
}
