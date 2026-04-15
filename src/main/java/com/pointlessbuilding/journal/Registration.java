package com.pointlessbuilding.journal;

import java.util.function.Supplier;

import com.pointlessbuilding.journal.blocks.AnchorBlock;
import com.pointlessbuilding.journal.blocks.ComplexAnchorBlock;
import com.pointlessbuilding.journal.blocks.ComplexAnchorBlockEntity;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registration {
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BuildingJournal.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BuildingJournal.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BuildingJournal.MODID);

    public static final RegistryObject<AnchorBlock> ANCHOR_BLOCK = BLOCKS.register("anchor_block", AnchorBlock::new);
    public static final RegistryObject<Item> ANCHOR_BLOCK_ITEM = ITEMS.register("anchor_block", () -> new BlockItem(ANCHOR_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<ComplexAnchorBlock> COMPLEX_ANCHOR_BLOCK = BLOCKS.register("complex_anchor_block", ComplexAnchorBlock::new);
    public static final RegistryObject<Item> COMPLEX_ANCHOR_BLOCK_ITEM = ITEMS.register("complex_anchor_block", () -> new BlockItem(COMPLEX_ANCHOR_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<ComplexAnchorBlockEntity>> COMPLEX_ANCHOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("complex_anchor_block",
        () -> BlockEntityType.Builder.of(ComplexAnchorBlockEntity::new, COMPLEX_ANCHOR_BLOCK.get()).build(null)
    );

    public static void init(IEventBus modEventBus) {
        // Register deffered register suppliers for blocks, block items, block entities
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
    }

    static void addCreative(BuildCreativeModeTabContentsEvent event) {
        // For now, adding items to the building blocks tab
        if(event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ANCHOR_BLOCK_ITEM);
            event.accept(COMPLEX_ANCHOR_BLOCK_ITEM);
        }
    }

}
