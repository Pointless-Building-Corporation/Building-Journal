package com.pointlessbuilding.journal.network;

import com.pointlessbuilding.journal.BuildingJournal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Network {
    private static SimpleChannel CHANNEL;
    private static int ID = 0;

    public static void init() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BuildingJournal.MODID, "main"),
            () -> BuildingJournal.VERSION,
            BuildingJournal.VERSION::equals,
            BuildingJournal.VERSION::equals
        );

        CHANNEL.registerMessage(ID++, ConfirmBlueprintPacket.class,
            ConfirmBlueprintPacket::encode,
            ConfirmBlueprintPacket::decode,
            ConfirmBlueprintPacket::handle
        );

        CHANNEL.registerMessage(ID++, BlueprintCompletePacket.class,
            BlueprintCompletePacket::encode,
            BlueprintCompletePacket::decode,
            BlueprintCompletePacket::handle
        );
    }

    public static void sendToClient(Object packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
