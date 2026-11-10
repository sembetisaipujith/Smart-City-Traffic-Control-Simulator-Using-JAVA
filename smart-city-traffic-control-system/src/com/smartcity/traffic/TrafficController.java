package com.smartcity.traffic;

import javax.swing.Timer;
import java.util.*;

/**
 * Central orchestrator of the Smart City Traffic Control System.
 * Manages simulation threads, adaptive signal allocation, starvation prevention,
 * and emergency vehicle priority override sequencing.
 */
public class TrafficController {
    public enum SimulationState {
        STOPPED,
        RUNNING,
        PAUSED
    }

    private final Map<RoadDirection, Road> roads;
    private final EmergencyManager emergencyManager;
    private final VehicleManager vehicleManager;
    private final ReportManager reportManager;

    // Simulation Clock & Timers
    private Timer physicsTimer;       // 30ms (~33 FPS) for vehicle physics & animation
    private Timer secondClockTimer;   // 1000ms for traffic signal countdowns & scheduling
    private Timer spawnerTimer;       // 600ms for smooth staggered vehicle entry

    private SimulationState state;
    private RoadDirection currentGreenRoad;
    private boolean isYellowPhase;
    private int yellowCountdown;

    // Starvation tracking: counts consecutive cycles a road with waiting vehicles was bypassed
    private final Map<RoadDirection, Integer> starvationCounters;

    // Observer/Listener for UI updates
    public interface SimulationListener {
        void onStateChanged(SimulationState newState);
        void onTick();
    }

    private SimulationListener listener;

    public TrafficController() {
        this.roads = new LinkedHashMap<>();
        // Default realistic starting vehicle counts per prompt example:
        // North: 20, South: 35, East: 12, West: 40
        roads.put(RoadDirection.NORTH, new Road(RoadDirection.NORTH, 20));
        roads.put(RoadDirection.SOUTH, new Road(RoadDirection.SOUTH, 35));
        roads.put(RoadDirection.EAST, new Road(RoadDirection.EAST, 12));
        roads.put(RoadDirection.WEST, new Road(RoadDirection.WEST, 40));

        this.emergencyManager = new EmergencyManager();
        this.vehicleManager = new VehicleManager(roads, emergencyManager);
        this.reportManager = new ReportManager();
        this.state = SimulationState.STOPPED;
        this.currentGreenRoad = null;
        this.isYellowPhase = false;
        this.yellowCountdown = 0;

        this.starvationCounters = new HashMap<>();
        for (RoadDirection dir : RoadDirection.values()) {
            starvationCounters.put(dir, 0);
        }

        setupTimers();
    }

    public void setListener(SimulationListener listener) {
        this.listener = listener;
    }

    private void setupTimers() {
        // 1. High frequency physics & animation timer (~33 FPS)
        physicsTimer = new Timer(30, e -> {
            if (state == SimulationState.RUNNING) {
                vehicleManager.updateVehicles();
                if (listener != null) {
                    listener.onTick();
                }
            }
        });

        // 2. Regular vehicle spawner timer
        spawnerTimer = new Timer(650, e -> {
            if (state == SimulationState.RUNNING) {
                // Try spawning on all roads where vehicles remain
                for (RoadDirection dir : RoadDirection.values()) {
                    vehicleManager.spawnVehicleIfNeeded(dir);
                }
            }
        });

        // 3. Second clock timer for signal state machines and adaptive scheduling
        secondClockTimer = new Timer(1000, e -> {
            if (state == SimulationState.RUNNING) {
                handleSecondClockTick();
            }
        });
    }

    /**
     * Executes once every second: evaluates countdowns, yellow transitions,
     * emergency overrides, and selects next green road using the adaptive algorithm.
     */
    private synchronized void handleSecondClockTick() {
        // Priority 1: Check Emergency Manager
        if (emergencyManager.hasPendingRequests()) {
            handleEmergencyPreemption();
            return;
        }

        // Priority 2: Normal Traffic Adaptive Operation
        if (currentGreenRoad == null) {
            selectNextGreenRoadAdaptively();
            return;
        }

        Road activeRoad = roads.get(currentGreenRoad);
        TrafficSignal signal = activeRoad.getSignal();

        if (isYellowPhase) {
            yellowCountdown--;
            signal.setCountdownSeconds(yellowCountdown);
            if (yellowCountdown <= 0) {
                // End of yellow phase: turn RED and advance
                isYellowPhase = false;
                signal.setState(TrafficSignal.State.RED);
                signal.setCountdownSeconds(0);
                selectNextGreenRoadAdaptively();
            }
        } else {
            // In GREEN phase: count down
            signal.decrementCountdown();
            if (signal.getCountdownSeconds() <= 0) {
                // Green expired: enter safety YELLOW phase (3 seconds)
                isYellowPhase = true;
                yellowCountdown = 3;
                signal.setState(TrafficSignal.State.YELLOW);
                signal.setCountdownSeconds(yellowCountdown);
            }
        }
    }

