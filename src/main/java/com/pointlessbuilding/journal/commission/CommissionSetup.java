package com.pointlessbuilding.journal.commission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.conditions.BlockAddedCondition;
import com.pointlessbuilding.journal.commission.conditions.BlockModifiedCondition;
import com.pointlessbuilding.journal.commission.conditions.BlockRemovedCondition;
import com.pointlessbuilding.journal.commission.conditions.DensityCondition;
import com.pointlessbuilding.journal.commission.conditions.DimensionCondition;
import com.pointlessbuilding.journal.commission.conditions.ElevationCondition;
import com.pointlessbuilding.journal.commission.conditions.TallnessCondition;
import com.pointlessbuilding.journal.commission.conditions.TotalVolumeCondition;
import com.pointlessbuilding.journal.commission.unlocks.BlockRewardUnlock;
import com.pointlessbuilding.journal.commission.unlocks.CommissionRewardUnlock;
import com.pointlessbuilding.journal.commission.unlocks.ExpRewardUnlock;

import net.minecraftforge.fml.loading.FMLPaths;

public class CommissionSetup {

    public static enum SUPPORTED_SCHEMA_VERSION {
        V1(1);

        public final int ver;

        private SUPPORTED_SCHEMA_VERSION(int ver) {
            this.ver = ver;
        }
    }

    private static final Map<String, Function<JsonObject, CommissionCondition>> CONDITION_PARSERS = Map.of(
        "BlockAdded", BlockAddedCondition::fromJson,
        "BlockRemoved", BlockRemovedCondition::fromJson,
        "BlockModified", BlockModifiedCondition::fromJson,
        "Density", DensityCondition::fromJson,
        "Dimension", DimensionCondition::fromJson,
        "Elevation", ElevationCondition::fromJson,
        "Tallness", TallnessCondition::fromJson,
        "TotalVolume", TotalVolumeCondition::fromJson
    );

    private static final Map<String, Function<JsonObject, CommissionUnlock>> UNLOCK_PARSERS = Map.of(
        "BlockReward", BlockRewardUnlock::fromJson,
        "CommissionReward", CommissionRewardUnlock::fromJson,
        "ExpReward", ExpRewardUnlock::fromJson
    );

    public static final Path commissionsFolder = FMLPaths.GAMEDIR.get().resolve("commissions");
    public static final String[] defaultCommissions = {
        "your_first_house.json"
    };

    public static void setup() {
        if (!Files.exists(commissionsFolder)) {
            try {
                Files.createDirectories(commissionsFolder); 
            }
            catch(Exception e) {
                BuildingJournal.LOGGER.error("Exception caught at commonSetup: {}", e);
            }
            
            for (String filename: defaultCommissions) {
                try (InputStream in = BuildingJournal.class.getResourceAsStream("/commissions/" + filename)) {
                    if (in == null) {
                        BuildingJournal.LOGGER.error("Missing file: {}", filename);
                        continue;
                    }
                    Files.copy(in, commissionsFolder.resolve(filename));
                }
                catch (IOException e) {
                    BuildingJournal.LOGGER.error("Failed to extract {}: Exception: {}", filename, e);
                }
            }
        }
    }

    public static List<Commission> loadCommissions() {

        List<Commission> availableCommissions = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(commissionsFolder, "*.json")) {
            for (Path file: stream) {
                if (file.getFileName().toString().equals("commission.schema.json")) continue;

                try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    
                    int schemaVersion = jsonObject.get("schemaVersion").getAsInt();
                    boolean supported = Arrays.stream(SUPPORTED_SCHEMA_VERSION.values()).anyMatch(v -> v.ver == schemaVersion);
                    if(!supported) {
                        BuildingJournal.LOGGER.error("Unsupported schemaVersion {} in file {}", schemaVersion, file);
                        continue;
                    }

                    String id = jsonObject.has("id")
                        ? jsonObject.get("id").getAsString()
                        : file.getFileName().toString().replace(".json", "");

                    String title = jsonObject.get("title").getAsString();

                    String thumbnailPathString = jsonObject.has("thumbnailPath")
                        ? jsonObject.get("thumbnailPath").getAsString()
                        : null;
                    Path thumbnailPath = thumbnailPathString != null
                        ? commissionsFolder.resolve(thumbnailPathString)
                        : null;

                    List<String> prerequisites = new ArrayList<>();
                    if(jsonObject.has("prerequisites")) {
                        for (JsonElement el : jsonObject.getAsJsonArray("prerequisites"))
                            prerequisites.add(el.getAsString());
                    }

                    JsonArray conditionsArray = jsonObject.getAsJsonArray("conditions");
                    JsonArray unlocksArray = jsonObject.has("unlocks") ? jsonObject.getAsJsonArray("unlocks") : new JsonArray();

                    List<CommissionCondition> conditions = new ArrayList<>();
                    List<CommissionUnlock> unlocks = new ArrayList<>();

                    // Conditions registry
                    for (JsonElement condition : conditionsArray) {
                        JsonObject conditionObject = condition.getAsJsonObject();
                        String conditionType = conditionObject.get("condition").getAsString();
                        Function<JsonObject, CommissionCondition> parser = CONDITION_PARSERS.get(conditionType);
                        if(parser == null) {
                            BuildingJournal.LOGGER.error("Unknown condition type {} in file {}", conditionType, file);
                            continue;
                        }
                        conditions.add(parser.apply(conditionObject));
                        
                    }

                    // Unlocks registry
                    for (JsonElement unlock : unlocksArray) {
                        JsonObject unlockObject = unlock.getAsJsonObject();
                        String unlockType = unlockObject.get("unlock").getAsString();
                        Function<JsonObject, CommissionUnlock> parser = UNLOCK_PARSERS.get(unlockType);
                        if(parser == null) {
                            BuildingJournal.LOGGER.error("Unknown unlock type {} in file {}", unlockType, file);
                            continue;
                        }
                        unlocks.add(parser.apply(unlockObject));        
                    }

                    availableCommissions.add(new Commission(id, title, thumbnailPath, conditions, unlocks, prerequisites));    
                
                }
                catch(IOException e) {
                    BuildingJournal.LOGGER.error("Failed to read commission file {}: {}", file, e);
                }
            }
        }
        catch (IOException e) {
            BuildingJournal.LOGGER.error("Failed to list commissions folder: {}", e);
        }

        return availableCommissions;
    }

}
