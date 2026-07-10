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
    public static ResourceLocation JOURNAL_BOOKMARKS = new ResourceLocation("buildingjournal:textures/gui/journal_page_bookmarks.png");

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

    private int scaledBookmarkOffset = (int)(40 * ((double) ui_width / 3840));
    private int scaledBookmarkWidth = (int)(600 * ((double) ui_width / 3840));
    private int scaledBookmarkHeight = (int)(80 * ((double) ui_height / 2160));

    private class BookmarkButton extends AbstractButton {
        
        private final int pageIndex;

        public BookmarkButton(int x, int y, int width, int height, int pageIndex) {
            super(x, y, width, height, CommonComponents.EMPTY);
            this.pageIndex = pageIndex;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale((float)this.width / 600, (float)this.height / 80, 1);
            guiGraphics.blit(JOURNAL_BOOKMARKS, (int)(this.getX() * 600f / this.width), (int)(this.getY() * 80f / this.height), 0, pageIndex * 80, 600, 80, 600, 160);
            guiGraphics.pose().popPose();
        }

        @Override
        public void onPress() {
            currentPage = pageIndex;
            updateButtonVisibility();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    } 

    private BookmarkButton guideButton;
    private BookmarkButton cityCommissionButton;

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
        guideButton = new BookmarkButton(
            (this.width - ui_width) / 2 + ui_width / 4 - scaledBookmarkWidth / 2,
            ui_height - scaledBookmarkHeight - scaledBookmarkOffset,
            scaledBookmarkWidth, scaledBookmarkHeight, 0
        );

        cityCommissionButton = new BookmarkButton(
            (this.width - ui_width) / 2 + 3 * ui_width / 4 - scaledBookmarkWidth / 2,
            ui_height - scaledBookmarkHeight - scaledBookmarkOffset,
            scaledBookmarkWidth, scaledBookmarkHeight, 1
        );

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

        this.addRenderableWidget(guideButton);
        this.addRenderableWidget(cityCommissionButton);
        this.addRenderableWidget(leftButton);
        this.addRenderableWidget(rightButton);
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

        scaledBookmarkOffset = (int)(40 * ((double) ui_width / 3840));
        scaledBookmarkWidth = (int)(600 * ((double) ui_width / 3840));
        scaledBookmarkHeight = (int)(80 * ((double) ui_height / 2160));
    }
    
}
