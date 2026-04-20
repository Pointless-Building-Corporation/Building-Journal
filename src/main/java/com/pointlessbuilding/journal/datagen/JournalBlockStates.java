package com.pointlessbuilding.journal.datagen;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.blocks.BlueprintRack;

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
        horizontalBlock(Registration.BLUEPRINT_RACK.get(), models().getExistingFile(modLoc("block/blueprint_rack")));
        // Later we switch this to check for filled condition
        // getVariantBuilder(Registration.BLUEPRINT_RACK.get())
        //     .partialState().with(BlueprintRack.FILLED, false)
        //         .modelForState().modelFile(models().getExistingFile(modLoc("block/blueprint_rack"))).addModel()
        //     .partialState().with(BlueprintRack.FILLED, true)
        //         .modelForState().modelFile(models().getExistingFile(modLoc("block/blueprint_rack_filled"))).addModel();
    }

}
