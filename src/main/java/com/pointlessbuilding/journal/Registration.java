package com.pointlessbuilding.journal;

import java.util.function.Supplier;

import com.pointlessbuilding.journal.blocks.DraftingTable;
import com.pointlessbuilding.journal.blocks.DraftingTableEntity;
import com.pointlessbuilding.journal.commission.CommissionCompleteTrigger;
import com.pointlessbuilding.journal.commission.CommissionState;
import com.pointlessbuilding.journal.items.Blueprint;
import com.pointlessbuilding.journal.items.BuildersCompass;
import com.pointlessbuilding.journal.menu.DraftingTableContainer;
import com.pointlessbuilding.journal.menu.CommissionContainer;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Registration {
    
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BuildingJournal.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BuildingJournal.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BuildingJournal.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, BuildingJournal.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, BuildingJournal.MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, BuildingJournal.MODID);
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES = DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, BuildingJournal.MODID);

    public static final DeferredHolder<Block, DraftingTable> DRAFTING_TABLE = BLOCKS.register("drafting_table", DraftingTable::new);
    public static final DeferredItem<Item> DRAFTING_TABLE_ITEM = ITEMS.registerSimpleItem("drafting_table", new Item.Properties());
    public static final Supplier<BlockEntityType<DraftingTableEntity>> DRAFTING_TABLE_ENTITY = BLOCK_ENTITIES.register("drafting_table",
        () -> BlockEntityType.Builder.of(DraftingTableEntity::new, DRAFTING_TABLE.get()).build(null)
    );
    public static final Supplier<MenuType<DraftingTableContainer>> DRAFTING_TABLE_CONTAINER = MENU_TYPES.register("drafting_table",
        () -> IMenuTypeExtension.create((windowId, inv, data) -> new DraftingTableContainer(windowId, inv.player, data.readBlockPos()))
    );

    public static final DeferredItem<Item> BUILDERS_COMPASS = ITEMS.register("builders_compass", () -> new BuildersCompass(new Item.Properties().stacksTo(1).setNoRepair()));
    public static final DeferredItem<Item> BLUEPRINT = ITEMS.register("blueprint", () -> new Blueprint(new Item.Properties().stacksTo(1).setNoRepair()));

    public static final Supplier<MenuType<CommissionContainer>> COMMISSION_CONTAINER = MENU_TYPES.register("commission_menu",
        () -> IMenuTypeExtension.create((windowId, inv, data) -> {
            String commissionId = data.readUtf();
            String title = data.readUtf();
            CommissionState state = data.readEnum(CommissionState.class);
            String conditionsJson = data.readUtf();
            String unlocksJson = data.readUtf();
            int commissionPage = data.readInt();
            return new CommissionContainer(windowId, inv.player, commissionId, title, state, conditionsJson, unlocksJson, commissionPage);
        })
    );

    public static Supplier<CreativeModeTab> TAB = TABS.register("building_journal", () -> CreativeModeTab.builder()
        .title(Component.translatable("tab.buildingjournal"))
        .icon(() -> new ItemStack(DRAFTING_TABLE.get()))
        .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
        .displayItems((featureFlags, output) -> {
            output.accept(DRAFTING_TABLE.get());
            output.accept(BUILDERS_COMPASS.get());
            output.accept(BLUEPRINT.get());
        })
        .build());

    // Sound registration
    public static final Supplier<SoundEvent> COMPASS_CLICK = SOUNDS.register("compass_click",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "compass_click"))
    );
    public static final Supplier<SoundEvent> COMPASS_CLACK = SOUNDS.register("compass_clack",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "compass_clack"))
    );
    public static final Supplier<SoundEvent> COMPASS_ERROR = SOUNDS.register("compass_error",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BuildingJournal.MODID, "compass_error"))
    );

    public static final Supplier<CommissionCompleteTrigger> COMMISSION_COMPLETE_TRIGGER = TRIGGER_TYPES.register("commission_completed", CommissionCompleteTrigger::new);

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        TABS.register(modEventBus);
        SOUNDS.register(modEventBus);
        TRIGGER_TYPES.register(modEventBus);
    }

}
