package dev.tr3ymix.cfm_circuit_manager.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.furniture.refurbished.block.ElectricityGeneratorBlock;
import com.mrcrayfish.furniture.refurbished.block.FurnitureHorizontalEntityBlock;
import com.mrcrayfish.furniture.refurbished.block.MetalType;
import com.mrcrayfish.furniture.refurbished.blockentity.ElectricityGeneratorBlockEntity;
import com.mrcrayfish.furniture.refurbished.data.tag.BlockTagSupplier;
import dev.tr3ymix.cfm_circuit_manager.blockentity.CircuitManagerBlockEntity;
import dev.tr3ymix.cfm_circuit_manager.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CircuitManagerBlock extends FurnitureHorizontalEntityBlock implements BlockTagSupplier {


    public CircuitManagerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(DIRECTION, Direction.NORTH).setValue(POWERED, false));

    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    protected Map<BlockState, VoxelShape> generateShapes(ImmutableList<BlockState> states) {
        return ImmutableMap.copyOf(states.stream().collect(Collectors.toMap((state) -> state, (o) -> Shapes.block())));
    }

    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                                     @NotNull Player player, @NotNull BlockHitResult result) {
        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof CircuitManagerBlockEntity circuitManager) {
                player.openMenu(circuitManager);
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.SUCCESS;
    }
protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(POWERED);
}

    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CircuitManagerBlockEntity(pos, state);
    }

    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return level.isClientSide() ? createTicker(type, ModBlockEntities.CIRCUIT_MANAGER.get(), CircuitManagerBlockEntity::clientTick) : null;
    }

    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource source) {
        if (state.getValue(POWERED)) {
            Direction direction = state.getValue(DIRECTION);
            Vec3 vec = (new Vec3(3.5, 16.0, 3.5)).scale(0.0625);
            vec = vec.yRot((float)direction.get2DDataValue() * -1.5707964F - 1.5707964F);
            vec = vec.add(pos.getX(), pos.getY(), pos.getZ()).add(0.5, 0.0, 0.5);
            level.addParticle(ParticleTypes.SMOKE, vec.x, vec.y, vec.z, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.SMOKE, vec.x, vec.y, vec.z, 0.0, 0.0, 0.0);
        }

    }

    public List<TagKey<Block>> getTags() {
        return List.of(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL);
    }
}
