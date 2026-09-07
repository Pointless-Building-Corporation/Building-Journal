package com.pointlessbuilding.journal.items;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pointlessbuilding.journal.BuildingJournalConfig;
import com.pointlessbuilding.journal.Registration;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
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
import net.neoforged.fml.loading.FMLEnvironment;

public class BuildersCompass extends Item{

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
                CompassData data = item.getOrDefault(Registration.COMPASS_DATA.get(), CompassData.EMPTY);
                if(data.active()) {
                    item.set(Registration.COMPASS_DATA.get(), new CompassData(false, BlockPos.ZERO, data.storedBoxes()));
                }
                else if(!data.storedBoxes().isEmpty()){
                    List<BoundaryData> boxes = new ArrayList<>(data.storedBoxes());
                    boxes.remove(boxes.size() - 1);
                    item.set(Registration.COMPASS_DATA.get(), new CompassData(data.active(), data.firstPos(), boxes));
                }
            }
            //Client Side
            if(level.isClientSide) {
                CompassData data = item.getOrDefault(Registration.COMPASS_DATA.get(), CompassData.EMPTY);
                if(data.active()) {
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
            CompassData data = item.getOrDefault(Registration.COMPASS_DATA.get(), CompassData.EMPTY);
            List<BoundaryData> boxes = new ArrayList<>(data.storedBoxes());
            if(boxes.size() >= BuildingJournalConfig.MAX_BOXES.get()) { // Too many existing boxes
                // Do nothing
            }
            else if(data.firstPos() == BlockPos.ZERO || !data.active()) {
                item.set(Registration.COMPASS_DATA.get(), new CompassData(true, pos, data.storedBoxes()));
            }
            else {
                BlockPos first = data.firstPos();
                int clampedX = first.getX() + Math.max(-BuildingJournalConfig.MAX_BOX_SIZE.get(), Math.min(BuildingJournalConfig.MAX_BOX_SIZE.get(), pos.getX()-first.getX()));
                int clampedY = first.getY() + Math.max(-BuildingJournalConfig.MAX_BOX_SIZE.get(), Math.min(BuildingJournalConfig.MAX_BOX_SIZE.get(), pos.getY()-first.getY()));
                int clampedZ = first.getZ() + Math.max(-BuildingJournalConfig.MAX_BOX_SIZE.get(), Math.min(BuildingJournalConfig.MAX_BOX_SIZE.get(), pos.getZ()-first.getZ()));
                BlockPos second = new BlockPos(clampedX, clampedY, clampedZ);

                // LOGGER.info("Created Bounding Box! At (%s,%s,%s) and (%s,%s,%s)".formatted(first[0], first[1], first[2], second[0], second[1], second[2]));
                BoundaryData box = new BoundaryData(first, second, level.dimension().location().toString());

                boxes.add(box);
                item.set(Registration.COMPASS_DATA.get(), new CompassData(false, BlockPos.ZERO, boxes));
            }
        }

        //Client Side
        if(level.isClientSide) {
            CompassData data = item.getOrDefault(Registration.COMPASS_DATA.get(), CompassData.EMPTY);
            List<BoundaryData> boxes = new ArrayList<>(data.storedBoxes());
            if(boxes.size() >= BuildingJournalConfig.MAX_BOXES.get()) {
                player.displayClientMessage(
                    Component.literal("Too Many Boundaries! Can only have "+ BuildingJournalConfig.MAX_BOXES.get() +" at a time.").withStyle(ChatFormatting.RED),
                    true
                );
                player.playSound(Registration.COMPASS_ERROR.get(), 1.0f, 1.0f);
            }
            else if(data.firstPos() == BlockPos.ZERO || !data.active()) {
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        Component useKey = Minecraft.getInstance().options.keyUse.getTranslatedKeyMessage();
        Component shiftKey = Minecraft.getInstance().options.keyShift.getTranslatedKeyMessage();

        if(FMLEnvironment.dist.isClient() && RenderSystem.isOnRenderThread()) {
            if(Screen.hasShiftDown()) {
                tooltipComponents.add(Component.translatable(BUILDERS_COMPASS_TOOLTIP_SELECT, useKey).withStyle(ChatFormatting.AQUA));
                tooltipComponents.add(Component.translatable(BUILDERS_COMPASS_TOOLTIP_DESELECT, shiftKey, useKey).withStyle(ChatFormatting.RED));
            }
            else {
                tooltipComponents.add(Component.translatable(BUILDERS_COMPASS_TOLLTIP_HINT, shiftKey)
                    .withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY).withItalic(true)));
            }
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
