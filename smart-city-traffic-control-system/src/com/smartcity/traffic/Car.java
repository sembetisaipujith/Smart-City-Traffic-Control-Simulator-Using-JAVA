package com.smartcity.traffic;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Concrete Vehicle implementation for standard Passenger Cars.
 */
public class Car extends Vehicle {

    public Car(String id, RoadDirection originDirection, Color color, int laneIndex) {
        super(id, "Car", originDirection, 16, 28, 2.6, color, new Color(50, 50, 50), laneIndex);
    }

    @Override
    public void draw(Graphics2D g2d) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.translate(x, y);

        // Rotate vehicle according to travel direction
        double angle = switch (originDirection) {
            case NORTH -> Math.PI / 2;      // Facing Down
            case SOUTH -> -Math.PI / 2;     // Facing Up
            case EAST  -> Math.PI;          // Facing Left
            case WEST  -> 0;                // Facing Right
        };
        g.rotate(angle);

        // Vehicle shadow
        g.setColor(new Color(0, 0, 0, 60));
        g.fillRoundRect(-length / 2 + 2, -width / 2 + 2, length, width, 6, 6);

        // Car chassis
        g.setColor(primaryColor);
        g.fillRoundRect(-length / 2, -width / 2, length, width, 6, 6);
        g.setColor(primaryColor.darker());
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(-length / 2, -width / 2, length, width, 6, 6);

        // Windshield and rear glass
        g.setColor(new Color(40, 60, 80));
        g.fillRoundRect(-length / 6, -width / 2 + 3, length / 2, width - 6, 3, 3);

        // Roof
        g.setColor(primaryColor.brighter());
        g.fillRoundRect(-length / 8, -width / 2 + 4, length / 3, width - 8, 2, 2);

        // Headlights (facing forward: +X)
        g.setColor(new Color(255, 255, 200));
        g.fillRect(length / 2 - 2, -width / 2 + 2, 2, 3);
        g.fillRect(length / 2 - 2, width / 2 - 5, 2, 3);

        // Tail lights (facing backward: -X)
        g.setColor(new Color(220, 20, 20));
        g.fillRect(-length / 2, -width / 2 + 2, 2, 3);
        g.fillRect(-length / 2, width / 2 - 5, 2, 3);

        g.dispose();
    }
}
