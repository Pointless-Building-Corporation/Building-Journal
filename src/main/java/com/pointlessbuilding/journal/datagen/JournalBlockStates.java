package com.pointlessbuilding.journal.datagen;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;

import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class JournalBlockStates extends BlockStateProvider {
    
    public JournalBlockStates(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BuildingJournal.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(Registration.ANCHOR_BLOCK.get());
        simpleBlock(Registration.COMPLEX_ANCHOR_BLOCK.get());
    }

}
