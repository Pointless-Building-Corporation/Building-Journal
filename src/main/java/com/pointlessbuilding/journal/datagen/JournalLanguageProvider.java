package com.pointlessbuilding.journal.datagen;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.blocks.DraftingTable;
import com.pointlessbuilding.journal.client.ClientSetup;
import com.pointlessbuilding.journal.gui.ConfigUI;
import com.pointlessbuilding.journal.gui.DraftingTableUI;
import com.pointlessbuilding.journal.items.BuildersCompass;
import com.pointlessbuilding.journal.network.packets.JournalToastPacket;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class JournalLanguageProvider extends LanguageProvider{

    public JournalLanguageProvider(PackOutput output, String locale) {
        super(output, BuildingJournal.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        // Config
        add(ConfigUI.CONFIG_UI_TITLE, "Building Journal Config");
        add(ConfigUI.CONFIG_UI_MAX_BOXES, "Maximum Boxes");
        add(ConfigUI.CONFIG_UI_MAX_BOXES_DESC, "Maximum number of stored boundaries");
        add(ConfigUI.CONFIG_UI_MAX_BOX_SIZE, "Maximum Size of Boxes");
        add(ConfigUI.CONFIG_UI_MAX_BOX_SIZE_DESC, "Maximum boundary size in any direction");
        add(ConfigUI.CONFIG_UI_USE_BLUEPRINT_SHADER, "Use Blueprint Shader");
        add(ConfigUI.CONFIG_UI_USE_BLUEPRINT_SHADER_DESC, "Is the custom compass blueprint shader enabled?");
        
        // Toasts
        add(JournalToastPacket.JOURNAL_TOAST_TITLE, "Try out the Journal!");
        add(JournalToastPacket.JOURNAL_TOAST_DESC, "Open with %s");

        // UI
        add(DraftingTable.DRAFTING_TABLE_UI_TITLE, "Drafting Table");

        add(DraftingTableUI.DRAFTING_TABLE_NO_BOUNDARIES_LOADED, "No boundaries loaded in compass!");
        add(DraftingTableUI.DRAFTING_TABLE_BOUNDARIES_COUNT, "%s boundaries loaded in compass.");
        add(DraftingTableUI.DRAFTING_TABLE_BLOCKS_COUNT, "Total blocks covered: %s");
        
        // Blocks and items
        add(Registration.DRAFTING_TABLE.get(), "Drafting Table");
        add(Registration.BUILDERS_COMPASS.get(), "Builder's Compass");
        add(Registration.BLUEPRINT.get(), "Blueprint");

        //Tooltips
        add(BuildersCompass.BUILDERS_COMPASS_TOOLTIP_SELECT, "Press %s to select the first and last positions of individual boundaries of builds.");
        add(BuildersCompass.BUILDERS_COMPASS_TOOLTIP_DESELECT, "Press %1$s + %2$s to remove the last boundary marked.");
        add(BuildersCompass.BUILDERS_COMPASS_TOLLTIP_HINT, "Hold <%s> for hint");

        add(ClientSetup.JOURNAL_KEYMAP_STRING, "Open Journal");
        add("tab.buildingjournal", "Building Journal");
    }
    
}
