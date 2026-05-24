package com.pointlessbuilding.journal.menu;

import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.blocks.BlueprintRackEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class BlueprintRackContainer extends AbstractContainerMenu{

    private final BlockPos pos;
    private final int slot_count = BlueprintRackEntity.SLOT_COUNT;

    public BlueprintRackContainer(int windowId, Player player, BlockPos pos) {
        super(Registration.BLUEPRINT_RACK_CONTAINER.get(), windowId);
        this.pos = pos;
        if(player.level().getBlockEntity(pos) instanceof BlueprintRackEntity rack) {
            // Loop across 16 slots and addSlot
            for(int i = 0; i < 4; i++) {
                for(int j = 0; j < 4; j++) {
                    // Coords start at (53, 15) at gaps of 18.
                    addSlot(new SlotItemHandler(rack.getItems(), BlueprintRackEntity.SLOT+(i*4)+j, 53+(18*i), 15+(18*j)));
                }
            }
        }
        // Player inventory starts at (8,97).
        Inventory curInventory = player.getInventory();
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                addSlot(new Slot(curInventory, 9+(9*i)+j, 8+(18*j), 97+(18*i)));
            }
        }
        for(int j = 0; j < 9; j++) {    // Main inventory bar at (8,155)
            addSlot(new Slot(curInventory, j, 8+(18*j), 155));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(slot != null && slot.hasItem()) {
            ItemStack slotitem = slot.getItem();
            itemstack = slotitem.copy();

            if(index < slot_count) {    // Is item inside the rack?
                if(!this.moveItemStackTo(slotitem, slot_count, Inventory.INVENTORY_SIZE + slot_count, true))
                    return ItemStack.EMPTY;
            }
            else if(!this.moveItemStackTo(slotitem, 0, slot_count, false))  // Is item in player inventory?
                return ItemStack.EMPTY;

            if(slotitem.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, Registration.BLUEPRINT_RACK.get());
    }
    
}
