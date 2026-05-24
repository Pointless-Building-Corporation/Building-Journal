package com.pointlessbuilding.journal.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.gui.BlueprintRackUI;
import com.pointlessbuilding.journal.gui.DraftingTableUI;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = BuildingJournal.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    public static final String JOURNAL_KEYMAP_STRING = "key.buildingjournal.journal";

    public static final Lazy<JournalKeyMap> JOURNAL_KEYMAP = Lazy.of(() ->
        new JournalKeyMap(JOURNAL_KEYMAP_STRING, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, KeyMapping.CATEGORY_MISC)
    );

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(Registration.BLUEPRINT_RACK_CONTAINER.get(), BlueprintRackUI::new);
            MenuScreens.register(Registration.DRAFTING_TABLE_CONTAINER.get(), DraftingTableUI::new);
        });
    }

    @SubscribeEvent
    public static void registerKeybindings(RegisterKeyMappingsEvent event) {
        event.register(JOURNAL_KEYMAP.get());
    }

}
