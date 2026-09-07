package com.pointlessbuilding.journal.network.packets;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.server.BlueprintEvaluator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ConfirmBlueprintPacket implements CustomPacketPayload{
    
    private final BlockPos pos;
    private final String name;

    public ConfirmBlueprintPacket(BlockPos pos, String name) {
        this.pos = pos;
        this.name = name;
    }

    public static final Type<ConfirmBlueprintPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "confirm_blueprint_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfirmBlueprintPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, ConfirmBlueprintPacket::getPos,
        ByteBufCodecs.STRING_UTF8, ConfirmBlueprintPacket::getName,
        ConfirmBlueprintPacket::new
    );

    public BlockPos getPos() {
        return pos;
    }

    public String getName() {
        return name;
    }

    public static void handle(ConfirmBlueprintPacket packet, IPayloadContext ctx) {
        // DraftingTableEntity.LOGGER.info("ConfirmBlueprintPacket received");
        ServerPlayer player = (ServerPlayer) ctx.player();
        BlueprintEvaluator.evaluate(player, packet.pos, packet.name);
    }

}
