package com.pointlessbuilding.journal.datagen;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.data.event.GatherDataEvent;

public class DataGeneration {
    
    public static void generate(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new JournalBlockStates(packOutput, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new JournalItemModels(packOutput, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new JournalLanguageProvider(packOutput, "en_us"));

        JournalBlockTags blockTags = new JournalBlockTags(packOutput, lookupProvider, event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new JournalItemTags(packOutput, lookupProvider, blockTags, event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new JournalRecipes(packOutput));
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(),
            List.of(new LootTableProvider.SubProviderEntry(JournalLootTables::new, LootContextParamSets.BLOCK))));

    }

}
