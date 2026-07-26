package com.pointlessbuilding.journal.server.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.pointlessbuilding.journal.commission.Commission;
import com.pointlessbuilding.journal.commission.CommissionLoader;
import com.pointlessbuilding.journal.commission.CommissionProgress;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CommissionCommand {
    
    public static final SuggestionProvider<CommandSourceStack> COMMISSION_ID_SUGGESTIONS = (ctx, builder) -> {
        return SharedSuggestionProvider.suggest(
            CommissionLoader.getAllLoadedCommissions().stream().map(Commission::id),
            builder
        );
    };

    public static LiteralArgumentBuilder<CommandSourceStack> register() {

        LiteralArgumentBuilder<CommandSourceStack> commission = Commands.literal("commission").requires(cs -> cs.hasPermission(2));

        commission.then(Commands.literal("complete")
            .then(Commands.argument("id", StringArgumentType.string())
                .suggests(COMMISSION_ID_SUGGESTIONS)
                .executes(ctx -> completeCommissionCommand(ctx, ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(ctx -> completeCommissionCommand(ctx, EntityArgument.getPlayer(ctx, "target")))
                )
            )
        );

        commission.then(Commands.literal("reset")
            .then(Commands.literal("all")
                .executes(ctx -> resetAllCommissionsCommand(ctx, ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(ctx -> resetAllCommissionsCommand(ctx, EntityArgument.getPlayer(ctx, "target")))
                )
            )
            .then(Commands.argument("id", StringArgumentType.string())
                .suggests(COMMISSION_ID_SUGGESTIONS)
                .executes(ctx -> resetCommissionCommand(ctx, ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(ctx -> resetCommissionCommand(ctx, EntityArgument.getPlayer(ctx, "target")))
                )
            )
        );

        return commission;
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

     private static int resetAllCommissionsCommand(CommandContext<CommandSourceStack> ctx, ServerPlayer player) throws CommandSyntaxException {
        player.getCapability(CommissionProgress.COMMISSION_PROGRESS)
            .ifPresent(progress -> progress.markAllIncomplete());
        ctx.getSource().sendSuccess(() -> Component.literal("Marked all commissions as incomplete."), true);
        return 1;
    }

}
