package com.pointlessbuilding.journal.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.Registration;
import com.pointlessbuilding.journal.items.BuildersCompass;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class DraftingTableEntity extends BlockEntity {

    public static final String ITEMS_TAG = "Inventory";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public static int SLOT_COUNT = 2;
    public static final int COMPASS_SLOT = 0;
    public static final int BLUEPRINT_SLOT = 1;

    private final ItemStackHandler items = createItemHandler();
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private boolean processing = false;

    public DraftingTableEntity(BlockPos pos, BlockState state) { 
        super(Registration.DRAFTING_TABLE_ENTITY.get(), pos, state);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveClientData(tag);
    }

    private void saveClientData(CompoundTag tag) {
        tag.put(ITEMS_TAG, items.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadClientData(tag);
    }

    private void loadClientData(CompoundTag tag) {
        if(tag.contains(ITEMS_TAG)) {
            items.deserializeNBT(tag.getCompound(ITEMS_TAG));
        }
    }

    // These two overrides happen when chunk is loaded for the first time

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveClientData(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if(tag != null) loadClientData(tag);
    }

    // These two overrides are called whenever block needs updating

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if(tag != null) loadClientData(tag);
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

    // This is the Capability for this block.
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        else {
            return super.getCapability(cap, side);
        }
    }

}
