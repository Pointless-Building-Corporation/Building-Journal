package com.pointlessbuilding.journal.commission;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.level.ServerPlayer;

public interface CommissionUnlock {
    void apply(ServerPlayer player);
    String getTitle();
    void renderIcon(GuiGraphics guiGraphics, int x, int y, int size);
}
