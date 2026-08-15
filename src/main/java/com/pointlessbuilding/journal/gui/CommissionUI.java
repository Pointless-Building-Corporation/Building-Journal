package com.pointlessbuilding.journal.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.client.ClientCommonEvents;
import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.CommissionState;
import com.pointlessbuilding.journal.commission.CommissionUnlock;
import com.pointlessbuilding.journal.menu.CommissionContainer;
import com.pointlessbuilding.journal.network.Network;
import com.pointlessbuilding.journal.network.packets.CommissionSubmitPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

@SuppressWarnings("removal")
public class CommissionUI extends AbstractContainerScreen<CommissionContainer>{

    private final ResourceLocation GUI = new ResourceLocation(BuildingJournal.MODID, "textures/gui/commission_ui.png");
    private final ResourceLocation InventoryGUI = new ResourceLocation(BuildingJournal.MODID, "textures/gui/commission_ui_inventory.png");
    private final ResourceLocation CheckboxGUI = new ResourceLocation(BuildingJournal.MODID, "textures/gui/checkbox.png");
    private final ResourceLocation UnlockIcon = new ResourceLocation(BuildingJournal.MODID, "textures/gui/unlock_icon.png");
    private final ResourceLocation DAILY_COMM_THUMBNAIL = new ResourceLocation("buildingjournal:textures/gui/daily_commission_thumbnail.png");
    private static final String DAILY_PREFIX = "daily_";

    private static final int inv_width = 188, inv_height = 110;
    private static final int ui_width = 256, ui_height = 170;
    private static final int max_ui_width = inv_width * 2;
    private static final int padding = 0;
    private static final int slot_offset = 22;

    private int scaledUiWidth, scaledUiHeight;
    private int scaledUiX, scaledUiY;

    private static final int title_padding = 5;
    private static final float title_max_scale = 1f, title_min_scale = 0.5f;
    private float titleScale = 1;

    //Thumbnail
    private static final int thumbnail_x = 10, thumbnail_y = 25;
    private static final int thumbnail_width = 124, thumbnail_height = 77;  // increased by 2 to fit the polaroid frame.
    private static ResourceLocation thumbnail = null;

    private int scaledThumbX, scaledThumbY, scaledThumbWidth, scaledThumbHeight;
    private int iconOffsetX, iconOffsetY;

    // Close button
    private static final int close_x = 243, close_y = 4, close_size = 9;
    private int scaledCloseX, scaledCloseY, scaledCloseWidth, scaledCloseHeight;

    // Conditions
    private static final int conditions_x = 141, conditions_y = 23, conditions_width = (250-141), conditions_height = (140-23);
    private static final int checkbox_size = 20, scroll_speed = 5;
    private static final float font_scale = 0.75f;

    private int scaledConditionsX, scaledConditionsY, scaledConditionsWidth, scaledConditionsHeight;
    private int scaledCtitleX, scaledCtitleY;
    private int scroll_offset = 0;
    private int scaledCheckboxSize, rowHeight;
    private int maxScroll = 0;

    private int conditionsSize;
    private List<List<FormattedCharSequence>> wrappedConditionLines = new ArrayList<>();

    // Unlocks
    private static final int unlock_x = 8, unlock_y = 115, unlock_width = 128, unlock_height = 27;
    private static final int unlock_size = 20, unlock_scroll_speed = 5;
    private int scaledUnlocksX, scaledUnlocksY, scaledUnlocksWidth, scaledUnlocksHeight;
    private int scaledUnlockIconSize, scaledIconInnerSize;
    private int unlock_scroll_offset = 0;
    private int unlockMaxScroll = 0;

    // Submit Button
    private static final int submit_button_x = 229, submit_button_y = 143;
    private static final int submit_button_size = 22;
    private int scaledSubmitX, scaledSubmitY, scaledSubmitSize;

    public CommissionUI(CommissionContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = inv_width;
        this.imageHeight = inv_height;
    }

