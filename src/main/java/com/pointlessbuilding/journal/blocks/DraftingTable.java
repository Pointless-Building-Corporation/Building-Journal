package com.pointlessbuilding.journal.blocks;

import javax.annotation.Nullable;

import com.pointlessbuilding.journal.menu.DraftingTableContainer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

public class DraftingTable extends Block implements EntityBlock {
    
    public static final String DRAFTING_TABLE_UI_TITLE = "screen.buildingjournal.drafting_table";

    public static final VoxelShape SHAPE_BASE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 15.0D);
    public static final VoxelShape SHAPE_LEGS_NS = Block.box(1.0D, 1.0D, 7.0D, 15.0D, 12.0D, 9.0D);
    public static final VoxelShape SHAPE_LEGS_EW = Block.box(7.0D, 1.0D, 1.0D, 9.0D, 12.0D, 15.0D);
    public static final VoxelShape OCCLUSION_SHAPE_NS = Shapes.or(SHAPE_BASE, SHAPE_LEGS_NS);
    public static final VoxelShape OCCLUSION_SHAPE_EW = Shapes.or(SHAPE_BASE, SHAPE_LEGS_EW);
    public static final VoxelShape TOP_PLATE = Block.box(1.0D, 13.0D, 1.0D, 15.0D, 14.0D, 15.0D);
    public static final VoxelShape COLLISION_SHAPE_NS = Shapes.or(OCCLUSION_SHAPE_NS, TOP_PLATE);
    public static final VoxelShape COLLISION_SHAPE_EW = Shapes.or(OCCLUSION_SHAPE_EW, TOP_PLATE);

    public DraftingTable() {
        super(BlockBehaviour.Properties.of()
            .strength(3.5f)
            .noOcclusion()
            .sound(SoundType.WOOD)
        );
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        switch ((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH:
            case SOUTH:
                return OCCLUSION_SHAPE_NS;
            case EAST:
            case WEST:
                return OCCLUSION_SHAPE_EW;
            default:
                return OCCLUSION_SHAPE_NS;
        }

    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch ((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH:
            case SOUTH:
                return COLLISION_SHAPE_NS;
            case EAST:
            case WEST:
                return COLLISION_SHAPE_EW;
            default:
                return COLLISION_SHAPE_NS;
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DraftingTableEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
        if(!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if(be instanceof DraftingTableEntity) {
                MenuProvider containerProvider = new MenuProvider() {

                    @Override
                    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                        return new DraftingTableContainer(containerId, player, pos);
                    }

                    @Override
                    public Component getDisplayName() {
                        return Component.translatable(DRAFTING_TABLE_UI_TITLE);
                    }
                };
                NetworkHooks.openScreen((ServerPlayer) player, containerProvider, be.getBlockPos());
            }
            else {
                throw new IllegalStateException("Drafting Table Container Entity missing!");
            }
        }
        return InteractionResult.SUCCESS;
    }

    // Apparently in this version even vanilla containers use this deprecated function. Keeping as is.
    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!state.is(newState.getBlock())) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof DraftingTableEntity table) {
                ItemStackHandler handler = table.getItems();
                for(int i = 0; i < handler.getSlots(); i++) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(i));
                }
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite(); // The opposite direction to what the player is facing on the flat plane (no up or down)
        return this.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, direction);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);   // 4 unique states
    }

}
