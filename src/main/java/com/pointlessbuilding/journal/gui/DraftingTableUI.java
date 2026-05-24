package com.pointlessbuilding.journal.gui;

import java.util.ArrayList;
import java.util.List;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.blocks.DraftingTableEntity;
import com.pointlessbuilding.journal.menu.DraftingTableContainer;
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

        if(cachedBoxCount == 0) {
            guiGraphics.drawString(this.font, "No Boundaries present in compass!", 50, 7, 0x404040, false);
        }
        else {
            guiGraphics.drawString(this.font, cachedBoxCount + " boundaries loaded in compass.", 50, 7, 0x404040, false);
            guiGraphics.drawString(this.font, "Total blocks covered: " + cachedTotalBlocks, 50, 30, 0x404040, false);
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

        renderNameField();
        renderConfirmButton();
        updateCompassData();
    }

    private void renderNameField() {
        nameField = new EditBox(this.font, leftPos + 51, topPos + 101, 108, 14, Component.empty());
        nameField.setCanLoseFocus(false);
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setMaxLength(50);
        nameField.setBordered(false);
        this.setInitialFocus(nameField);
        this.addWidget(nameField);
    }

    private void renderConfirmButton() {
        confirmButton = new ConfirmButton(leftPos + 176, topPos + 89, GUI, () -> !menu.getSlot(DraftingTableEntity.COMPASS_SLOT).getItem().isEmpty());
        addRenderableWidget(confirmButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        renderBackground(guiGraphics);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);

        //Name Field checks
        boolean hasCompass = !(menu.getSlot(DraftingTableEntity.COMPASS_SLOT).getItem().isEmpty());
        if(hasCompass) {
            guiGraphics.blit(GUI, leftPos + 48, topPos + 97, 0, 205, 110, 16);
        }
        else {
            guiGraphics.blit(GUI, leftPos + 48, topPos + 97, 0, 221, 110, 16);
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
        updateCompassData();

        boolean hasCompass = !menu.getSlot(DraftingTableEntity.COMPASS_SLOT).getItem().isEmpty();
        if (hasCompass != lastHasCompass) {
            lastHasCompass = hasCompass;
            nameField.setEditable(hasCompass);
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
        if (keyCode == 256) {
         this.minecraft.player.closeContainer();
      }

      return !this.nameField.keyPressed(keyCode, scanCode, modifiers) && !this.nameField.canConsumeInput() ? super.keyPressed(keyCode, scanCode, modifiers) : true;
    }

}
