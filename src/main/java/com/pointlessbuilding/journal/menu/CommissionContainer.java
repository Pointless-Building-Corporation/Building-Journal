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
    private final List<CommissionUnlock> unlocks;
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

    public CommissionContainer(int containerId, Player player, String commissionId, String title, CommissionState state, String conditionsJson, List<CommissionUnlock> unlocks, int commissionPage) {
        super(Registration.COMMISSION_CONTAINER.get(), containerId);
        this.commissionId = commissionId;
        this.title = title;
        this.state = state;
        this.conditionsJson = conditionsJson;
        this.unlocks = unlocks;
        this.commissionPage = commissionPage;

        parseConditionsJson();

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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // Do nothing. This probably changes later
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
