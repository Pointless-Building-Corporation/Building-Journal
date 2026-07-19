package com.pointlessbuilding.journal.gui;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.client.ClientCommonEvents;
import com.pointlessbuilding.journal.menu.CommissionContainer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CommissionUI extends AbstractContainerScreen<CommissionContainer>{

    private final ResourceLocation GUI = new ResourceLocation(BuildingJournal.MODID, "textures/gui/commission_ui.png");
    private final ResourceLocation InventoryGUI = new ResourceLocation(BuildingJournal.MODID, "textures/gui/commission_ui_inventory.png");

    private static final int inv_width = 188, inv_height = 110;
    private static final int ui_width = 256, ui_height = 170;
    private static final int max_ui_width = inv_width * 2;
    private static final int padding = 0;
    private static final int slot_offset = 22;

    private int scaledUiWidth, scaledUiHeight;
    private int scaledUiX, scaledUiY;

    private static final int title_padding = 5;

    private static final int thumbnail_x = 12, thumbnail_y = 27;
    private static final int thumbnail_width = 122, thumbnail_height = 75;
    private static ResourceLocation thumbnail = null;

    private int scaledThumbX, scaledThumbY, scaledThumbWidth, scaledThumbHeight;

    public CommissionUI(CommissionContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = inv_width;
        this.imageHeight = inv_height;
    }

    @Override
    protected void init() {
        super.init();
        thumbnail = ClientCommonEvents.getCardThumbnail(this.getMenu().getId());
        flex();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int titleX = (this.scaledUiX - this.leftPos) + (this.scaledUiWidth - this.font.width(this.title)) / 2;
        int titleY = (this.scaledUiY - this.topPos) + title_padding;
        guiGraphics.drawString(this.font, this.title, titleX, titleY, 0x000000, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        flex();
        renderBackground(guiGraphics);

        // Commission UI
        guiGraphics.blit(GUI, scaledUiX, scaledUiY, scaledUiWidth, scaledUiHeight, 0, 0, ui_width, ui_height, 256, 256);

        if (thumbnail != null) {
            guiGraphics.blit(thumbnail, scaledThumbX, scaledThumbY, scaledThumbWidth, scaledThumbHeight, 0, 0, scaledThumbWidth, scaledThumbHeight, scaledThumbWidth, scaledThumbHeight);
        } else {
            guiGraphics.fillGradient(scaledThumbX, scaledThumbY, scaledThumbX + scaledThumbWidth, scaledThumbY + scaledThumbHeight, 0xFFDCD8CE, 0xFFC9C4B6);
        }

        // Inventory UI
        guiGraphics.blit(InventoryGUI, leftPos, topPos, 0, 0, inv_width, inv_height, 256, 256);

    }
    
    protected void flex() {
        float scale = (float)(this.height - inv_height - padding) / ui_height;

        if(Math.round(ui_width * scale) > max_ui_width) scale = (float) max_ui_width / ui_width; 

        scaledUiWidth = Math.round(ui_width * scale);
        scaledUiHeight = Math.round(ui_height * scale);
        scaledUiX = (this.width - scaledUiWidth) / 2;
        scaledUiY = padding + slot_offset;

        scaledThumbX = scaledUiX + Math.round(thumbnail_x * scale);
        scaledThumbY = scaledUiY + Math.round(thumbnail_y * scale);
        scaledThumbWidth = Math.round(thumbnail_width * scale);
        scaledThumbHeight = Math.round(thumbnail_height * scale);

        this.leftPos = (this.width - inv_width) / 2;
        this.topPos = this.scaledUiY + scaledUiHeight - slot_offset;

    }

}
