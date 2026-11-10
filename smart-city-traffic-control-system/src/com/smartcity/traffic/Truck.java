package com.smartcity.traffic;

import java.awt.*;

/**
 * Concrete Vehicle implementation for Commercial Freight Truck.
 * Distinct driver cab and cargo container with realistic heavy-duty proportions.
 */
public class Truck extends Vehicle {

    public Truck(String id, RoadDirection originDirection, Color color, int laneIndex) {
        super(id, "Truck", originDirection, 22, 48, 1.8, color, new Color(100, 110, 120), laneIndex);
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
        g.fillRoundRect(-length / 2 + 3, -width / 2 + 3, length, width, 5, 5);

        // Cargo Container (Rear section)
        g.setColor(accentColor);
        g.fillRoundRect(-length / 2, -width / 2, (int)(length * 0.65), width, 4, 4);
        
        // Container ribbed pattern
        g.setColor(accentColor.darker());
        for (int i = -length / 2 + 4; i < -length / 2 + (int)(length * 0.65); i += 6) {
            g.drawLine(i, -width / 2 + 2, i, width / 2 - 2);
        }

        // Driver Cabin (Front section)
        int cabStart = -length / 2 + (int)(length * 0.65) + 2;
        int cabLength = length - (cabStart + length / 2);
        g.setColor(primaryColor);
        g.fillRoundRect(cabStart, -width / 2 + 1, cabLength, width - 2, 5, 5);

        // Cabin Windshield
        g.setColor(new Color(30, 45, 65));
        g.fillRect(cabStart + cabLength - 5, -width / 2 + 3, 4, width - 6);

        // Side mirrors
        g.setColor(new Color(40, 40, 40));
        g.fillRect(cabStart + cabLength - 4, -width / 2 - 2, 3, 2);
        g.fillRect(cabStart + cabLength - 4, width / 2, 3, 2);

        // Heavy Headlights
        g.setColor(new Color(255, 255, 200));
        g.fillRect(length / 2 - 2, -width / 2 + 2, 2, 4);
        g.fillRect(length / 2 - 2, width / 2 - 6, 2, 4);

        // Heavy Tail lights
        g.setColor(new Color(220, 20, 20));
        g.fillRect(-length / 2, -width / 2 + 2, 2, 4);
        g.fillRect(-length / 2, width / 2 - 6, 2, 4);

        g.dispose();
    }
}
