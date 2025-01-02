package dev.tr3ymix.cfm_circuit_breaker.core;

import dev.tr3ymix.cfm_circuit_breaker.CFM_Circuit_Breaker_Mod;
import dev.tr3ymix.cfm_circuit_breaker.blockentity.CircuitBreakerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CFM_Circuit_Breaker_Mod.MOD_ID);

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CircuitBreakerBlockEntity>> CIRCUIT_BREAKER =
            BLOCK_ENTITY_TYPES.register("circuit_breaker", () ->
                    BlockEntityType.Builder.of(CircuitBreakerBlockEntity::new, ModBlocks.CIRCUIT_BREAKER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
