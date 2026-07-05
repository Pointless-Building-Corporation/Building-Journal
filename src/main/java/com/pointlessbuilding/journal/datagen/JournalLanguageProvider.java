package com.pointlessbuilding.journal.datagen;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.blocks.BlueprintRack;
import com.pointlessbuilding.journal.blocks.DraftingTable;
import com.pointlessbuilding.journal.client.ClientSetup;
import com.pointlessbuilding.journal.gui.ConfigUI;
import com.pointlessbuilding.journal.items.BuildersCompass;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class JournalLanguageProvider extends LanguageProvider{

    public JournalLanguageProvider(PackOutput output, String locale) {
        super(output, BuildingJournal.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        // Config translatables
        add(ConfigUI.CONFIG_UI_TITLE, "Building Journal Config");
        add(ConfigUI.CONFIG_UI_MAX_BOXES, "Maximum Boxes");
        add(ConfigUI.CONFIG_UI_MAX_BOXES_DESC, "Maximum number of stored boundaries");
        add(ConfigUI.CONFIG_UI_MAX_BOX_SIZE, "Maximum Size of Boxes");
        add(ConfigUI.CONFIG_UI_MAX_BOX_SIZE_DESC, "Maximum boundary size in any direction");
        add(ConfigUI.CONFIG_UI_USE_BLUEPRINT_SHADER, "Use Blueprint Shader");
        add(ConfigUI.CONFIG_UI_USE_BLUEPRINT_SHADER_DESC, "Is the custom compass blueprint shader enabled?");
        
        add(Registration.ANCHOR_BLOCK.get(), "Anchor Block");
        add(Registration.DRAFTING_TABLE.get(), "Drafting Table");
        add(Registration.BLUEPRINT_RACK.get(), "Blueprint Rack");
        add(BlueprintRack.BLUEPRINT_RACK_UI_TITLE, "Blueprint Rack");
        add(DraftingTable.DRAFTING_TABLE_UI_TITLE, "Drafting Table");
        add(ClientSetup.JOURNAL_KEYMAP_STRING, "Open Journal");
        add("tab.buildingjournal", "Building Journal");
        add(Registration.BUILDERS_COMPASS.get(), "Builder's Compass");
        add(Registration.BLUEPRINT.get(), "Blueprint");
        add(BuildersCompass.BUILDERS_COMPASS_TOOLTIP_SELECT, "Press %s to select the first and last positions of individual boundaries of builds.");
        add(BuildersCompass.BUILDERS_COMPASS_TOOLTIP_DESELECT, "Press %1$s + %2$s to remove the last boundary marked.");
    }
    
}