    @Override
    protected void init() {
        super.init();
        if(this.menu.getId().startsWith(DAILY_PREFIX)) thumbnail = DAILY_COMM_THUMBNAIL;
        else thumbnail = ClientCommonEvents.getCardThumbnail(this.getMenu().getId());
        flex();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int titleX = (this.scaledThumbX - this.leftPos) + this.scaledThumbWidth / 2;
        int titleY = (this.scaledUiY - this.topPos) + title_padding;
        renderScaledText(guiGraphics, this.title, titleX, titleY, titleScale);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        flex();
        renderBackground(guiGraphics);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Thumbnail
        if (thumbnail != null) {
            guiGraphics.blit(thumbnail, scaledThumbX, scaledThumbY, scaledThumbWidth, scaledThumbHeight, 0, 0, scaledThumbWidth, scaledThumbHeight, scaledThumbWidth, scaledThumbHeight);
        } else {
            guiGraphics.fillGradient(scaledThumbX, scaledThumbY, scaledThumbX + scaledThumbWidth, scaledThumbY + scaledThumbHeight, 0xFF000000, 0xFFFFFFFF);
        }
        // If completed, set the state
        if (this.menu.getState() == CommissionState.COMPLETED) {
            guiGraphics.blit(GUI, iconOffsetX, iconOffsetY, 22, 170, 16, 16, 256, 256);
            guiGraphics.fill(scaledThumbX, scaledThumbY, scaledThumbX + scaledThumbWidth, scaledThumbY + scaledThumbHeight, 0x551D9E75);
        }

        // Commission UI
        guiGraphics.blit(GUI, scaledUiX, scaledUiY, scaledUiWidth, scaledUiHeight, 0, 0, ui_width, ui_height, 256, 256);

        renderScaledText(guiGraphics, Component.literal("Conditions"), scaledCtitleX, scaledCtitleY, font_scale);
        // Conditions
        renderConditionsList(guiGraphics, mouseX, mouseY);
        // Seperators
        guiGraphics.fill(scaledConditionsX, scaledConditionsY - 2, scaledConditionsX + scaledConditionsWidth, scaledConditionsY - 1, 0xFF000000);
        guiGraphics.fill(scaledConditionsX, scaledConditionsY + scaledConditionsHeight, scaledConditionsX + scaledConditionsWidth, scaledConditionsY + scaledConditionsHeight + 1, 0xFF000000);

        // Unlocks
        renderUnlocksList(guiGraphics, mouseX, mouseY);
        // Seperators
        guiGraphics.fill(scaledUnlocksX - 1, scaledUnlocksY, scaledUnlocksX, scaledUnlocksY + scaledUnlocksHeight, 0xFF000000);
        guiGraphics.fill(scaledUnlocksX + scaledUnlocksWidth, scaledUnlocksY, scaledUnlocksX + scaledUnlocksWidth + 1, scaledUnlocksY + scaledUnlocksHeight, 0xFF000000);

        // Submit
        renderSubmitButton(guiGraphics, mouseX, mouseY);

        // Inventory UI
        guiGraphics.blit(InventoryGUI, leftPos, topPos, 0, 0, inv_width, inv_height, 256, 256);

        RenderSystem.disableBlend();
    }
    
    protected void renderConditionsList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.enableScissor(scaledConditionsX, scaledConditionsY, scaledConditionsX + scaledConditionsWidth, scaledConditionsY + scaledConditionsHeight);

        int y = scaledConditionsY - scroll_offset + 1;
        List<Boolean> results = this.menu.getConditionResults();
        List<String> descriptions = this.menu.getFailureDescriptions();
        // List<CommissionCondition> conditionList = this.menu.getConditions();
        // List<String> hoveredTitles = new ArrayList<>();
        // for(CommissionCondition condition : conditionList) hoveredTitles.add(condition.getTitle(true));
        String hoveredTooltip = null;

