package com.pointlessbuilding.journal;

import net.neoforged.neoforge.common.ModConfigSpec;

public class BuildingJournalConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MAX_BOXES;
    public static final ModConfigSpec.IntValue MAX_BOX_SIZE;
    public static final ModConfigSpec.BooleanValue USE_BLUEPRINT_SHADER;
    public static final ModConfigSpec.ConfigValue<String> SHADER_VARIANT;
    
    static {
        BUILDER.comment("Builder's Compass Settings");
        MAX_BOXES = BUILDER.comment("Maximum number of stored boundaries").defineInRange("max_boxes",10,1,50);
        MAX_BOX_SIZE = BUILDER.comment("Maximum boundary size in any direction").defineInRange("max_box_size",128,16,256);
        USE_BLUEPRINT_SHADER = BUILDER.comment("Is the custom compass blueprint shader enabled?").define("use_blueprint_shader", true);
        SHADER_VARIANT = BUILDER.comment("Name of the blueprint shader variant to use").define("shader_variant", "blueprint_shader");
        SPEC = BUILDER.build();
    }

}