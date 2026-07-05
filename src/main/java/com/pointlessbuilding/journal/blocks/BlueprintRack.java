package com.pointlessbuilding.journal.blocks;

import org.jetbrains.annotations.Nullable;

import com.pointlessbuilding.journal.menu.BlueprintRackContainer;

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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

public class BlueprintRack extends Block implements EntityBlock{

    public static final String BLUEPRINT_RACK_UI_TITLE = "screen.buildingjournal.blueprint_rack";
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    // Back side of the rack
    public static final VoxelShape SHAPE_BACK_N = Block.box(0.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);
    public static final VoxelShape SHAPE_BACK_S = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
    public static final VoxelShape SHAPE_BACK_W = Block.box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    public static final VoxelShape SHAPE_BACK_E = Block.box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 16.0D);

    // Sides of the rack
    public static final VoxelShape SHAPE_SIDE_NS = Shapes.or(Block.box(0.0D, 0.0D, 0.0D, 2.0D, 11.0D, 16.0D), Block.box(14.0D, 0.0D, 0.0D, 16.0D, 11.0D, 16.0D));
    public static final VoxelShape SHAPE_SIDE_EW = Shapes.or(Block.box(0.0D, 0.0D, 14.0D, 16.0D, 11.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 11.0D, 2.0D));

    public static final VoxelShape SHAPE_BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public BlueprintRack() {
        super(BlockBehaviour.Properties.of()
            .strength(3.5f) // Average block strength
            .noOcclusion()  // This ensures the block won't occlude other blocks
            .sound(SoundType.WOOD)
        );
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return returnGeneralShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return returnGeneralShape(state);
    }

    protected VoxelShape returnGeneralShape(BlockState state) {
        switch ((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH:
                return Shapes.or(SHAPE_BACK_N, SHAPE_SIDE_NS, SHAPE_BASE);
            case SOUTH:
                return Shapes.or(SHAPE_BACK_S, SHAPE_SIDE_NS, SHAPE_BASE);
            case EAST:
                return Shapes.or(SHAPE_BACK_E, SHAPE_SIDE_EW, SHAPE_BASE);
            case WEST:
                return Shapes.or(SHAPE_BACK_W, SHAPE_SIDE_EW, SHAPE_BASE);
            default:
                return Shapes.or(SHAPE_BACK_N, SHAPE_SIDE_NS, SHAPE_BASE);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlueprintRackEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
        if(!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if(be instanceof BlueprintRackEntity) {
                MenuProvider containerProvider = new MenuProvider() {

                    @Override
                    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                        return new BlueprintRackContainer(containerId, player, pos);
                    }

                    @Override
                    public Component getDisplayName() {
                        return Component.translatable(BLUEPRINT_RACK_UI_TITLE);
                    }
                };
                NetworkHooks.openScreen((ServerPlayer) player, containerProvider, be.getBlockPos());
            }
            else {
                throw new IllegalStateException("Blueprint Rack Container Entity missing!");
            }
        }
        return InteractionResult.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!state.is(newState.getBlock())) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof BlueprintRackEntity rack) {
                ItemStackHandler handler = rack.getItems();
                for(int i = 0; i < handler.getSlots(); i++) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(i));
                }
            }

         super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    // Set the block state when a player places the block down, i.e if it's facing a certain way, default states etc.
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite(); // The opposite direction to what the player is facing on the flat plane (no up or down)
        return this.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, direction)
            .setValue(FILLED, false);
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, FILLED);   // 4 X 2 = 8 unique states
    }

}
