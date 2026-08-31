package com.pointlessbuilding.journal.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.items.BuildersCompass;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class DraftingTableEntity extends BlockEntity {

    public static final String ITEMS_TAG = "Inventory";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public static int SLOT_COUNT = 2;
    public static final int COMPASS_SLOT = 0;
    public static final int BLUEPRINT_SLOT = 1;

    private final ItemStackHandler items = createItemHandler();
    private boolean processing = false;

    public DraftingTableEntity(BlockPos pos, BlockState state) { 
        super(Registration.DRAFTING_TABLE_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveClientData(tag, registries);
    }

    private void saveClientData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(ITEMS_TAG, items.serializeNBT(registries));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadClientData(tag, registries);
    }

    private void loadClientData(CompoundTag tag, HolderLookup.Provider registries) {
        if(tag.contains(ITEMS_TAG)) {
            items.deserializeNBT(registries, tag.getCompound(ITEMS_TAG));
        }
    }

    // These two overrides happen when chunk is loaded for the first time

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
    }

    // These two overrides are called whenever block needs updating

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        super.onDataPacket(net, packet, registries);
    }

    @Nonnull
    private ItemStackHandler createItemHandler() {
        return new ItemStackHandler(SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == COMPASS_SLOT) return stack.getItem() instanceof BuildersCompass;
                if(slot == BLUEPRINT_SLOT) return false;
                return super.isItemValid(slot, stack);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        };
    }

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? (processing ? 1 : 0): 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) processing = value != 0;
        }

        @Override
        public int getCount() { return 1; }
    };

    public ContainerData getData() { return data; }

    public ItemStackHandler getItems() {
        return items;
    }

    public Boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean value) {
        processing = value;
    }

}
