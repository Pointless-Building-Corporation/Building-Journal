package com.pointlessbuilding.journal.network;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.network.packets.BlueprintCompletePacket;
import com.pointlessbuilding.journal.network.packets.CommissionDetailPacket;
import com.pointlessbuilding.journal.network.packets.CommissionSubmitPacket;
import com.pointlessbuilding.journal.network.packets.ConfirmBlueprintPacket;
import com.pointlessbuilding.journal.network.packets.JournalToastPacket;
import com.pointlessbuilding.journal.network.packets.RequestCardCommissionsPacket;
import com.pointlessbuilding.journal.network.packets.RequestCardThumbnailPacket;
import com.pointlessbuilding.journal.network.packets.SyncCardCommissionsPacket;
import com.pointlessbuilding.journal.network.packets.SyncCardThumbnailPacket;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = BuildingJournal.MODID)
public class RegisterNetwork {
    
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(BuildingJournal.MODID);

        registrar.playToClient(BlueprintCompletePacket.TYPE, BlueprintCompletePacket.STREAM_CODEC, BlueprintCompletePacket::handle);
        registrar.playToServer(CommissionDetailPacket.TYPE, CommissionDetailPacket.STREAM_CODEC, CommissionDetailPacket::handle);
        registrar.playToServer(CommissionSubmitPacket.TYPE, CommissionSubmitPacket.STREAM_CODEC, CommissionSubmitPacket::handle);
        registrar.playToServer(ConfirmBlueprintPacket.TYPE, ConfirmBlueprintPacket.STREAM_CODEC, ConfirmBlueprintPacket::handle);
        registrar.playToClient(JournalToastPacket.TYPE, JournalToastPacket.STREAM_CODEC, JournalToastPacket::handle);
        registrar.playToServer(RequestCardCommissionsPacket.TYPE, RequestCardCommissionsPacket.STREAM_CODEC, RequestCardCommissionsPacket::handle);
        registrar.playToServer(RequestCardThumbnailPacket.TYPE, RequestCardThumbnailPacket.STREAM_CODEC, RequestCardThumbnailPacket::handle);
        registrar.playToClient(SyncCardCommissionsPacket.TYPE, SyncCardCommissionsPacket.STREAM_CODEC, SyncCardCommissionsPacket::handle);
        registrar.playToClient(SyncCardThumbnailPacket.TYPE, SyncCardThumbnailPacket.STREAM_CODEC, SyncCardThumbnailPacket::handle);
    }

}
