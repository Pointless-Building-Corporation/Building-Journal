package com.pointlessbuilding.journal.datagen;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.blocks.BlueprintRack;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class JournalBlockStates extends BlockStateProvider {
    
    public JournalBlockStates(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BuildingJournal.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(Registration.ANCHOR_BLOCK.get());
        horizontalBlock(Registration.DRAFTING_TABLE.get(), models().getExistingFile(modLoc("block/drafting_table")));
        getVariantBuilder(Registration.BLUEPRINT_RACK.get())
            .forAllStates(state -> {
                Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                boolean filled = state.getValue(BlueprintRack.FILLED);
                int rotation = switch (facing) {
                    case NORTH -> 0;
                    case SOUTH -> 180;
                    case EAST -> 90;
                    case WEST -> 270;
                    default -> 0;
                };
                ModelFile model = filled ? models().getExistingFile(modLoc("block/blueprint_rack_filled")) : models().getExistingFile(modLoc("block/blueprint_rack"));
                return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(rotation)
                    .build();
            });
    }

}
