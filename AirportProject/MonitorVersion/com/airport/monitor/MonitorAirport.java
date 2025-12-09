package com.airport.monitor;

import com.airport.common.AirportManager;

public class MonitorAirport implements AirportManager {

    private final int totalRunways;
    private final int totalGates;
    
    // Shared State
    private int freeRunways;
    private int freeGates;
    private int waitingArrivals = 0;

    public MonitorAirport(int numRunways, int numGates) {
        this.totalRunways = numRunways;
        this.totalGates = numGates;
        this.freeRunways = numRunways;
        this.freeGates = numGates;
    }

    // --- ARRIVAL LOGIC ---

    @Override
    public synchronized void requestRunwayForLanding(int planeId) {
        // 1. Register high priority wait
        waitingArrivals++;

        try {
            // 2. Wait for runway
            while (freeRunways == 0) {
                wait();
            }
            
            // 3. Acquire runway
            freeRunways--;
            waitingArrivals--; // No longer waiting

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public synchronized void finishLandingAndDock(int planeId) {
        try {
            // 1. Wait for gate
            while (freeGates == 0) {
                wait();
            }
            
            // 2. Acquire gate
            freeGates--;

            // 3. Release runway
            freeRunways++;
            notifyAll(); // Wake everyone up to check conditions

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // --- DEPARTURE LOGIC ---

    @Override
    public synchronized void requestRunwayForTakeoff(int planeId) {
        try {
            // 1. PRIORITY CHECK: Wait if runways full OR arrivals are waiting
            while (freeRunways == 0 || waitingArrivals > 0) {
                wait();
            }

            // 2. Acquire runway
            freeRunways--;

            // 3. Release gate
            freeGates++;
            notifyAll();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public synchronized void finishTakeoff(int planeId) {
        // 1. Release runway
        freeRunways++;
        notifyAll();
    }

    // --- GETTERS FOR UI ---

    @Override
    public synchronized int getFreeRunways() {
        return freeRunways;
    }

    @Override
    public int getTotalRunways() {
        return totalRunways;
    }

    @Override
    public synchronized int getFreeGates() {
        return freeGates;
    }

    @Override
    public int getTotalGates() {
        return totalGates;
    }
}