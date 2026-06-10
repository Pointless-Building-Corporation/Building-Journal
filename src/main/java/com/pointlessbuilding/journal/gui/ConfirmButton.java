package com.pointlessbuilding.journal.gui;

import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ConfirmButton extends Button{

    private final ResourceLocation gui;
    private final Supplier<Boolean> hasCompass;
    private final Supplier<Boolean> isProcessing;

    public ConfirmButton(int x, int y, ResourceLocation gui, Supplier<Boolean> hasCompass, Supplier<Boolean> isProcessing,  Button.OnPress onPress) {
        super(x, y, 22,22, Component.empty(), onPress, DEFAULT_NARRATION);
        this.gui = gui;
        this.hasCompass = hasCompass;
        this.isProcessing = isProcessing;
    }
    
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean active = hasCompass.get();
        boolean processing = isProcessing.get();
        this.active = !processing && active;
        if(processing) {
            int num_frames = 3;
            int frame = (int)((System.currentTimeMillis() / 100) % num_frames);
            guiGraphics.blit(gui, this.getX(), this.getY(), 208 + (frame * 22), 155, 22, 22, 512, 256);
        }
        else {
            int textureY = !active ? 111 : (isHovered ? 133 : 89);
            guiGraphics.blit(gui, this.getX(), this.getY(), 208, textureY, 22, 22, 512, 256);
        }
    }

}
