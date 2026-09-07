package com.pointlessbuilding.journal.datagen;

import java.util.Map;
import java.util.stream.Collectors;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;

public class JournalLootTables extends VanillaBlockLoot{
    
    public JournalLootTables(Provider registries) {
        super(registries);
    }

    @Override
    protected void generate() {
        dropSelf(Registration.DRAFTING_TABLE.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.entrySet().stream()
                .filter(e -> e.getKey().location().getNamespace().equals(BuildingJournal.MODID))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

}
