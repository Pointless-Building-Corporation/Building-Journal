package com.pointlessbuilding.journal.datagen;

import java.util.concurrent.CompletableFuture;
import com.pointlessbuilding.journal.Registration;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

public class JournalRecipes extends RecipeProvider{

    public JournalRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // The drafting table needs 4 planks, paper and a stick.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registration.DRAFTING_TABLE.get())
            .pattern(" s ")
            .pattern("wpw")
            .pattern("w w")
            .define('s', Items.STICK)
            .define('w', ItemTags.PLANKS)
            .define('p', Items.PAPER)
            .unlockedBy("has_paper", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(Items.PAPER).build()))
            .save(output);

        // The builder's compass needs 2 sticks and an iron ingot.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Registration.BUILDERS_COMPASS.get())
            .pattern("   ")
            .pattern("  s")
            .pattern(" si")
            .define('s', Items.STICK)
            .define('i', Items.IRON_INGOT)
            .unlockedBy("has_iron", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(Items.IRON_INGOT).build()))
            .save(output);
    }
    
}
