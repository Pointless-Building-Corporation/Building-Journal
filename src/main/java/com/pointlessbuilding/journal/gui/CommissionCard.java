package com.pointlessbuilding.journal.gui;

import javax.annotation.Nullable;

import com.pointlessbuilding.journal.commission.CommissionState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CommissionCard extends AbstractWidget{

    private final ResourceLocation thumbnail;
    private final CommissionState state;

    private static final int frame_width = 128, frame_height = 88;
    private static final int window_width = 122, window_height = 75;
    private static final int border_width = 3;

    private static ResourceLocation COMMISSION_FRAME = new ResourceLocation("buildingjournal:textures/gui/commission_frame.png");
    private static ResourceLocation COMMISSION_STATE_ICONS = new ResourceLocation("buildingjournal:textures/gui/commission_state_icons.png");


    public CommissionCard(int x, int y, int width, int height, Component title, @Nullable ResourceLocation thumbnail, CommissionState state) {
        super(x, y, width, height, title);
        this.thumbnail = thumbnail;
        this.state = state;
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
            guiGraphics.fillGradient(winX, winY, winX + winW, winY + winH, 0xFFDCD8CE, 0XFFC9C4B6);
        }
        else {
            guiGraphics.blit(thumbnail, winX, winY, winW, winH, 0, 0, winW, winH, winW, winH);
        }

        guiGraphics.blit(COMMISSION_FRAME, getX(), getY(), this.width, this.height, 0, 0, frame_width, frame_height, frame_width, frame_height);

        if (state == CommissionState.UNAVAILABLE) {
            guiGraphics.fill(winX, winY, winX + winW, winY + winH, 0x99141412);
        }
        else if (state == CommissionState.COMPLETED) {
            guiGraphics.fill(winX, winY, winX + winW, winY + winH, 0x551D9E75);
        }
        if (state == CommissionState.UNAVAILABLE || state == CommissionState.COMPLETED) {
            int iconU = state == CommissionState.UNAVAILABLE ? 16 : 0;
            int iconX = winX + (winW - 16) / 2;
            int iconY = winY + (winH - 16) / 2;
            guiGraphics.blit(COMMISSION_STATE_ICONS, iconX, iconY, iconU, 0, 16, 16, 32, 16);
        }

        // Title
        Font font = Minecraft.getInstance().font;
        Component title = this.getMessage();
        int captionTop = getY() + Math.round((frame_height - 10 + 1) * scale);
        int captionBottom = getY() + Math.round((frame_height - 1) * scale);
        int captionY = captionTop + (captionBottom - captionTop - font.lineHeight) / 2;
        int textWidth = font.width(title);
        float drawnWidth = textWidth * scale;
        int textX = getX() + this.width / 2 - Math.round(drawnWidth / 2);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(textX, captionY, 0);
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.drawString(font, title, 0, 0, 0xFF3C2E1A, false);
        guiGraphics.pose().popPose();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.getMessage());
    }

}
