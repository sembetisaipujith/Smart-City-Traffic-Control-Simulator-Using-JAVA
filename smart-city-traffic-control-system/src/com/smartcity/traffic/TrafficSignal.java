package com.smartcity.traffic;

/**
 * Represents a traffic signal at an intersection approach.
 * Encapsulates signal state (RED, YELLOW, GREEN), countdown timer, and transitions.
 */
public class TrafficSignal {
    public enum State {
        RED,
        YELLOW,
        GREEN
    }

    private final RoadDirection direction;
    private State state;
    private int countdownSeconds;
    private int greenDurationSeconds;

    public TrafficSignal(RoadDirection direction) {
        this.direction = direction;
        this.state = State.RED;
        this.countdownSeconds = 0;
        this.greenDurationSeconds = 10;
    }

    public RoadDirection getDirection() {
        return direction;
    }

    public synchronized State getState() {
        return state;
    }

    public synchronized void setState(State state) {
        this.state = state;
    }

    public synchronized int getCountdownSeconds() {
        return countdownSeconds;
    }

    public synchronized void setCountdownSeconds(int seconds) {
        this.countdownSeconds = Math.max(0, seconds);
    }

    public synchronized void decrementCountdown() {
        if (countdownSeconds > 0) {
            countdownSeconds--;
        }
    }

    public synchronized int getGreenDurationSeconds() {
        return greenDurationSeconds;
    }

    public synchronized void setGreenDurationSeconds(int duration) {
        this.greenDurationSeconds = Math.max(3, duration);
    }

    public boolean isGreen() {
        return state == State.GREEN;
    }

    public boolean isYellow() {
        return state == State.YELLOW;
    }

    public boolean isRed() {
        return state == State.RED;
    }

    @Override
    public String toString() {
        return direction + ": " + state + " (" + countdownSeconds + "s)";
    }
}
