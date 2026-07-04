package com.pointlessbuilding.journal.menu;

import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.blocks.DraftingTableEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class DraftingTableContainer extends AbstractContainerMenu{

    private final BlockPos pos;
    private DraftingTableEntity tableEntity;

    public DraftingTableContainer(int windowId, Player player, BlockPos pos) {
        super(Registration.DRAFTING_TABLE_CONTAINER.get(), windowId);
        Inventory curInventory = player.getInventory();
        this.pos = pos;

        if(player.level().getBlockEntity(pos) instanceof DraftingTableEntity tableEntity) {
            this.tableEntity = tableEntity;
            addDataSlots(tableEntity.getData());
            this.addSlot(new SlotItemHandler(tableEntity.getItems(), DraftingTableEntity.COMPASS_SLOT, 13, 79));
            this.addSlot(new SlotItemHandler(tableEntity.getItems(), DraftingTableEntity.BLUEPRINT_SLOT, 179, 67));
        }

        // Player inventory starts at (24,123).
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                addSlot(new Slot(curInventory, 9+(9*i)+j, 24+(18*j), 123+(18*i)));
            }
        }
        for(int j = 0; j < 9; j++) {    // Main inventory bar at (24,181)
            addSlot(new Slot(curInventory, j, 24+(18*j), 181));
        }
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean getProcessing() {
        return tableEntity.getData().get(0) != 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(),pos), player, Registration.DRAFTING_TABLE.get());
    }
    
}
