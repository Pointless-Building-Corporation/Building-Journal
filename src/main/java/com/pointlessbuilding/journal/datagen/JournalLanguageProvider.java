package com.pointlessbuilding.journal.datagen;

import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.Registration;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class JournalLanguageProvider extends LanguageProvider{

    public JournalLanguageProvider(PackOutput output, String locale) {
        super(output, BuildingJournal.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add(Registration.ANCHOR_BLOCK.get(), "Anchor Block");
        add(Registration.COMPLEX_ANCHOR_BLOCK.get(), "Complex Anchor Block");
    }
    
}
