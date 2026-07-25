package com.pointlessbuilding.journal.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.CommissionLoader;
import com.pointlessbuilding.journal.commission.CommissionState;
import com.pointlessbuilding.journal.commission.CommissionUnlock;
import com.pointlessbuilding.journal.commission.EvaluationResult;
import com.pointlessbuilding.journal.items.Blueprint;

import net.minecraft.nbt.CompoundTag;
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
    private List<CommissionCondition> conditions = new ArrayList<>();
    private final String unlocksJson;
    private List<CommissionUnlock> unlocks = new ArrayList<>();
    private final int commissionPage;

    private final ItemStackHandler blueprintHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() == Registration.BLUEPRINT.get();
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            evaluateConditions();
        }
    };
    private List<Boolean> conditionResults = new ArrayList<>();
    private List<String> failureDescriptions = new ArrayList<>();

    public CommissionContainer(int containerId, Player player, String commissionId, String title, CommissionState state, String conditionsJson, String unlocksJson, int commissionPage) {
        super(Registration.COMMISSION_CONTAINER.get(), containerId);
        this.commissionId = commissionId;
        this.title = title;
        this.state = state;
        this.conditionsJson = conditionsJson;
        this.unlocksJson = unlocksJson;
        this.commissionPage = commissionPage;

        parseConditionsJson();
        parseUnlocksJson();

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

    protected void parseConditionsJson() {
        JsonArray conditionsArray;
        try {
            conditionsArray = JsonParser.parseString(conditionsJson).getAsJsonArray();
        }
        catch (Exception e) {
            BuildingJournal.LOGGER.error("Failed to parse conditions Json for commission {}: {}", commissionId, e);
            return;
        }

        for (JsonElement condition : conditionsArray) {
            JsonObject conditionObject = condition.getAsJsonObject();
            if(!conditionObject.has("condition")) {
                BuildingJournal.LOGGER.error("Missing condition field within conditions of {}", commissionId);
                continue;
            }
            String conditionType = conditionObject.get("condition").getAsString();
            Function<JsonObject, CommissionCondition> parser = CommissionLoader.CONDITION_PARSERS.get(conditionType);
            if(parser == null) {
                BuildingJournal.LOGGER.error("Unknown condition type {} in commission {}", conditionType, commissionId);
                continue;
            }
            CommissionCondition result = parser.apply(conditionObject);
            if(result != null) conditions.add(result);
        }
    }

    protected void parseUnlocksJson() {
        JsonArray unlocksArray;
        try {
            unlocksArray = JsonParser.parseString(unlocksJson).getAsJsonArray();
        }
        catch (Exception e) {
            BuildingJournal.LOGGER.error("Failed to parse unlocks Json for commission {}: {}", commissionId, e);
            return;
        }

        for (JsonElement unlock : unlocksArray) {
            JsonObject unlockObject = unlock.getAsJsonObject();
            if(!unlockObject.has("unlock")) {
                BuildingJournal.LOGGER.error("Missing unlock field within unlocks of {}", commissionId);
                continue;
            }
            String unlockType = unlockObject.get("unlock").getAsString();
            Function<JsonObject, CommissionUnlock> parser = CommissionLoader.UNLOCK_PARSERS.get(unlockType);
            if(parser == null) {
                BuildingJournal.LOGGER.error("Unknown unlock type {} in commission {}", unlockType, commissionId);
                continue;
            }
            CommissionUnlock result = parser.apply(unlockObject);
            if(result != null) unlocks.add(result);
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

    public List<CommissionCondition> getConditions() {
        return conditions;
    }

    public List<CommissionUnlock> getUnlocks() {
        return this.unlocks;
    }

    public List<Boolean> getConditionResults() {
        return this.conditionResults;
    }

    public List<String> getFailureDescriptions() {
        return failureDescriptions;
    }

    public int getCommissionPage() {
        return this.commissionPage;
    }

    private void evaluateConditions() {
        ItemStack blueprintStack = blueprintHandler.getStackInSlot(0);
        CompoundTag tag = blueprintStack.getTag();

        conditionResults.clear();
        failureDescriptions.clear();

        if(tag == null) {
            return;
        }

        EvaluationResult result;
        try {
            result = EvaluationResult.fromTag(tag);
        }
        catch (Exception e) {
            BuildingJournal.LOGGER.error("Error parsing blueprint: {}", e);
            failureDescriptions.add("Invalid or corrupted blueprint.");
            return;
        }

        for(CommissionCondition condition: conditions) {
            conditionResults.add(condition.test(result));
        }

        for (int i = 0; i < conditions.size(); i++) {
            failureDescriptions.add(conditions.get(i).describeFailure(result));
        }
    }

    public boolean isSubmitActive() {
        return state != CommissionState.COMPLETED &&
            !conditionResults.isEmpty() &&
            conditionResults.size() == conditions.size() && 
            conditionResults.stream().allMatch(Boolean::booleanValue);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            newStack = slotStack.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) return ItemStack.EMPTY;
            }
            else {
                if (slotStack.getItem() instanceof Blueprint) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) return ItemStack.EMPTY;
                }
                else return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }

        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // This is not tied to a block and therefore is always valid
    }
    
    @Override
    public void removed(Player player) {
        super.removed(player);

        if (!player.level().isClientSide) {
            ItemStack blueprintStack = blueprintHandler.extractItem(0, 1, false);
            if (!blueprintStack.isEmpty()) {
                if (!player.addItem(blueprintStack)) {
                    player.drop(blueprintStack, false);
                }
            }
        }
    }

}
