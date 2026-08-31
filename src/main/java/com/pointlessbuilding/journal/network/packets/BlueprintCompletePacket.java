package com.pointlessbuilding.journal.network.packets;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.gui.DraftingTableUI;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class BlueprintCompletePacket implements CustomPacketPayload{

    private final BlockPos pos;

    public BlueprintCompletePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static final Type<BlueprintCompletePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "blueprint_complete_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintCompletePacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, BlueprintCompletePacket::getPos,
        BlueprintCompletePacket::new
    );

    public BlockPos getPos() {
        return pos;
    }

    public static void handle(BlueprintCompletePacket packet, IPayloadContext ctx) {
        // DraftingTableEntity.LOGGER.info("BluePrintCompletePacket received");
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof DraftingTableUI ui) {
            ui.onBlueprintComplete();
        }
    }

}
