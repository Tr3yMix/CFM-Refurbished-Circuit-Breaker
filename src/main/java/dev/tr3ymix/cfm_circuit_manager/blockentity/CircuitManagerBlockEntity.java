package dev.tr3ymix.cfm_circuit_manager.blockentity;



import com.mrcrayfish.furniture.refurbished.Config;
import com.mrcrayfish.furniture.refurbished.blockentity.*;
import com.mrcrayfish.furniture.refurbished.client.audio.AudioManager;
import com.mrcrayfish.furniture.refurbished.core.ModSounds;
import com.mrcrayfish.furniture.refurbished.electricity.NodeSearchResult;
import com.mrcrayfish.furniture.refurbished.inventory.BuildableContainerData;
import com.mrcrayfish.furniture.refurbished.inventory.ElectricityGeneratorMenu;
import com.mrcrayfish.furniture.refurbished.platform.Services;
import com.mrcrayfish.furniture.refurbished.util.Utils;
import dev.tr3ymix.cfm_circuit_manager.block.CircuitManagerBlock;
import dev.tr3ymix.cfm_circuit_manager.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;



//, IEnergyStorage

public class CircuitManagerBlockEntity extends ElectricitySourceLootBlockEntity implements IProcessingBlock, IPowerSwitch, ILevelAudio {

    //private int energyStored;
   // private int maxEnergyStored;
    //private int energyPerTick;

    public static final int DATA_ENERGY = 0;
    public static final int DATA_TOTAL_ENERGY = 1;
    public static final int DATA_ENABLED = 2;
    public static final int DATA_OVERLOADED = 3;
    public static final int DATA_POWERED = 4;
    public static final int DATA_NODE_COUNT = 5;
    protected final Vec3 audioPosition;
    protected int totalEnergy;
    protected int energy;
    protected boolean enabled;
    protected int nodeCount;
    protected final ContainerData data;

    public CircuitManagerBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CIRCUIT_MANAGER.get(), pos, state);
    }

    public CircuitManagerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1);
        this.data = new BuildableContainerData((builder -> {
            builder.add(DATA_ENERGY, () -> this.energy, (value) -> {
            });
            builder.add(DATA_TOTAL_ENERGY, () -> this.totalEnergy, (value) -> {
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
       // this.maxEnergyStored = 10000;  // Set a maximum value for energy storage
        //this.energyStored = 0;  // Start with no energy
       // this.energyPerTick = 100;
    }

    public int getNodeMaximumConnections(){
        return Config.SERVER.electricity.maximumLinksPerElectricityGenerator.get();
    }

    protected @NotNull Component getDefaultName() {
        return Utils.translation("container", "electricity_generator");
    }

    protected @NotNull AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory) {
        if(!this.enabled){
            this.searchNodeNetwork(false);
        }
        return new ElectricityGeneratorMenu(windowId, playerInventory, this, this.data);
    }

    public boolean isMatchingContainerMenu(AbstractContainerMenu menu) {
        if(menu instanceof ElectricityGeneratorMenu generator){
            return generator.getContainer() == this;
        }
        return false;
    }

    public SoundEvent getSound(){return ModSounds.BLOCK_ELECTRICITY_GENERATOR_ENGINE.get();}

    public SoundSource getSource() {return SoundSource.BLOCKS;}

    public Vec3 getAudioPosition() {return audioPosition;}

    public boolean canPlayAudio() {return this.isNodePowered() && !this.isRemoved();}

    public int getAudioHash(){ return this.worldPosition.hashCode();}

    public boolean isAudioEqual(ILevelAudio other){return other == this;}

    public boolean isNodePowered(){
        BlockState state = this.getBlockState();
        return state.hasProperty(CircuitManagerBlock.POWERED) ? state.getValue(CircuitManagerBlock.POWERED) : false;
    }

    public void setNodePowered(boolean powered) {
        BlockState state = this.getBlockState();
        if (state.hasProperty(CircuitManagerBlock.POWERED)) {
            assert this.level != null;
            this.level.setBlock(this.worldPosition, state.setValue(CircuitManagerBlock.POWERED, powered), 3);
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
            } else {
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
        return Config.SERVER.electricity.cheats.freeGeneratorPower.get() ? 1 : this.energy;
    }

    public void addEnergy(int energy) {
        this.energy += energy;
        this.setChanged();
    }

    public boolean requiresEnergy() {
        return true;
    }

    public int retrieveEnergy(boolean simulate) {
        ItemStack stack = this.getItem(0);
        if (!stack.isEmpty()) {
            int energy = Services.ITEM.getBurnTime(stack, null) * Config.SERVER.electricity.fuelToPowerRatio.get();
            if (energy > 0) {
                if (!simulate) {
                    @SuppressWarnings("deprecation") Item remainingItem = stack.getItem().getCraftingRemainingItem();
                    if (stack.getMaxStackSize() == 1 && remainingItem != null) {
                        this.setItem(0, new ItemStack(remainingItem));
                    } else {
                        stack.shrink(1);
                    }

                    if (this.totalEnergy != energy) {
                        this.totalEnergy = energy;
                        this.setChanged();
                    }
                }

                return energy;
            }
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

    public static void clientTick(Level level, BlockPos pos, BlockState state, CircuitManagerBlockEntity entity) {
        AudioManager.get().playLevelAudio(entity);
    }

    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("Enabled", 1)) {
            this.enabled = tag.getBoolean("Enabled");
        }

        if (tag.contains("Energy", 3)) {
            this.energy = tag.getInt("Energy");
        }

        if (tag.contains("TotalEnergy", 3)) {
            this.totalEnergy = tag.getInt("TotalEnergy");
        }

    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("Enabled", this.enabled);
        tag.putInt("Energy", this.energy);
        tag.putInt("TotalEnergy", this.totalEnergy);
    }

    public void earlyNodeTick(Level level) {
        if (!level.isClientSide()) {
            this.processTick();
        }

        super.earlyNodeTick(level);
    }

/*
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyReceived = Math.min(this.maxEnergyStored - this.energyStored, maxReceive);
        if(!simulate) {
            this.energyStored += energyReceived;
            this.setChanged();
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(this.maxEnergyStored, maxExtract);
        if(!simulate) {
            this.energyStored -= energyExtracted;
            this.setChanged();
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return this.energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.maxEnergyStored;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

 */
}
