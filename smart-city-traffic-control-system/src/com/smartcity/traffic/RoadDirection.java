package com.smartcity.traffic;

/**
 * Enumeration of the four connected intersection roads.
 */
public enum RoadDirection {
    NORTH("North Road", 0, 1),
    SOUTH("South Road", 0, -1),
    EAST("East Road", -1, 0),
    WEST("West Road", 1, 0);

    private final String displayName;
    private final int dx; // Movement direction along X-axis
    private final int dy; // Movement direction along Y-axis

    RoadDirection(String displayName, int dx, int dy) {
        this.displayName = displayName;
        this.dx = dx;
        this.dy = dy;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public RoadDirection getOpposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }
}
