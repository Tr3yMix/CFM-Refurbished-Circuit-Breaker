package dev.tr3ymix.cfm_circuit_manager.core;

import dev.tr3ymix.cfm_circuit_manager.CFM_Circuit_Manager_Mod;
import dev.tr3ymix.cfm_circuit_manager.blockentity.CircuitManagerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CFM_Circuit_Manager_Mod.MOD_ID);

    @SuppressWarnings("DataFlowIssue")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CircuitManagerBlockEntity>> CIRCUIT_MANAGER =
            BLOCK_ENTITY_TYPES.register("circuit_manager", () ->
                    BlockEntityType.Builder.of(CircuitManagerBlockEntity::new, ModBlocks.CIRCUIT_MANAGER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
