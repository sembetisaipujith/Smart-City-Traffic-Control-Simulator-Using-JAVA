package com.smartcity.traffic;

/**
 * Calculates traffic density tiers and optimal adaptive green light allocations
 * based on real-time vehicle counts, queuing depth, and waiting durations.
 */
public class TrafficDensityCalculator {

    public enum DensityLevel {
        LOW("Low", 0, 10),
        MEDIUM("Medium", 11, 25),
        HIGH("High", 26, 45),
        VERY_HIGH("Very High", 46, Integer.MAX_VALUE);

        private final String label;
        private final int minVehicles;
        private final int maxVehicles;

        DensityLevel(String label, int minVehicles, int maxVehicles) {
            this.label = label;
            this.minVehicles = minVehicles;
            this.maxVehicles = maxVehicles;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Determines the density tier for a road based on active remaining vehicle count.
     */
    public static DensityLevel calculateDensity(int remainingVehicles) {
        if (remainingVehicles <= 10) {
            return DensityLevel.LOW;
        } else if (remainingVehicles <= 25) {
            return DensityLevel.MEDIUM;
        } else if (remainingVehicles <= 45) {
            return DensityLevel.HIGH;
        } else {
            return DensityLevel.VERY_HIGH;
        }
    }

    /**
     * Computes the adaptive green signal duration (in seconds) for a road.
     * Incorporates remaining vehicle load and accumulated waiting time.
     * Guaranteed bounds: Minimum 6 seconds, Maximum 35 seconds.
     *
     * @param remainingVehicles Number of vehicles yet to clear the intersection
     * @param averageWaitSeconds Average waiting time experienced by queued vehicles
     * @return Calculated green signal duration in seconds
     */
    public static int calculateAdaptiveGreenDuration(int remainingVehicles, double averageWaitSeconds) {
        if (remainingVehicles <= 0) {
            return 6; // Minimum safe allocation when the road is empty
        }

        // Base allocation: 1.2 seconds per vehicle queued
        double baseTime = remainingVehicles * 1.2;

        // Waiting time urgency bonus: add 1s for every 8s of average waiting
        double urgencyBonus = averageWaitSeconds * 0.125;

        int totalDuration = (int) Math.round(baseTime + urgencyBonus);

        // Clamping between safe realistic urban bounds
        return Math.min(35, Math.max(6, totalDuration));
    }
}
