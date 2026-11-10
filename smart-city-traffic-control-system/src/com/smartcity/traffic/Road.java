package com.smartcity.traffic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents one of the four intersection approaches (North, South, East, West).
 * Manages vehicle queues, remaining/completed vehicle counters, stop lines, and statistics.
 */
public class Road {
    private final RoadDirection direction;
    private final TrafficSignal signal;
    
    // Exact vehicle counters
    private int initialCount;
    private int completedCount;
    private int remainingCount;
    private int emergencyRequestCount;

    // Active visible vehicles currently modeled on the road & intersection
    private final List<Vehicle> activeVehicles;

    // Coordinate boundaries for stop lines in simulation space
    private double stopLineCoord;

    public Road(RoadDirection direction, int initialCount) {
        this.direction = direction;
        this.signal = new TrafficSignal(direction);
        this.initialCount = Math.max(0, initialCount);
        this.completedCount = 0;
        this.remainingCount = this.initialCount;
        this.emergencyRequestCount = 0;
        this.activeVehicles = Collections.synchronizedList(new ArrayList<>());
    }

    public RoadDirection getDirection() {
        return direction;
    }

    public TrafficSignal getSignal() {
        return signal;
    }

    public synchronized int getInitialCount() {
        return initialCount;
    }

    public synchronized void setInitialCount(int count) {
        this.initialCount = Math.max(0, count);
        this.completedCount = 0;
        this.remainingCount = this.initialCount;
    }

    public synchronized int getCompletedCount() {
        return completedCount;
    }

    public synchronized int getRemainingCount() {
        return remainingCount;
    }

    /**
     * Decrements the remaining count when a vehicle fully exits the intersection.
     * Fulfills Requirement 6: "Every time a vehicle successfully exits the simulated
     * intersection, decrease the remaining count by one. 20 -> 19 -> 18 -> 17..."
     */
    public synchronized void recordVehicleExit() {
        if (remainingCount > 0) {
            remainingCount--;
            completedCount++;
        }
    }

    public synchronized int getEmergencyRequestCount() {
        return emergencyRequestCount;
    }

    public synchronized void incrementEmergencyCount() {
        emergencyRequestCount++;
    }

    public synchronized void decrementEmergencyCount() {
        if (emergencyRequestCount > 0) {
            emergencyRequestCount--;
        }
    }

    public List<Vehicle> getActiveVehicles() {
        return activeVehicles;
    }

    public double getStopLineCoord() {
        return stopLineCoord;
    }

    public void setStopLineCoord(double stopLineCoord) {
        this.stopLineCoord = stopLineCoord;
    }

    /**
     * Computes the average waiting time (in seconds) of currently active vehicles on this road.
     */
    public synchronized double getAverageWaitSeconds() {
        synchronized (activeVehicles) {
            if (activeVehicles.isEmpty()) return 0.0;
            long totalWait = 0;
            int count = 0;
            for (Vehicle v : activeVehicles) {
                if (v.getState() == Vehicle.State.WAITING || v.getState() == Vehicle.State.APPROACHING) {
                    totalWait += v.getWaitTimeMillis();
                    count++;
                }
            }
            return count > 0 ? (totalWait / (double) count) / 1000.0 : 0.0;
        }
    }

    public TrafficDensityCalculator.DensityLevel getDensityLevel() {
        return TrafficDensityCalculator.calculateDensity(getRemainingCount());
    }

    public synchronized void reset(int initialCount) {
        this.initialCount = Math.max(0, initialCount);
        this.completedCount = 0;
        this.remainingCount = this.initialCount;
        this.emergencyRequestCount = 0;
        activeVehicles.clear();
        signal.setState(TrafficSignal.State.RED);
        signal.setCountdownSeconds(0);
    }
}
