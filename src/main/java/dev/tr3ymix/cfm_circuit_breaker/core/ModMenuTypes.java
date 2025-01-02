package dev.tr3ymix.cfm_circuit_breaker.core;

import dev.tr3ymix.cfm_circuit_breaker.CFM_Circuit_Breaker_Mod;
import dev.tr3ymix.cfm_circuit_breaker.inventory.CircuitManagerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, CFM_Circuit_Breaker_Mod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CircuitManagerMenu>> CIRCUIT_BREAKER_MENU =
            registerMenuType(CircuitManagerMenu::new, "circuit_breaker_menu");

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType
            (MenuType.MenuSupplier<T> supplier, String name) {
        return MENU_TYPES.register(name, () -> new MenuType<>(supplier, FeatureFlagSet.of()));
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
