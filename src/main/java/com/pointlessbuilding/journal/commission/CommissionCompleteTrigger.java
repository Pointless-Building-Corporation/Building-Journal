package com.pointlessbuilding.journal.commission;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;

@SuppressWarnings("removal")
public class CommissionCompleteTrigger extends SimpleCriterionTrigger<CommissionCompleteTrigger.CommissionTriggerInstance>{
    public static final CommissionCompleteTrigger INSTANCE = new CommissionCompleteTrigger();
    static final ResourceLocation ID = new ResourceLocation("buildingjournal:commission_completed");

    public CommissionCompleteTrigger() {
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected CommissionTriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext deserializationContext) {
        JsonArray jsonIds = GsonHelper.getAsJsonArray(json, "ids");
        List<String> ids = new ArrayList<>();
        for(JsonElement id : jsonIds) ids.add(id.getAsString());
        return new CommissionTriggerInstance(predicate, ids);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, (T) -> T.completed(player));
    }

    public static class CommissionTriggerInstance extends AbstractCriterionTriggerInstance {
        private final List<String> ids;

        public CommissionTriggerInstance(ContextAwarePredicate player, List<String> ids) {
            super(CommissionCompleteTrigger.ID, player);
            this.ids = ids;
        }
        
        boolean completed(ServerPlayer player) {
            Set<String> completedComms = player.getCapability(CommissionProgress.COMMISSION_PROGRESS)
            .resolve()
            .map(ICommissionProgress::getCompletedCommissions)
            .orElse(Collections.emptySet());

            for(String id : ids) {
                if(!completedComms.contains(id)) {
                    return false;
                }
            }
            BuildingJournal.LOGGER.info("Completed! Returning true");
            return true;
        }

        public JsonObject serializeToJson(SerializationContext conditions) {
            JsonObject jsonobject = super.serializeToJson(conditions);
            JsonArray jsonIds = new JsonArray();
            for(String id : ids) {
                jsonIds.add(id);
            }

            jsonobject.add("ids", jsonIds);

            return jsonobject;
        }

    }
    
}
