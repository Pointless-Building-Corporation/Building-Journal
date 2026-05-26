package com.pointlessbuilding.journal.datagen;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;

import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class JournalItemModels extends ItemModelProvider{

    public JournalItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BuildingJournal.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent(Registration.ANCHOR_BLOCK.getId().getPath(), modLoc("block/anchor_block"));
        withExistingParent(Registration.DRAFTING_TABLE.getId().getPath(), modLoc("block/drafting_table"));
        withExistingParent(Registration.BLUEPRINT_RACK.getId().getPath(), modLoc("block/blueprint_rack"));
        basicItem(Registration.BUILDERS_COMPASS.get());
        basicItem(Registration.BLUEPRINT.get());
    }
    
}
