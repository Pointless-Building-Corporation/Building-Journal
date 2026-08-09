package com.pointlessbuilding.journal.server.commands;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.pointlessbuilding.journal.commission.Commission;
import com.pointlessbuilding.journal.commission.CommissionLoader;
import com.pointlessbuilding.journal.commission.CommissionProgress;
import com.pointlessbuilding.journal.commission.DailyCommission;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

        commission.then(Commands.literal("streak")
            .then(Commands.literal("list")
                .executes(ctx -> showStreakProgressCommand(ctx, ctx.getSource().getPlayerOrException()))
            )
            .then(Commands.literal("reset")
                .executes(ctx -> resetStreakProgressCommand(ctx, ctx.getSource().getPlayerOrException(), false))
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(ctx -> resetStreakProgressCommand(ctx, EntityArgument.getPlayer(ctx, "target"), false))
                    .then(Commands.argument("isHardReset", BoolArgumentType.bool())
                        .executes(ctx -> resetStreakProgressCommand(ctx, EntityArgument.getPlayer(ctx, "target"), BoolArgumentType.getBool(ctx, "isHardReset")))
                    )
                )
            )
        );

        commission.then(Commands.literal("dailyCommission")
            .then(Commands.argument("offset", IntegerArgumentType.integer())
                .executes(ctx -> generateDailyCommissionCommand(ctx, IntegerArgumentType.getInteger(ctx, "offset"), ctx.getSource().getPlayerOrException()))
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

    private static int showStreakProgressCommand(CommandContext<CommandSourceStack> ctx, ServerPlayer player) throws CommandSyntaxException {
        long today = LocalDate.now().toEpochDay();
        player.getCapability(CommissionProgress.COMMISSION_PROGRESS)
            .ifPresent(progress -> {
                String formattedDay = progress.getLastCompletionDay() < 0 ? "Never" : LocalDate.ofEpochDay(progress.getLastCompletionDay()).format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
                ctx.getSource().sendSuccess(() -> Component.literal("Current Streak: " + progress.getCurrentStreak(today) + "\nMax Streak: " + progress.getMaxStreak() + "\nLast Completion: " + formattedDay), true);
            });
        return 1;
    }

    private static int resetStreakProgressCommand(CommandContext<CommandSourceStack> ctx, ServerPlayer player, boolean isHardReset) throws CommandSyntaxException {
        player.getCapability(CommissionProgress.COMMISSION_PROGRESS)
            .ifPresent(progress -> progress.resetStreak(isHardReset));
        ctx.getSource().sendSuccess(() -> Component.literal("Reset player streak."), true);
        return 1;
    }

    private static int generateDailyCommissionCommand(CommandContext<CommandSourceStack> ctx, int offset, ServerPlayer player) throws CommandSyntaxException {
        DailyCommission test = new DailyCommission(LocalDate.now().plusDays(offset), player.getServer());

        MutableComponent output = Component.literal("");

        output.append(Component.literal("Id: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(test.getId()))
            .append(Component.literal("\n"));

        output.append(Component.literal("Title: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(test.getTitle()))
            .append(Component.literal("\n"));

        output.append(Component.literal("Conditions: ").withStyle(ChatFormatting.GRAY));
        String conditionJson = test.getConditionJson();
        output.append(Component.literal(conditionJson))
            .append(Component.literal("\n"));

        ctx.getSource().sendSuccess(() -> output, false);
        return 1;
    }

}
