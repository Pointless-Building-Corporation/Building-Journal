package com.pointlessbuilding.journal.network.packets;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.client.ClientSetup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class JournalToastPacket implements CustomPacketPayload{

    public static final String JOURNAL_TOAST_TITLE = "tutorial.buildingjournal.title";
    public static final String JOURNAL_TOAST_DESC = "tutorial.buildingjournal.desc";
    public static final JournalToastPacket INSTANCE = new JournalToastPacket();

    public JournalToastPacket() {
        // nothing
    }

    public static final Type<JournalToastPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "journal_toast_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, JournalToastPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handle(JournalToastPacket packet, IPayloadContext ctx) {
        TutorialToast toast = new TutorialToast(TutorialToast.Icons.RECIPE_BOOK,
            Component.translatable(JOURNAL_TOAST_TITLE),
            Component.translatable(JOURNAL_TOAST_DESC, ClientSetup.JOURNAL_KEYMAP.get().getTranslatedKeyMessage()), false
        );
        Minecraft.getInstance().getTutorial().addTimedToast(toast, 160);
    }
}
