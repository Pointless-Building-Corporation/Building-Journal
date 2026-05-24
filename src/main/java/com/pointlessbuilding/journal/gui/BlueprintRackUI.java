package com.pointlessbuilding.journal.gui;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.menu.BlueprintRackContainer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BlueprintRackUI extends AbstractContainerScreen<BlueprintRackContainer>{

    private final ResourceLocation GUI = new ResourceLocation(BuildingJournal.MODID, "textures/gui/blueprint_rack.png");

    public BlueprintRackUI(BlueprintRackContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.imageHeight = 186;
        this.titleLabelY = 5;
        this.inventoryLabelY = 87;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        renderBackground(guiGraphics);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }
    
}
