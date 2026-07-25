package com.pointlessbuilding.journal.datagen;

import java.util.Map;
import java.util.stream.Collectors;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class JournalLootTables extends VanillaBlockLoot{
    
    @Override
    protected void generate() {
        dropSelf(Registration.DRAFTING_TABLE.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ForgeRegistries.BLOCKS.getEntries().stream()
                .filter(e -> e.getKey().location().getNamespace().equals(BuildingJournal.MODID))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

}
