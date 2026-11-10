package com.smartcity.traffic;

import java.awt.*;

/**
 * Concrete Emergency Vehicle for Police Interceptor Cruiser.
 * Features high-contrast black & white livery, interceptor markings, and flashing siren bar.
 */
public class PoliceVehicle extends EmergencyVehicle {

    public PoliceVehicle(String id, RoadDirection originDirection, int laneIndex) {
        super(id, "Police Cruiser", originDirection, 16, 30, 3.4, new Color(20, 24, 32), Color.WHITE, laneIndex);
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

        // Black Chassis (Front & Rear)
        g.setColor(primaryColor);
        g.fillRoundRect(-length / 2, -width / 2, length, width, 6, 6);

        // White Door and Roof Section (Iconic Police interceptor pattern)
        g.setColor(accentColor);
        g.fillRect(-length / 6, -width / 2, (int)(length * 0.45), width);

        // Windshield and rear window
        g.setColor(new Color(30, 45, 65));
        g.fillRoundRect(length / 4 - 2, -width / 2 + 2, 5, width - 4, 2, 2);
        g.fillRoundRect(-length / 4 - 2, -width / 2 + 2, 4, width - 4, 2, 2);

        // Emergency light bar (central roof)
        drawEmergencyBeacons(g, 1, 0);

        // Interceptor Headlights
        g.setColor(new Color(255, 255, 220));
        g.fillRect(length / 2 - 2, -width / 2 + 2, 2, 3);
        g.fillRect(length / 2 - 2, width / 2 - 5, 2, 3);

        // Tail lights
        g.setColor(new Color(220, 20, 20));
        g.fillRect(-length / 2, -width / 2 + 2, 2, 3);
        g.fillRect(-length / 2, width / 2 - 5, 2, 3);

        g.dispose();
    }
}
