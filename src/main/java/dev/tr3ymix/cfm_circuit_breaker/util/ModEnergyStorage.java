package dev.tr3ymix.cfm_circuit_breaker.util;

import net.neoforged.neoforge.energy.EnergyStorage;

public abstract class ModEnergyStorage extends EnergyStorage {

    public ModEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }
    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        return super.extractEnergy(toExtract, simulate);
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        return super.receiveEnergy(toReceive, simulate);
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public abstract void onEnergyChanged();
}
