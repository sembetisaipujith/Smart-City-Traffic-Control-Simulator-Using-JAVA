package com.smartcity.traffic;

import java.awt.*;

/**
 * Abstract base class representing a vehicle in the traffic simulation.
 * Demonstrates Object-Oriented principles: Abstraction, Encapsulation, and Polymorphism.
 */
public abstract class Vehicle {
    public enum State {
        APPROACHING, // Traveling towards intersection
        WAITING,     // Stopped at queue or stop line
        CROSSING,    // Currently inside intersection box
        EXITED       // Cleared intersection and exiting the map
    }

    protected final String id;
    protected final String vehicleType;
    protected final RoadDirection originDirection;
    
    // Spatial coordinates & dimensions
    protected double x;
    protected double y;
    protected final int width;
    protected final int length;
    
    // Physics & Movement
    protected double currentSpeed;
    protected final double maxSpeed;
    protected final double acceleration;
    protected final double deceleration;
    
    // State & Statistics
    protected State state;
    protected Color primaryColor;
    protected Color accentColor;
    protected int laneIndex; // 0: inner lane, 1: outer lane
    protected long spawnTimestamp;
    protected long waitTimeMillis;
    protected long crossingTimestamp;
    protected long exitTimestamp;
    protected boolean isEmergency;

    public Vehicle(String id, String vehicleType, RoadDirection originDirection,
                   int width, int length, double maxSpeed, Color primaryColor, Color accentColor,
                   int laneIndex) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.originDirection = originDirection;
        this.width = width;
        this.length = length;
        this.maxSpeed = maxSpeed;
        this.currentSpeed = 0.0;
        this.acceleration = 0.25;
        this.deceleration = 0.35;
        this.primaryColor = primaryColor;
        this.accentColor = accentColor;
        this.laneIndex = laneIndex;
        this.state = State.APPROACHING;
        this.spawnTimestamp = System.currentTimeMillis();
        this.waitTimeMillis = 0;
        this.isEmergency = false;
    }

    /**
     * Polymorphic drawing method. Each concrete vehicle class provides its custom rendering.
     * @param g2d Graphics2D context with rendering hints enabled
     */
    public abstract void draw(Graphics2D g2d);

    /**
     * Updates physics, speed, and position based on target speed.
     */
    public void updatePhysics(double targetSpeed) {
        if (currentSpeed < targetSpeed) {
            currentSpeed = Math.min(targetSpeed, currentSpeed + acceleration);
        } else if (currentSpeed > targetSpeed) {
            currentSpeed = Math.max(targetSpeed, currentSpeed - deceleration);
        }

        // Apply movement vector according to road direction
        switch (originDirection) {
            case NORTH -> y += currentSpeed; // Moving downwards
            case SOUTH -> y -= currentSpeed; // Moving upwards
            case EAST  -> x -= currentSpeed; // Moving leftwards
            case WEST  -> x += currentSpeed; // Moving rightwards
        }

        // Accumulate wait time if vehicle is stationary
        if (state == State.WAITING || (currentSpeed < 0.1 && state == State.APPROACHING)) {
            waitTimeMillis += 30; // Approx frame delta (30-33ms)
        }
    }

    // Getters and Setters (Encapsulation)
    public String getId() { return id; }
    public String getVehicleType() { return vehicleType; }
    public RoadDirection getOriginDirection() { return originDirection; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public int getWidth() { return width; }
    public int getLength() { return length; }
    public double getCurrentSpeed() { return currentSpeed; }
    public void setCurrentSpeed(double speed) { this.currentSpeed = speed; }
    public double getMaxSpeed() { return maxSpeed; }
    public State getState() { return state; }
    public void setState(State state) { 
        this.state = state;
        if (state == State.CROSSING && crossingTimestamp == 0) {
            crossingTimestamp = System.currentTimeMillis();
        } else if (state == State.EXITED && exitTimestamp == 0) {
            exitTimestamp = System.currentTimeMillis();
        }
    }
    public int getLaneIndex() { return laneIndex; }
    public void setLaneIndex(int laneIndex) { this.laneIndex = laneIndex; }
    public long getWaitTimeMillis() { return waitTimeMillis; }
    public boolean isEmergency() { return isEmergency; }
    public Color getPrimaryColor() { return primaryColor; }

    /**
     * Calculates distance to front bumper along travel direction.
     */
    public double getFrontEdge() {
        return switch (originDirection) {
            case NORTH -> y + length / 2.0;
            case SOUTH -> y - length / 2.0;
            case EAST  -> x - length / 2.0;
            case WEST  -> x + length / 2.0;
        };
    }

    /**
     * Calculates distance to rear bumper along travel direction.
     */
    public double getRearEdge() {
        return switch (originDirection) {
            case NORTH -> y - length / 2.0;
            case SOUTH -> y + length / 2.0;
            case EAST  -> x + length / 2.0;
            case WEST  -> x - length / 2.0;
        };
    }
}
