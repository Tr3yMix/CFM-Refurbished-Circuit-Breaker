package dev.tr3ymix.cfm_circuit_breaker.blockentity;

import com.mrcrayfish.furniture.refurbished.Config;
import com.mrcrayfish.furniture.refurbished.blockentity.*;
import com.mrcrayfish.furniture.refurbished.core.ModSounds;
import com.mrcrayfish.furniture.refurbished.electricity.NodeSearchResult;
import com.mrcrayfish.furniture.refurbished.inventory.BuildableContainerData;
import dev.tr3ymix.cfm_circuit_breaker.block.CircuitBreakerBlock;
import dev.tr3ymix.cfm_circuit_breaker.core.ModBlockEntities;
import dev.tr3ymix.cfm_circuit_breaker.inventory.CircuitManagerMenu;
import dev.tr3ymix.cfm_circuit_breaker.util.ModEnergyStorage;
import dev.tr3ymix.cfm_circuit_breaker.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.jarjar.nio.util.Lazy;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CircuitBreakerBlockEntity extends ElectricitySourceLootBlockEntity implements IProcessingCircuitBreakerBlock, IPowerSwitch{

    public static final int DATA_ENERGY = 0;
    public static final int DATA_MAX_ENERGY = 1;
    public static final int DATA_ENABLED = 2;
    public static final int DATA_OVERLOADED = 3;
    public static final int DATA_POWERED = 4;
    public static final int DATA_NODE_COUNT = 5;
    protected final Vec3 audioPosition;
    protected boolean enabled;
    protected int nodeCount;
    protected final ContainerData data;

    private final ModEnergyStorage ENERGY_STORAGE = new ModEnergyStorage(1000, 50) {
        @Override
        public void onEnergyChanged() {
            setChanged();
        }
    };

    private final Lazy<IEnergyStorage> energyHandler = Lazy.of(() -> ENERGY_STORAGE);

    private static final int ENERGY_REQ = 10;

    public CircuitBreakerBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CIRCUIT_BREAKER.get(), pos, state);
    }

    public CircuitBreakerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1);
        this.data = new BuildableContainerData((builder -> {
            builder.add(DATA_ENERGY, this::getEnergy, (value) -> {
            });

            builder.add(DATA_MAX_ENERGY, this::getMaxEnergy, (value) -> {
            });
            builder.add(DATA_ENABLED, () -> this.enabled ? 1 : 0, (value) -> {
            });
            builder.add(DATA_OVERLOADED, () -> this.overloaded ? 1 : 0, (value) -> {
            });
            builder.add(DATA_POWERED, () -> this.isNodePowered() ? 1 : 0, (value) -> {
            });
            builder.add(DATA_NODE_COUNT, () -> this.nodeCount, (value) -> {
            });
        }));
        this.audioPosition = pos.getCenter().add(0.0, -0.375, 0.0);
    }

    public int getNodeMaximumConnections(){
        return Config.SERVER.electricity.maximumLinksPerElectricityGenerator.get();
    }

    protected @NotNull Component getDefaultName() {
        return Utils.translation("container", "circuit_breaker");
    }

    protected @NotNull AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory) {
        if(!this.enabled){
            this.searchNodeNetwork(false);
        }
        return new CircuitManagerMenu(windowId, playerInventory,this, this.data);
    }

    public boolean isMatchingContainerMenu(AbstractContainerMenu menu) {
        if(menu instanceof CircuitManagerMenu manager){
            return manager.getContainer() == this;
        }
        return false;
    }

    public boolean isNodePowered(){
        BlockState state = this.getBlockState();
        return state.hasProperty(CircuitBreakerBlock.POWERED) ? state.getValue(CircuitBreakerBlock.POWERED) : false;
    }

    public void setNodePowered(boolean powered) {
        BlockState state = this.getBlockState();
        if (state.hasProperty(CircuitBreakerBlock.POWERED)) {
            assert this.level != null;
            this.level.setBlock(this.worldPosition, state.setValue(CircuitBreakerBlock.POWERED, powered), 3);
        }

    }

    public void togglePower() {
        this.enabled = !this.enabled;
        if (this.enabled) {
            NodeSearchResult result = this.searchNodeNetwork(false);
            if (!result.overloaded()) {
                if (this.overloaded) {
                    this.overloaded = false;
                }
            }
            else {
                this.enabled = false;
            }
        }
        this.setChanged();
    }

    public void onNodeOverloaded() {
        this.enabled = false;
        this.setChanged();
    }

    public NodeSearchResult searchNodeNetwork(boolean cancelAtLimit) {
        NodeSearchResult result = super.searchNodeNetwork(cancelAtLimit);
        this.nodeCount = result.nodes().size();
        return result;
    }

    public IProcessingBlock.EnergyMode getEnergyMode() {
        return EnergyMode.ONLY_WHEN_PROCESSING;
    }

    public int getEnergy() {
        return this.ENERGY_STORAGE.getEnergyStored();
    }

    public int getMaxEnergy() {
        return this.ENERGY_STORAGE.getMaxEnergyStored();
    }

    public int getEnergyRate() {
        return ENERGY_REQ;
    }

    public void addEnergy(int energy) {
        this.ENERGY_STORAGE.setEnergy(energy);
        this.setChanged();
    }

    public void removeEnergy(int energy) {
        this.ENERGY_STORAGE.extractEnergy(energy, false);
    }

    public boolean requiresEnergy() {
        return true;
    }


    public int retrieveEnergy(boolean simulate) {
        if(!simulate){
            return this.ENERGY_STORAGE.receiveEnergy(ENERGY_REQ, false);
        }
        return 0;
    }

    public int updateAndGetTotalProcessingTime() {
        return this.getTotalProcessingTime();
    }

    public int getTotalProcessingTime() {
        return 1;
    }

    public int getProcessingTime() {
        return 0;
    }

    public void setProcessingTime(int time) {
        if (this.isNodePowered()) {
            if (time == 0) {
                this.setNodePowered(false);
            }
        } else if (time == 1) {
            this.setNodePowered(true);
        }

    }

    public void onCompleteProcess() {
    }

    public boolean canProcess() {
        return this.enabled && !this.isNodeOverloaded();
    }


    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("Enabled", 1)) {
            this.enabled = tag.getBoolean("Enabled");
        }
        if (tag.contains("Energy", 3)) {
            this.ENERGY_STORAGE.setEnergy(tag.getInt("Energy"));
        }

    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("Enabled", this.enabled);
        tag.putInt("Energy", this.ENERGY_STORAGE.getEnergyStored());
    }

    public void earlyNodeTick(Level level) {
        if (!level.isClientSide()) {
            this.processTick();
        }

        super.earlyNodeTick(level);
    }

    @Override
    public void onOpen(Level level, BlockPos pos, BlockState state) {
        Vec3 center = Vec3.atCenterOf(this.worldPosition);
        level.playSound(null,center.x, center.y, center.z, ModSounds.BLOCK_STOVE_OPEN.get(),
                SoundSource.BLOCKS, 1.0F, 0.9F + 0.1F * level.random.nextFloat());
        setDoorState(state, true);
    }

    @Override
    public void onClose(Level level, BlockPos pos, BlockState state) {
        super.onClose(level, pos, state);
        Vec3 center = Vec3.atCenterOf(this.worldPosition);
        level.playSound(null,center.x, center.y, center.z, ModSounds.BLOCK_MICROWAVE_CLOSE.get(),
                SoundSource.BLOCKS, 1.0F, 0.9F + 0.1F * level.random.nextFloat());
        setDoorState(state, false);
    }

    private void setDoorState(BlockState state, boolean open){
        Level level = this.getLevel();
        if(level != null){
            level.setBlock(this.getBlockPos(), state.setValue(CircuitBreakerBlock.OPEN, open), 3);
        }
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CircuitBreakerBlockEntity circuitManager) {
        circuitManager.processTick();
    }

    public Lazy<IEnergyStorage> getEnergyHandler() {
        return energyHandler;
    }
}
