package com.pointlessbuilding.journal.server;

import org.slf4j.Logger;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionProgress;
import com.pointlessbuilding.journal.network.Network;
import com.pointlessbuilding.journal.network.packets.JournalToastPacket;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BuildingJournal.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerCommonEvents {
    
    public static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        // Check if compass is unlocked
        ResourceLocation compass_recipe = new ResourceLocation("buildingjournal:recipes/tools/builders_compass");

        if (event.getAdvancement().getId().equals(compass_recipe)) {
            Network.sendToClient(new JournalToastPacket(), (ServerPlayer) event.getEntity());
        }

    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(BuildingJournal.MODID, "commission_progress"), new CommissionProgress());
        }
    }

    @SubscribeEvent
    public static void OnPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(CommissionProgress.COMMISSION_PROGRESS).ifPresent(oldCap -> {
            CompoundTag tag = oldCap.serializeNBT();
            event.getEntity().getCapability(CommissionProgress.COMMISSION_PROGRESS).ifPresent(newCap -> {
                newCap.deserializeNBT(tag);
            });
        });
    }

    @SubscribeEvent
    public static void OnCommand(RegisterCommandsEvent event) {

        LiteralArgumentBuilder<CommandSourceStack> commission = Commands.literal("commission");

        commission.then(Commands.literal("complete")
            .then(Commands.argument("id", StringArgumentType.string())
                .executes(ctx -> completeCommissionCommand(ctx, ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(ctx -> completeCommissionCommand(ctx, EntityArgument.getPlayer(ctx, "target")))
                )
            )
        );

        commission.then(Commands.literal("reset")
            .then(Commands.argument("id", StringArgumentType.string())
                .executes(ctx -> resetCommissionCommand(ctx, ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(ctx -> resetCommissionCommand(ctx, EntityArgument.getPlayer(ctx, "target")))
                )
            )
        );

        event.getDispatcher().register(commission);
    }

    private static int completeCommissionCommand(CommandContext<CommandSourceStack> ctx, ServerPlayer player) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "id");
        player.getCapability(CommissionProgress.COMMISSION_PROGRESS)
            .ifPresent(progress -> progress.markCompleted(id));
        ctx.getSource().sendSuccess(() -> Component.literal("Marked commission '" + id + "' as complete."), true);
        return 1;
    }

    private static int resetCommissionCommand(CommandContext<CommandSourceStack> ctx, ServerPlayer player) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "id");
        player.getCapability(CommissionProgress.COMMISSION_PROGRESS)
            .ifPresent(progress -> progress.markIncomplete(id));
        ctx.getSource().sendSuccess(() -> Component.literal("Marked commission '" + id + "' as incomplete."), true);
        return 1;
    }

}
