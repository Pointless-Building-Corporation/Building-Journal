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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.commission.conditions.BiomeCondition;
import com.pointlessbuilding.journal.commission.conditions.BlockAddedCondition;
import com.pointlessbuilding.journal.commission.conditions.BlockModifiedCondition;
import com.pointlessbuilding.journal.commission.conditions.BlockRemovedCondition;
import com.pointlessbuilding.journal.commission.conditions.DensityCondition;
import com.pointlessbuilding.journal.commission.conditions.DimensionCondition;
import com.pointlessbuilding.journal.commission.conditions.ElevationCondition;
import com.pointlessbuilding.journal.commission.conditions.TallnessCondition;
import com.pointlessbuilding.journal.commission.conditions.TotalVolumeCondition;
import com.pointlessbuilding.journal.commission.conditions.WhitelistCondition;
import com.pointlessbuilding.journal.commission.unlocks.BlockRewardUnlock;
import com.pointlessbuilding.journal.commission.unlocks.CommissionRewardUnlock;
import com.pointlessbuilding.journal.commission.unlocks.ExpRewardUnlock;
import com.pointlessbuilding.journal.menu.CommissionContainer;
import com.pointlessbuilding.journal.network.Network;
import com.pointlessbuilding.journal.network.packets.SyncCardCommissionsPacket;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkHooks;

public class CommissionLoader {

    public static enum SUPPORTED_SCHEMA_VERSION {
        V1(1);

        public final int ver;

        private SUPPORTED_SCHEMA_VERSION(int ver) {
            this.ver = ver;
        }
    }

    public static final Map<String, Function<JsonObject, CommissionCondition>> CONDITION_PARSERS = Map.of(
        "BlockAdded", BlockAddedCondition::fromJson,
        "BlockRemoved", BlockRemovedCondition::fromJson,
        "BlockModified", BlockModifiedCondition::fromJson,
        "Density", DensityCondition::fromJson,
        "Dimension", DimensionCondition::fromJson,
        "Elevation", ElevationCondition::fromJson,
        "Tallness", TallnessCondition::fromJson,
        "TotalVolume", TotalVolumeCondition::fromJson,
        "Whitelist", WhitelistCondition::fromJson,
        "Biome", BiomeCondition::fromJson
    );

    public static final Map<String, Function<JsonObject, CommissionUnlock>> UNLOCK_PARSERS = Map.of(
        "BlockReward", BlockRewardUnlock::fromJson,
        "CommissionReward", CommissionRewardUnlock::fromJson,
        "ExpReward", ExpRewardUnlock::fromJson
    );

    public static final Path commissionsFolder = FMLPaths.GAMEDIR.get().resolve("commissions");
    public static final String[] defaultCommissions = {
        "your_first_house.json"
    };

    private static List<Commission> loadedCommissions = new ArrayList<>();
    private static Map<String, String> rawConditionsJson = new HashMap<>();

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
                    
                    if(!jsonObject.has("schemaVersion")) {
                        BuildingJournal.LOGGER.error("Missing schemaVersion in file {}", file);
                        continue;
                    }
                    int schemaVersion = jsonObject.get("schemaVersion").getAsInt();
                    boolean supported = Arrays.stream(SUPPORTED_SCHEMA_VERSION.values()).anyMatch(v -> v.ver == schemaVersion);
                    if(!supported) {
                        BuildingJournal.LOGGER.error("Unsupported schemaVersion {} in file {}", schemaVersion, file);
                        continue;
                    }

                    String id = jsonObject.has("id")
                        ? jsonObject.get("id").getAsString()
                        : file.getFileName().toString().replace(".json", "");

                    if(!jsonObject.has("title")) {
                        BuildingJournal.LOGGER.error("Missing title in file {}", file);
                        continue;
                    }
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

                    if(!jsonObject.has("conditions")) {
                        BuildingJournal.LOGGER.error("Missing conditions in file {}", file);
                        continue;
                    }
                    JsonArray conditionsArray = jsonObject.getAsJsonArray("conditions");
                    rawConditionsJson.put(id, conditionsArray.toString());
                       
