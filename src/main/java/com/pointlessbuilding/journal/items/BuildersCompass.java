package com.pointlessbuilding.journal.items;

import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.BuildingJournalConfig;
import com.pointlessbuilding.journal.Registration;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class BuildersCompass extends Item{

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String BUILDERS_COMPASS_TOOLTIP_SELECT = "tooltip.buildingjournal.compass.select";
    public static final String BUILDERS_COMPASS_TOOLTIP_DESELECT = "tooltip.buildingjournal.compass.deselect";
    public static final String BUILDERS_COMPASS_TOLLTIP_HINT = "tooltip.buildjournal.compass.hint";

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
            if(level.isClientSide) {
                if(item.hasTag() && item.getTag().getBoolean("Active")) {
                    player.displayClientMessage(
                        Component.literal("Current Selection Cancelled.").withStyle(ChatFormatting.GOLD),
                        true
                    );
                    player.playSound(Registration.COMPASS_CLACK.get(), 1.0f, 1.0f);
                }
                else {
                    player.displayClientMessage(
                        Component.literal("Previous Boundary Removed.").withStyle(ChatFormatting.GOLD),
                        true
                    );
                    player.playSound(Registration.COMPASS_CLACK.get(), 1.0f, 1.0f);
                }
            }

            return InteractionResultHolder.success(item);
        }


        //Server Side
        if(!level.isClientSide) {
            ListTag boxes = item.getOrCreateTag().getList("StoredBoxes", Tag.TAG_COMPOUND);
            if(boxes.size() >= BuildingJournalConfig.MAX_BOXES.get()) { // Too many existing boxes
                // Do nothing
            }
            else if(!item.hasTag() || !item.getTag().getBoolean("Active")) {
                item.getOrCreateTag().putIntArray("FirstPos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
                item.getOrCreateTag().putBoolean("Active", true);
            }
            else {
                int[] first = item.getTag().getIntArray("FirstPos");
                int clampedX = first[0] + Math.max(-BuildingJournalConfig.MAX_BOX_SIZE.get(), Math.min(BuildingJournalConfig.MAX_BOX_SIZE.get(), pos.getX()-first[0]));
                int clampedY = first[1] + Math.max(-BuildingJournalConfig.MAX_BOX_SIZE.get(), Math.min(BuildingJournalConfig.MAX_BOX_SIZE.get(), pos.getY()-first[1]));
                int clampedZ = first[2] + Math.max(-BuildingJournalConfig.MAX_BOX_SIZE.get(), Math.min(BuildingJournalConfig.MAX_BOX_SIZE.get(), pos.getZ()-first[2]));
                int[] second = new int[]{clampedX, clampedY, clampedZ};

                LOGGER.info("Created Bounding Box! At (%s,%s,%s) and (%s,%s,%s)".formatted(first[0], first[1], first[2], second[0], second[1], second[2]));
                CompoundTag box = new CompoundTag();
                box.putIntArray("FirstPos", first);
                box.putIntArray("SecondPos", second);
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
            if(boxes.size() >= BuildingJournalConfig.MAX_BOXES.get()) {
                player.displayClientMessage(
                    Component.literal("Too Many Boundaries! Can only have "+ BuildingJournalConfig.MAX_BOXES.get() +" at a time.").withStyle(ChatFormatting.RED),
                    true
                );
                player.playSound(Registration.COMPASS_ERROR.get(), 1.0f, 1.0f);
            }
            else if(!item.hasTag() || !item.getTag().getBoolean("Active")) {
                player.displayClientMessage(
                    Component.literal("First Position Set: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()).withStyle(ChatFormatting.AQUA),
                    true
                );
                player.playSound(Registration.COMPASS_CLICK.get(), 1.0f, 1.0f);
            }
            else {
                player.displayClientMessage(
                    Component.literal("Second Position Set: " + pos.getX() + " " + pos.getY() + " " + pos.getZ()).withStyle(ChatFormatting.AQUA),
                    true
                );
                player.playSound(Registration.COMPASS_CLACK.get(), 1.0f, 1.0f);
            }
        }

        return InteractionResultHolder.success(item);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Component useKey = Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage();
        Component shiftKey = Minecraft.getInstance().options.keyShift.getTranslatedKeyMessage();

        if(Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable(BUILDERS_COMPASS_TOOLTIP_SELECT, useKey).withStyle(ChatFormatting.AQUA));
            tooltipComponents.add(Component.translatable(BUILDERS_COMPASS_TOOLTIP_DESELECT, shiftKey, useKey).withStyle(ChatFormatting.RED));
        }
        else {
            tooltipComponents.add(Component.translatable(BUILDERS_COMPASS_TOLLTIP_HINT, shiftKey)
                .withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY).withItalic(true)));
        }
    }

    public static boolean currentHoldingCompass(Player player) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof BuildersCompass)) {
            held = player.getOffhandItem();
            if (!(held.getItem() instanceof BuildersCompass)) return false;
        }
        return true;
    }

}
