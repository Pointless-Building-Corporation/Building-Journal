package com.pointlessbuilding.journal.datagen;

import java.util.function.Consumer;
import com.pointlessbuilding.journal.Registration;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

public class JournalRecipes extends RecipeProvider{

    public JournalRecipes(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        // The anchor block needs 4 planks, paper and a stick.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registration.ANCHOR_BLOCK.get())
            .pattern(" s ")
            .pattern("wpw")
            .pattern("w w")
            .define('s', Items.STICK)
            .define('w', ItemTags.PLANKS)
            .define('p', Items.PAPER)
            .unlockedBy("has_paper", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(Items.PAPER).build()))
            .save(consumer);

        // The complex anchor block needs 4 sticks and a chest.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Registration.COMPLEX_ANCHOR_BLOCK.get())
            .pattern("   ")
            .pattern("s s")
            .pattern("scs")
            .define('s', Items.STICK)
            .define('c', Tags.Items.CHESTS)
            .unlockedBy("has_crafting_table", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(Items.CRAFTING_TABLE).build()))
            .save(consumer);
    }
    
}
