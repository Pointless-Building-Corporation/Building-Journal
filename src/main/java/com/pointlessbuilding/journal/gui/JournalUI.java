package com.pointlessbuilding.journal.gui;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;


public class JournalUI extends Screen{

    public static List<ResourceLocation> JOURNAL_PAGES = Arrays.asList(new ResourceLocation("buildingjournal:textures/gui/journal_ui_menu.png"), new ResourceLocation("buildingjournal:textures/gui/journal_ui_guidebook.png"));
    public static int ui_width = 800;
    public static int ui_height = 450;
    private static float aspect_ratio = 16f/9f;

    private int currentPage;

    public JournalUI() {
        super(GameNarrator.NO_TITLE);
        currentPage = 0;
    }

    protected void init() {
        flex();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
         this.onClose();
        }).bounds(this.width / 2 - 100, ui_height, 200, 20).build());
        
        renderMenuButtons();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.blit(JOURNAL_PAGES.get(currentPage), (this.width - ui_width) / 2, 2, 0, 0, ui_width, ui_height, ui_width, ui_height);
    
        for(Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    protected void renderMenuButtons() {
        this.addRenderableWidget(new PageButton(116, 159, true, (button) -> {
         this.pageForward();
        }, false));

        this.addRenderableWidget(new PageButton(43, 159, false, (button) -> {
         this.pageBack();
        }, false));
    }
    
    protected void pageForward() {
        currentPage = Mth.clamp(currentPage+1, 0, JOURNAL_PAGES.size()-1);
    }

    protected void pageBack() {
        currentPage = Mth.clamp(currentPage-1, 0, JOURNAL_PAGES.size()-1);
    }

    @Override
    protected void repositionElements() {
        flex();
        this.rebuildWidgets();
    }

    protected void flex() {
        ui_width = 800;
        ui_height = 450;
        if(this.width < ui_width*1.25) {
            ui_width = (int)(this.width*0.8);
            ui_height = (int)(ui_width/aspect_ratio);
        }
        if(this.height < ui_height*1.25) {
            ui_height = (int)(this.height*0.8);
            ui_width = (int)(ui_height*aspect_ratio);
        }
    }

}
