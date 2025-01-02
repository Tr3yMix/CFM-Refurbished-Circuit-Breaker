package dev.tr3ymix.cfm_circuit_breaker.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mrcrayfish.furniture.refurbished.block.FurnitureHorizontalEntityBlock;
import com.mrcrayfish.furniture.refurbished.data.tag.BlockTagSupplier;
import dev.tr3ymix.cfm_circuit_breaker.blockentity.CircuitBreakerBlockEntity;
import dev.tr3ymix.cfm_circuit_breaker.util.voxel.CircuitBreakerBlockShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CircuitBreakerBlock extends FurnitureHorizontalEntityBlock implements BlockTagSupplier {


    public CircuitBreakerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(DIRECTION, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(OPEN, false));

    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    protected Map<BlockState, VoxelShape> generateShapes(ImmutableList<BlockState> states) {
        return ImmutableMap.copyOf(states.stream().collect(Collectors.toMap((state) -> state, (o) -> Shapes.block())));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return CircuitBreakerBlockShapes.getShape(state.getValue(DIRECTION));
    }

    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                     @NotNull Player player, @NotNull BlockHitResult result) {
        super.useWithoutItem(state, level, pos, player, result);
        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof CircuitBreakerBlockEntity circuitManager) {

                player.openMenu(circuitManager);
                return InteractionResult.CONSUME;
            }

        }

        return InteractionResult.SUCCESS;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
        builder.add(OPEN);
    }

    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CircuitBreakerBlockEntity(pos, state);
    }

    public List<TagKey<Block>> getTags() {
        return List.of(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction faceDirection = context.getClickedFace();
        if(faceDirection == Direction.DOWN || faceDirection == Direction.UP) {
            return null;
        }
        Direction direction = context.getHorizontalDirection();
        return this.defaultBlockState().setValue(DIRECTION, direction);

    }
}
