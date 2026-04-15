package com.pointlessbuilding.journal.datagen;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.pointlessbuilding.journal.BuildingJournal;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class JournalItemTags extends ItemTagsProvider{

    public JournalItemTags(PackOutput output, CompletableFuture<Provider> lookupProvider, BlockTagsProvider blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags.contentsGetter(), BuildingJournal.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(Provider arg0) {
        //NA
    }
    
}
