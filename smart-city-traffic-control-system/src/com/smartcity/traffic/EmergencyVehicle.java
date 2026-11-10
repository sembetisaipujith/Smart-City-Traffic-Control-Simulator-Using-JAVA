package com.smartcity.traffic;

import java.awt.*;

/**
 * Abstract class representing high-priority emergency response vehicles.
 * Implements siren beacon strobes and priority flags for adaptive intersection clearance.
 */
public abstract class EmergencyVehicle extends Vehicle {
    protected int beaconFlashTick;
    protected boolean flashState;

    public EmergencyVehicle(String id, String vehicleType, RoadDirection originDirection,
                            int width, int length, double maxSpeed, Color primaryColor, Color accentColor,
                            int laneIndex) {
        super(id, vehicleType, originDirection, width, length, maxSpeed, primaryColor, accentColor, laneIndex);
        this.isEmergency = true;
        this.beaconFlashTick = 0;
        this.flashState = false;
        // Emergency vehicles have stronger acceleration to clear routes swiftly
        this.currentSpeed = 1.0;
    }

    /**
     * Cycles emergency roof strobe beacon state on every animation frame.
     */
    public void tickBeacon() {
        beaconFlashTick++;
        if (beaconFlashTick % 6 == 0) { // Toggle every ~180ms
            flashState = !flashState;
        }
    }

    /**
     * Draws emergency light strobes (Red / Blue alternation).
     */
    protected void drawEmergencyBeacons(Graphics2D g, int cx, int cy) {
        Color leftBeacon = flashState ? new Color(255, 30, 30, 240) : new Color(30, 100, 255, 240);
        Color rightBeacon = flashState ? new Color(30, 100, 255, 240) : new Color(255, 30, 30, 240);

        // Halo glow
        g.setColor(new Color(flashState ? 255 : 30, flashState ? 40 : 120, flashState ? 40 : 255, 70));
        g.fillOval(cx - 10, cy - 8, 20, 16);

        // Emergency light bar
        g.setColor(new Color(50, 50, 50));
        g.fillRect(cx - 3, cy - 6, 6, 12);

        // Left & Right strobe diodes
        g.setColor(leftBeacon);
        g.fillRect(cx - 2, cy - 5, 4, 4);
        g.setColor(rightBeacon);
        g.fillRect(cx - 2, cy + 1, 4, 4);
    }
}
