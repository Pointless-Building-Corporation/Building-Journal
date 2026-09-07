package com.pointlessbuilding.journal.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.gui.CommissionUI;
import com.pointlessbuilding.journal.gui.ConfigUI;
import com.pointlessbuilding.journal.gui.DraftingTableUI;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = BuildingJournal.MODID, value = Dist.CLIENT)
public class ClientSetup {

    public static final String JOURNAL_KEYMAP_STRING = "key.buildingjournal.journal";

    public static final Lazy<JournalKeyMap> JOURNAL_KEYMAP = Lazy.of(() ->
        new JournalKeyMap(JOURNAL_KEYMAP_STRING, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, KeyMapping.CATEGORY_MISC)
    );

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.DRAFTING_TABLE_CONTAINER.get(), DraftingTableUI::new);
        event.register(Registration.COMMISSION_CONTAINER.get(), CommissionUI::new);
    }

    public static void registerConfigScreen(ModContainer container) {
        container.registerExtensionPoint(
            IConfigScreenFactory.class,
            (containerInstance, parent) -> new ConfigUI(parent)
        );
    }

    @SubscribeEvent
    public static void registerKeybindings(RegisterKeyMappingsEvent event) {
        event.register(JOURNAL_KEYMAP.get());
    }

}
