package com.smartcity.traffic;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * High-fidelity top-down 2D City Intersection simulation panel rendered via Java Graphics2D.
 * Features realistic asphalt textures, lane markings, pedestrian zebra crossings,
 * architectural buildings, street trees, streetlights, glowing traffic signal heads, and vehicle rendering.
 */
public class CityMapPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final TrafficController controller;
    private final int roadWidth = 140; // 70px each side from center
    private final int stopLineOffset = 75;

    public CityMapPanel(TrafficController controller) {
        this.controller = controller;
        setBackground(new Color(24, 30, 42)); // Modern slate background
        setPreferredSize(new Dimension(800, 700));
        setDoubleBuffered(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        // Enable high quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = h / 2;

        // Synchronize vehicle manager dimensions with current panel size
        controller.getVehicleManager().setDimensions(w, h);

        // 1. Draw City Grass & Sidewalk Ground
        drawUrbanGround(g2d, w, h);

        // 2. Draw 4 Surrounding Quadrant Buildings & Landmarks
        drawBuildingsAndGreenery(g2d, w, h, cx, cy);

        // 3. Draw Sidewalks & Curbs
        drawSidewalks(g2d, w, h, cx, cy);

        // 4. Draw Roads (Asphalt)
        drawRoads(g2d, w, h, cx, cy);

        // 5. Draw Pedestrian Zebra Crossings & Stop Lines
        drawCrossingsAndMarkings(g2d, cx, cy);

        // 6. Draw Streetlights & Trees
        drawStreetDecorations(g2d, cx, cy);

        // 7. Draw Traffic Signals (Gantries, LEDs, and Countdowns)
        drawTrafficSignals(g2d, cx, cy);

        // 8. Draw All Active Vehicles (Polymorphic rendering)
        drawVehicles(g2d);

        // 9. Draw HUD Overlays (Emergency alert banner, scale)
        drawHudOverlay(g2d, w, h);
    }

    private void drawUrbanGround(Graphics2D g2d, int w, int h) {
        // Base grass/park green
        g2d.setColor(new Color(36, 48, 54));
        g2d.fillRect(0, 0, w, h);
    }

    private void drawBuildingsAndGreenery(Graphics2D g2d, int w, int h, int cx, int cy) {
        int rw2 = roadWidth / 2;

        // Quadrant 1: Top-Left (North-West) - Tech Park / Modern Glass Tower
        drawBuildingBlock(g2d, 30, 30, cx - rw2 - 50, cy - rw2 - 50,
                new Color(35, 45, 60), new Color(48, 62, 82), "CITY TECH HUB", new Color(0, 200, 255));

        // Quadrant 2: Top-Right (North-East) - Municipal Civic Center
        drawBuildingBlock(g2d, cx + rw2 + 20, 30, w - (cx + rw2 + 50), cy - rw2 - 50,
                new Color(42, 52, 65), new Color(55, 68, 85), "CIVIC CENTER", new Color(255, 180, 50));

        // Quadrant 3: Bottom-Left (South-West) - Emergency General Hospital
        drawBuildingBlock(g2d, 30, cy + rw2 + 20, cx - rw2 - 50, h - (cy + rw2 + 50),
                new Color(45, 40, 50), new Color(60, 50, 68), "CENTRAL HOSPITAL", new Color(255, 70, 70));

        // Quadrant 4: Bottom-Right (South-East) - Metro Shopping Plaza
        drawBuildingBlock(g2d, cx + rw2 + 20, cy + rw2 + 20, w - (cx + rw2 + 50), h - (cy + rw2 + 50),
                new Color(38, 50, 58), new Color(50, 65, 75), "METRO PLAZA", new Color(100, 220, 100));
    }

    private void drawBuildingBlock(Graphics2D g2d, int x, int y, int bw, int bh,
                                   Color wall, Color roof, String title, Color accent) {
        if (bw <= 0 || bh <= 0) return;

        // Drop shadow
        g2d.setColor(new Color(0, 0, 0, 70));
        g2d.fillRoundRect(x + 6, y + 6, bw, bh, 12, 12);

        // Building outer structure
        g2d.setColor(wall);
        g2d.fillRoundRect(x, y, bw, bh, 12, 12);

        // Building roof
        g2d.setColor(roof);
        g2d.fillRoundRect(x + 8, y + 8, bw - 16, bh - 16, 8, 8);

        // Accent roof border
        g2d.setColor(accent);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRoundRect(x + 12, y + 12, bw - 24, bh - 24, 6, 6);

        // Architectural details: window rows
        g2d.setColor(new Color(255, 255, 255, 30));
        for (int r = y + 25; r < y + bh - 30; r += 20) {
            for (int c = x + 25; c < x + bw - 30; c += 22) {
                g2d.fillRect(c, r, 12, 8);
            }
        }

        // Title Tag
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2d.setColor(new Color(240, 245, 250));
        FontMetrics fm = g2d.getFontMetrics();
        int tw = fm.stringWidth(title);
        if (tw < bw - 20) {
            g2d.drawString(title, x + (bw - tw) / 2, y + bh / 2 + 4);
        }
    }

    private void drawSidewalks(Graphics2D g2d, int w, int h, int cx, int cy) {
        int rw2 = roadWidth / 2;
        int sw = 14; // Sidewalk width

        g2d.setColor(new Color(85, 95, 105)); // Sidewalk stone gray

        // North-South sidewalk strips
        g2d.fillRect(cx - rw2 - sw, 0, sw, h);
        g2d.fillRect(cx + rw2, 0, sw, h);

        // East-West sidewalk strips
        g2d.fillRect(0, cy - rw2 - sw, w, sw);
        g2d.fillRect(0, cy + rw2, w, sw);

        // Curb highlight lines
        g2d.setColor(new Color(130, 140, 150));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(cx - rw2 - sw, 0, sw, h);
        g2d.drawRect(cx + rw2, 0, sw, h);
        g2d.drawRect(0, cy - rw2 - sw, w, sw);
        g2d.drawRect(0, cy + rw2, w, sw);
    }

    private void drawRoads(Graphics2D g2d, int w, int h, int cx, int cy) {
        int rw2 = roadWidth / 2;

        // Asphalt surface
        g2d.setColor(new Color(28, 34, 40));
        g2d.fillRect(cx - rw2, 0, roadWidth, h); // Vertical road (North - South)
        g2d.fillRect(0, cy - rw2, w, roadWidth); // Horizontal road (East - West)

        // Road edge borders
        g2d.setColor(new Color(60, 68, 76));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(cx - rw2, 0, cx - rw2, h);
        g2d.drawLine(cx + rw2, 0, cx + rw2, h);
        g2d.drawLine(0, cy - rw2, w, cy - rw2);
        g2d.drawLine(0, cy + rw2, w, cy + rw2);

        // Center Yellow Double Road Dividers
        g2d.setColor(new Color(241, 196, 15));
        g2d.setStroke(new BasicStroke(2.0f));

        // North road divider (Stops before zebra crossing)
        g2d.drawLine(cx - 2, 0, cx - 2, cy - stopLineOffset - 25);
        g2d.drawLine(cx + 2, 0, cx + 2, cy - stopLineOffset - 25);

        // South road divider
        g2d.drawLine(cx - 2, cy + stopLineOffset + 25, cx - 2, h);
        g2d.drawLine(cx + 2, cy + stopLineOffset + 25, cx + 2, h);

        // East road divider
        g2d.drawLine(cx + stopLineOffset + 25, cy - 2, w, cy - 2);
        g2d.drawLine(cx + stopLineOffset + 25, cy + 2, w, cy + 2);

        // West road divider
        g2d.drawLine(0, cy - 2, cx - stopLineOffset - 25, cy - 2);
        g2d.drawLine(0, cy + 2, cx - stopLineOffset - 25, cy + 2);

        // Dashed White Lane Dividers (Separating inner and outer lanes)
        float[] dashPattern = { 10.0f, 10.0f };
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
        g2d.setColor(new Color(220, 225, 230, 180));

        // North lanes
        g2d.drawLine(cx - rw2 / 2, 0, cx - rw2 / 2, cy - stopLineOffset - 25);
        g2d.drawLine(cx + rw2 / 2, 0, cx + rw2 / 2, cy - stopLineOffset - 25);

        // South lanes
        g2d.drawLine(cx - rw2 / 2, cy + stopLineOffset + 25, cx - rw2 / 2, h);
        g2d.drawLine(cx + rw2 / 2, cy + stopLineOffset + 25, cx + rw2 / 2, h);

        // East lanes
        g2d.drawLine(cx + stopLineOffset + 25, cy - rw2 / 2, w, cy - rw2 / 2);
        g2d.drawLine(cx + stopLineOffset + 25, cy + rw2 / 2, w, cy + rw2 / 2);

        // West lanes
        g2d.drawLine(0, cy - rw2 / 2, cx - stopLineOffset - 25, cy - rw2 / 2);
        g2d.drawLine(0, cy + rw2 / 2, cx - stopLineOffset - 25, cy + rw2 / 2);

        // Reset stroke
        g2d.setStroke(new BasicStroke(1.0f));
    }

    private void drawCrossingsAndMarkings(Graphics2D g2d, int cx, int cy) {
        int rw2 = roadWidth / 2;

        // Solid White Stop Lines
        g2d.setColor(new Color(250, 250, 250));
        g2d.setStroke(new BasicStroke(3.5f));

        // North Stop Line (Approaching vehicles are on West half: cx - rw2 to cx)
        g2d.drawLine(cx - rw2, cy - stopLineOffset, cx, cy - stopLineOffset);

        // South Stop Line (Approaching vehicles are on East half: cx to cx + rw2)
        g2d.drawLine(cx, cy + stopLineOffset, cx + rw2, cy + stopLineOffset);

        // East Stop Line (Approaching vehicles are on North half: cy - rw2 to cy)
        g2d.drawLine(cx + stopLineOffset, cy - rw2, cx + stopLineOffset, cy);

        // West Stop Line (Approaching vehicles are on South half: cy to cy + rw2)
        g2d.drawLine(cx - stopLineOffset, cy, cx - stopLineOffset, cy + rw2);

        // Pedestrian Zebra Crossings (drawn behind stop lines)
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.setColor(new Color(245, 245, 245, 210));

        // North Zebra Crossing
        for (int x = cx - rw2 + 4; x < cx + rw2 - 4; x += 12) {
            g2d.fillRect(x, cy - stopLineOffset - 20, 7, 16);
        }

        // South Zebra Crossing
        for (int x = cx - rw2 + 4; x < cx + rw2 - 4; x += 12) {
            g2d.fillRect(x, cy + stopLineOffset + 4, 7, 16);
        }

        // East Zebra Crossing
        for (int y = cy - rw2 + 4; y < cy + rw2 - 4; y += 12) {
            g2d.fillRect(cx + stopLineOffset + 4, y, 16, 7);
        }

        // West Zebra Crossing
        for (int y = cy - rw2 + 4; y < cy + rw2 - 4; y += 12) {
            g2d.fillRect(cx - stopLineOffset - 20, y, 16, 7);
        }

        // Center intersection junction box markings (Yellow hatched box)
        g2d.setColor(new Color(241, 196, 15, 60));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRect(cx - rw2 + 10, cy - rw2 + 10, roadWidth - 20, roadWidth - 20);
        for (int d = -rw2 + 20; d < rw2 - 10; d += 20) {
            g2d.drawLine(cx - rw2 + 10, cy + d, cx - d, cy + rw2 - 10);
            g2d.drawLine(cx + d, cy - rw2 + 10, cx + rw2 - 10, cy - d);
        }
    }

    private void drawStreetDecorations(Graphics2D g2d, int cx, int cy) {
        int rw2 = roadWidth / 2;

        // Street Trees along sidewalks
        int[][] treeCoords = {
                { cx - rw2 - 35, cy - rw2 - 25 }, { cx + rw2 + 35, cy - rw2 - 25 },
                { cx - rw2 - 35, cy + rw2 + 25 }, { cx + rw2 + 35, cy + rw2 + 25 },
                { cx - rw2 - 35, 80 }, { cx + rw2 + 35, 80 },
                { cx - rw2 - 35, getHeight() - 80 }, { cx + rw2 + 35, getHeight() - 80 },
                { 80, cy - rw2 - 35 }, { getWidth() - 80, cy - rw2 - 35 },
                { 80, cy + rw2 + 35 }, { getWidth() - 80, cy + rw2 + 35 }
        };

        for (int[] pt : treeCoords) {
            drawTree(g2d, pt[0], pt[1]);
        }
    }

    private void drawTree(Graphics2D g2d, int tx, int ty) {
        // Tree shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillOval(tx - 12, ty - 8, 24, 20);

        // Trunk base
        g2d.setColor(new Color(80, 50, 30));
        g2d.fillOval(tx - 3, ty - 3, 6, 6);

        // Foliage layers (Dark to light green for depth)
        g2d.setColor(new Color(34, 112, 60));
        g2d.fillOval(tx - 13, ty - 13, 26, 26);
        g2d.setColor(new Color(46, 145, 80));
        g2d.fillOval(tx - 10, ty - 11, 20, 20);
        g2d.setColor(new Color(65, 185, 102));
        g2d.fillOval(tx - 6, ty - 9, 12, 12);
    }

    private void drawTrafficSignals(Graphics2D g2d, int cx, int cy) {
        int rw2 = roadWidth / 2;

        // Positions of traffic signal heads at intersection approaches
        // North Signal: placed on the right curb for incoming North traffic
        drawSignalGantry(g2d, cx - rw2 - 24, cy - stopLineOffset - 18, RoadDirection.NORTH);

        // South Signal: placed on the right curb for incoming South traffic
        drawSignalGantry(g2d, cx + rw2 + 6, cy + stopLineOffset - 12, RoadDirection.SOUTH);

        // East Signal: placed on the right curb for incoming East traffic
        drawSignalGantry(g2d, cx + stopLineOffset - 12, cy - rw2 - 26, RoadDirection.EAST);

        // West Signal: placed on the right curb for incoming West traffic
        drawSignalGantry(g2d, cx - stopLineOffset - 24, cy + rw2 + 6, RoadDirection.WEST);
    }

    private void drawSignalGantry(Graphics2D g2d, int sx, int sy, RoadDirection direction) {
        TrafficSignal signal = controller.getRoads().get(direction).getSignal();
        TrafficSignal.State state = signal.getState();
        int countdown = signal.getCountdownSeconds();

        // Signal Housing Box (Dark metal)
        g2d.setColor(new Color(15, 20, 25));
        g2d.fillRoundRect(sx, sy, 22, 54, 6, 6);
        g2d.setColor(new Color(70, 80, 90));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(sx, sy, 22, 54, 6, 6);

        // Red Light
        boolean isRed = state == TrafficSignal.State.RED;
        drawLedLight(g2d, sx + 5, sy + 5, new Color(240, 40, 40), isRed);

        // Yellow Light
        boolean isYellow = state == TrafficSignal.State.YELLOW;
        drawLedLight(g2d, sx + 5, sy + 21, new Color(245, 190, 25), isYellow);

        // Green Light
        boolean isGreen = state == TrafficSignal.State.GREEN;
        drawLedLight(g2d, sx + 5, sy + 37, new Color(40, 230, 90), isGreen);

        // Digital Countdown Badge next to signal
        g2d.setColor(new Color(10, 15, 20, 220));
        g2d.fillRoundRect(sx - 2, sy + 58, 26, 16, 4, 4);
        g2d.setColor(isGreen ? new Color(46, 204, 113) : isYellow ? new Color(241, 196, 15) : new Color(231, 76, 60));
        g2d.setFont(new Font("Consolas", Font.BOLD, 11));
        String cdText = String.format("%02d", countdown);
        g2d.drawString(cdText, sx + 5, sy + 70);
    }

    private void drawLedLight(Graphics2D g2d, int lx, int ly, Color activeColor, boolean isActive) {
        int diameter = 12;
        if (isActive) {
            // Halo glow
            g2d.setColor(new Color(activeColor.getRed(), activeColor.getGreen(), activeColor.getBlue(), 80));
            g2d.fillOval(lx - 4, ly - 4, diameter + 8, diameter + 8);

            // Active LED
            g2d.setColor(activeColor);
            g2d.fillOval(lx, ly, diameter, diameter);

            // Inner hot core
            g2d.setColor(Color.WHITE);
            g2d.fillOval(lx + 3, ly + 3, diameter - 6, diameter - 6);
        } else {
            // Inactive dimmed lens
            g2d.setColor(new Color(activeColor.getRed() / 4, activeColor.getGreen() / 4, activeColor.getBlue() / 4));
            g2d.fillOval(lx, ly, diameter, diameter);
        }
    }

    private void drawVehicles(Graphics2D g2d) {
        for (RoadDirection dir : RoadDirection.values()) {
            Road road = controller.getRoads().get(dir);
            if (road == null) continue;

            List<Vehicle> list = road.getActiveVehicles();
            synchronized (list) {
                for (Vehicle v : list) {
                    v.draw(g2d);
                }
            }
        }
    }

    private void drawHudOverlay(Graphics2D g2d, int w, int h) {
        // Emergency Preemption Alert banner across the top if active
        EmergencyManager em = controller.getEmergencyManager();
        if (em.hasPendingRequests()) {
            EmergencyManager.EmergencyRequest active = em.getActiveRequest();
            String alertText = (active != null)
                    ? "EMERGENCY CLEARANCE ACTIVE: " + active.getVehicleType().toUpperCase() + " ON " + active.getRoadDirection().getDisplayName().toUpperCase()
                    : "EMERGENCY REQUEST QUEUED: PREPARING INTERSECTION";

            g2d.setColor(new Color(220, 20, 30, 215));
            g2d.fillRoundRect(w / 2 - 240, 10, 480, 30, 8, 8);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int tw = fm.stringWidth(alertText);
            g2d.drawString(alertText, w / 2 - tw / 2, 30);
        }

        // Compass Rose / Navigation Indicator at bottom-right
        int compX = w - 50;
        int compY = h - 50;
        g2d.setColor(new Color(20, 25, 35, 180));
        g2d.fillOval(compX - 20, compY - 20, 40, 40);
        g2d.setColor(new Color(230, 235, 240));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2d.drawString("N", compX - 4, compY - 7);
        g2d.setColor(new Color(220, 50, 50));
        int[] nx = { compX, compX - 4, compX + 4 };
        int[] ny = { compY - 14, compY - 6, compY - 6 };
        g2d.fillPolygon(nx, ny, 3);
    }
}
