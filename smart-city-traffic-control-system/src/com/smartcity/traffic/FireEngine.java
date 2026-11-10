package com.smartcity.traffic;

import java.awt.*;

/**
 * Concrete Emergency Vehicle for Fire & Rescue Engines.
 * Large truck profile with bright fire-red body, roof equipment ladder, and strobe lights.
 */
public class FireEngine extends EmergencyVehicle {

    public FireEngine(String id, RoadDirection originDirection, int laneIndex) {
        super(id, "Fire Engine", originDirection, 22, 50, 2.7, new Color(220, 25, 25), new Color(245, 220, 20), laneIndex);
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
        g.setColor(new Color(0, 0, 0, 70));
        g.fillRoundRect(-length / 2 + 3, -width / 2 + 3, length, width, 5, 5);

        // Bright Red Truck Body
        g.setColor(primaryColor);
        g.fillRoundRect(-length / 2, -width / 2, length, width, 5, 5);

        // High-vis yellow chevron stripes at rear
        g.setColor(accentColor);
        g.fillRect(-length / 2, -width / 2, 4, width);

        // Cabin windshield
        g.setColor(new Color(25, 40, 60));
        g.fillRect(length / 4, -width / 2 + 2, 6, width - 4);

        // Roof rescue ladder (silver)
        g.setColor(new Color(210, 215, 220));
        int ladderStart = -length / 2 + 6;
        int ladderEnd = length / 6;
        g.fillRect(ladderStart, -4, ladderEnd - ladderStart, 8);
        g.setColor(new Color(80, 80, 80));
        for (int rx = ladderStart + 3; rx < ladderEnd - 2; rx += 5) {
            g.drawLine(rx, -4, rx, 3);
        }

        // Dual roof emergency strobe bars
        drawEmergencyBeacons(g, length / 4 + 7, 0);

        // High intensity dual headlights
        g.setColor(new Color(255, 255, 220));
        g.fillRect(length / 2 - 2, -width / 2 + 2, 2, 5);
        g.fillRect(length / 2 - 2, width / 2 - 7, 2, 5);

        g.dispose();
    }
}
