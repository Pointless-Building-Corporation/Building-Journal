package com.pointlessbuilding.journal.gui;

import java.util.Collection;
import java.util.List;

import com.pointlessbuilding.journal.BuildingJournalConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigUI extends Screen{

    public static final String CONFIG_UI_TITLE = "screen.buildingjournal.ui_title";

    public static final String CONFIG_UI_MAX_BOXES = "screen.buildingjournal.max_boxes";
    public static final String CONFIG_UI_MAX_BOXES_DESC = "screen.buildingjournal.max_boxes_desc";

    public static final String CONFIG_UI_MAX_BOX_SIZE = "screen.buildingjournal.max_box_size";
    public static final String CONFIG_UI_MAX_BOX_SIZE_DESC = "screen.buildingjournal.max_box_size_desc";

    public static final String CONFIG_UI_USE_BLUEPRINT_SHADER = "screen.buildingjournal.use_blueprint_shader";
    public static final String CONFIG_UI_USE_BLUEPRINT_SHADER_DESC = "screen.buildingjournal.use_blueprint_shader_desc";

    public static final String CONFIG_UI_SHADER_VARIANT = "screen.buildingjournal.shader_variant";
    public static final String CONFIG_UI_SHADER_VARIANT_DESC = "screen.buildingjournal.shader_variant_desc";
    

    private class ConfigSlider extends AbstractSliderButton {

        private int min, max;
        protected ForgeConfigSpec.IntValue configField;

        public ConfigSlider(int x, int y, int width, int height, Component message, ForgeConfigSpec.IntValue configField, double value, int min, int max) {
            super(x, y, width, height, message, (value-min)/(max-min));
            this.min = min;
            this.max = max;
            this.configField = configField;
            applyValue();
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(String.valueOf((int)(value*(max-min)+min))));
        }

        @Override
        protected void applyValue() {
            // Needs to do nothing
        }

        protected void setConfigValue() {
            configField.set((int)(value*(max-min)+min));
        }
        
    }

    ConfigSlider maxBoxesSlider;
    ConfigSlider maxBoxSizeSlider;

    CycleButton<Boolean> useBlueprintShaderButton;
    Boolean useBlueprintShadervalue;

    CycleButton<String> shaderVariantButton;
    String shaderVariantValue;

    private Screen parentScreen;

    private int vertical_padding = 2;
    private int starting_y = 8;

    public ConfigUI(Screen parentScreen) {
        super(Component.translatable(CONFIG_UI_TITLE));
        this.parentScreen = parentScreen;
    }

    @Override
    public void init() {
        this.addRenderableWidget(Button.builder(Component.literal("Back"), btn -> this.onClose())
        .bounds(width/2 - 100, height - 27, 200, 20)
        .build());

        maxBoxesSlider = new ConfigSlider((int)(width * 0.9) - 200, starting_y + 2*(20 + vertical_padding), 200, 20, Component.empty(), BuildingJournalConfig.MAX_BOXES, BuildingJournalConfig.MAX_BOXES.get(), 1, 50);
        this.addRenderableWidget(maxBoxesSlider);

        maxBoxSizeSlider = new ConfigSlider((int)(width * 0.9) - 200, starting_y + 4*(20 + vertical_padding), 200, 20, Component.empty(), BuildingJournalConfig.MAX_BOX_SIZE, BuildingJournalConfig.MAX_BOX_SIZE.get(), 16, 256);
        this.addRenderableWidget(maxBoxSizeSlider);

        useBlueprintShadervalue = BuildingJournalConfig.USE_BLUEPRINT_SHADER.get();
        useBlueprintShaderButton = CycleButton.onOffBuilder(useBlueprintShadervalue)
        .create((int)(width * 0.9) - 200, starting_y + 6*(20 + vertical_padding), 200, 20, Component.literal("Shader"), (button, value) -> {
            this.useBlueprintShadervalue = value;
        });
        this.addRenderableWidget(useBlueprintShaderButton);

        shaderVariantValue = BuildingJournalConfig.SHADER_VARIANT.get();
        shaderVariantButton = CycleButton.builder(Component::literal).withValues(getAvailableShaderVariants()).withInitialValue(shaderVariantValue)
        .create((int)(width * 0.9) - 200, starting_y + 8*(20 + vertical_padding), 200, 20, Component.literal("Shader Variant"), (button, value) -> {
            shaderVariantValue = value;
        });
        this.addRenderableWidget(shaderVariantButton);
    }

    private int labelY(int row) {
        return starting_y + row*(20 + vertical_padding) + (20 - this.font.lineHeight) / 2;
    }

    public static List<String> getAvailableShaderVariants() {
        ResourceManager rm = Minecraft.getInstance().getResourceManager();
        Collection<ResourceLocation> found = rm.listResources("shaders/program/shader_variants", name -> name.getPath().endsWith(".json")).keySet();
        return found.stream().map(rl -> {
            String path = rl.getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            return fileName.substring(0, fileName.length() - ".json".length());
        }).sorted().toList();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        // Title
        guiGraphics.drawString(this.font, Component.translatable(CONFIG_UI_TITLE), (width - this.font.width(this.title.getString())) / 2, starting_y, 0xFFFFFF);
        // Bg
        guiGraphics.fill((int)(width * 0.08), starting_y + 10 + vertical_padding, (int)(width * 0.92), height - 27 - vertical_padding, 0x80000000);

        //
        // Config values
        //

        // MaxBoxes
        guiGraphics.drawString(this.font, Component.translatable(CONFIG_UI_MAX_BOXES_DESC), (int)(width * 0.1), labelY(1), 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable(CONFIG_UI_MAX_BOXES), (int)(width * 0.1), labelY(2), 0xFFFFFF);
        // MaxBoxSize
        guiGraphics.drawString(this.font, Component.translatable(CONFIG_UI_MAX_BOX_SIZE_DESC), (int)(width * 0.1), labelY(3), 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable(CONFIG_UI_MAX_BOX_SIZE), (int)(width * 0.1), labelY(4), 0xFFFFFF);
        // UseBlueprintShader
        guiGraphics.drawString(this.font, Component.translatable(CONFIG_UI_USE_BLUEPRINT_SHADER_DESC), (int)(width * 0.1), labelY(5), 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable(CONFIG_UI_USE_BLUEPRINT_SHADER), (int)(width * 0.1), labelY(6), 0xFFFFFF);
        // ShaderVariant
        guiGraphics.drawString(this.font, Component.translatable(CONFIG_UI_SHADER_VARIANT_DESC), (int)(width * 0.1), labelY(7), 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable(CONFIG_UI_SHADER_VARIANT), (int)(width * 0.1), labelY(8), 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        // Set the config values
        maxBoxesSlider.setConfigValue();
        maxBoxSizeSlider.setConfigValue();
        BuildingJournalConfig.USE_BLUEPRINT_SHADER.set(useBlueprintShadervalue);
        BuildingJournalConfig.SHADER_VARIANT.set(shaderVariantValue);

        BuildingJournalConfig.SPEC.save();
        Minecraft.getInstance().setScreen(parentScreen);
    }

}
