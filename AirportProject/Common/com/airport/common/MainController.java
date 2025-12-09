package com.airport.common;

/**
 * Controller interface the UI implements to receive events from Plane threads
 * and to allow other modules (PerformanceTester) to interact with the UI.
 */
public interface MainController {
    void log(String message);
    void updateRunwayVisuals();
    void updateGateStatus();
    void updateQueue(int planeId, String status);

    /**
     * Called by a Plane when it fully finishes its lifecycle (landing+docking OR takeoff).
     * PerformanceTester listens to these notifications during a benchmark run.
     */
    void notifyPlaneFinished(Plane p);
}
