package com.pointlessbuilding.journal.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.blocks.DraftingTableEntity;
import com.pointlessbuilding.journal.menu.DraftingTableContainer;
import com.pointlessbuilding.journal.network.ConfirmBlueprintPacket;
import com.pointlessbuilding.journal.network.Network;
import com.pointlessbuilding.journal.utility.BoundaryMath;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class DraftingTableUI extends AbstractContainerScreen<DraftingTableContainer>{

    private final ResourceLocation GUI = new ResourceLocation(BuildingJournal.MODID, "textures/gui/drafting_table.png");

    private EditBox nameField;
    private ConfirmButton confirmButton;

    private boolean lastHasCompass;
    private int cachedBoxCount = 0;
    private long cachedTotalBlocks = 0;
    private boolean processingBlueprint = false;

    public DraftingTableUI(DraftingTableContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 208;
        this.imageHeight = 205;
        this.titleLabelY = 5;
        this.inventoryLabelY = 113;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        boolean hasCompass = !(menu.getSlot(DraftingTableEntity.COMPASS_SLOT).getItem().isEmpty());
        if(cachedBoxCount == 0) {
            if(hasCompass) guiGraphics.drawWordWrap(this.font, Component.literal("No boundaries loaded in compass!"), 65, 12, 70, 0xFFFFFF);
        }
        else {
            guiGraphics.drawWordWrap(this.font, Component.literal(cachedBoxCount + " boundaries loaded in compass."), 65, 12, 70, 0xFFFFFF);
            guiGraphics.drawWordWrap(this.font, Component.literal("Total blocks covered: " + cachedTotalBlocks), 65, 45, 70, 0xFFFFFF);
        }
    }

    private void updateCompassData() {
        ItemStack compass = menu.getSlot(DraftingTableEntity.COMPASS_SLOT).getItem();
        if(compass.isEmpty() || !compass.hasTag() || !compass.getTag().contains("StoredBoxes")) {
            cachedBoxCount = 0;
            cachedTotalBlocks = 0;
            return;
        }
        ListTag boxes = compass.getTag().getList("StoredBoxes", Tag.TAG_COMPOUND);
        cachedBoxCount = boxes.size();
        cachedTotalBlocks = 0;
        List<int[]> firsts = new ArrayList<>();
        List<int[]> seconds = new ArrayList<>();
        for(int i = 0; i < boxes.size(); i++) {
            CompoundTag box = boxes.getCompound(i);
            int[] first = box.getIntArray("FirstPos");
            int[] second = box.getIntArray("SecondPos");
            firsts.add(first);
            seconds.add(second);
        }
        cachedTotalBlocks = BoundaryMath.unionVolume(firsts, seconds);
    }

    @Override
    protected void init() {
        super.init();

        initNameField();
        initConfirmButton();
        updateCompassData();
        nameField.setEditable(!menu.getSlot(DraftingTableEntity.COMPASS_SLOT).getItem().isEmpty());
    }

    private void initNameField() {
        nameField = new EditBox(this.font, leftPos + 51, topPos + 101, 108, 14, Component.empty());
        nameField.setCanLoseFocus(false);
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setMaxLength(50);
        nameField.setBordered(false);
        this.setInitialFocus(nameField);
        this.addWidget(nameField);
    }

    private void initConfirmButton() {
        // Check both that the compass slot is full and the blueprint slot is empty.
        processingBlueprint = menu.getProcessing();
        confirmButton = new ConfirmButton(leftPos + 176, topPos + 89, GUI,
            () -> !menu.getSlot(DraftingTableEntity.COMPASS_SLOT).getItem().isEmpty() && menu.getSlot(DraftingTableEntity.BLUEPRINT_SLOT).getItem().isEmpty(),
            () -> processingBlueprint,
            btn -> {
                if(!menu.getSlot(DraftingTableEntity.COMPASS_SLOT).getItem().isEmpty())
                    Network.sendToServer(new ConfirmBlueprintPacket(menu.getPos(), nameField.getValue()));
                processingBlueprint = true;
            }
        );
        addRenderableWidget(confirmButton);
    }

    public void onBlueprintStart() {
        // BuildingJournal.LOGGER.info("Inside onBlueprintStart");
        processingBlueprint = true;
    }

    public void onBlueprintComplete() {
        //BuildingJournal.LOGGER.info("Inside onBlueprintComplete");
        processingBlueprint = false;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        renderBackground(guiGraphics);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight, 512, 256);

        boolean hasCompass = !(menu.getSlot(DraftingTableEntity.COMPASS_SLOT).getItem().isEmpty());
        if(hasCompass) {
            guiGraphics.blit(GUI, leftPos + 48, topPos + 97, 0, 205, 110, 16, 512, 256); // name field
            guiGraphics.blit(GUI, leftPos + 55, topPos + 0, 208, 0, 98, 80, 512, 256);   // blueprint
        }
        else {
            guiGraphics.blit(GUI, leftPos + 48, topPos + 97, 0, 221, 110, 16, 512, 256);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        nameField.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        nameField.tick();

        boolean hasCompass = !menu.getSlot(DraftingTableEntity.COMPASS_SLOT).getItem().isEmpty();
        if (hasCompass != lastHasCompass) {
            lastHasCompass = hasCompass;
            nameField.setEditable(hasCompass);
            updateCompassData();
            if (!hasCompass) {
                nameField.setValue("");
            } else {
                ItemStack compass = menu.getSlot(DraftingTableEntity.COMPASS_SLOT).getItem();
                if(compass.hasCustomHoverName()) {
                    nameField.setValue(compass.getHoverName().getString());
                }
                else {
                    nameField.setValue("Blueprint");
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE) {
            this.minecraft.player.closeContainer();
            return true;
        }

        return !this.nameField.keyPressed(keyCode, scanCode, modifiers) && !this.nameField.canConsumeInput() ? super.keyPressed(keyCode, scanCode, modifiers) : true;
    }

}
