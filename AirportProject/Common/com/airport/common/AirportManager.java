package com.airport.common;

public interface AirportManager {
    // Attempt to land. Blocks if no runway.
    void requestRunwayForLanding(int planeId);
    
    // Attempt to dock at a gate. Blocks if no gate. Releases Runway.
    void finishLandingAndDock(int planeId);
    
    // Attempt to leave gate. Blocks if no runway.
    void requestRunwayForTakeoff(int planeId);
    
    // Leave the system. Releases Runway.
    void finishTakeoff(int planeId);
  
    int getFreeRunways();
    int getTotalRunways();

    int getFreeGates();
    int getTotalGates();
    
}