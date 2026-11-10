package com.smartcity.traffic;

import java.awt.*;

/**
 * Concrete Vehicle implementation for City Transit Bus.
 * Heavy, elongated profile with passenger windows and slower acceleration.
 */
public class Bus extends Vehicle {

    public Bus(String id, RoadDirection originDirection, Color color, int laneIndex) {
        super(id, "Bus", originDirection, 20, 52, 1.9, color, new Color(240, 240, 240), laneIndex);
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
        g.setColor(new Color(0, 0, 0, 70));
        g.fillRoundRect(-length / 2 + 3, -width / 2 + 3, length, width, 6, 6);

        // Bus body
        g.setColor(primaryColor);
        g.fillRoundRect(-length / 2, -width / 2, length, width, 6, 6);

        // Roof strip
        g.setColor(accentColor);
        g.fillRoundRect(-length / 2 + 6, -width / 2 + 3, length - 12, width - 6, 4, 4);

        // AC roof units
        g.setColor(new Color(180, 180, 180));
        g.fillRect(-length / 4, -width / 4, 14, width / 2);
        g.fillRect(length / 8, -width / 4, 12, width / 2);

        // Front panoramic windshield
        g.setColor(new Color(30, 50, 70));
        g.fillRoundRect(length / 2 - 8, -width / 2 + 2, 7, width - 4, 2, 2);

        // Side windows
        for (int i = -length / 2 + 6; i < length / 2 - 10; i += 8) {
            g.fillRect(i, -width / 2 + 1, 6, 2);
            g.fillRect(i, width / 2 - 3, 6, 2);
        }

        // Headlights
        g.setColor(new Color(255, 255, 210));
        g.fillRect(length / 2 - 1, -width / 2 + 2, 2, 4);
        g.fillRect(length / 2 - 1, width / 2 - 6, 2, 4);

        // Tail lights
        g.setColor(new Color(220, 20, 20));
        g.fillRect(-length / 2, -width / 2 + 2, 2, 4);
        g.fillRect(-length / 2, width / 2 - 6, 2, 4);

        g.dispose();
    }
}
