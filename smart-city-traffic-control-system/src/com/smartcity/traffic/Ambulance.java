package com.smartcity.traffic;

import java.awt.*;

/**
 * Concrete Emergency Vehicle for Medical Ambulances.
 * Features high-visibility medical livery, red cross emblem, and dual-tone emergency strobes.
 */
public class Ambulance extends EmergencyVehicle {

    public Ambulance(String id, RoadDirection originDirection, int laneIndex) {
        super(id, "Ambulance", originDirection, 18, 36, 3.0, Color.WHITE, new Color(220, 20, 30), laneIndex);
    }

    @Override
    public void draw(Graphics2D g2d) {
        tickBeacon();

        Graphics2D g = (Graphics2D) g2d.create();
        g.translate(x, y);

        double angle = switch (originDirection) {
            case NORTH -> Math.PI / 2;
            case SOUTH -> -Math.PI / 2;
            case EAST  -> Math.PI;
            case WEST  -> 0;
        };
        g.rotate(angle);

        // Shadow
        g.setColor(new Color(0, 0, 0, 60));
        g.fillRoundRect(-length / 2 + 2, -width / 2 + 2, length, width, 6, 6);

        // White vehicle body
        g.setColor(primaryColor);
        g.fillRoundRect(-length / 2, -width / 2, length, width, 6, 6);

        // Red medical high-vis side stripes
        g.setColor(accentColor);
        g.fillRect(-length / 2, -width / 2, length, 2);
        g.fillRect(-length / 2, width / 2 - 2, length, 2);

        // Red Cross symbol on roof
        g.fillRect(-length / 8 - 1, -4, 3, 8);
        g.fillRect(-length / 8 - 4, -1, 9, 2);

        // Windshield
        g.setColor(new Color(30, 45, 65));
        g.fillRect(length / 4, -width / 2 + 2, 4, width - 4);

        // Emergency light bar (central roof)
        drawEmergencyBeacons(g, 2, 0);

        // Front headlights
        g.setColor(new Color(255, 255, 220));
        g.fillRect(length / 2 - 1, -width / 2 + 2, 2, 4);
        g.fillRect(length / 2 - 1, width / 2 - 6, 2, 4);

        g.dispose();
    }
}
