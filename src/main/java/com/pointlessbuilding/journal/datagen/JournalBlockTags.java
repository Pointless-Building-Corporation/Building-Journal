package com.pointlessbuilding.journal.datagen;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class JournalBlockTags extends BlockTagsProvider{

    public JournalBlockTags(PackOutput output, CompletableFuture<Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, BuildingJournal.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(Provider arg0) {
        // Blocks are mineable via axe
        tag(BlockTags.MINEABLE_WITH_AXE).add(Registration.DRAFTING_TABLE.get(), Registration.ANCHOR_BLOCK.get(), Registration.BLUEPRINT_RACK.get());
    }

}
