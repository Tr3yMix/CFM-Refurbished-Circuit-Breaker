package dev.tr3ymix.cfm_circuit_breaker;

import com.mrcrayfish.furniture.refurbished.core.ModCreativeTabs;
import dev.tr3ymix.cfm_circuit_breaker.block.CircuitBreakerBlock;
import dev.tr3ymix.cfm_circuit_breaker.client.gui.screen.CircuitBreakerScreen;
import dev.tr3ymix.cfm_circuit_breaker.client.renderer.blockentity.CircuitBreakerBlockEntityRenderer;
import dev.tr3ymix.cfm_circuit_breaker.core.ModBlockEntities;
import dev.tr3ymix.cfm_circuit_breaker.core.ModBlocks;
import dev.tr3ymix.cfm_circuit_breaker.core.ModItems;
import dev.tr3ymix.cfm_circuit_breaker.core.ModMenuTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CFM_Circuit_Breaker_Mod.MOD_ID)
public class CFM_Circuit_Breaker_Mod
{

    public static final String MOD_ID = "cfm_circuit_breaker";
    private static final Logger LOGGER = LogUtils.getLogger();



    public CFM_Circuit_Breaker_Mod(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);


        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerCapabilities);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.CIRCUIT_BREAKER.get(),
                (entity, context) -> {
                    BlockState state = entity.getBlockState();
                    if(state.hasProperty(CircuitBreakerBlock.DIRECTION)){
                        return context == state.getValue(CircuitBreakerBlock.DIRECTION) ?
                                entity.getEnergyHandler().get() : null;

                    }
                    return null;
                });
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if(event.getTab().equals(ModCreativeTabs.MAIN.get())){
            event.accept(ModBlocks.CIRCUIT_BREAKER.get());
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {


        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> {
                BlockEntityRenderers.register(
                        ModBlockEntities.CIRCUIT_BREAKER.get(),
                        CircuitBreakerBlockEntityRenderer::new
                );
            });
        }
        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event){
            event.register(ModMenuTypes.CIRCUIT_BREAKER_MENU.get(), CircuitBreakerScreen::new);
        }


    }
}
