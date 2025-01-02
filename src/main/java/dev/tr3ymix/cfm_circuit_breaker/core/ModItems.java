package dev.tr3ymix.cfm_circuit_breaker.core;

import dev.tr3ymix.cfm_circuit_breaker.CFM_Circuit_Breaker_Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CFM_Circuit_Breaker_Mod.MOD_ID);


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
