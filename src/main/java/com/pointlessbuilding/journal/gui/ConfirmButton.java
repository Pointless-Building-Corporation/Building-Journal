package com.pointlessbuilding.journal.gui;

import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ConfirmButton extends Button{

    private final ResourceLocation gui;
    private final Supplier<Boolean> hasCompass;

    public ConfirmButton(int x, int y, ResourceLocation gui, Supplier<Boolean> hasCompass) {
        super(x, y, 22,22, Component.empty(), btn -> {}, DEFAULT_NARRATION);
        this.gui = gui;
        this.hasCompass = hasCompass;
    }
    
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean active = hasCompass.get();
        int textureY = !active ? 111 : (isHovered ? 133 : 89);
        guiGraphics.blit(gui, this.getX(), this.getY(), 208, textureY, 22, 22, 256, 256);
    }

}
