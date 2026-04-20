package com.pointlessbuilding.journal.menu;

import com.pointlessbuilding.journal.Registration;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class JournalContainer extends AbstractContainerMenu{

    public JournalContainer(int containerId, Player player) {
        super(Registration.JOURNAL_CONTAINER.get(), containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // Do nothing
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // This is not tied to a block and therefore is always valid
    }
    
}
