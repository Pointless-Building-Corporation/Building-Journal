package com.pointlessbuilding.journal;

import net.minecraftforge.common.ForgeConfigSpec;

public class BuildingJournalConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MAX_BOXES;
    public static final ForgeConfigSpec.IntValue MAX_BOX_SIZE;
    public static final ForgeConfigSpec.BooleanValue USE_BLUEPRINT_SHADER;

    static {
        BUILDER.comment("Builder's Compass Settings");
        MAX_BOXES = BUILDER.comment("Maximum number of stored boundaries").defineInRange("max_boxes",10,1,50);
        MAX_BOX_SIZE = BUILDER.comment("Maximum boundary size in any direction").defineInRange("max_box_size",128,16,256);
        USE_BLUEPRINT_SHADER = BUILDER.comment("Is the custom compass blueprint shader enabled?").define("use_blueprint_shader", true);
        SPEC = BUILDER.build();
    }

}