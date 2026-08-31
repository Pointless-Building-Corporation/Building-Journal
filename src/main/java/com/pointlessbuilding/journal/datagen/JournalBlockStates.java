package com.pointlessbuilding.journal.datagen;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class JournalBlockStates extends BlockStateProvider {
    
    public JournalBlockStates(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BuildingJournal.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        horizontalBlock(Registration.DRAFTING_TABLE.get(), models().getExistingFile(modLoc("block/drafting_table")));
    }

}
