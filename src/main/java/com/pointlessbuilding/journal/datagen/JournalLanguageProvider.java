package com.pointlessbuilding.journal.datagen;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.blocks.BlueprintRack;
import com.pointlessbuilding.journal.blocks.DraftingTable;
import com.pointlessbuilding.journal.client.ClientSetup;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class JournalLanguageProvider extends LanguageProvider{

    public JournalLanguageProvider(PackOutput output, String locale) {
        super(output, BuildingJournal.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add(Registration.ANCHOR_BLOCK.get(), "Anchor Block");
        add(Registration.DRAFTING_TABLE.get(), "Drafting Table");
        add(Registration.BLUEPRINT_RACK.get(), "Blueprint Rack");
        add(BlueprintRack.BLUEPRINT_RACK_UI_TITLE, "Blueprint Rack");
        add(DraftingTable.DRAFTING_TABLE_UI_TITLE, "Drafting Table");
        add(ClientSetup.JOURNAL_KEYMAP_STRING, "Open Journal");
        add("tab.buildingjournal", "Building Journal");
        add(Registration.BUILDERS_COMPASS.get(), "Builder's Compass");
    }
    
}
