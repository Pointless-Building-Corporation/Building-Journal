package com.pointlessbuilding.journal.network.packets;

import java.util.function.Supplier;

import com.pointlessbuilding.journal.blocks.DraftingTableEntity;
import com.pointlessbuilding.journal.gui.DraftingTableUI;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class BlueprintCompletePacket {

    private final BlockPos pos;

    public BlueprintCompletePacket(BlockPos pos) {
        this.pos = pos;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public static BlueprintCompletePacket decode(FriendlyByteBuf buf) {
        return new BlueprintCompletePacket(buf.readBlockPos());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        DraftingTableEntity.LOGGER.info("BluePrintCompletePacket received");
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof DraftingTableUI ui) {
                ui.onBlueprintComplete();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
