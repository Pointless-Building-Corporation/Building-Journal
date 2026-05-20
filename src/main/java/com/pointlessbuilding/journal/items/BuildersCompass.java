package com.pointlessbuilding.journal.items;

import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
    private static final int MAX_BOXES = 10;

    public BuildersCompass(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack item = player.getItemInHand(usedHand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        BlockPos pos = hit.getBlockPos();

        //Check shift+use
        if(player.isShiftKeyDown()) {
            //Server Side
            if(!level.isClientSide) {
                if(item.hasTag() && item.getTag().getBoolean("Active")) {
                    item.getOrCreateTag().putBoolean("Active", false);
                    item.getTag().remove("FirstPos");
                }
                else {
                    ListTag boxes = item.getOrCreateTag().getList("StoredBoxes", Tag.TAG_COMPOUND);
                    if(!boxes.isEmpty()) {
                        boxes.remove(boxes.size() - 1);
                        item.getOrCreateTag().put("StoredBoxes", boxes);
                    }
                }
            }
            //Client Side
            if(!level.isClientSide) {
                if(item.hasTag() && item.getTag().getBoolean("Active")) {
                    player.displayClientMessage(
                        Component.literal("Current Selection Cancelled.").withStyle(ChatFormatting.GOLD),
                        true
                    );
                }
                else {
                    player.displayClientMessage(
                        Component.literal("Previous Boundary Removed.").withStyle(ChatFormatting.GOLD),
                        true
                    );
                }
            }

            return InteractionResultHolder.success(item);
        }


        //Server Side
        if(!level.isClientSide) {
            ListTag boxes = item.getOrCreateTag().getList("StoredBoxes", Tag.TAG_COMPOUND);
            if(boxes.size() >= MAX_BOXES) { // Too many existing boxes
                // Do nothing
            }
            else if(!item.hasTag() || !item.getTag().getBoolean("Active")) {
                item.getOrCreateTag().putIntArray("FirstPos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
                item.getOrCreateTag().putBoolean("Active", true);
            }
            else {
                int[] first = item.getTag().getIntArray("FirstPos");
                LOGGER.info("Created Bounding Box! At (%s,%s,%s) and (%s,%s,%s)".formatted(first[0], first[1], first[2], pos.getX(), pos.getY(), pos.getZ()));
                CompoundTag box = new CompoundTag();
                box.putIntArray("FirstPos", first);
                box.putIntArray("SecondPos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
                box.putString("Dimension", level.dimension().location().toString());

                boxes.add(box);
                item.getOrCreateTag().put("StoredBoxes", boxes);

                item.getOrCreateTag().putBoolean("Active", false);
                item.getTag().remove("FirstPos");
            }
        }

        //Client Side
        if(level.isClientSide) {
            ListTag boxes = item.getOrCreateTag().getList("StoredBoxes", Tag.TAG_COMPOUND);
            if(boxes.size() >= MAX_BOXES) {
                player.displayClientMessage(
                    Component.literal("Too Many Boundaries! Can only have "+ MAX_BOXES +" at a time.").withStyle(ChatFormatting.RED),
                    true
                );
            }
            else if(!item.hasTag() || !item.getTag().getBoolean("Active")) {
                player.displayClientMessage(
                    Component.literal("First Position Set: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()).withStyle(ChatFormatting.BLUE),
                    true
                );
            }
            else {
                player.displayClientMessage(
                    Component.literal("Second Position Set: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()).withStyle(ChatFormatting.BLUE),
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
