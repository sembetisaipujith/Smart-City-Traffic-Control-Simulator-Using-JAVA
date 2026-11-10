package com.smartcity.traffic;

import java.awt.*;

/**
 * Concrete Vehicle implementation for Auto-Rickshaw (Three-Wheeler).
 * Distinct yellow canopy and green/black body common in urban Indian/Asian cities.
 */
public class AutoRickshaw extends Vehicle {

    public AutoRickshaw(String id, RoadDirection originDirection, int laneIndex) {
        // Distinct yellow-green livery
        super(id, "Auto-Rickshaw", originDirection, 14, 22, 2.3, new Color(245, 190, 20), new Color(20, 130, 60), laneIndex);
    }

    @Override
    public void draw(Graphics2D g2d) {
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
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRoundRect(-length / 2 + 2, -width / 2 + 2, length, width, 5, 5);

        // Lower body (Green or Black base)
        g.setColor(accentColor);
        g.fillRoundRect(-length / 2, -width / 2, length, width, 4, 4);

        // Yellow Roof Canopy
        g.setColor(primaryColor);
        g.fillRoundRect(-length / 2 + 4, -width / 2 + 1, length - 6, width - 2, 4, 4);

        // Front tapered hood (three-wheeler characteristic)
        int[] xPoints = { length / 2 - 4, length / 2, length / 2 - 4 };
        int[] yPoints = { -width / 2 + 2, 0, width / 2 - 2 };
        g.setColor(accentColor.darker());
        g.fillPolygon(xPoints, yPoints, 3);

        // Windshield
        g.setColor(new Color(30, 45, 60));
        g.fillRect(length / 4, -width / 2 + 2, 3, width - 4);

        // Single front center headlight
        g.setColor(new Color(255, 255, 200));
        g.fillRect(length / 2 - 2, -2, 2, 4);

        // Rear tail lights
        g.setColor(new Color(220, 30, 30));
        g.fillRect(-length / 2, -width / 2 + 1, 2, 3);
        g.fillRect(-length / 2, width / 2 - 4, 2, 3);

        g.dispose();
    }
}
