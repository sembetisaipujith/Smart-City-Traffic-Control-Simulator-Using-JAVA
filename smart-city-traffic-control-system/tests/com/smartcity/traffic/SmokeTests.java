package com.smartcity.traffic;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dependency-free smoke tests for the core model and headless map rendering.
 * Run with test.bat on Windows or ./test.sh on macOS/Linux.
 */
public final class SmokeTests {
    private static int passed = 0;

    private SmokeTests() { }

    public static void main(String[] args) throws Exception {
        testRoadDirectionOpposites();
        testTrafficDensityBoundaries();
        testAdaptiveGreenDurationBounds();
        testRoadCountersAndReset();
        testEmergencyQueueIsFifo();
        testEmergencyCompletionCounterReset();
        testReportIncludesEmergencyCount();
        testMapPanelCanRenderHeadlessly();
        System.out.println("PASS: " + passed + " smoke tests.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void testRoadDirectionOpposites() {
        check(RoadDirection.NORTH.getOpposite() == RoadDirection.SOUTH, "North opposite");
        check(RoadDirection.EAST.getOpposite() == RoadDirection.WEST, "East opposite");
        passed++;
    }

    private static void testTrafficDensityBoundaries() {
        check(TrafficDensityCalculator.calculateDensity(0) == TrafficDensityCalculator.DensityLevel.LOW, "0 density");
        check(TrafficDensityCalculator.calculateDensity(10) == TrafficDensityCalculator.DensityLevel.LOW, "10 density");
        check(TrafficDensityCalculator.calculateDensity(11) == TrafficDensityCalculator.DensityLevel.MEDIUM, "11 density");
        check(TrafficDensityCalculator.calculateDensity(26) == TrafficDensityCalculator.DensityLevel.HIGH, "26 density");
        check(TrafficDensityCalculator.calculateDensity(46) == TrafficDensityCalculator.DensityLevel.VERY_HIGH, "46 density");
        passed++;
    }

    private static void testAdaptiveGreenDurationBounds() {
        check(TrafficDensityCalculator.calculateAdaptiveGreenDuration(0, 0) == 6, "Empty-road minimum duration");
        check(TrafficDensityCalculator.calculateAdaptiveGreenDuration(1, 0) >= 6, "Positive load minimum");
        check(TrafficDensityCalculator.calculateAdaptiveGreenDuration(1000, 10000) == 35, "Maximum duration clamp");
        passed++;
    }

    private static void testRoadCountersAndReset() {
        Road road = new Road(RoadDirection.NORTH, 2);
        road.recordVehicleExit();
        check(road.getRemainingCount() == 1 && road.getCompletedCount() == 1, "Exit counters decrement");
        road.recordVehicleExit();
        road.recordVehicleExit();
        check(road.getRemainingCount() == 0 && road.getCompletedCount() == 2, "Counters do not go negative");
        road.reset(-5);
        check(road.getInitialCount() == 0 && road.getRemainingCount() == 0, "Negative reset input is clamped");
        passed++;
    }

    private static void testEmergencyQueueIsFifo() {
        EmergencyManager manager = new EmergencyManager();
        Ambulance firstVehicle = new Ambulance("E-1", RoadDirection.NORTH, 0);
        FireEngine secondVehicle = new FireEngine("E-2", RoadDirection.SOUTH, 0);
        manager.enqueueRequest(new EmergencyManager.EmergencyRequest("E-1", RoadDirection.NORTH, "Ambulance", firstVehicle));
        manager.enqueueRequest(new EmergencyManager.EmergencyRequest("E-2", RoadDirection.SOUTH, "Fire Engine", secondVehicle));
        check(manager.pollNextRequest().getVehicle() == firstVehicle, "First emergency request served first");
        check(manager.pollNextRequest().getVehicle() == secondVehicle, "Second emergency request served second");
        passed++;
    }

    private static void testEmergencyCompletionCounterReset() {
        Map<RoadDirection, Road> roads = new LinkedHashMap<>();
        for (RoadDirection direction : RoadDirection.values()) {
            roads.put(direction, new Road(direction, 0));
        }
        VehicleManager manager = new VehicleManager(roads, new EmergencyManager());
        check(manager.getTotalEmergencyVehiclesCompleted() == 0, "Emergency completion counter starts at zero");
        manager.resetAll();
        check(manager.getTotalEmergencyVehiclesCompleted() == 0, "Emergency completion counter resets");
        passed++;
    }

    private static void testReportIncludesEmergencyCount() {
        Map<RoadDirection, Road> roads = new LinkedHashMap<>();
        for (RoadDirection direction : RoadDirection.values()) {
            roads.put(direction, new Road(direction, 0));
        }
        ReportManager reports = new ReportManager();
        String csv = reports.generateCsvContent(roads, 3);
        check(csv.contains("# Emergency Vehicles Handled: 3"), "Report contains emergency count");
        reports.logSignalAllocation(RoadDirection.NORTH, 12, 4, false);
        check(reports.getHistoryEntries().size() == 1, "Signal history is recorded");
        passed++;
    }

    private static void testMapPanelCanRenderHeadlessly() {
        TrafficController controller = new TrafficController();
        CityMapPanel panel = new CityMapPanel(controller);
        panel.setSize(800, 700);
        BufferedImage image = new BufferedImage(800, 700, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            panel.paint(graphics);
        } finally {
            graphics.dispose();
        }
        check(image.getRGB(400, 350) != 0, "Map panel paints to an image");
        passed++;
    }
}