        for (int i = 0; i < conditionsSize; i++) {
            List<FormattedCharSequence> lines = wrappedConditionLines.get(i);
            int thisRowHeight = lines.size() * rowHeight;

            if (y + thisRowHeight  >= scaledConditionsY && y <= scaledConditionsY + scaledConditionsHeight) {
                // Checkbox
                int checkboxY = y + (rowHeight - scaledCheckboxSize) / 2;
                int checkboxV = (i < results.size()) ? (results.get(i) ? 21 : 41) : 0;
                guiGraphics.blit(CheckboxGUI, scaledConditionsX, checkboxY, scaledCheckboxSize, scaledCheckboxSize, 0, checkboxV, 20, 20, 64, 64);

                int textX = scaledConditionsX + (2 * scaledCheckboxSize) + 2;
                for (int line = 0; line < lines.size(); line++) {
                    int lineY = y + line * rowHeight + (rowHeight - Math.round(this.font.lineHeight * font_scale)) / 2;

                    // this fool again?!
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().scale(font_scale, font_scale, 1f);
                    // Fake drop shadow because the text is black
                    guiGraphics.drawString(this.font, lines.get(line), (int)(textX / font_scale) + 1, (int)(lineY / font_scale) + 1, 0xC7BB93, false);
                    guiGraphics.drawString(this.font, lines.get(line), (int)(textX / font_scale), (int)(lineY / font_scale), 0x000000, false);
                    guiGraphics.pose().popPose();
                }

                boolean hovered = mouseX >= scaledConditionsX && mouseX < scaledConditionsX + scaledConditionsWidth && mouseY >= y && mouseY < y+ thisRowHeight;
                if(hovered) {
                    // If no blueprint in slot; maybe for later
                    // if(results.isEmpty() && descriptions.size() == 0) {
                    //     hoveredTooltip = hoveredTitles.get(i);
                    // }
                    if(results.isEmpty() && descriptions.size() != 0) {
                        hoveredTooltip = descriptions.get(0);
                    }
                    else if (i < results.size() && !results.get(i) && i < descriptions.size()) {
                        hoveredTooltip = descriptions.get(i);
                    }
                }
                
            }
            y += thisRowHeight;
        }

        guiGraphics.disableScissor();

