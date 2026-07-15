package com.pointlessbuilding.journal.commission.unlocks;

import com.pointlessbuilding.journal.commission.CommissionUnlock;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockRewardUnlock implements CommissionUnlock{

    private final ResourceLocation blockId;
    private final int blockCount;

    public BlockRewardUnlock(ResourceLocation blockId, int blockCount) {
        this.blockId = blockId;
        this.blockCount = blockCount;
    }

    @Override
    public void apply(ServerPlayer player) {
        ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(blockId), blockCount);
        boolean added = player.getInventory().add(stack);
        if(!added || !stack.isEmpty()) player.drop(stack, false);
    }
    
}
