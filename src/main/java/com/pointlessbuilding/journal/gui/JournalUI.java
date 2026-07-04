package com.pointlessbuilding.journal.gui;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.client.ClientSetup;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;


public class JournalUI extends Screen{

    private static final Logger LOGGER = LogUtils.getLogger();

    public static List<ResourceLocation> JOURNAL_PAGES = Arrays.asList(
        new ResourceLocation("buildingjournal:textures/gui/journal_ui_guidebook.png"),
        new ResourceLocation("buildingjournal:textures/gui/journal_ui_commscreen.png")
        );
    public static int ui_width = 800;
    public static int ui_height = 450;
    private static float aspect_ratio = 16f/9f;

    private int currentPage;
    private Button guideButton;
    private Button cityCommissionButton;

    public JournalUI() {
        super(GameNarrator.NO_TITLE);
    }

    @Override
    protected void init() {
        flex();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
         this.onClose();
        }).bounds(this.width / 2 - 100, ui_height, 200, 20).build());

        currentPage = 1;
        
        renderMenuButtons();
        updateButtonVisibility();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.blit(JOURNAL_PAGES.get(currentPage), (this.width - ui_width) / 2, 0, 0, 0, ui_width, ui_height, ui_width, ui_height);

        for(Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(super.keyPressed(keyCode, scanCode, modifiers))
            return true;
        if( ClientSetup.JOURNAL_KEYMAP.get().matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected void renderMenuButtons() {
        this.guideButton = this.addRenderableWidget(Button.builder(CommonComponents.EMPTY, (button) -> {
            this.toGuide();
        }).bounds((this.width + ui_width) / 2, this.height/2 - 20 - 10, 20, 20).build());

        this.cityCommissionButton = this.addRenderableWidget(Button.builder(CommonComponents.EMPTY, (button) -> {
            this.toCityCommissions();
        }).bounds((this.width + ui_width) / 2, this.height/2 + 20 - 10, 20, 20).build());
    }
    protected void toGuide() {
        currentPage = 0;
        updateButtonVisibility();
    }

    protected void toCityCommissions() {
        currentPage = 1;
        updateButtonVisibility();
    }

    @Override
    protected void repositionElements() {
        flex();
        this.rebuildWidgets();
    }

    protected void updateButtonVisibility() {
        this.guideButton.visible = currentPage != 0;
        this.cityCommissionButton.visible = currentPage != 1;
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
