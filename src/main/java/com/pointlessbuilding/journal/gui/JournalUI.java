package com.pointlessbuilding.journal.gui;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.client.ClientSetup;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;


public class JournalUI extends Screen{

    private static final Logger LOGGER = LogUtils.getLogger();

    public static List<ResourceLocation> JOURNAL_PAGES = Arrays.asList(
        new ResourceLocation("buildingjournal:textures/gui/journal_ui_guidebook.png"),
        new ResourceLocation("buildingjournal:textures/gui/journal_ui_commscreen.png")
        );
    public static ResourceLocation JOURNAL_TABS = new ResourceLocation("buildingjournal:textures/gui/journal_page_tabs.png");

    public static int ui_width = 800;
    public static int ui_height = 450;

    public static int button_width = 50;
    public static int button_height = 20;
    public static int padding = 20;

    private int currentPage;
    private int currentCommissionPage;
    private static int commissionCount = 7;

    private int scaledTabOffset = (int)(68 * ((double) ui_width / 3840));
    private int scaledTabWidth = (int)(118 * ((double) ui_width / 3840));
    private int scaledTabHeight = (int)(480 * ((double) ui_height / 2160));

    private class PageButton extends AbstractButton {
        
        private final int pageIndex;

        public PageButton(int x, int y, int width, int height, int pageIndex) {
            super(x, y, width, height, CommonComponents.EMPTY);
            this.pageIndex = pageIndex;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale((float)this.width / 118, (float)this.height / 480, 1);
            guiGraphics.blit(JOURNAL_TABS, (int)(this.getX() * 118f / this.width), (int)(this.getY() * 480f / this.height), pageIndex * 118, 0, 118, 480, 472, 480);
            guiGraphics.pose().popPose();
        }

        @Override
        public void onPress() {
            currentPage = pageIndex;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    } 

    private Button guideButton;
    private Button cityCommissionButton;

    private class TabButton extends AbstractButton {
        
        private final boolean isLeft;

        public TabButton(int x, int y, int width, int height, boolean isLeft) {
            super(x, y, width, height, CommonComponents.EMPTY);
            this.isLeft = isLeft;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            boolean isActive = isLeft ? currentCommissionPage > 0 : currentCommissionPage < Math.ceil(commissionCount / 6.0) - 1;

            int frame = isLeft ? (isActive ? 0 : 1) : (isActive ? 3 : 2);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale((float)this.width / 118, (float)this.height / 480, 1);
            guiGraphics.blit(JOURNAL_TABS, (int)(this.getX() * 118f / this.width), (int)(this.getY() * 480f / this.height), frame * 118, 0, 118, 480, 472, 480);
            guiGraphics.pose().popPose();
        }

        @Override
        public void onPress() {
            if(isLeft) currentCommissionPage = Math.max(0, currentCommissionPage - 1);
            else currentCommissionPage = Math.min((int)Math.ceil(commissionCount / 6.0) - 1, currentCommissionPage + 1);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    }
    private TabButton leftButton;
    private TabButton rightButton;

    public JournalUI() {
        super(GameNarrator.NO_TITLE);
        currentPage = 0;
        currentCommissionPage = 0;
    }

    @Override
    protected void init() {
        flex();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
         this.onClose();
        }).bounds(this.width / 2 - 2 * button_width, ui_height + padding, button_width * 4, button_height).build());
        
        renderMenuButtons();
        updateButtonVisibility();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(JOURNAL_PAGES.get(currentPage), (this.width - ui_width) / 2, 0, 0, 0, ui_width, ui_height, ui_width, ui_height);

        for(Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        RenderSystem.disableBlend();
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
        guideButton = this.addRenderableWidget(Button.builder(CommonComponents.EMPTY, (button) -> {
            this.toGuide();
        }).bounds((this.width - ui_width) / 2 + ui_width / 4 - button_width / 2, (this.height + ui_height)/2 - button_height - padding, button_width, button_height).build());

        cityCommissionButton = this.addRenderableWidget(Button.builder(CommonComponents.EMPTY, (button) -> {
            this.toCityCommissions();
        }).bounds((this.width - ui_width) / 2 + 3 * ui_width / 4 - button_width / 2, (this.height + ui_height)/2 - button_height - padding, button_width, button_height).build());
    
        leftButton = new TabButton(
            (this.width - ui_width) / 2 + scaledTabOffset,
            ui_height / 2 - scaledTabHeight / 2,
            scaledTabWidth, scaledTabHeight, true
        );
        rightButton = new TabButton(
            (this.width + ui_width) / 2 - scaledTabOffset - scaledTabWidth,
            ui_height /2 - scaledTabHeight / 2,
            scaledTabWidth, scaledTabHeight, false
        );

        this.addRenderableWidget(leftButton);
        this.addRenderableWidget(rightButton);
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
        guideButton.visible = currentPage != 0;
        leftButton.visible = rightButton.visible = currentPage == 1;
        cityCommissionButton.visible = currentPage != 1;
    }

    protected void flex() {
        double scale = Math.min(this.width / 800.0, this.height / 450.0) * 0.8;
        ui_width = (int)(800 * scale);
        ui_height = (int)(450 * scale);
        scaledTabOffset = (int)(68 * ((double) ui_width / 3840));
        scaledTabWidth = (int)(118 * ((double) ui_width / 3840));
        scaledTabHeight = (int)(480 * ((double) ui_height / 2160));
    }

}
