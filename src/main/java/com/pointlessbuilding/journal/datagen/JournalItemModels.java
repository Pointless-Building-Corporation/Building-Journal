package com.pointlessbuilding.journal.datagen;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class JournalItemModels extends ItemModelProvider{

    public JournalItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BuildingJournal.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent(Registration.DRAFTING_TABLE.getId().getPath(), modLoc("block/drafting_table"));
        basicItem(Registration.BUILDERS_COMPASS.get());
        basicItem(Registration.BLUEPRINT.get());
    }
    
}
