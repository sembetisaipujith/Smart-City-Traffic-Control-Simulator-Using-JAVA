package com.smartcity.traffic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Manages traffic performance logging and generates detailed CSV reports.
 * Records simulation runs, vehicle throughput, wait times, and emergency dispatches.
 */
public class ReportManager {

    public static class SignalHistoryEntry {
        private final String timestamp;
        private final RoadDirection direction;
        private final int allocatedSeconds;
        private final int queueAtAllocation;
        private final boolean isEmergencyOverride;

        public SignalHistoryEntry(RoadDirection direction, int allocatedSeconds, int queueAtAllocation, boolean isEmergencyOverride) {
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.direction = direction;
            this.allocatedSeconds = allocatedSeconds;
            this.queueAtAllocation = queueAtAllocation;
            this.isEmergencyOverride = isEmergencyOverride;
        }

        public String getTimestamp() { return timestamp; }
        public RoadDirection getDirection() { return direction; }
        public int getAllocatedSeconds() { return allocatedSeconds; }
        public int getQueueAtAllocation() { return queueAtAllocation; }
        public boolean isEmergencyOverride() { return isEmergencyOverride; }
    }

    private final List<SignalHistoryEntry> historyEntries;
    private final LocalDateTime simulationStartTime;

    public ReportManager() {
        this.historyEntries = Collections.synchronizedList(new ArrayList<>());
        this.simulationStartTime = LocalDateTime.now();
    }

    public void logSignalAllocation(RoadDirection direction, int durationSeconds, int remainingVehicles, boolean isEmergency) {
        historyEntries.add(new SignalHistoryEntry(direction, durationSeconds, remainingVehicles, isEmergency));
    }

    public List<SignalHistoryEntry> getHistoryEntries() {
        synchronized (historyEntries) {
            return new ArrayList<>(historyEntries);
        }
    }

    /**
     * Generates a comprehensive CSV string formatted according to Requirement 10.
     */
    public String generateCsvContent(Map<RoadDirection, Road> roads, int totalEmergenciesHandled) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = LocalDateTime.now().format(dtf);

        sb.append("# SMART CITY TRAFFIC CONTROL SYSTEM - SIMULATION REPORT\n");
        sb.append("# Generated At: ").append(currentTime).append("\n");
        sb.append("# Simulation Started: ").append(simulationStartTime.format(dtf)).append("\n");
        sb.append("# Emergency Vehicles Handled: ").append(totalEmergenciesHandled).append("\n\n");

        // Section 1: Road Performance Summary
        sb.append("=== ROAD PERFORMANCE SUMMARY ===\n");
        sb.append("Road Name,Initial Vehicles,Vehicles Completed,Vehicles Remaining,Traffic Density,Avg Wait Time (s),Emergency Requests\n");

        for (Road road : roads.values()) {
            sb.append(String.format("%s,%d,%d,%d,%s,%.2f,%d\n",
                    road.getDirection().getDisplayName(),
                    road.getInitialCount(),
                    road.getCompletedCount(),
                    road.getRemainingCount(),
                    road.getDensityLevel().getLabel(),
                    road.getAverageWaitSeconds(),
                    road.getEmergencyRequestCount()
            ));
        }

        sb.append("\n=== SIGNAL ALLOCATION & ADAPTIVE SCHEDULING LOG ===\n");
        sb.append("Timestamp,Road Name,Duration (s),Queue Depth,Allocation Type\n");

        synchronized (historyEntries) {
            for (SignalHistoryEntry entry : historyEntries) {
                sb.append(String.format("%s,%s,%d,%d,%s\n",
                        entry.getTimestamp(),
                        entry.getDirection().getDisplayName(),
                        entry.getAllocatedSeconds(),
                        entry.getQueueAtAllocation(),
                        entry.isEmergencyOverride() ? "EMERGENCY_OVERRIDE" : "ADAPTIVE_ALLOCATION"
                ));
            }
        }

        return sb.toString();
    }

    /**
     * Exports the generated CSV report directly to the user's filesystem.
     */
    public File exportReportToFile(File targetFile, Map<RoadDirection, Road> roads, int totalEmergencies) throws IOException {
        String content = generateCsvContent(roads, totalEmergencies);
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.print(content);
        }
        return targetFile;
    }

    public void clear() {
        historyEntries.clear();
    }
}
