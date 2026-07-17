package com.pointlessbuilding.journal.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pointlessbuilding.journal.client.ClientCommonEvents;
import com.pointlessbuilding.journal.client.ClientSetup;
import com.pointlessbuilding.journal.commission.CommissionCardData;
import com.pointlessbuilding.journal.network.Network;
import com.pointlessbuilding.journal.network.packets.RequestCardCommissionsPacket;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public class JournalUI extends Screen {

    public static List<ResourceLocation> JOURNAL_PAGES = Arrays.asList(
        new ResourceLocation("buildingjournal:textures/gui/journal_ui_guidebook.png"),
        new ResourceLocation("buildingjournal:textures/gui/journal_ui_commscreen.png")
    );
    public static ResourceLocation JOURNAL_TABS = new ResourceLocation("buildingjournal:textures/gui/journal_page_tabs.png");
    public static ResourceLocation JOURNAL_BOOKMARKS = new ResourceLocation("buildingjournal:textures/gui/journal_page_bookmarks.png");
    public static ResourceLocation TEST_COMM_IMAGE = new ResourceLocation("buildingjournal:textures/gui/test.png");

    private static int ui_width = 800;
    private static int ui_height = 450;
    private int x_offset = (this.width - ui_width) / 2;
    private int y_offset = (int) (this.height * 0.05f);

    private static int button_width = 50;
    private static int button_height = 20;
    private static int padding = 20;

    private int currentPage;
    private int currentCommissionPage;

    private int scaledTabOffset = (int)(68 * ((double) ui_width / 3840));
    private int scaledTabWidth = (int)(118 * ((double) ui_width / 3840));
    private int scaledTabHeight = (int)(480 * ((double) ui_height / 2160));

    private int bookmarkWidth = 96*8;
    private int bookmarkHeight = 16*8;
    private int bookmarkOffset = -40;
    private int scaledBookmarkOffset = (int)(bookmarkOffset * ((double) ui_width / 3840));
    private int scaledBookmarkWidth = (int)(bookmarkWidth * ((double) ui_width / 3840));
    private int scaledBookmarkHeight = (int)(bookmarkHeight * ((double) ui_height / 2160));

    private List<CommissionCard> allCards = new ArrayList<>();
    private List<CommissionCard> visibleCards = new ArrayList<>();
    private List<CommissionCardData> allCardData = new ArrayList<>();

    private static int cardWidth = 873, cardHeight = 600;
    private static int commissionY = 120, commisionSpacing = cardHeight + 40;
    private static int commissionAnchor = (int) ((1920 - cardWidth) / 2);

    private void buildAllCards() {

        // Loop through allCardData and construct the cards
        for (CommissionCardData cardData : allCardData) {
            ResourceLocation thumbnail = ClientCommonEvents.getCardThumbnail(cardData.id());
            allCards.add(new CommissionCard(0,0,0,0,
                Component.literal(cardData.title()), thumbnail, cardData.state()
            ));
        }

        // allCards.add(new CommissionCard(0,0,0,0,
        //     Component.literal("Test card 1"), null, CommissionState.AVAILABLE
        // ));
        // allCards.add(new CommissionCard(0,0,0,0,
        //     Component.literal("Test card 2"), null, CommissionState.UNAVAILABLE
        // ));
        // allCards.add(new CommissionCard(0,0,0,0,
        //     Component.literal("Test card 3"), null, CommissionState.COMPLETED
        // ));
        // allCards.add(new CommissionCard(0,0,0,0,
        //     Component.literal("Test card 4"), TEST_COMM_IMAGE, CommissionState.AVAILABLE
        // ));
        // allCards.add(new CommissionCard(0,0,0,0,
        //     Component.literal("Test card 5"), TEST_COMM_IMAGE, CommissionState.UNAVAILABLE
        // ));
        // allCards.add(new CommissionCard(0,0,0,0,
        //     Component.literal("Test card 6"), TEST_COMM_IMAGE, CommissionState.COMPLETED
        // ));
        // allCards.add(new CommissionCard(0,0,0,0,
        //     Component.literal("Test card 7"), TEST_COMM_IMAGE, CommissionState.COMPLETED
        // ));

    }

    private class BookmarkButton extends AbstractButton {
        
        private final int pageIndex;

        public BookmarkButton(int x, int y, int width, int height, int pageIndex) {
            super(x, y, width, height, CommonComponents.EMPTY);
            this.pageIndex = pageIndex;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale((float)this.width / bookmarkWidth, (float)this.height / bookmarkHeight, 1);
            guiGraphics.blit(JOURNAL_BOOKMARKS, (int)(this.getX() * bookmarkWidth / this.width), (int)(this.getY() * bookmarkHeight / this.height), 0, pageIndex * bookmarkHeight, bookmarkWidth, bookmarkHeight, bookmarkWidth, 2 * bookmarkHeight);
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
            boolean isActive = isLeft ? currentCommissionPage > 0 : currentCommissionPage < Math.ceil(allCards.size() / 6.0) - 1;

            int frame = isLeft ? (isActive ? 0 : 1) : (isActive ? 3 : 2);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale((float)this.width / 118, (float)this.height / 480, 1);
            guiGraphics.blit(JOURNAL_TABS, (int)(this.getX() * 118f / this.width), (int)(this.getY() * 480f / this.height), frame * 118, 0, 118, 480, 472, 480);
            guiGraphics.pose().popPose();
        }

        @Override
        public void onPress() {
            if(isLeft) currentCommissionPage = Math.max(0, currentCommissionPage - 1);
            else currentCommissionPage = Math.min((int)Math.ceil(allCards.size() / 6.0) - 1, currentCommissionPage + 1);
            flex();
            updateButtonVisibility();
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
        Network.sendToServer(new RequestCardCommissionsPacket());
        allCardData = ClientCommonEvents.getCards();

        flex();
        // Done button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
         this.onClose();
        }).bounds(this.width / 2 - 2 * button_width, Math.min(y_offset + ui_height + padding, this.height - button_height), button_width * 4, button_height).build());
        
        renderMenuButtons();
        updateButtonVisibility();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(JOURNAL_PAGES.get(currentPage), x_offset, y_offset, 0, 0, ui_width, ui_height, ui_width, ui_height);

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
            x_offset + ui_width / 4 - scaledBookmarkWidth / 2,
            y_offset + ui_height - scaledBookmarkHeight - scaledBookmarkOffset,
            scaledBookmarkWidth, scaledBookmarkHeight, 0
        );

        cityCommissionButton = new BookmarkButton(
            x_offset + 3 * ui_width / 4 - scaledBookmarkWidth / 2,
            y_offset + ui_height - scaledBookmarkHeight - scaledBookmarkOffset,
            scaledBookmarkWidth, scaledBookmarkHeight, 1
        );

        leftButton = new TabButton(
            x_offset + scaledTabOffset,
            y_offset + ui_height / 2 - scaledTabHeight / 2,
            scaledTabWidth, scaledTabHeight, true
        );
        rightButton = new TabButton(
            x_offset + ui_width - scaledTabOffset - scaledTabWidth,
            y_offset + ui_height /2 - scaledTabHeight / 2,
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

        for (CommissionCard card: visibleCards) {
            card.visible = currentPage == 1;
        }
    }

    protected void flex() {
        double scale = Math.min(this.width / 800.0, this.height / 450.0) * 0.8;
        ui_width = (int)(800 * scale);
        ui_height = (int)(450 * scale);
        x_offset = (this.width - ui_width) / 2;
        y_offset = (int) (this.height * 0.1f);
        
        scaledTabOffset = (int)(68 * ((double) ui_width / 3840));
        scaledTabWidth = (int)(118 * ((double) ui_width / 3840));
        scaledTabHeight = (int)(480 * ((double) ui_height / 2160));

        scaledBookmarkOffset = (int)(bookmarkOffset * ((double) ui_width / 3840));
        scaledBookmarkWidth = (int)(bookmarkWidth * ((double) ui_width / 3840));
        scaledBookmarkHeight = (int)(bookmarkHeight * ((double) ui_height / 2160));

        if (allCards.isEmpty()) buildAllCards();

        for (CommissionCard card: visibleCards) this.removeWidget(card);
        visibleCards.clear();

        int scaled_w = (int) (cardWidth * ((double) ui_width / 3840));
        int scaled_h = (int) (cardHeight * ((double) ui_width / 3840));
        int scaled_spacing = (int) (commisionSpacing * ((double) ui_width / 3840));
        int scaled_y = (int) (commissionY * ((double) ui_width / 3840));
        int scaled_anchor = (int) (commissionAnchor * ((double) ui_width / 3840));
        int page_halfwidth = (int) (1920 * ((double) ui_width / 3840));

        int start = currentCommissionPage * 6;
        int end = Math.min(start + 6, allCards.size());

        for (int i = start; i < end; i++) {
            CommissionCard card = allCards.get(i);
            int x = x_offset + ((i - start)/3 == 0 ? 0 : page_halfwidth) + scaled_anchor;
            int y = y_offset + scaled_y + ((i - start) % 3) * scaled_spacing;
            card.setX(x); card.setY(y);
            card.setWidth(scaled_w); card.setHeight(scaled_h);

            this.addRenderableWidget(card);
            visibleCards.add(card);
        }
    }
    
}
