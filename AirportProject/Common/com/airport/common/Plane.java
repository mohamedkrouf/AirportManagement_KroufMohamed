package com.airport.common;

public class Plane implements Runnable {
    private int id;
    private AirportManager airport;
    private boolean isArriving; // true = arrival, false = departure
    private MainController uiController; // To update the UI

    // For performance measurement (nanoseconds)
    private volatile long startTimeNs;
    private volatile long endTimeNs;

    public Plane(int id, AirportManager airport, boolean isArriving, MainController ui) {
        this.id = id;
        this.airport = airport;
        this.isArriving = isArriving;
        this.uiController = ui;
    }

    @Override
    public void run() {
        // Mark start time as soon as thread runs (for hybrid measurement)
        startTimeNs = System.nanoTime();

        try {
            if (isArriving) {
                // PHASE: ARRIVAL
                uiController.log("Plane " + id + " (Arrival) entering airspace.");
                uiController.updateQueue(id, "Waiting for Runway");

                airport.requestRunwayForLanding(id);

                uiController.updateQueue(id, "Landing...");
                uiController.updateRunwayVisuals(); // Occupy runway visual
                Thread.sleep(800); // Simulate landing time (shorter for benchmark snappiness)

                airport.finishLandingAndDock(id);

                uiController.updateRunwayVisuals(); // Free runway visual
                uiController.updateGateStatus();    // Occupy gate visual
                uiController.log("Plane " + id + " docked at gate.");

                // Simulate docking time
                Thread.sleep(700);

                // For arrival-only lifecycle in this sim, plane remains at gate until
                // (we consider finishTime once it docked for arrival measurement)
                // Mark end time here for arrivals
                endTimeNs = System.nanoTime();

                // NOTE: In a real full lifecycle you might wait for departure; for our hybrid
                // benchmark we consider arrival latency as time until docked.

            } else {
                // PHASE: DEPARTURE
                uiController.log("Plane " + id + " (Departure) boarding.");
                uiController.updateGateStatus(); // Starts at gate
                Thread.sleep(600); // Boarding time (shorter)

                airport.requestRunwayForTakeoff(id);

                uiController.updateGateStatus(); // Free gate
                uiController.updateRunwayVisuals(); // Occupy runway
                uiController.log("Plane " + id + " taking off.");
                Thread.sleep(900); // Takeoff time

                airport.finishTakeoff(id);

                uiController.updateRunwayVisuals(); // Free runway
                uiController.log("Plane " + id + " left the system.");

                // Mark end time for departure lifecycle
                endTimeNs = System.nanoTime();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // If interrupted, mark end time anyway
            endTimeNs = System.nanoTime();
        } finally {
            // Notify UI/benchmark that this plane finished its measured lifecycle.
            // The PerformanceTester will read getLatencyNs() to aggregate results.
            try {
                uiController.notifyPlaneFinished(this);
            } catch (Exception ignored) {
            }
        }
    }

    public long getLatencyNs() {
        // If endTime wasn't set (unexpected), use current time
        long end = endTimeNs == 0 ? System.nanoTime() : endTimeNs;
        return Math.max(0L, end - startTimeNs);
    }

    public int getId() {
        return id;
    }

    public boolean isArriving() {
        return isArriving;
    }

    public AirportManager getAirportManager() {
        return airport;
    }
}
