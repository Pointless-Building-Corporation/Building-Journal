package com.pointlessbuilding.journal.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.pointlessbuilding.journal.Registration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class BlueprintRackEntity extends BlockEntity{

    public static final String ITEM_TAG = "Inventory";

    public static int SLOT_COUNT = 16;
    public static int SLOT = 0;

    private final ItemStackHandler items = createItemHandler();
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    public BlueprintRackEntity( BlockPos pos, BlockState blockState) {
        super(Registration.BLUEPRINT_RACK_ENTITY.get(), pos, blockState);
    }

    // Load and SaveAdditional
    // These load and save data via NBT Tags.

    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(ITEM_TAG, items.serializeNBT());
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound(ITEM_TAG));
    }

    public ItemStackHandler getItems() {
        return items;
    }

    @Nonnull
    private ItemStackHandler createItemHandler() {
        return new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                boolean filled = false;
                for (int i = 0; i < getSlots(); i++) {
                    if(!getStackInSlot(i).isEmpty()) {
                        filled = true;
                        break;
                    }
                }
                BlockState newState = getBlockState().setValue(BlueprintRack.FILLED, filled);
                level.setBlock(worldPosition, newState, Block.UPDATE_ALL);
            }
        };
    }

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
    
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

}
