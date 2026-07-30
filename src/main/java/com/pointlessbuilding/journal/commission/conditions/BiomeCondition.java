package com.pointlessbuilding.journal.commission.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class BiomeCondition implements CommissionCondition{

    private final String title;
    private final String failureDescription;
    private final List<String> biomes;

    public BiomeCondition(String title, String failureDescription, List<String> biomes) {
        this.title = title;
        this.failureDescription = failureDescription;
        this.biomes = biomes;
    }

    @Override
    public boolean test(EvaluationResult result) {
        if (Minecraft.getInstance().level == null) {
            return false;
        }

        Registry<Biome> biomeRegistry = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.BIOME);

        return result.biomes().stream().anyMatch(currentBiome -> {
            for (String biome : this.biomes) {
                if (!biome.startsWith("#") && !biome.startsWith("regex:")) {
                    if (biome.equals(currentBiome)) return true;
                }
                else if (biome.startsWith("regex:")) {
                    Pattern pattern = Pattern.compile(biome.substring("regex:".length()));
                    if (pattern.matcher(currentBiome).matches()) return true;
                }
                else if (biome.startsWith("#")) {
                    ResourceLocation tagId = ResourceLocation.tryParse(biome.substring(1));
                    if (tagId == null) continue;

                    ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, ResourceLocation.tryParse(currentBiome));
                    Holder<Biome> biomeHolder = biomeRegistry.getHolderOrThrow(biomeKey);
                    if (biomeHolder.is(TagKey.create(Registries.BIOME, tagId))) return true;
                }
            }

            return false;
        });
    }
    
    @Override
    public String getTitle() {
        if(title == null) {
            String generatedTitle = "Must be built in one of " + biomes;
            return generatedTitle;
        }
        return title;
    }

    @Override
    public String describeFailure(EvaluationResult result) {
        if(failureDescription == null) {
            String generatedDesc = "Biome not present in build!";
            return generatedDesc;
        }
        else {
            return failureDescription;
        }
    }

    public static CommissionCondition fromJson(JsonObject json) {
        String jsonTitle = null;
        String jsonFailure = null;
        List<String> jsonBiomes = new ArrayList<>();

        if(json.has("title")) {
            jsonTitle = json.get("title").getAsString();
        }

        if(json.has("failureDescription")) {
            jsonFailure = json.get("failureDescription").getAsString();
        }

        if(json.has("biomes")) {
            JsonArray biomeArray = json.get("biomes").getAsJsonArray();
            if(biomeArray.size() < 1) {
                BuildingJournal.LOGGER.warn("Too few biomes in condition {}", jsonTitle != null ? jsonTitle : "BiomeCondition");
                return null;
            }
            for(JsonElement biome : biomeArray) {
                String biomeString = biome.getAsString();
                jsonBiomes.add(biomeString);
            }
        }
        else {
            BuildingJournal.LOGGER.warn("Missing biomes field in condition {}", jsonTitle != null ? jsonTitle : "BiomeCondition");
            return null;
        }

        return new BiomeCondition(jsonTitle, jsonFailure, jsonBiomes);
    }

}
