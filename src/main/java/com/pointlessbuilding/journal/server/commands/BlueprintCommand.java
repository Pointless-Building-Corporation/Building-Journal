package com.pointlessbuilding.journal.server.commands;

import java.util.Map;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.pointlessbuilding.journal.items.Blueprint;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class BlueprintCommand {
    
    public static LiteralArgumentBuilder<CommandSourceStack> register() {

        LiteralArgumentBuilder<CommandSourceStack> blueprint = Commands.literal("blueprint").requires(cs -> cs.hasPermission(0));

        blueprint.then(Commands.literal("data")
            .executes(ctx -> viewBlueprintDataCommand(ctx, ctx.getSource().getPlayerOrException()))
        );

        return blueprint;
    }

    private static String formatBiomeList(ListTag list) {
        if (list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(list.get(i).getAsString());
        }
        return sb.toString();
    }

    private static String formatBoxesList(ListTag boxes) {
        if (boxes.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < boxes.size(); i++) {
            CompoundTag box = boxes.getCompound(i);
            int[] first = box.getIntArray("FirstPos");
            int[] second = box.getIntArray("SecondPos");
            if (i > 0) sb.append(",\n");
            sb.append("(").append(first[0]).append(", ").append(first[1]).append(", ").append(first[2]).append(")")
            .append(" -> ")
            .append("(").append(second[0]).append(", ").append(second[1]).append(", ").append(second[2]).append(")");
        }
        return sb.toString();
    }

    private static String formatBlockCounts(Map<String, long[]> counts) {
        if (counts.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, long[]> entry : counts.entrySet()) {
            if (!first) sb.append(",\n");
            first = false;
            long[] values = entry.getValue();
            sb.append(entry.getKey()).append(" (added: ").append(values[0]).append(", removed: ").append(values[1]).append(")");
        }
        return sb.toString();
    }



    private static int viewBlueprintDataCommand(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        ItemStack held = player.getMainHandItem();

        if(!(held.getItem() instanceof Blueprint)) {
            ctx.getSource().sendFailure(Component.literal("You must be holding a blueprint."));
            return 0;
        }

        MutableComponent output = Component.literal("");

        output.append(Component.literal("Name: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(String.valueOf(Blueprint.getBlueprintName(held))))
            .append(Component.literal("\n"));

        output.append(Component.literal("Dimension: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(String.valueOf(Blueprint.getDimension(held))))
            .append(Component.literal("\n"));

        output.append(Component.literal("Biomes: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(formatBiomeList(Blueprint.getBiome(held))))
            .append(Component.literal("\n"));

        output.append(Component.literal("Boxes:\n").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(formatBoxesList(Blueprint.getBoxes(held))))
            .append(Component.literal("\n"));

        output.append(Component.literal("Block Counts:\n").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(formatBlockCounts(Blueprint.getBlockCounts(held))))
            .append(Component.literal("\n"));

        output.append(Component.literal("Blocks Modified: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(String.format("%,d",Blueprint.getModifiedCount(held))))
            .append(Component.literal("\n"));
        
        output.append(Component.literal("Union Volume: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(String.format("%,d",Blueprint.getUnionVolume(held))))
            .append(Component.literal("\n"));

        output.append(Component.literal("UUID: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(Blueprint.getUUID(held).toString()))
            .append(Component.literal("\n"));

        ctx.getSource().sendSuccess(() -> output, false);
        return 1;
    }

}
