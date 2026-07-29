package com.pointlessbuilding.journal.client;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.NativeImage;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionCardData;
import com.pointlessbuilding.journal.network.Network;
import com.pointlessbuilding.journal.network.packets.RequestCardCommissionsPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BuildingJournal.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientCommonEvents {
    
    private static List<CommissionCardData> commissionCardData;
    private static Map<String, ResourceLocation> commissionThumbnails = new HashMap<>();
    private static long nextResetEpochMillis;
    private static int currentStreak;
    private static int maxStreak;
    private static int completionCount;

    public static void updateCards(List<CommissionCardData> cards) {
        commissionCardData = cards;

        for(CommissionCardData data : commissionCardData) {
            if(data.thumbnailBytes().length == 0 || commissionThumbnails.containsKey(data.id())) {
                continue;
            }
                        
            try {
                // If I call NativeImage.read(thumbnailBytes) directly it loops internally and closes the thread, stopping the loading of every other card.
                // This has something to do with the malloc() call there, but unsure. God knows why this happens
                ByteBuffer buffer = ByteBuffer.allocateDirect(data.thumbnailBytes().length);
                buffer.put(data.thumbnailBytes());
                buffer.flip();

                NativeImage image = NativeImage.read(buffer);
                DynamicTexture texture = new DynamicTexture(image);
                ResourceLocation location = new ResourceLocation(BuildingJournal.MODID, "commission_thumb_" + data.id());
                Minecraft.getInstance().getTextureManager().register(location, texture);
                commissionThumbnails.put(data.id(), location);
                image.close();
            }
            catch(Exception e) {
                BuildingJournal.LOGGER.error("Failed to load thumbnail for commission {}: {}", data.id(), e);
            }
        }

    }

    public static void updateNextResetTime(long EpochMillis) {
        nextResetEpochMillis = EpochMillis;
    }

    public static void updateStats(int curStk, int maxStk, int completion) {
        currentStreak = curStk;
        maxStreak = maxStk;
        completionCount = completion;
    }

    public static int getCurrentStreak() {
        return currentStreak;
    }

    public static int getMaxStreak() {
        return maxStreak;
    }

    public static int getCompletionCount() {
        return completionCount;
    }


    public static List<CommissionCardData> getCards() {
        return commissionCardData;
    }

    public static ResourceLocation getCardThumbnail(String commissionId) {
        return commissionThumbnails.get(commissionId);
    }

    public static long getNextRestMillis() {
        return nextResetEpochMillis;
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Network.sendToServer(new RequestCardCommissionsPacket());
        BuildingJournal.LOGGER.info("Updated Commission List.");
    }

}
