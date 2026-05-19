package com.pointlessbuilding.journal.items;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

        //Server Side
        if(!level.isClientSide) {
            if(!item.hasTag() || !item.getTag().getBoolean("Active")) {
                item.getOrCreateTag().putIntArray("FirstPos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
                item.getOrCreateTag().putBoolean("Active", true);
            }
            else {
                int[] first = item.getTag().getIntArray("FirstPos");
                item.getOrCreateTag().putBoolean("Active", false);
                LOGGER.info("Created Bounding Box! At (%s,%s,%s) and (%s,%s,%s)".formatted(first[0], first[1], first[2], pos.getX(), pos.getY(), pos.getZ()));
                item.getTag().remove("FirstPos");
            }
        }

        //Client Side
        if(level.isClientSide) {
            if(!item.hasTag() || !item.getTag().getBoolean("Active")) {
                player.displayClientMessage(
                    Component.literal("First Position Set: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()),
                    true
                );
            }
            else {
                player.displayClientMessage(
                    Component.literal("Second Position Set: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()),
                    true
                );
            }
        }

        return InteractionResultHolder.success(item);
    }

    public static boolean currentHoldingCompass(Player player) {
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof BuildersCompass) return true;
        else return false; 
    }

}
