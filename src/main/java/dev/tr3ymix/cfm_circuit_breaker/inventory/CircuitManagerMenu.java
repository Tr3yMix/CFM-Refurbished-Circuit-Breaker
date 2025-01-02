package dev.tr3ymix.cfm_circuit_breaker.inventory;

import com.mrcrayfish.furniture.refurbished.blockentity.IPowerSwitch;
import com.mrcrayfish.furniture.refurbished.inventory.IPowerSwitchMenu;
import com.mrcrayfish.furniture.refurbished.inventory.SimpleContainerMenu;
import dev.tr3ymix.cfm_circuit_breaker.core.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CircuitManagerMenu extends SimpleContainerMenu implements IPowerSwitchMenu {
    private final ContainerData data;

    public CircuitManagerMenu(int id, Inventory playerInventory) {
        this(id, playerInventory,new SimpleContainer(0), new SimpleContainerData(6));
    }

    public CircuitManagerMenu(int id,Inventory playerInventory ,Container container, ContainerData data) {
        super(ModMenuTypes.CIRCUIT_BREAKER_MENU.get(), id, container);
        checkContainerSize(container, 0);
        checkContainerDataCount(data, 6);
        container.startOpen(playerInventory.player);
        this.data = data;
        this.addDataSlots(data);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return null;
    }


    public int getEnergy() {
        return this.data.get(0);
    }

    public int getMaxEnergy() {
        return this.data.get(1);
    }

    public boolean isEnabled() {
        return this.data.get(2) != 0;
    }

    public boolean isOverloaded() {
        return this.data.get(3) != 0;
    }

    public boolean isPowered() {
        return this.data.get(4) != 0;
    }

    public int getNodeCount() {
        return this.data.get(5);
    }

    public void toggle() {
        if (this.container instanceof IPowerSwitch powerSwitch) {
            powerSwitch.togglePower();
        }
    }
}
