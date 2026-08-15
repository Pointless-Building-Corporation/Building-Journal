package com.pointlessbuilding.journal.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pointlessbuilding.journal.client.ClientCommonEvents;
import com.pointlessbuilding.journal.client.ClientSetup;
import com.pointlessbuilding.journal.client.ClientTickEvents;
import com.pointlessbuilding.journal.commission.CommissionCardData;
import com.pointlessbuilding.journal.commission.CommissionLoader;
import com.pointlessbuilding.journal.commission.CommissionState;
import com.pointlessbuilding.journal.network.Network;
import com.pointlessbuilding.journal.network.packets.RequestCardCommissionsPacket;
import com.pointlessbuilding.journal.network.packets.RequestCardThumbnailPacket;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("removal")
public class JournalUI extends Screen {

    public static List<ResourceLocation> JOURNAL_PAGES = Arrays.asList(
        new ResourceLocation("buildingjournal:textures/gui/journal_ui_guidebook.png"),
        new ResourceLocation("buildingjournal:textures/gui/journal_ui_commscreen.png")
    );
    public static ResourceLocation JOURNAL_TABS = new ResourceLocation("buildingjournal:textures/gui/journal_page_tabs.png");
    public static ResourceLocation JOURNAL_BOOKMARKS = new ResourceLocation("buildingjournal:textures/gui/journal_page_bookmarks.png");
    public static ResourceLocation TEST_COMM_IMAGE = new ResourceLocation("buildingjournal:textures/gui/test.png");
    public static ResourceLocation DAILY_COMM_THUMBNAIL = new ResourceLocation("buildingjournal:textures/gui/daily_commission_thumbnail.png");

    private static final String DAILY_PREFIX = "daily_";

    private static int ui_width = 800;
    private static int ui_height = 450;
    private int x_offset = (this.width - ui_width) / 2;
    private int y_offset = (int) (this.height * 0.05f);

    private static int button_width = 50, button_height = 20, button_padding = 20;

    private int currentPage;
    private int currentCommissionPage;

    // Tab buttons
    private int scaledTabOffset, scaledTabWidth, scaledTabHeight;

    // Stats
    private static int stats_x = 1920+960, stats_y = 1800, stats_spacing = 100;
    private int scaledStatsX, scaledStatsY, scaledStatsLineSpacing;
    private float scaledStatsTextScale;

    // Bookmark buttons
    private int bookmarkWidth = 96*8;
    private int bookmarkHeight = 16*8;
    private int bookmarkOffset = -40;
    private int scaledBookmarkOffset, scaledBookmarkWidth, scaledBookmarkHeight;

    // Cards
    private List<CommissionCard> allCards = new ArrayList<>();
    private List<CommissionCard> visibleCards = new ArrayList<>();
    private List<CommissionCardData> allCardData = new ArrayList<>();

    private static int cardWidth = 873, cardHeight = 600;
    private static int commissionY = 120, commisionSpacing = cardHeight + 40;
    private static int commissionAnchor = (int) ((1920 - cardWidth) / 2);

    private static final Map<CommissionState, Integer> STATE_ORDER = Map.of(
        CommissionState.AVAILABLE, 0,
        CommissionState.UNAVAILABLE, 1,
        CommissionState.COMPLETED, 2
    );

    private static final Map<String, Integer> DEFAULT_ORDER = IntStream.range(0, CommissionLoader.defaultCommissions.length)
    .boxed()
    .collect(Collectors.toMap(i -> CommissionLoader.defaultCommissions[i].replace(".json", ""), i -> i));