                    JsonArray unlocksArray = jsonObject.has("unlocks") ? jsonObject.getAsJsonArray("unlocks") : new JsonArray();

                    List<CommissionCondition> conditions = new ArrayList<>();
                    List<CommissionUnlock> unlocks = new ArrayList<>();

                    // Conditions registry
                    for (JsonElement condition : conditionsArray) {
                        JsonObject conditionObject = condition.getAsJsonObject();
                        if(!conditionObject.has("condition")) {
                            BuildingJournal.LOGGER.error("Missing condition field within conditions at file {}", file);
                            continue;
                        }
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
                         if(!unlockObject.has("unlock")) {
                            BuildingJournal.LOGGER.error("Missing unlock field within unlocks at file {}", file);
                            continue;
                        }
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

        loadedCommissions = availableCommissions;
        return availableCommissions;
    }

    public static Optional<Commission> getById(String id) {
        return loadedCommissions.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    public static String getRawConditionsJson(String id) {
        return rawConditionsJson.getOrDefault(id, "[]");
    }

    public static CommissionState fetchCommissionState(Set<String> completed, Commission commission) {
        if (completed.contains(commission.id())) return CommissionState.COMPLETED;
        for (String prereq : commission.prerequisites()) {
            if(!completed.contains(prereq)) return CommissionState.UNAVAILABLE;
        }

        return CommissionState.AVAILABLE;
    }

    public static byte[] fetchThumbnailBytes(Commission commission) {
        Path thumbnailPath = commission.thumbnailPath();
        if(thumbnailPath == null) return new byte[0];
        try {
            return Files.readAllBytes(thumbnailPath);
        }
        catch(Exception e) {
            BuildingJournal.LOGGER.error("Failed to read thumbnail for commission {} : {}", commission.id(), e);
            return new byte[0];
        }
    }

    public static void sendCommissionCardData(ServerPlayer player) {

        List<Commission> availableCommissions = loadCommissions();
        Set<String> completed = player.getCapability(CommissionProgress.COMMISSION_PROGRESS)
            .resolve()
            .map(ICommissionProgress::getCompletedCommissions)
            .orElse(Collections.emptySet());

        List<CommissionCardData> cardCommissions = new ArrayList<>();

        for (Commission c : availableCommissions) {
            String id = c.id();
            String title = c.title();

            byte[] thumbnailBytes = fetchThumbnailBytes(c);

            CommissionState state = fetchCommissionState(completed, c);

            cardCommissions.add(new CommissionCardData(id, title, thumbnailBytes, state));

        }

        Network.sendToClient(new SyncCardCommissionsPacket(cardCommissions), player);

    }

    public static void sendCommissionDetailData(ServerPlayer player, String commissionId, int commissionPage) {
        Commission commission = CommissionLoader.getById(commissionId).orElse(null);
        if (commission == null) {
            BuildingJournal.LOGGER.error("Commission doesn't exist of the id {} inside CommissionLoader.", commissionId);
            return;
        }

        Set<String> completed = player.getCapability(CommissionProgress.COMMISSION_PROGRESS)
            .resolve()
            .map(ICommissionProgress::getCompletedCommissions)
            .orElse(Collections.emptySet());

        CommissionState state = fetchCommissionState(completed, commission);

        String conditionsJson = CommissionLoader.getRawConditionsJson(commissionId);

        NetworkHooks.openScreen(player, new SimpleMenuProvider(
            (windowId, inv, p) -> new CommissionContainer(windowId, inv.player, commissionId, commission.title(), state, conditionsJson, commission.unlocks(), commissionPage), Component.literal(commission.title())),
            buf -> {
                buf.writeUtf(commissionId);
                buf.writeUtf(commission.title());
                buf.writeEnum(state);
                buf.writeUtf(conditionsJson);
                buf.writeInt(commissionPage);
            }
        );
    }

}
