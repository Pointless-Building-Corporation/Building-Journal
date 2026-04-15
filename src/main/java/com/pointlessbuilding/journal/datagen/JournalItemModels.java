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
        withExistingParent(Registration.COMPLEX_ANCHOR_BLOCK.getId().getPath(), modLoc("block/complex_anchor_block"));
    }
    
}
