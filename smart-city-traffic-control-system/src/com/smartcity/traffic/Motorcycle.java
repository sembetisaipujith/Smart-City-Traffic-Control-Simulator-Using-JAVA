package com.smartcity.traffic;

import java.awt.*;

/**
 * Concrete Vehicle implementation for Motorcycles / Two-Wheelers.
 * Agile, narrower profile, higher top speed.
 */
public class Motorcycle extends Vehicle {

    public Motorcycle(String id, RoadDirection originDirection, Color color, int laneIndex) {
        super(id, "Motorcycle", originDirection, 8, 18, 3.2, color, Color.BLACK, laneIndex);
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
        g.fillOval(-length / 2 + 1, -width / 2 + 1, length, width);

        // Bike wheels (front & rear)
        g.setColor(new Color(30, 30, 30));
        g.fillRect(length / 2 - 4, -1, 4, 3);
        g.fillRect(-length / 2, -1, 4, 3);

        // Bike body / frame
        g.setColor(primaryColor);
        g.fillRoundRect(-length / 3, -width / 2 + 1, (int)(length * 0.7), width - 2, 4, 4);

        // Handlebars
        g.setColor(new Color(180, 180, 180));
        g.fillRect(length / 4, -width / 2, 2, width);

        // Rider helmet
        g.setColor(accentColor);
        g.fillOval(-2, -3, 6, 6);

        // Front headlight
        g.setColor(new Color(255, 255, 220));
        g.fillRect(length / 2 - 1, -1, 2, 3);

        g.dispose();
    }
}
