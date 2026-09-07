package com.pointlessbuilding.journal.commission;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;

public class CommissionCompleteTrigger extends SimpleCriterionTrigger<CommissionCompleteTrigger.CommissionTriggerInstance>{
    public static final CommissionCompleteTrigger INSTANCE = new CommissionCompleteTrigger();
    static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "commission_completed");

    public CommissionCompleteTrigger() {
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, (T) -> T.completed(player));
    }

    @Override
    public Codec<CommissionTriggerInstance> codec() {
        return CommissionTriggerInstance.CODEC;
    }

    public record CommissionTriggerInstance(
        Optional<ContextAwarePredicate> player,
        List<String> ids
    ) implements SimpleCriterionTrigger.SimpleInstance {
        
        public static final Codec<CommissionTriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CommissionTriggerInstance::player),
            Codec.STRING.listOf().fieldOf("ids").forGetter(CommissionTriggerInstance::ids)
        ).apply(instance, CommissionTriggerInstance::new));

        boolean completed(ServerPlayer player) {
            Set<String> completedComms = player.getData(Registration.COMMISSION_PROGRESS).getCompletedCommissions();

            for(String id : ids) {
                if(!completedComms.contains(id)) {
                    return false;
                }
            }
            BuildingJournal.LOGGER.info("Completed! Returning true");
            return true;
        }

    }
    
}
