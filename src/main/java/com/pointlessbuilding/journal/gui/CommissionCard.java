package com.pointlessbuilding.journal.gui;

import javax.annotation.Nullable;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.client.ClientCommonEvents;
import com.pointlessbuilding.journal.commission.CommissionState;
import com.pointlessbuilding.journal.network.Network;
import com.pointlessbuilding.journal.network.packets.CommissionDetailPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("removal")
public class CommissionCard extends AbstractWidget{

    private final String commissionId;
    private ResourceLocation thumbnail;
    private final CommissionState state;
    private int currentCommissionPage;
    private final boolean isDaily;

    private static final int frame_width = 128, frame_height = 88;
    private static final int window_width = 122, window_height = 75;
    private static final int border_width = 3;

    private static ResourceLocation COMMISSION_FRAME = new ResourceLocation("buildingjournal:textures/gui/commission_frame.png");
    private static ResourceLocation COMMISSION_STATE_ICONS = new ResourceLocation("buildingjournal:textures/gui/commission_state_icons.png");


    public CommissionCard(String commissionId, Component title, @Nullable ResourceLocation thumbnail, CommissionState state, int currentCommissionPage, boolean isDaily) {
        // I'm manually resizing the cards in flex()
        super(0, 0, 0, 0, title);

        this.commissionId = commissionId;
        this.thumbnail = thumbnail;
        this.state = state;
        this.currentCommissionPage = currentCommissionPage;
        this.isDaily = isDaily;
        this.setTooltip(Tooltip.create(title));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float scale = (float) this.width / frame_width;

        int winX = getX() + Math.round(border_width * scale);
        int winY = getY() + Math.round(border_width * scale);
        int winW = Math.round(window_width * scale);
        int winH = Math.round(window_height * scale);

        // If thumbnail is missing
        if (thumbnail == null) {
            guiGraphics.fillGradient(winX, winY, winX + winW, winY + winH, 0xFF000000, 0xFFFFFFFF);
        }
        else {
            guiGraphics.blit(thumbnail, winX, winY, winW, winH, 0, 0, winW, winH, winW, winH);
        }

        guiGraphics.blit(COMMISSION_FRAME, getX(), getY(), this.width, this.height, 0, 0, frame_width, frame_height, frame_width, frame_height);

        if(isDaily) {
            guiGraphics.fill(getX(), getY(), getX() + this.width, winY, 0x99FFD700);
            guiGraphics.fill(getX(), winY + winH, getX() + this.width, getY() + this.height, 0x99FFD700);
            guiGraphics.fill(getX(), winY, winX, winY + winH, 0x99FFD700);
            guiGraphics.fill(winX + winW, winY, getX() + this.width, winY + winH, 0x99FFD700);
        }

        if (state == CommissionState.UNAVAILABLE) {
            guiGraphics.fill(winX, winY, winX + winW, winY + winH, 0x99141412);
        }
        else if (state == CommissionState.COMPLETED) {
            guiGraphics.fill(winX, winY, winX + winW, winY + winH, 0x551D9E75);
        }

        // Title
        Font font = Minecraft.getInstance().font;
        Component title = this.getMessage();
        String timerText = isDaily ? getDailyTimerText() : null;

        if (state == CommissionState.UNAVAILABLE || state == CommissionState.COMPLETED) {
            int iconU = state == CommissionState.UNAVAILABLE ? 16 : 0;
            int iconX = winX + (winW - 16) / 2;
            int iconY = winY + (winH - 16) / 2;

            if (isDaily && state == CommissionState.COMPLETED) {
                int textWidth = font.width(timerText);
                float textScale = (float) winW * 0.9f / textWidth;
                textScale = Math.min(textScale, scale);
                int scaledTextHeight = Math.round(font.lineHeight * textScale);
                int scaledTextWidth = Math.round(textWidth * textScale);
                int groupHeight = 16 + 2 + scaledTextHeight;
                iconY = winY + (winH - groupHeight) / 2;
                int textX = winX + (winW - scaledTextWidth) / 2;
                int textY = iconY + 16 + 2;

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(textX, textY, 0);
                guiGraphics.pose().scale(textScale, textScale, 1f);
                guiGraphics.drawString(font, timerText, 0, 0, 0xFFFFFFFF, false);
                guiGraphics.pose().popPose();
            }

            guiGraphics.blit(COMMISSION_STATE_ICONS, iconX, iconY, iconU, 0, 16, 16, 32, 16);
        }
        else if (isDaily) {
            int textWidth = font.width(timerText);
            float textScale = (float) winW * 0.9f / scale;
            textScale = Math.min(textScale, scale);
            int scaledWidth = Math.round(textWidth * textScale);
            int textX = winX + (winW - scaledWidth) / 2;
            int textY = winY + Math.round((winH - font.lineHeight * textScale) / 2);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(textX, textY, 0);
            guiGraphics.pose().scale(textScale, textScale, 1f);
            guiGraphics.drawString(font, timerText, 0, 0, 0xFFFFFFFF, false);
            guiGraphics.pose().popPose();
        }


        int textMargin = 2;
        int captionTop = getY() + Math.round((frame_height - 10 + textMargin) * scale);
        int captionBottom = getY() + Math.round((frame_height - textMargin) * scale);
        float fontScale = (float) (captionBottom - captionTop)/font.lineHeight;
        fontScale = (float) Math.floor(fontScale * 4f) / 4f;
        int textWidth = font.width(title);
        int textX = getX() + this.width / 2 - Math.round(textWidth * fontScale / 2);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(textX, captionTop, 0);
        guiGraphics.pose().scale(fontScale, fontScale, 1f);
        guiGraphics.drawString(font, title, 0, 0, 0xFF3C2E1A, false);
        guiGraphics.pose().popPose();
    }

    protected String getDailyTimerText() {
        long remainingMillis = ClientCommonEvents.getNextResetMillis() - System.currentTimeMillis();
        if(remainingMillis < 0) remainingMillis = 0;

        long totalSeconds = remainingMillis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        String timeString =  String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return state == CommissionState.COMPLETED ? "Next Commission in " + timeString : timeString + " remaining";
    }

    public String getId() {
        return commissionId;
    }

    public void updateThumbnail(ResourceLocation thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void updateCommissionPage(int currentPage) {
        this.currentCommissionPage = currentPage;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        if(this.state != CommissionState.UNAVAILABLE) {
            Network.sendToServer(new CommissionDetailPacket(commissionId, currentCommissionPage));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.getMessage());
    }

}