    /**
     * Processes Emergency Vehicle FIFO queue:
     * - Gracefully clears intersection
     * - Grants emergency road GREEN priority
     */
    private void handleEmergencyPreemption() {
        EmergencyManager.EmergencyRequest active = emergencyManager.getActiveRequest();

        if (active == null) {
            // Poll next request from FIFO queue
            active = emergencyManager.pollNextRequest();
            if (active != null) {
                emergencyManager.setActiveRequest(active);
                emergencyManager.setClearingIntersectionForEmergency(true);
            } else {
                return;
            }
        }

        RoadDirection targetRoad = active.getRoadDirection();

        // If another road is currently green, switch it safely
        if (currentGreenRoad != null && currentGreenRoad != targetRoad) {
            Road current = roads.get(currentGreenRoad);
            current.getSignal().setState(TrafficSignal.State.RED);
            current.getSignal().setCountdownSeconds(0);
        }

        // Wait until intersection clears before opening emergency gate
        if (emergencyManager.isClearingIntersectionForEmergency()) {
            if (!vehicleManager.isIntersectionOccupied()) {
                emergencyManager.setClearingIntersectionForEmergency(false);
                // Open green for emergency road
                currentGreenRoad = targetRoad;
                isYellowPhase = false;
                Road emergRoad = roads.get(targetRoad);
                emergRoad.getSignal().setState(TrafficSignal.State.GREEN);
                emergRoad.getSignal().setCountdownSeconds(15);
                reportManager.logSignalAllocation(targetRoad, 15, emergRoad.getRemainingCount(), true);
            }
        } else {
            // Emergency vehicle is currently en route
            Road emergRoad = roads.get(targetRoad);
            emergRoad.getSignal().decrementCountdown();
        }
    }

    /**
     * Requirement 8: Adaptive Intelligent Traffic Scheduling Algorithm.
     * Evaluates remaining vehicle count, average waiting time, and starvation rounds.
     * Formula:
     * Score(R) = (RemainingVehicles * 2.0) + (AvgWaitSeconds * 1.5) + (StarvationRounds * 8.0)
     */
    private void selectNextGreenRoadAdaptively() {
        RoadDirection bestRoad = null;
        double maxScore = -1.0;

        for (RoadDirection dir : RoadDirection.values()) {
            Road road = roads.get(dir);
            int remaining = road.getRemainingCount();
            double avgWait = road.getAverageWaitSeconds();
            int starvation = starvationCounters.get(dir);

            if (remaining > 0) {
                // Compute adaptive priority score
                double score = (remaining * 2.0) + (avgWait * 1.5) + (starvation * 8.0);
                if (score > maxScore) {
                    maxScore = score;
                    bestRoad = dir;
                }
            }
        }

        // If no road has vehicles left, cycle North-South-East-West with standard duration
        if (bestRoad == null) {
            bestRoad = (currentGreenRoad == null) ? RoadDirection.NORTH : 
                RoadDirection.values()[(currentGreenRoad.ordinal() + 1) % RoadDirection.values().length];
        }

        // Update starvation counters
        for (RoadDirection dir : RoadDirection.values()) {
            if (dir == bestRoad) {
                starvationCounters.put(dir, 0); // Reset served road
            } else if (roads.get(dir).getRemainingCount() > 0) {
                starvationCounters.put(dir, starvationCounters.get(dir) + 1); // Increment starving road
            }
        }

        currentGreenRoad = bestRoad;
        Road selected = roads.get(bestRoad);

        // Turn all other signals RED
        for (Road r : roads.values()) {
            if (r.getDirection() != bestRoad) {
                r.getSignal().setState(TrafficSignal.State.RED);
                r.getSignal().setCountdownSeconds(0);
            }
        }

        // Calculate dynamic green duration based on traffic density
        int adaptiveDuration = TrafficDensityCalculator.calculateAdaptiveGreenDuration(
                selected.getRemainingCount(),
                selected.getAverageWaitSeconds()
        );

        selected.getSignal().setState(TrafficSignal.State.GREEN);
        selected.getSignal().setCountdownSeconds(adaptiveDuration);
        selected.getSignal().setGreenDurationSeconds(adaptiveDuration);

        reportManager.logSignalAllocation(bestRoad, adaptiveDuration, selected.getRemainingCount(), false);
    }

    // Lifecycle Controls
    public synchronized void startSimulation() {
        if (state == SimulationState.RUNNING) return;
        state = SimulationState.RUNNING;
        physicsTimer.start();
        secondClockTimer.start();
        spawnerTimer.start();
        if (currentGreenRoad == null) {
            selectNextGreenRoadAdaptively();
        }
        if (listener != null) listener.onStateChanged(state);
    }

    public synchronized void pauseSimulation() {
        if (state != SimulationState.RUNNING) return;
        state = SimulationState.PAUSED;
        physicsTimer.stop();
        secondClockTimer.stop();
        spawnerTimer.stop();
        if (listener != null) listener.onStateChanged(state);
    }

    public synchronized void resumeSimulation() {
        if (state != SimulationState.PAUSED) return;
        state = SimulationState.RUNNING;
        physicsTimer.start();
        secondClockTimer.start();
        spawnerTimer.start();
        if (listener != null) listener.onStateChanged(state);
    }

    public synchronized void resetSimulation(Map<RoadDirection, Integer> initialCounts) {
        state = SimulationState.STOPPED;
        physicsTimer.stop();
        secondClockTimer.stop();
        spawnerTimer.stop();

        currentGreenRoad = null;
        isYellowPhase = false;
        yellowCountdown = 0;

        emergencyManager.clear();
        vehicleManager.resetAll();

        for (RoadDirection dir : RoadDirection.values()) {
            int count = (initialCounts != null && initialCounts.containsKey(dir)) ? initialCounts.get(dir) : 20;
            roads.get(dir).reset(count);
            starvationCounters.put(dir, 0);
        }

        if (listener != null) {
            listener.onStateChanged(state);
            listener.onTick();
        }
    }

    // Accessors
    public Map<RoadDirection, Road> getRoads() { return roads; }
    public EmergencyManager getEmergencyManager() { return emergencyManager; }
    public VehicleManager getVehicleManager() { return vehicleManager; }
    public ReportManager getReportManager() { return reportManager; }
    public SimulationState getState() { return state; }
    public RoadDirection getCurrentGreenRoad() { return currentGreenRoad; }
    public boolean isYellowPhase() { return isYellowPhase; }
}
