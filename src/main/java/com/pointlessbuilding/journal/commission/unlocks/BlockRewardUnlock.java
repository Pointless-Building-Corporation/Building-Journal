package com.pointlessbuilding.journal.commission.unlocks;

import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionUnlock;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockRewardUnlock implements CommissionUnlock{

    private final String title;
    private final ResourceLocation blockId;
    private final int blockCount;

    public BlockRewardUnlock(String title, ResourceLocation blockId, int blockCount) {
        this.title = title;
        this.blockId = blockId;
        this.blockCount = blockCount;
    }

    @Override
    public void apply(ServerPlayer player) {
        ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(blockId), blockCount);
        boolean added = player.getInventory().add(stack);
        if(!added || !stack.isEmpty()) player.drop(stack, false);
    }
    
    @Override
    public String getTitle() {
        if(title == null) {
            String generatedTitle = blockCount + "x " + blockId.toString();
            return generatedTitle;
        }
        return title;
    }

    public static CommissionUnlock fromJson(JsonObject json) {
        String jsonTitle = null;
        ResourceLocation jsonBlock;
        int jsonCount;

        if(json.has("title")) {
            jsonTitle = json.get("title").getAsString();
        }

        if(json.has("block")) {
            jsonBlock = ResourceLocation.tryParse(json.get("block").getAsString());
        }
        else {
            BuildingJournal.LOGGER.warn("Missing block field in unlock {}", jsonTitle != null ? jsonTitle : "BlockRewardUnlock");
            return null;
        }

        if(json.has("count")) {
            jsonCount = json.get("count").getAsInt();
        }
        else {
            BuildingJournal.LOGGER.warn("Missing count field in unlock {}", jsonTitle != null ? jsonTitle : "BlockRewardUnlock");
            return null;
        }

        return new BlockRewardUnlock(jsonTitle, jsonBlock, jsonCount);
    }

}
