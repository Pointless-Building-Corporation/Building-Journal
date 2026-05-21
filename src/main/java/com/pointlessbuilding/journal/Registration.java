package com.pointlessbuilding.journal;

import com.pointlessbuilding.journal.blocks.AnchorBlock;
import com.pointlessbuilding.journal.blocks.BlueprintRack;
import com.pointlessbuilding.journal.blocks.BlueprintRackContainer;
import com.pointlessbuilding.journal.blocks.BlueprintRackEntity;
import com.pointlessbuilding.journal.blocks.DraftingTable;
import com.pointlessbuilding.journal.blocks.DraftingTableEntity;
import com.pointlessbuilding.journal.items.BuildersCompass;
import com.pointlessbuilding.journal.menu.JournalContainer;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registration {
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BuildingJournal.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BuildingJournal.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BuildingJournal.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, BuildingJournal.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BuildingJournal.MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BuildingJournal.MODID);

    public static final RegistryObject<AnchorBlock> ANCHOR_BLOCK = BLOCKS.register("anchor_block", AnchorBlock::new);
    public static final RegistryObject<Item> ANCHOR_BLOCK_ITEM = ITEMS.register("anchor_block", () -> new BlockItem(ANCHOR_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<DraftingTable> DRAFTING_TABLE = BLOCKS.register("drafting_table", DraftingTable::new);
    public static final RegistryObject<Item> DRAFTING_TABLE_ITEM = ITEMS.register("drafting_table", () -> new BlockItem(DRAFTING_TABLE.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<DraftingTableEntity>> DRAFTING_TABLE_ENTITY = BLOCK_ENTITIES.register("drafting_table",
        () -> BlockEntityType.Builder.of(DraftingTableEntity::new, DRAFTING_TABLE.get()).build(null)
    );

    public static final RegistryObject<Item> BUILDERS_COMPASS = ITEMS.register("builders_compass", () -> new BuildersCompass(new Item.Properties().stacksTo(1).setNoRepair()));

    public static final RegistryObject<BlueprintRack> BLUEPRINT_RACK = BLOCKS.register("blueprint_rack", BlueprintRack::new);
    public static final RegistryObject<Item> BLUEPRINT_RACK_ITEM = ITEMS.register("blueprint_rack", () -> new BlockItem(BLUEPRINT_RACK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<BlueprintRackEntity>> BLUEPRINT_RACK_ENTITY = BLOCK_ENTITIES.register("blueprint_rack",
        () -> BlockEntityType.Builder.of(BlueprintRackEntity::new, BLUEPRINT_RACK.get()).build(null)
    );
    public static final RegistryObject<MenuType<BlueprintRackContainer>> BLUEPRINT_RACK_CONTAINER = MENU_TYPES.register("blueprint_rack",
        () -> IForgeMenuType.create((windowId, inv, data) -> new BlueprintRackContainer(windowId, inv.player, data.readBlockPos()))
    );

    public static final RegistryObject<MenuType<JournalContainer>> JOURNAL_CONTAINER = MENU_TYPES.register("journal_menu",
        () -> IForgeMenuType.create((windowId, inv, data) -> new JournalContainer(windowId, inv.player))
    );

    public static RegistryObject<CreativeModeTab> TAB = TABS.register("building_journal", () -> CreativeModeTab.builder()
        .title(Component.translatable("tab.buildingjournal"))
        .icon(() -> new ItemStack(DRAFTING_TABLE.get()))
        .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
        .displayItems((featureFlags, output) -> {
            output.accept(ANCHOR_BLOCK.get());
            output.accept(DRAFTING_TABLE.get());
            output.accept(BLUEPRINT_RACK.get());
            output.accept(BUILDERS_COMPASS.get());
        })
        .build());

    // Sound registration
    public static final RegistryObject<SoundEvent> COMPASS_CLICK = SOUNDS.register("compass_click",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BuildingJournal.MODID, "compass_click"))
    );
    public static final RegistryObject<SoundEvent> COMPASS_CLACK = SOUNDS.register("compass_clack",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BuildingJournal.MODID, "compass_clack"))
    );
    public static final RegistryObject<SoundEvent> COMPASS_ERROR = SOUNDS.register("compass_error",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BuildingJournal.MODID, "compass_error"))
    );

    public static void init(IEventBus modEventBus) {
        // Register deffered register suppliers for blocks, block items, block entities
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        TABS.register(modEventBus);
        SOUNDS.register(modEventBus);
    }

}