    private void buildAllCards() {
        CommissionCardData dailyData = null;
        List<CommissionCardData> regularData = new ArrayList<>();
        if(allCardData == null) allCardData = new ArrayList<>();

        for(CommissionCardData cardData : allCardData) {
            if(cardData.id().startsWith(DAILY_PREFIX)) {
                dailyData = cardData;
            }
            else regularData.add(cardData);
        }

        List<CommissionCardData> sorted = regularData.stream().sorted(Comparator
            .comparingInt((CommissionCardData data) -> STATE_ORDER.get(data.state()))
            .thenComparing(data -> DEFAULT_ORDER.containsKey(data.id()))
            .thenComparingInt(data -> DEFAULT_ORDER.getOrDefault(data.id(), Integer.MAX_VALUE))
            .thenComparing(CommissionCardData::id))
        .toList();

        if (dailyData != null) {
            ResourceLocation thumbnail = DAILY_COMM_THUMBNAIL;
            allCards.add(new CommissionCard(dailyData.id(), Component.literal(dailyData.title()), thumbnail, dailyData.state(), currentCommissionPage, true));
        }

        // Loop through allCardData and construct the cards
        for (CommissionCardData cardData : sorted) {
            ResourceLocation thumbnail = ClientCommonEvents.getCardThumbnail(cardData.id());
            allCards.add(new CommissionCard(cardData.id(), Component.literal(cardData.title()), thumbnail, cardData.state(), currentCommissionPage, false));
        }
    }

    public void refreshCards() {
        allCardData = ClientCommonEvents.getCards();
        allCards.clear();
        buildAllCards();
        flex();
        updateButtonVisibility();
    }

    public void refreshCardThumbnail(String id) {
        for(CommissionCard card : visibleCards) {
            if(card.getId().equals(id)) card.updateThumbnail(ClientCommonEvents.getCardThumbnail(id));
        }
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

    public JournalUI(int currentPage, int currentCommissionPage) {
        super(GameNarrator.NO_TITLE);
        this.currentPage = currentPage;
        this.currentCommissionPage = currentCommissionPage;
    }

    @Override
    protected void init() {
        Network.sendToServer(new RequestCardCommissionsPacket());
        allCardData = ClientCommonEvents.getCards();

        flex();
        // Done button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
         this.onClose();
        }).bounds(this.width / 2 - 2 * button_width, Math.min(y_offset + ui_height + button_padding, this.height - button_height), button_width * 4, button_height).build());
        
        renderMenuButtons();
        updateButtonVisibility();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(JOURNAL_PAGES.get(currentPage), x_offset, y_offset, 0, 0, ui_width, ui_height, ui_width, ui_height);
        if(currentPage == 0) renderStats(guiGraphics);

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

    protected void renderStats(GuiGraphics guiGraphics) {
        Font font = Minecraft.getInstance().font;

        String[] labels = { "Current Streak: ", "Max Streak: ", "Completed: " };
        long[] values = {
            ClientCommonEvents.getCurrentStreak(),
            ClientCommonEvents.getMaxStreak(),
            ClientCommonEvents.getCompletionCount()
        };

        for (int i = 0; i < labels.length; i++) {
            String full = labels[i] + values[i];
            int lineY = scaledStatsY + i * scaledStatsLineSpacing;
            int lineX = scaledStatsX - Math.round(font.width(full) * scaledStatsTextScale/ 2);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(lineX, lineY, 0);
            guiGraphics.pose().scale(scaledStatsTextScale, scaledStatsTextScale, 1f);
            guiGraphics.drawString(font, labels[i], 0, 0, 0xFF808080, false);
            guiGraphics.drawString(font, String.valueOf(values[i]), font.width(labels[i]), 0, 0xFF000000, false);
            guiGraphics.pose().popPose();
        }
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
            card.updateCommissionPage(currentCommissionPage);
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

        scaledStatsX = x_offset + (int)(stats_x * ((double) ui_width / 3840));
        scaledStatsY = y_offset + (int)(stats_y * ((double) ui_width / 3840));
        int targetTextHeight = (int)(75 * ((double) ui_width / 3840));
        scaledStatsTextScale = (float) targetTextHeight / font.lineHeight;
        scaledStatsLineSpacing = (int)(stats_spacing * ((double) ui_width / 3840));

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
            Network.sendToServer(new RequestCardThumbnailPacket(card.getId()));

            int x = x_offset + ((i - start)/3 == 0 ? 0 : page_halfwidth) + scaled_anchor;
            int y = y_offset + scaled_y + ((i - start) % 3) * scaled_spacing;
            card.setX(x); card.setY(y);
            card.setWidth(scaled_w); card.setHeight(scaled_h);

            this.addRenderableWidget(card);
            visibleCards.add(card);
        }
    }
    
    @Override
    public void removed() {
        ClientTickEvents.currentJournalPage = this.currentPage;
        ClientTickEvents.currentJournalCommissionPage = this.currentCommissionPage;
        super.removed();
    }

}