        if(hoveredTooltip != null) {
            guiGraphics.renderTooltip(this.font, Component.literal(hoveredTooltip), mouseX, mouseY);
        }
    }

    protected void renderUnlocksList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.enableScissor(scaledUnlocksX, scaledUnlocksY, scaledUnlocksX + scaledUnlocksWidth, scaledUnlocksY + scaledUnlocksHeight);

        List<CommissionUnlock> unlocks = this.menu.getUnlocks();
        int scaledGap = Math.round(4 * (scaledUnlockIconSize / (float) unlock_size));
        int x = scaledUnlocksX + scaledGap - unlock_scroll_offset;
        int y = scaledUnlocksY + (scaledUnlocksHeight - scaledUnlockIconSize) / 2;
        int iconOffset = (scaledUnlockIconSize - scaledIconInnerSize) / 2;
        String hoveredTitle = null;

        for(CommissionUnlock unlock : unlocks) {
            if(x + scaledUnlockIconSize >= scaledUnlocksX && x <= scaledUnlocksX + scaledUnlocksWidth) {
                guiGraphics.blit(UnlockIcon, x, y, scaledUnlockIconSize, scaledUnlockIconSize, 0, 0, unlock_size, unlock_size, unlock_size, unlock_size);
                unlock.renderIcon(guiGraphics, x + iconOffset, y + iconOffset, scaledIconInnerSize);

                boolean hovered = mouseX >= x && mouseX < x + scaledUnlockIconSize && mouseY > y && mouseY < y + scaledUnlockIconSize;
                if(hovered) hoveredTitle = unlock.getTitle(); 
            }
            x += scaledUnlockIconSize + scaledGap;
        }

        guiGraphics.disableScissor();

        if(hoveredTitle != null) {
            guiGraphics.renderTooltip(this.font, Component.literal(hoveredTitle), mouseX, mouseY);
        }
    }

    protected void renderSubmitButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        boolean active = this.menu.isSubmitActive();
        boolean hovered = active &&
            mouseX >= scaledSubmitX && mouseX < scaledSubmitX + scaledSubmitSize &&
            mouseY >= scaledSubmitY && mouseY < scaledSubmitY + scaledSubmitSize;
        
        int v = hovered ? 2 * submit_button_size : (active ? 0 : submit_button_size);

        guiGraphics.blit(GUI, scaledSubmitX, scaledSubmitY, scaledSubmitSize, scaledSubmitSize, 0, 170 + v, submit_button_size, submit_button_size, 256, 256);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Close
        if (mouseX >= scaledCloseX && mouseX < scaledCloseX + scaledCloseWidth && 
        mouseY >= scaledCloseY && mouseY < scaledCloseY + scaledCloseHeight) {
            this.onClose();
            return true;
        }

        // Submit
        if(this.menu.isSubmitActive() &&
        mouseX >= scaledSubmitX && mouseX < scaledSubmitX + scaledSubmitSize &&
        mouseY >= scaledSubmitY && mouseY < scaledSubmitY + scaledSubmitSize) {
            Network.sendToServer(new CommissionSubmitPacket(this.menu.getId()));
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Conditions
        if (mouseX >= scaledConditionsX && mouseX < scaledConditionsX + scaledConditionsWidth && 
            mouseY >= scaledConditionsY && mouseY < scaledConditionsY + scaledConditionsHeight) {
            scroll_offset = Mth.clamp(scroll_offset - (int) delta * scroll_speed, 0, maxScroll);
            return true;
        }

        // Unlocks
        if (mouseX >= scaledUnlocksX && mouseX < scaledUnlocksX + scaledUnlocksWidth && 
            mouseY >= scaledUnlocksY && mouseY < scaledUnlocksY + scaledUnlocksHeight) {
            unlock_scroll_offset = Mth.clamp(unlock_scroll_offset - (int)(delta * unlock_scroll_speed), 0, unlockMaxScroll);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    protected void renderScaledText(GuiGraphics guiGraphics, Component text, int x, int y, float scale) {
        // Pose nonsense to "scale" the existing gui font to reasonable sizes that are also not unreadable
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1f);
        int adjustedX = x - (int)(this.font.width(text) * scale) / 2;
        guiGraphics.drawString(this.font, text, (int)(adjustedX / scale) + 1, (int)(y / scale) + 1, 0xC7BB93, false);
        guiGraphics.drawString(this.font, text, (int)(adjustedX / scale), (int)(y / scale), 0x000000, false);
        guiGraphics.pose().popPose();
    }

    private void updateMaxScroll() {
        int contentHeight = 0;
        for (List<FormattedCharSequence> lines : wrappedConditionLines) {
            contentHeight += lines.size() * rowHeight;
        }
        maxScroll = Math.max(0, contentHeight - scaledConditionsHeight);
        scroll_offset = Mth.clamp(scroll_offset, 0, maxScroll);
    }

    private void updateUnlockMaxScroll() {
        List<CommissionUnlock> unlocks = this.menu.getUnlocks();
        int scaledGap = Math.round(4 * (scaledUnlockIconSize / (float) unlock_size));
        int contentWidth = unlocks.isEmpty() ? 0 : unlocks.size() * scaledUnlockIconSize + unlocks.size() * scaledGap;
        unlockMaxScroll = Math.max(0, contentWidth - scaledUnlocksWidth);
        unlock_scroll_offset = Mth.clamp(unlock_scroll_offset, 0, unlockMaxScroll);
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().setScreen(new JournalUI(1, this.menu.getCommissionPage()));
    }

    protected void flex() {
        float scale = (float)(this.height - inv_height - padding) / ui_height;

        if(Math.round(ui_width * scale) > max_ui_width) scale = (float) max_ui_width / ui_width; 

        scaledUiWidth = Math.round(ui_width * scale);
        scaledUiHeight = Math.round(ui_height * scale);
        scaledUiX = (this.width - scaledUiWidth) / 2;
        scaledUiY = padding + slot_offset;

        this.leftPos = (this.width - inv_width) / 2;
        this.topPos = this.scaledUiY + scaledUiHeight - slot_offset;

        scaledThumbX = scaledUiX + Math.round(scaledUiWidth * (thumbnail_x / (float) ui_width));
        scaledThumbY = scaledUiY + Math.round(scaledUiHeight * (thumbnail_y / (float) ui_height));
        scaledThumbWidth = Math.round(scaledUiWidth * (thumbnail_width / (float) ui_width));
        scaledThumbHeight = Math.round(scaledUiHeight * (thumbnail_height / (float) ui_height));

        int availableTitleWidth = 2 * (scaledThumbX - scaledUiX + scaledThumbWidth / 2);
        int currentTitleWidth = this.font.width(this.title);
        titleScale = currentTitleWidth > 0 ? Mth.clamp(availableTitleWidth / (float) currentTitleWidth, title_min_scale, title_max_scale) : title_max_scale;

        iconOffsetX = scaledThumbX + (scaledThumbWidth - 16) / 2;
        iconOffsetY = scaledThumbY + (scaledThumbHeight - 16) / 2;

        scaledCloseX = scaledUiX + Math.round(close_x * scale);
        scaledCloseY = scaledUiY + Math.round(close_y * scale);
        scaledCloseWidth = Math.round(close_size * scale);
        scaledCloseHeight = Math.round(close_size * scale);

        scaledConditionsX = scaledUiX + Math.round(conditions_x * scale);
        scaledConditionsY = scaledUiY + Math.round(conditions_y * scale);
        scaledConditionsWidth = Math.round(conditions_width * scale);
        scaledConditionsHeight = Math.round(conditions_height * scale);

        scaledCtitleX = scaledConditionsX + scaledConditionsWidth / 2;
        scaledCtitleY = scaledConditionsY - Math.round(this.font.lineHeight * font_scale) - 2;

        scaledCheckboxSize = Math.round(checkbox_size * scale * 0.25f);
        int scaledFontLineHeight = Math.round(this.font.lineHeight * font_scale);
        rowHeight = Math.max(2 * scaledCheckboxSize, scaledFontLineHeight);

        int wrapWidthScreen = scaledConditionsWidth - (2 * scaledCheckboxSize) - 2;
        int wrapWidthUnscaled = Math.round(wrapWidthScreen / font_scale);

        wrappedConditionLines.clear();
        List<CommissionCondition> conditionList = this.menu.getConditions();
        for(CommissionCondition condition : conditionList) {
            String titleText = condition.getTitle(false);
            wrappedConditionLines.add(this.font.split(Component.literal(titleText), wrapWidthUnscaled));
        }
        conditionsSize = conditionList.size();

        scaledUnlocksX = scaledUiX + Math.round(unlock_x * scale);
        scaledUnlocksY = scaledUiY + Math.round(unlock_y * scale);
        scaledUnlocksWidth = Math.round(unlock_width * scale);
        scaledUnlocksHeight = Math.round(unlock_height * scale);

        scaledUnlockIconSize = Math.round(unlock_size * scale);
        scaledIconInnerSize = Math.round(16 * scale);

        scaledSubmitX = scaledUiX + Math.round(submit_button_x * scale);
        scaledSubmitY = scaledUiY + Math.round(submit_button_y * scale);
        scaledSubmitSize = Math.round(submit_button_size * scale);

        updateUnlockMaxScroll();
        updateMaxScroll();
    }

}
