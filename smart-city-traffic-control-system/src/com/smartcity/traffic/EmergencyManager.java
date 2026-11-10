package com.smartcity.traffic;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Manages emergency response vehicle priority using a strict First-In, First-Out (FIFO) queue.
 * Coordinates preemption of normal adaptive signals, intersection clearance, and post-exit resumption.
 */
public class EmergencyManager {

    public static class EmergencyRequest {
        private final String requestId;
        private final RoadDirection roadDirection;
        private final String vehicleType;
        private final EmergencyVehicle vehicle;
        private final long timestamp;

        public EmergencyRequest(String requestId, RoadDirection roadDirection, String vehicleType, EmergencyVehicle vehicle) {
            this.requestId = requestId;
            this.roadDirection = roadDirection;
            this.vehicleType = vehicleType;
            this.vehicle = vehicle;
            this.timestamp = System.currentTimeMillis();
        }

        public String getRequestId() { return requestId; }
        public RoadDirection getRoadDirection() { return roadDirection; }
        public String getVehicleType() { return vehicleType; }
        public EmergencyVehicle getVehicle() { return vehicle; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return "[" + requestId + "] " + vehicleType + " on " + roadDirection.getDisplayName();
        }
    }

    private final Queue<EmergencyRequest> emergencyQueue;
    private EmergencyRequest activeRequest;
    private boolean isClearingIntersectionForEmergency;

    public EmergencyManager() {
        this.emergencyQueue = new LinkedList<>();
        this.activeRequest = null;
        this.isClearingIntersectionForEmergency = false;
    }

    /**
     * Enqueues an emergency vehicle request in strict FIFO order.
     */
    public synchronized void enqueueRequest(EmergencyRequest request) {
        emergencyQueue.offer(request);
    }

    public synchronized boolean hasPendingRequests() {
        return !emergencyQueue.isEmpty() || activeRequest != null;
    }

    public synchronized EmergencyRequest peekNextRequest() {
        return emergencyQueue.peek();
    }

    public synchronized EmergencyRequest pollNextRequest() {
        return emergencyQueue.poll();
    }

    public synchronized EmergencyRequest getActiveRequest() {
        return activeRequest;
    }

    public synchronized void setActiveRequest(EmergencyRequest activeRequest) {
        this.activeRequest = activeRequest;
    }

    public synchronized boolean isClearingIntersectionForEmergency() {
        return isClearingIntersectionForEmergency;
    }

    public synchronized void setClearingIntersectionForEmergency(boolean clearing) {
        this.isClearingIntersectionForEmergency = clearing;
    }

    public synchronized List<EmergencyRequest> getPendingList() {
        return new LinkedList<>(emergencyQueue);
    }

    public synchronized int getQueueSize() {
        return emergencyQueue.size() + (activeRequest != null ? 1 : 0);
    }

    public synchronized void clear() {
        emergencyQueue.clear();
        activeRequest = null;
        isClearingIntersectionForEmergency = false;
    }
}
