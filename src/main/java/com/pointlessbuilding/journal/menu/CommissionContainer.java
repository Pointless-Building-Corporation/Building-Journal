package com.pointlessbuilding.journal.menu;

import java.util.List;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.commission.CommissionState;
import com.pointlessbuilding.journal.commission.CommissionUnlock;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class CommissionContainer extends AbstractContainerMenu{

    private final String commissionId;
    private final String title;
    private final CommissionState state;
    private final String conditionsJson;
    private final List<CommissionUnlock> unlocks;
    private final int commissionPage;
    private final ItemStackHandler blueprintHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() == Registration.BLUEPRINT.get();
        }
    };

    public CommissionContainer(int containerId, Player player, String commissionId, String title, CommissionState state, String conditionsJson, List<CommissionUnlock> unlocks, int commissionPage) {
        super(Registration.COMMISSION_CONTAINER.get(), containerId);
        BuildingJournal.LOGGER.info("Inside {}", commissionId);
        this.commissionId = commissionId;
        this.title = title;
        this.state = state;
        this.conditionsJson = conditionsJson;
        this.unlocks = unlocks;
        this.commissionPage = commissionPage;

        Inventory curInventory = player.getInventory();

        // Add blueprint slot at (86,6).
        this.addSlot(new SlotItemHandler(blueprintHandler, 0, 86, 6));
    
        // Player inventory starts at (14,28).
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                addSlot(new Slot(curInventory, 9+(9*i)+j, 14+(18*j), 28+(18*i)));
            }
        }
        // Main inventory bar at (14,86).
        for(int j = 0; j < 9; j++) {    
            addSlot(new Slot(curInventory, j, 14+(18*j), 86));
        }
    }

    public String getId() {
        return this.commissionId;
    }

    public String getTitle() {
        return this.title;
    }

    public CommissionState getState() {
        return this.state;
    }
 
    public String getConditionsJson() {
        return this.conditionsJson;
    }

    public List<CommissionUnlock> getUnlocks() {
        return this.unlocks;
    }

    public int getCommissionPage() {
        return this.commissionPage;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // Do nothing. This probably changes later
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // This is not tied to a block and therefore is always valid
    }
    
}
