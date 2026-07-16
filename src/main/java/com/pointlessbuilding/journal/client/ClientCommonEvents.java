package com.pointlessbuilding.journal.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.NativeImage;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionCardData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BuildingJournal.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientCommonEvents {
    
    private static List<CommissionCardData> commissionCardData;
    private static Map<String, ResourceLocation> commissionThumbnails = new HashMap<>();

    public static void updateCards(List<CommissionCardData> cards) {
        commissionCardData = cards;

        for(CommissionCardData data : commissionCardData) {
            if(data.thumbnailBytes().length == 0 || commissionThumbnails.containsKey(data.id()))
                continue;
            
            try {
                NativeImage image = NativeImage.read(data.thumbnailBytes());
                DynamicTexture texture = new DynamicTexture(image);
                ResourceLocation location = new ResourceLocation(BuildingJournal.MODID, "commission_thumb_" + data.id());
                Minecraft.getInstance().getTextureManager().register(location, texture);
                commissionThumbnails.put(data.id(), location);
            }
            catch(Exception e) {
                BuildingJournal.LOGGER.error("Failed to load thumbnail for commission {}: {}", data.id(), e);
            }

        }
    }

    public static List<CommissionCardData> getCards() {
        return commissionCardData;
    }

    public static ResourceLocation getCardThumbnail(String commissionId) {
        return commissionThumbnails.get(commissionId);
    }

}
