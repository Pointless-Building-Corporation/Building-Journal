package com.pointlessbuilding.journal.network.packets;

import java.util.function.Supplier;

import com.pointlessbuilding.journal.client.ClientSetup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

public class JournalToastPacket {

    public static final String JOURNAL_TOAST_TITLE = "tutorial.buildingjournal.title";
    public static final String JOURNAL_TOAST_DESC = "tutorial.buildingjournal.desc";

    public JournalToastPacket() {
        // nothing
    }

    public void encode(FriendlyByteBuf buf) {
        // nothing
    }

    public static JournalToastPacket decode(FriendlyByteBuf buf) {
        return new JournalToastPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TutorialToast toast = new TutorialToast(TutorialToast.Icons.RECIPE_BOOK,
                Component.translatable(JOURNAL_TOAST_TITLE),
                Component.translatable(JOURNAL_TOAST_DESC, ClientSetup.JOURNAL_KEYMAP.get().getTranslatedKeyMessage()), false
            );
            Minecraft.getInstance().getTutorial().addTimedToast(toast, 160);
        });
        ctx.get().setPacketHandled(true);
    }
}
