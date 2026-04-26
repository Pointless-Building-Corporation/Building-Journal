package com.pointlessbuilding.journal.items;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class BuildersCompass extends Item{

    private static final Logger LOGGER = LogUtils.getLogger();

    public BuildersCompass(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack item = player.getItemInHand(usedHand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        BlockPos pos = hit.getBlockPos();

        if(!item.hasTag() || !item.getTag().contains("FirstPos")) {
            item.getOrCreateTag().putIntArray("FirstPos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
            return InteractionResultHolder.success(item);
        }
        else {
            int[] first = item.getTag().getIntArray("FirstPos");
            BlockPos firstPos = new BlockPos(first[0], first[1], first[2]);
            LOGGER.info("Created Bounding Box! At (%s,%s,%s) and (%s,%s,%s)".formatted(first[0], first[1], first[2], pos.getX(), pos.getY(), pos.getZ()));
            item.getTag().remove("FirstPos");
            return InteractionResultHolder.success(item);
        }
    }

}
