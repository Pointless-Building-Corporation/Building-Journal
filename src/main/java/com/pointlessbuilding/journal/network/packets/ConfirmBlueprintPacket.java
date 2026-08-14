package com.pointlessbuilding.journal.network.packets;

import java.util.function.Supplier;

import com.pointlessbuilding.journal.server.BlueprintEvaluator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ConfirmBlueprintPacket{
    
    private final BlockPos pos;
    private final String name;

    public ConfirmBlueprintPacket(BlockPos pos, String name) {
        this.pos = pos;
        this.name = name;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(name);
    }

    public static ConfirmBlueprintPacket decode(FriendlyByteBuf buf) {
        return new ConfirmBlueprintPacket(buf.readBlockPos(), buf.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        // DraftingTableEntity.LOGGER.info("ConfirmBlueprintPacket received");
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            BlueprintEvaluator.evaluate(player, pos, name);
        });
        ctx.get().setPacketHandled(true);
    }

}
