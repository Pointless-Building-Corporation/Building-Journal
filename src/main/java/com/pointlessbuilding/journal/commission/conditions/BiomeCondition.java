package com.pointlessbuilding.journal.commission.conditions;

import com.google.gson.JsonObject;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.CommissionCondition;
import com.pointlessbuilding.journal.commission.EvaluationResult;

public class BiomeCondition implements CommissionCondition{

    private final String title;
    private final String failureDescription;
    private final String biome;

    public BiomeCondition(String title, String failureDescription, String biome) {
        this.title = title;
        this.failureDescription = failureDescription;
        this.biome = biome;
    }

    @Override
    public boolean test(EvaluationResult result) {
        return result.biomes().contains(biome);
    }
    
    @Override
    public String getTitle() {
        if(title == null) {
            String generatedTitle = "Must be built in " + biome;
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
        String jsonBiome;

        if(json.has("title")) {
            jsonTitle = json.get("title").getAsString();
        }

        if(json.has("failureDescription")) {
            jsonFailure = json.get("failureDescription").getAsString();
        }

        if(json.has("biome")) {
            jsonBiome = json.get("biome").getAsString();
        }
        else {
            BuildingJournal.LOGGER.warn("Missing biome field in condition {}", jsonTitle != null ? jsonTitle : "BiomeCondition");
            return null;
        }

        return new BiomeCondition(jsonTitle, jsonFailure, jsonBiome);
    }

}
