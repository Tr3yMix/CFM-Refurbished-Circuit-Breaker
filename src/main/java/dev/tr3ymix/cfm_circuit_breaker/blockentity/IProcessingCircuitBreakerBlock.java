package dev.tr3ymix.cfm_circuit_breaker.blockentity;

import com.mrcrayfish.furniture.refurbished.blockentity.IProcessingBlock;

public interface IProcessingCircuitBreakerBlock extends IProcessingBlock {

    int getMaxEnergy();

    int getEnergyRate();

    void removeEnergy(int var);

    @Override
    default boolean processTick() {
        boolean processing = false;
        if (this.canProcess()) {
            if (this.requiresEnergy() && this.getEnergy() < this.getMaxEnergy() && this.retrieveEnergy(true) > 0) {
                this.addEnergy(retrieveEnergy(false));
            }

            if (!this.requiresEnergy() || this.getEnergy() > 0) {
                processing = true;
                int totalProcessingTime = this.updateAndGetTotalProcessingTime();
                int processingTime = this.getProcessingTime();
                if (processingTime < totalProcessingTime) {
                    this.setProcessingTime(processingTime + 1);
                    if (this.requiresEnergy() && this.getEnergyMode() == IProcessingBlock.EnergyMode.ONLY_WHEN_PROCESSING) {
                        this.removeEnergy(getEnergyRate());
                    }
                }

                if (processingTime >= totalProcessingTime) {
                    this.onCompleteProcess();
                    this.setProcessingTime(0);
                }
            }
        }

        if (this.requiresEnergy() && this.getEnergyMode() == IProcessingBlock.EnergyMode.ALWAYS_CONSUME && this.getEnergy() > 0) {
            this.addEnergy(-getEnergyRate());
        }

        if (!processing) {
            this.setProcessingTime(0);
        }

        return processing;
    }
}
