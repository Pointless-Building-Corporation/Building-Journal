package com.pointlessbuilding.journal.commission;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class DailyCommission {
    
    private final LocalDate date;
    private final Random random;
    private final MinecraftServer server;

    public DailyCommission(LocalDate date, MinecraftServer server) {
        this.date = date;
        this.random = new Random(date.toEpochDay());
        this.server = server;
        generate();
    }

    private String id;
    private String title;
    private String conditionJson;

    public LocalDate getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getConditionJson() {
        return conditionJson;
    }

    private static final Map<String, List<String>> BIOME_BLOCKS;
    static {
        Map<String, List<String>> map = new HashMap<>();

        List<String> stoneBricks = new ArrayList<>(resolveBlockTag(BlockTags.STONE_BRICKS));
        List<String> oakLogs = new ArrayList<>(resolveBlockTag(BlockTags.OAK_LOGS));
        List<String> birchLogs = new ArrayList<>(resolveBlockTag(BlockTags.BIRCH_LOGS));
        List<String> cherryLogs = new ArrayList<>(resolveBlockTag(BlockTags.CHERRY_LOGS));
        List<String> spruceLogs = new ArrayList<>(resolveBlockTag(BlockTags.SPRUCE_LOGS));
        List<String> darkOakLogs = new ArrayList<>(resolveBlockTag(BlockTags.DARK_OAK_LOGS));
        List<String> jungleLogs = new ArrayList<>(resolveBlockTag(BlockTags.JUNGLE_LOGS));
        List<String> acaciaLogs = new ArrayList<>(resolveBlockTag(BlockTags.ACACIA_LOGS));
        List<String> mangroveLogs = new ArrayList<>(resolveBlockTag(BlockTags.MANGROVE_LOGS));
        List<String> bambooBlocks = new ArrayList<>(resolveBlockTag(BlockTags.BAMBOO_BLOCKS));
        List<String> terracotta = new ArrayList<>(resolveBlockTag(BlockTags.TERRACOTTA));
        List<String> crimsonStems = new ArrayList<>(resolveBlockTag(BlockTags.CRIMSON_STEMS));
        List<String> warpedStems = new ArrayList<>(resolveBlockTag(BlockTags.WARPED_STEMS));

        // Biome categories taken from the wiki
        // Offshore Biomes:
        map.put("minecraft:ocean", List.of("minecraft:stone", "minecraft:gravel", "minecraft:sand", "minecraft:cobblestone", "#minecraft:stone_bricks"));
        map.put("minecraft:deep_ocean", Stream.concat(stoneBricks.stream(), Stream.of("minecraft:stone", "minecraft:gravel", "minecraft:sand", "minecraft:cobblestone", "minecraft:dried_kelp_block")).toList());
        map.put("minecraft:warm_ocean", Stream.concat(stoneBricks.stream(), Stream.of("minecraft:stone", "minecraft:gravel", "minecraft:sand", "minecraft:cobblestone")).toList());
        map.put("minecraft:lukewarm_ocean", Stream.concat(stoneBricks.stream(), Stream.of("minecraft:stone", "minecraft:gravel", "minecraft:sand", "minecraft:cobblestone", "minecraft:clay", "minecraft:bricks")).toList());
        map.put("minecraft:deep_lukewarm_ocean", Stream.concat(stoneBricks.stream(), Stream.of("minecraft:stone", "minecraft:gravel", "minecraft:sand", "minecraft:cobblestone", "minecraft:dried_kelp_block")).toList());
        map.put("minecraft:cold_ocean", Stream.concat(stoneBricks.stream(), Stream.of("minecraft:stone", "minecraft:gravel", "minecraft:sand", "minecraft:cobblestone")).toList());
        map.put("minecraft:deep_cold_ocean", Stream.concat(stoneBricks.stream(), Stream.of("minecraft:stone", "minecraft:gravel", "minecraft:sand", "minecraft:cobblestone", "minecraft:dried_kelp_block")).toList());
        map.put("minecraft:frozen_ocean", Stream.concat(stoneBricks.stream(), Stream.of("minecraft:stone", "minecraft:gravel", "minecraft:sand", "minecraft:cobblestone", "minecraft:snow_block", "minecraft:packed_ice")).toList());
        map.put("minecraft:deep_frozen_ocean", Stream.concat(stoneBricks.stream(), Stream.of("minecraft:stone", "minecraft:gravel", "minecraft:sand", "minecraft:cobblestone", "minecraft:dried_kelp_block", "minecraft:snow_block")).toList());
        map.put("minecraft:mushroom_fields", List.of("minecraft:mycelium", "minecraft:mushroom_stem", "minecraft:red_mushroom_block", "minecraft:brown_mushroom_block"));

        // Highland Biomes:
        map.put("minecraft:jagged_peaks", Stream.concat(stoneBricks.stream(), Stream.of("minecraft:stone", "minecraft:gravel", "minecraft:sand", "minecraft:cobblestone")).toList());
        map.put("minecraft:frozen_peaks", Stream.concat(stoneBricks.stream(), Stream.of("minecraft:stone", "minecraft:gravel", "minecraft:sand", "minecraft:cobblestone", "minecraft:snow_block", "minecraft:packed_ice")).toList());
        map.put("minecraft:stony_peaks", Stream.concat(stoneBricks.stream(), Stream.of("minecraft:stone", "minecraft:cobblestone", "minecraft:calcite")).toList());
        map.put("minecraft:meadow", Stream.of(stoneBricks.stream(), oakLogs.stream(), Stream.of("minecraft:dirt", "minecraft:oak_planks")).flatMap(s -> s).toList());
        map.put("minecraft:cherry_grove", Stream.concat(cherryLogs.stream(), Stream.of("minecraft:cherry_planks", "minecraft:cherry_leaves", "minecraft:grass_block")).toList());
        map.put("minecraft:grove", Stream.concat(spruceLogs.stream(), Stream.of("minecraft:spruce_planks", "minecraft:snow_block")).toList());
        map.put("minecraft:snowy_slopes", Stream.concat(spruceLogs.stream(), Stream.of( "minecraft:spruce_planks", "minecraft:snow_block")).toList());
        map.put("minecraft:windswept_hills", Stream.of(stoneBricks.stream(), oakLogs.stream(), Stream.of("minecraft:dirt", "minecraft:oak_planks", "minecraft:stone")).flatMap(s -> s).toList());
        map.put("minecraft:windswept_gravelly_hills", Stream.of(stoneBricks.stream(), oakLogs.stream(), Stream.of("minecraft:gravel", "minecraft:stone")).flatMap(s -> s).toList());
        map.put("minecraft:windswept_forest", Stream.of(stoneBricks.stream(), oakLogs.stream(), Stream.of("minecraft:dirt", "minecraft:oak_planks", "minecraft:stone")).flatMap(s -> s).toList());
        
        // Woodland Biomes:
        map.put("minecraft:forest", Stream.of(stoneBricks.stream(), oakLogs.stream(), Stream.of("minecraft:dirt", "minecraft:oak_planks", "minecraft:stone")).flatMap(s -> s).toList());
        map.put("minecraft:flower_forest", Stream.concat(oakLogs.stream(), Stream.of("minecraft:dirt", "minecraft:oak_planks", "minecraft:stone", "#minecraft:honeycomb_block")).toList());
        map.put("minecraft:taiga", Stream.concat(spruceLogs.stream(), Stream.of("minecraft:spruce_planks", "minecraft:cobblestone", "minecraft:stone")).toList());
        map.put("minecraft:old_growth_pine_taiga", Stream.concat(spruceLogs.stream(), Stream.of("minecraft:spruce_planks", "minecraft:cobblestone", "minecraft:mossy_cobblestone", "minecraft:podzol")).toList());
        map.put("minecraft:old_growth_spruce_taiga", Stream.concat(spruceLogs.stream(), Stream.of("minecraft:spruce_planks", "minecraft:cobblestone", "minecraft:mossy_cobblestone", "minecraft:podzol")).toList());
        map.put("minecraft:snowy_taiga", Stream.concat(spruceLogs.stream(), Stream.of( "minecraft:spruce_planks", "minecraft:cobblestone", "minecraft:stone")).toList());
        map.put("minecraft:birch_forest", Stream.concat(birchLogs.stream(), Stream.of("minecraft:dirt", "minecraft:birch_planks", "minecraft:stone")).toList());
        map.put("minecraft:old_growth_birch_forest", Stream.concat(birchLogs.stream(), Stream.of("minecraft:dirt", "minecraft:birch_planks", "minecraft:stone")).toList());
        map.put("minecraft:dark_forest", Stream.concat(darkOakLogs.stream(), Stream.of("minecraft:dirt", "minecraft:dark_oak_planks", "minecraft:stone")).toList());
        map.put("minecraft:jungle", Stream.of(jungleLogs.stream(), bambooBlocks.stream(), Stream.of("minecraft:jungle_planks", "minecraft:jungle_leaves")).flatMap(s -> s).toList());
        map.put("minecraft:sparse_jungle", Stream.concat(jungleLogs.stream(), Stream.of( "minecraft:jungle_planks", "minecraft:jungle_leaves")).toList());
        map.put("minecraft:bamboo_jungle", Stream.of(jungleLogs.stream(), bambooBlocks.stream(), Stream.of("minecraft:jungle_planks", "minecraft:jungle_leaves")).flatMap(s -> s).toList());

        // Wetland Biomes:
        map.put("minecraft:river", List.of("minecraft:sand", "minecraft:gravel", "minecraft:cobblestone", "minecraft:clay"));
        map.put("minecraft:frozen_river", List.of("minecraft:sand", "minecraft:gravel", "minecraft:cobblestone", "minecraft:packed_ice"));
        map.put("minecraft:swamp", Stream.concat(oakLogs.stream(), Stream.of("minecraft:mud", "minecraft:oak_planks", "minecraft:moss_block")).toList());
        map.put("minecraft:mangrove_swamp", Stream.concat(mangroveLogs.stream(), Stream.of("minecraft:mud", "minecraft:mangrove_planks", "minecraft:moss_block")).toList());
        map.put("minecraft:beach", List.of("minecraft:sand", "minecraft:sandstone", "minecraft:gravel", "minecraft:stone"));
        map.put("minecraft:snowy_beach", List.of("minecraft:sand", "minecraft:sandstone", "minecraft:gravel", "minecraft:stone"));
        map.put("minecraft:stony_shore", List.of("minecraft:stone", "minecraft:cobblestone"));

        // Flatland Biomes:
        map.put("minecraft:plains", Stream.of(stoneBricks.stream(), oakLogs.stream(), Stream.of("minecraft:dirt", "minecraft:oak_planks", "minecraft:stone")).flatMap(s -> s).toList());
        map.put("minecraft:sunflower_plains", Stream.of(stoneBricks.stream(), oakLogs.stream(), Stream.of("minecraft:dirt", "minecraft:oak_planks")).flatMap(s -> s).toList());
        map.put("minecraft:snowy_plains", Stream.of(stoneBricks.stream(), oakLogs.stream(), Stream.of("minecraft:dirt", "minecraft:oak_planks")).flatMap(s -> s).toList());
        map.put("minecraft:ice_spikes", List.of("minecraft:snow_block", "minecraft:packed_ice"));

        // Arid-land Biomes:
        map.put("minecraft:desert", List.of("minecraft:sand", "minecraft:sandstone", "minecraft:smooth_sandstone", "minecraft:cut_sandstone", "minecraft:chiseled_sandstone"));
        map.put("minecraft:savanna", Stream.of(stoneBricks.stream(), acaciaLogs.stream(), Stream.of("minecraft:acacia_planks", "minecraft:dirt", "minecraft:stone")).flatMap(s -> s).toList());
        map.put("minecraft:savanna_plateau", Stream.of(stoneBricks.stream(), acaciaLogs.stream(), Stream.of("minecraft:acacia_planks", "minecraft:dirt", "minecraft:stone")).flatMap(s -> s).toList());
        map.put("minecraft:windswept_savanna", Stream.of(stoneBricks.stream(), acaciaLogs.stream(), Stream.of("minecraft:acacia_planks", "minecraft:dirt", "minecraft:stone")).flatMap(s -> s).toList());
        map.put("minecraft:badlands", Stream.concat(terracotta.stream(), Stream.of("#minecraft:terracotta", "minecraft:red_sand", "minecraft:red_sandstone")).toList());
        map.put("minecraft:wooded_badlands", Stream.concat(terracotta.stream(), Stream.of("#minecraft:terracotta", "minecraft:red_sand", "minecraft:red_sandstone")).toList());
        map.put("minecraft:eroded_badlands", Stream.concat(terracotta.stream(), Stream.of("#minecraft:terracotta", "minecraft:red_sand", "minecraft:red_sandstone")).toList());

        // Cave Biomes:
        map.put("minecraft:deep_dark", List.of("minecraft:deepslate", "minecraft:cobbled_deepslate", "minecraft:sculk", "minecraft:tuff"));
        map.put("minecraft:dripstone_caves", List.of("minecraft:deepslate", "minecraft:cobbled_deepslate", "minecraft:dripstone_block", "minecraft:tuff"));
        map.put("minecraft:lush_caves", List.of("minecraft:deepslate", "minecraft:cobbled_deepslate", "minecraft:moss_block", "minecraft:clay"));

        // The Nether:
        map.put("minecraft:nether_wastes", List.of("minecraft:netherrack", "minecraft:nether_bricks", "minecraft:blackstone", "minecraft:basalt"));
        map.put("minecraft:soul_sand_valley", List.of("minecraft:soul_sand", "minecraft:soul_soil", "minecraft:basalt", "minecraft:bone_block"));
        map.put("minecraft:crimson_forest", Stream.concat(crimsonStems.stream(), Stream.of("minecraft:crimson_planks", "minecraft:netherrack", "minecraft:shroomlight")).toList());
        map.put("minecraft:warped_forest", Stream.concat(warpedStems.stream(), Stream.of( "minecraft:warped_planks", "minecraft:netherrack", "minecraft:shroomlight")).toList());
        map.put("minecraft:basalt_deltas", List.of("minecraft:basalt", "minecraft:blackstone", "minecraft:smooth_basalt", "minecraft:magma_block"));

        // The End:
        map.put("minecraft:the_end", List.of("minecraft:end_stone", "minecraft:end_stone_bricks", "minecraft:obsidian"));
        map.put("minecraft:small_end_islands", List.of("minecraft:end_stone", "minecraft:end_stone_bricks"));
        map.put("minecraft:end_midlands", List.of("minecraft:end_stone", "minecraft:end_stone_bricks", "minecraft:purpur_block"));
        map.put("minecraft:end_highlands", List.of("minecraft:end_stone", "minecraft:end_stone_bricks", "minecraft:purpur_block", "minecraft:purpur_pillar"));
        map.put("minecraft:end_barrens", List.of("minecraft:end_stone", "minecraft:end_stone_bricks"));

        BIOME_BLOCKS = Collections.unmodifiableMap(map);
    }

    private static final List<String> GENERIC_BLOCKS = List.of(
        "minecraft:stone",
        "minecraft:cobblestone",
        "minecraft:dirt",
        "minecraft:bricks"
    );

    private static final Set<String> BLOCK_BANLIST;
    static {
        Set<String> set = new HashSet<>();

        // Ore blocks that too expensive to use
        set.addAll(resolveBlockTag(BlockTags.DIAMOND_ORES));
        set.addAll(resolveBlockTag(BlockTags.EMERALD_ORES));
        set.addAll(resolveBlockTag(BlockTags.LAPIS_ORES));
        set.addAll(resolveBlockTag(BlockTags.GOLD_ORES));
        set.addAll(resolveBlockTag(BlockTags.REDSTONE_ORES));
        set.add("minecraft:ancient_debris");
        set.add("minecraft:netherite_block");
        set.add("minecraft:gold_block");
        set.add("minecraft:emerald_block");
        set.add("minecraft:diamond_block");
        set.add("minecraft:gold_block");
        set.add("minecraft:raw_gold_block");

        // Other expensive blocks
        set.add("minecraft:enchanting_table");
        set.add("minecraft:dragon_egg");
        set.add("minecraft:beacon");
        set.add("minecraft:conduit");
        set.add("minecraft:respawn_anchor");
        set.add("minecraft:lodestone");
        set.add("minecraft:gilded_blackstone");
        set.addAll(resolveBlockTag(BlockTags.SHULKER_BOXES));

        // Heads
        set.add("minecraft:wither_skeleton_skull");
        set.add("minecraft:wither_skeleton_wall_skull");
        set.add("minecraft:skeleton_skull");
        set.add("minecraft:skeleton_wall_skull");
        set.add("minecraft:player_head");
        set.add("minecraft:player_wall_head");
        set.add("minecraft:creeper_head");
        set.add("minecraft:creeper_wall_head");
        set.add("minecraft:dragon_head");
        set.add("minecraft:dragon_wall_head");
        set.add("minecraft:zombie_head");
        set.add("minecraft:zombie_wall_head");
        set.add("minecraft:piglin_head");
        set.add("minecraft:piglin_wall_head");

        // Saplings, mushrooms and farmed plants
        set.addAll(resolveBlockTag(BlockTags.SAPLINGS));
        set.addAll(resolveBlockTag(BlockTags.CROPS));
        set.add("minecraft:attached_pumpkin_stem");
        set.add("minecraft:attached_melon_stem");
        set.addAll(resolveBlockTag(BlockTags.CAVE_VINES));
        set.addAll(resolveBlockTag(BlockTags.UNDERWATER_BONEMEALS));
        set.addAll(resolveBlockTag(BlockTags.CORAL_BLOCKS));
        set.add("minecraft:fern");
        set.add("minecraft:dead_bush");
        set.add("minecraft:tall_seagrass");
        set.add("minecraft:brown_mushroom");
        set.add("minecraft:red_mushroom");
        set.add("minecraft:sugar_cane");
        set.add("minecraft:farmland");
        set.add("minecraft:cactus");
        set.add("minecraft:cocoa");
        set.add("minecraft:kelp");
        set.add("minecraft:kelp_plant");
        set.add("minecraft:big_dripleaf_stem");
        set.add("minecraft:small_dripleaf");
        

        // Some redstone functional blocks and rails
        set.add("minecraft:dispenser");
        set.add("minecraft:dropper");
        set.add("minecraft:hopper");
        set.add("minecraft:piston");
        set.add("minecraft:piston_head");
        set.add("minecraft:sticky_piston");
        set.add("minecraft:repeater");
        set.add("minecraft:comparator");
        set.add("minecraft:daylight_detector");
        set.addAll(resolveBlockTag(BlockTags.RAILS));

        // Infested blocks
        set.add("minecraft:infested_stone");
        set.add("minecraft:infested_deepslate");
        set.add("minecraft:infested_cobblestone");
        set.add("minecraft:infested_stone_bricks");
        set.add("minecraft:infested_cracked_stone_bricks");
        set.add("minecraft:infested_mossy_stone_bricks");
        set.add("minecraft:infested_chiseled_stone_bricks");

        // Unobtainable blocks in survival
        set.add("minecraft:reinforced_deepslate");
        set.add("minecraft:barrier");
        set.add("minecraft:command_block");
        set.add("minecraft:repeating_command_block");
        set.add("minecraft:chain_command_block");
        set.add("minecraft:structure_block");
        set.add("minecraft:structure_void");
        set.add("minecraft:jigsaw");
        set.add("minecraft:bedrock");
        set.add("minecraft:end_portal_frame");
        set.add("minecraft:spawner");
        set.add("minecraft:light");
        set.add("minecraft:air");
        set.add("minecraft:fire");
        set.add("minecraft:soul_fire");
        set.add("minecraft:budding_amethyst");
        set.add("minecraft:nether_portal");
        set.add("minecraft:end_portal");
        set.add("minecraft:end_gateway");
        set.add("minecraft:frosted_ice");
        set.add("minecraft:bubble_column");
        set.add("minecraft:void_air");
        set.add("minecraft:cave_air");

        // Misc difficult to get blocks
        set.add("minecraft:suspicious_sand");
        set.add("minecraft:suspicious_gravel");
        set.add("minecraft:sponge");
        set.add("minecraft:wet_sponge");
        set.add("minecraft:turtle_egg");
        set.add("minecraft:sniffer_egg");
        set.add("minecraft:frogspawn");

        BLOCK_BANLIST = Collections.unmodifiableSet(set);
    }

    private static final List<String> ALL_BLOCKS_ALLOWED;
    static {
        List<String> list = new ArrayList<>();
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            String id = ForgeRegistries.BLOCKS.getKey(block).toString();
            if (!BLOCK_BANLIST.contains(id)) {
                list.add(id);
            }
        }

        list.sort(Comparator.naturalOrder());
        ALL_BLOCKS_ALLOWED = Collections.unmodifiableList(list);
    }

    private static final Map<String, Double> DIMENSION_WEIGHTS;
    static {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("minecraft:overworld", 10.0);
        map.put("minecraft:the_nether", 4.0);
        map.put("minecraft:the_end", 2.0);
        DIMENSION_WEIGHTS = Collections.unmodifiableMap(map);
    }

    private static final List<String> CAVE_BIOMES = List.of(
        "minecraft:dripstone_caves",
        "minecraft:lush_caves",
        "minecraft:deep_dark"
    );

    private static final List<String> SlOPES_AND_PEAKS_BIOMES = List.of(
        "minecraft:jagged_peaks",
        "minecraft:frozen_peaks",
        "minecraft:stony_peaks",
        "minecraft:meadow",
        "minecraft:cherry_grove",
        "minecraft:grove",
        "minecraft:snowy_slopes"
    );

    private static final Map<String, Double> CONDITION_WEIGHTS;
    static {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("BlockAdded", 10.0);
        map.put("BlockRemoved", 6.0);
        map.put("Whitelist", 9.0);
        map.put("BlockModified", 4.0);
        map.put("Density", 4.0);
        map.put("TotalVolume", 4.0);
        map.put("Elevation", 4.0);
        map.put("Length", 8.0);
        map.put("Tallness", 8.0);
        CONDITION_WEIGHTS = Collections.unmodifiableMap(map);
    }

    private final Map<String, BiFunction<String, String, String>> CONDITION_GENERATORS = Map.of(
        "BlockAdded",    this::genBlockAdded,
        "BlockRemoved",  this::genBlockRemoved,
        "Whitelist",     this::genWhitelist,
        "BlockModified", this::genBlockModified,
        "Density",       this::genDensity,
        "TotalVolume",   this::genTotalVolume,
        "Elevation",     this::genElevation,
        "Length",        this::genLength,
        "Tallness",      this::genTallness
    );

    private static final Set<String> SINGLE_INSTANCE_TYPES = Set.of(
        "BlockModified", "Density", "TotalVolume", "Elevation",
        "Length", "Tallness", "Dimension", "Biome"
    );

    private <T> T weightedChoice(Map<T, Double> weightMap) {
        double totalWeight = weightMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = this.random.nextDouble() * totalWeight;

        double cumulative = 0.0;
        for (Map.Entry<T, Double> entry : weightMap.entrySet()) {
            cumulative += entry.getValue();
            if(roll < cumulative) return entry.getKey();
        }

        return weightMap.keySet().stream().reduce((first, second) -> second).orElseThrow();
    }

    private static Set<String> resolveBlockTag(TagKey<Block> tag) {
        Set<String> result = new HashSet<>();
        ForgeRegistries.BLOCKS.tags().getTag(tag).forEach(block ->
            result.add(ForgeRegistries.BLOCKS.getKey(block).toString())
        );
        return result;
    }

    private List<ResourceKey<Biome>> getBiomesForDimension(ResourceKey<Level> dimensionKey) {
        ServerLevel dimensionLevel = server.getLevel(dimensionKey);
        return dimensionLevel.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream()
        .map(Holder::unwrapKey)
        .flatMap(Optional::stream)
        .toList();
    }

    // 50-50 chance of biome-specific vs any allowed block
    private List<String> pickBlocks(String biome, int n, boolean allowUnthemed) {
        List<String> themedPool = BIOME_BLOCKS.getOrDefault(biome, GENERIC_BLOCKS).stream().filter(b -> !BLOCK_BANLIST.contains(b)).toList();
        if(themedPool.isEmpty()) themedPool = GENERIC_BLOCKS.stream().filter(b -> !BLOCK_BANLIST.contains(b)).toList();

        Set<String> result = new LinkedHashSet<>();
        int attempts = 0;
        while(result.size() < n && attempts < 50) {
            if(allowUnthemed) {
                if(random.nextDouble() < 0.50) result.add(ALL_BLOCKS_ALLOWED.get(random.nextInt(ALL_BLOCKS_ALLOWED.size())));
                else result.add(themedPool.get(random.nextInt(themedPool.size())));
            }
            else
                result.add(themedPool.get(random.nextInt(themedPool.size())));
            attempts++;
        }

        return result.stream().toList();
    }

    private String genBlockAdded(String dimension, String biome) {
        boolean useBlocks = random.nextDouble() < 0.75;
        List<String> blocks = useBlocks ? pickBlocks(biome, 1 + random.nextInt(3), true) : new ArrayList<>();
        String operator = random.nextBoolean() ? ">" : "<";
        long threshold = 20 + random.nextInt(181);

        String json = "{\"condition\":\"BlockAdded\",\"blocks\":[";
        json += String.join(",", blocks.stream().map(s -> "\"" + s + "\"").toList());
        json += "],";
        json += "\"operator\":\"" + operator + "\",";
        json += "\"threshold\":" + threshold + "}";

        return json;
    }

    private String genBlockRemoved(String dimension, String biome) {
        String operator = random.nextBoolean() ? ">" : "<";
        long threshold = 5 + random.nextInt(76);

        String json = "{\"condition\":\"BlockRemoved\",\"blocks\":[],";
        json += "\"operator\":\"" + operator + "\",";
        json += "\"threshold\":" + threshold + "}";

        return json;
    }

    private String genWhitelist(String dimension, String biome) {
        boolean isBlacklist = random.nextDouble() < 0.25;
        int nBlocks = 1 + random.nextInt(4);
        List<String> blocks = pickBlocks(biome, nBlocks, true);
        
        String json = "{\"condition\":\"Whitelist\",\"blocks\":[";
        json += String.join(",", blocks.stream().map(s -> "\"" + s + "\"").toList());
        json += "],";
        json += "\"isBlacklist\":" + isBlacklist + "}";

        return json;
    }

    private String genBlockModified(String dimension, String biome) {
        String operator = random.nextBoolean() ? ">" : "<";
        long threshold = 10 + random.nextInt(291);

        String json = "{\"condition\":\"BlockModified\",";
        json += "\"operator\":\"" + operator + "\",";
        json += "\"threshold\":" + threshold + "}";

        return json;
    }

    private String genDensity(String dimension, String biome) {
        String operator = random.nextBoolean() ? ">" : "<";
        float threshold;
        if(operator == ">") threshold = Math.round((0.1 + random.nextDouble() * 0.45) * 100.0) / 100.0f;
        else threshold = Math.round((0.30 + random.nextDouble() * 0.40) * 100.0) / 100.0f;

        String json = "{\"condition\":\"Density\",";
        json += "\"operator\":\"" + operator + "\",";
        json += "\"threshold\":" + threshold + "}";

        return json;
    }

    private String genTotalVolume(String dimension, String biome) {
        String operator = random.nextBoolean() ? ">" : "<";
        long threshold = 50 + random.nextInt(1151);

        String json = "{\"condition\":\"TotalVolume\",";
        json += "\"operator\":\"" + operator + "\",";
        json += "\"threshold\":" + threshold + "}";

        return json;
    }

    private int[] elevationRangeFor(ResourceKey<Level> dimensionKey) {
        ServerLevel dimLevel = server.getLevel(dimensionKey);
        return new int[] { dimLevel.getMinBuildHeight(), dimLevel.getMaxBuildHeight() };
    }

    private String genElevation(String dimension, String biome) {
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimension));
        int[] range = elevationRangeFor(dimensionKey);
        int lo = range[0] + 30;
        int hi = range[1] - 30;
        if (lo >= hi) {
            lo = range[0];
            hi = range[1];
        }
        long threshold = lo + random.nextInt(hi - lo + 1);
        String operator = random.nextBoolean() ? ">" : "<";

        String json = "{\"condition\":\"Elevation\",";
        json += "\"operator\":\"" + operator + "\",";
        json += "\"threshold\":" + threshold + "}";

        return json;
    }

    private String genLength(String dimension, String biome) {
        String operator = random.nextBoolean() ? ">" : "<";
        long threshold = 4 + random.nextInt(17);

        String json = "{\"condition\":\"Length\",";
        json += "\"operator\":\"" + operator + "\",";
        json += "\"threshold\":" + threshold + "}";

        return json;
    }

    private String genTallness(String dimension, String biome) {
        String operator = random.nextBoolean() ? ">" : "<";
        long threshold = 3 + random.nextInt(18);

        String json = "{\"condition\":\"Tallness\",";
        json += "\"operator\":\"" + operator + "\",";
        json += "\"threshold\":" + threshold + "}";

        return json;
    }

    private String genDimensionCondition(String dimension, boolean hasBiomeCondition) {
        List<String> dims = new ArrayList<>();
        dims.add(dimension);
        if(!hasBiomeCondition && random.nextDouble() < 0.2) {
            List<String> others = DIMENSION_WEIGHTS.keySet().stream().filter(d -> !d.equals(dimension)).toList();
            dims.add(others.get(random.nextInt(others.size())));
        }

        String json = "{\"condition\":\"Dimension\",\"dimensions\":[";
        json += String.join(",", dims.stream().map(s -> "\"" + s + "\"").toList());
        json += "]}";

        return json;
    }

    private String genBiomeCondition(String biome) {
        String json = "{\"condition\":\"Biome\",\"biomes\":[\"" + biome + "\"]}";

        return json;
    }

    private boolean hasConflict(List<String> conditions, String candidate, String dimension) {
        JsonObject candidateJson = JsonParser.parseString(candidate).getAsJsonObject();
        String candidateType = candidateJson.get("condition").getAsString();

        // Ensure only one condition per type for specific conditions
        if (SINGLE_INSTANCE_TYPES.contains(candidateType)) {
            if (conditions.stream().anyMatch(c ->
                    JsonParser.parseString(c).getAsJsonObject()
                            .get("condition").getAsString().equals(candidateType))) {
                return true;
            }
        }

        // BlockModified and TotalVolume are mutually exclusive
        if (candidateType.equals("BlockModified")) {
            if (conditions.stream().anyMatch(c ->
                    JsonParser.parseString(c).getAsJsonObject()
                            .get("condition").getAsString().equals("TotalVolume"))) return true;
        }

        if (candidateType.equals("TotalVolume")) {
            if (conditions.stream().anyMatch(c ->
                    JsonParser.parseString(c).getAsJsonObject()
                            .get("condition").getAsString().equals("BlockModified"))) return true;
        }

        // BlockAdded/Removed operator checks
        if (candidateType.equals("BlockAdded") || candidateType.equals("BlockRemoved")) {

            List<JsonObject> sameType = conditions.stream()
                    .map(c -> JsonParser.parseString(c).getAsJsonObject())
                    .filter(c -> c.get("condition").getAsString().equals(candidateType))
                    .toList();

            if (!sameType.isEmpty()) {
                boolean candidateGreaterThan =
                        candidateJson.get("operator").getAsString().equals(">");

                long candidateThreshold =
                        candidateJson.get("threshold").getAsLong();

                for (JsonObject existing : sameType) {
                    boolean existingGreaterThan = existing.get("operator").getAsString().equals(">");

                    long existingThreshold = existing.get("threshold").getAsLong();

                    if (candidateGreaterThan == existingGreaterThan) return true;

                    if (!candidateGreaterThan && existingGreaterThan) {
                        if (existingThreshold >= candidateThreshold) return true;
                    } else if (candidateGreaterThan && !existingGreaterThan) {
                        if (candidateThreshold >= existingThreshold) return true;
                    }
                }
            }
        }

        // Whitelist/BlockAdded block congruency;
        // two of the same condition cannot have the same blocks, and Whitelist vs BlockAdded cannot clash.
        if (candidateType.equals("Whitelist") || candidateType.equals("BlockAdded")) {

            Set<String> candidateBlocks = new HashSet<>();

            for (JsonElement element : candidateJson.getAsJsonArray("blocks")) {
                candidateBlocks.add(element.getAsString());
            }

            for (String c : conditions) {
                JsonObject existing = JsonParser.parseString(c).getAsJsonObject();
                String existingType = existing.get("condition").getAsString();

                if (!existingType.equals("Whitelist") &&
                    !existingType.equals("BlockAdded")) {
                    continue;
                }

                for (JsonElement element : existing.getAsJsonArray("blocks")) {
                    if (candidateBlocks.contains(element.getAsString())) {
                        return true;
                    }
                }
            }
        }

        // Bunch of constraint checks regarding length, height, volume etc. These need a bounds mapping.
        Map<String, double[]> bounds = new HashMap<>();

        for (String condition : conditions) {
            JsonObject json = JsonParser.parseString(condition).getAsJsonObject();

            // Some conditions don't have numeric constraints
            if (!json.has("operator") || !json.has("threshold"))
                continue;

            String type = json.get("condition").getAsString();
            String operator = json.get("operator").getAsString();
            double threshold = json.get("threshold").getAsDouble();

            double[] bound = bounds.computeIfAbsent(type,
                    k -> new double[]{
                            Double.NEGATIVE_INFINITY,
                            Double.POSITIVE_INFINITY
                    });

            switch (operator) {
                case ">" -> bound[0] = Math.max(bound[0], threshold);
                case ">=" -> bound[0] = Math.max(bound[0], threshold);
                case "<" -> bound[1] = Math.min(bound[1], threshold);
                case "<=" -> bound[1] = Math.min(bound[1], threshold);
            }
        }

        // BlockAdded > BlockModified not allowed, Same with BlockRemoved
        if (bounds.containsKey("BlockAdded") && bounds.containsKey("BlockModified")
            && bounds.get("BlockAdded")[0] > bounds.get("BlockModified")[1])
            return true;
        if (bounds.containsKey("BlockRemoved") && bounds.containsKey("BlockModified")
            && bounds.get("BlockRemoved")[0] > bounds.get("BlockModified")[1])
            return true;

        // BlockAdded > TotalVolume not allowed, Same with BlockRemoved
        if (bounds.containsKey("BlockAdded") && bounds.containsKey("TotalVolume")
            && bounds.get("BlockAdded")[0] > bounds.get("TotalVolume")[1])
            return true;
        if (bounds.containsKey("BlockRemoved") && bounds.containsKey("TotalVolume")
            && bounds.get("BlockRemoved")[0] > bounds.get("TotalVolume")[1])
            return true;

        // BlockModified > TotalVolume not allowed
        if (bounds.containsKey("BlockModified") && bounds.containsKey("TotalVolume")
            && bounds.get("BlockModified")[0] > bounds.get("TotalVolume")[1])
            return true;

        // Density and BlockModified/Volume must be congruent.
        if (bounds.containsKey("Density") && bounds.containsKey("BlockModified") && bounds.containsKey("TotalVolume"))
        {
            if(bounds.get("Density")[0] <= bounds.get("BlockModified")[0] / bounds.get("TotalVolume")[1]) return true;
            if(bounds.get("Density")[1] >= bounds.get("BlockModified")[1] / bounds.get("TotalVolume")[0]) return true;
        }

        // Tallness, Length and Volume must be congruent.
        if (bounds.containsKey("Volume") && bounds.containsKey("Length") && bounds.containsKey("Tallness")
            && bounds.get("Length")[0] * bounds.get("Tallness")[0] > bounds.get("Volume")[1])
            return true;

        // BlockAdded, BlockRemoved and BlockModified must be congruent.
        if (bounds.containsKey("BlockAdded") && bounds.containsKey("BlockRemoved") && bounds.containsKey("BlockModified")) {
            if(bounds.get("BlockModified")[0] >= bounds.get("BlockAdded")[1] / bounds.get("BlockRemoved")[1]) return true;
            if(2 * bounds.get("BlockModified")[0] <= bounds.get("BlockAdded")[0] / bounds.get("BlockRemoved")[0]) return true;
        }

        // Elevation and tallness must be congruent with the build heights.
        if (bounds.containsKey("Elevation") && bounds.containsKey("Tallness")) {
            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimension));
            int[] range = elevationRangeFor(dimensionKey);
            if(bounds.get("Elevation")[0] + bounds.get("Tallness")[0] < range[1]) return true;
            if(bounds.get("Elevation")[1] - bounds.get("Tallness")[0] < range[0]) return true;
        }

        // No Conflicts
        return false;
    }

    private void generate() {
        // Roll dimension
        String dimensionId = weightedChoice(DIMENSION_WEIGHTS);
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimensionId));

        // Roll biome
        List<ResourceKey<Biome>> biomes = getBiomesForDimension(dimensionKey);
        String biomeId = biomes.get(random.nextInt(biomes.size())).location().toString();

        boolean includeDimensionCondition = random.nextDouble() < 0.5;
        boolean includeBiomeCondition = random.nextDouble() < 0.5;

        // Condition pool
        Map<String, Double> poolWeights = new LinkedHashMap<>(CONDITION_WEIGHTS);
        if(dimensionId.equals("minecraft:the_nether")) poolWeights.remove("Elevation");
        if(CAVE_BIOMES.contains(biomeId) || SlOPES_AND_PEAKS_BIOMES.contains(biomeId)) poolWeights.remove("Elevation");

        // Draw n conditions, a mean of around 5, clamped from 1 to 10
        int nConditions = Math.min(10, Math.max(1, (int) Math.round(random.nextGaussian() * 1.5 + 5)));

        // Construct the conditions
        List<String> conditions = new ArrayList<>();
        if(includeDimensionCondition) conditions.add(genDimensionCondition(dimensionId, includeBiomeCondition));
        if(includeBiomeCondition) conditions.add(genBiomeCondition(biomeId));

        int remaining = nConditions - conditions.size();
        int maxTries = remaining * 10;
        int tries = 0;

        while(conditions.size() < nConditions && tries < maxTries) {
            tries++;
            String conditionType = weightedChoice(poolWeights);
            String candidate = CONDITION_GENERATORS.get(conditionType).apply(dimensionId, biomeId);
            if(!hasConflict(conditions, candidate, dimensionId)) {
                conditions.add(candidate);
            }
        }

        Collections.shuffle(conditions, random);

        // Construct the final commission
        String id = "daily_" + date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String title = "Daily Commission, " + date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        
        this.id = id;
        this.title = title;

        JsonArray array = new JsonArray();
        conditions.forEach(c -> array.add(JsonParser.parseString(c)));
        this.conditionJson = array.toString();

    }

}
