package com.pointlessbuilding.journal;

import com.pointlessbuilding.journal.blocks.AnchorBlock;
import com.pointlessbuilding.journal.blocks.BlueprintRack;
import com.pointlessbuilding.journal.blocks.BlueprintRackContainer;
import com.pointlessbuilding.journal.blocks.BlueprintRackEntity;
import com.pointlessbuilding.journal.blocks.ComplexAnchorBlock;
import com.pointlessbuilding.journal.blocks.ComplexAnchorBlockEntity;
import com.pointlessbuilding.journal.menu.JournalContainer;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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

    public static final RegistryObject<AnchorBlock> ANCHOR_BLOCK = BLOCKS.register("anchor_block", AnchorBlock::new);
    public static final RegistryObject<Item> ANCHOR_BLOCK_ITEM = ITEMS.register("anchor_block", () -> new BlockItem(ANCHOR_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<ComplexAnchorBlock> COMPLEX_ANCHOR_BLOCK = BLOCKS.register("complex_anchor_block", ComplexAnchorBlock::new);
    public static final RegistryObject<Item> COMPLEX_ANCHOR_BLOCK_ITEM = ITEMS.register("complex_anchor_block", () -> new BlockItem(COMPLEX_ANCHOR_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<ComplexAnchorBlockEntity>> COMPLEX_ANCHOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("complex_anchor_block",
        () -> BlockEntityType.Builder.of(ComplexAnchorBlockEntity::new, COMPLEX_ANCHOR_BLOCK.get()).build(null)
    );

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
        .icon(() -> new ItemStack(COMPLEX_ANCHOR_BLOCK.get()))
        .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
        .displayItems((featureFlags, output) -> {
            output.accept(ANCHOR_BLOCK.get());
            output.accept(COMPLEX_ANCHOR_BLOCK.get());
            output.accept(BLUEPRINT_RACK.get());
        })
        .build());

    public static void init(IEventBus modEventBus) {
        // Register deffered register suppliers for blocks, block items, block entities
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        TABS.register(modEventBus);
    }

}
