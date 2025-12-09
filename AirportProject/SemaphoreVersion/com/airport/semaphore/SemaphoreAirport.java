package com.airport.semaphore;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import com.airport.common.AirportManager;

public class SemaphoreAirport implements AirportManager {
    
    // Resources (Runways and Gates)
    private Semaphore runways; 
    private Semaphore gates;
    private final int totalRunways;
    private final int totalGates;
    // Priority Mechanism
    // This semaphore acts as a mutex lock protecting access to the runways
    // We use a counter to track how many high-priority arrivals are waiting.
    private Semaphore runwayAccessMutex = new Semaphore(1, true); 
    private AtomicInteger waitingArrivals = new AtomicInteger(0);
    
    // Note: Use the actual values from your GUI setup here
    public SemaphoreAirport(int numRunways, int numGates) {
        this.runways = new Semaphore(numRunways, true); // Fair queue
        this.gates = new Semaphore(numGates, true);     // Fair queue
        this.totalRunways = numRunways; // Store the total count
        this.totalGates = numGates ; 
    }
    
    // --- ARRIVAL LOGIC (PRIORITY) ---

    @Override
    public void requestRunwayForLanding(int planeId) {
        // 1. Mark yourself as a waiting Arrival (high priority)
        waitingArrivals.incrementAndGet();

        try {
            // 2. Acquire the main runway access lock (Mutex)
            runwayAccessMutex.acquire(); 
            
            // 3. Acquire the actual Runway resource.
            //    No Departure can pass the mutex if an Arrival is waiting.
            runways.acquire();
            
            // 4. Release the Mutex and decrement the counter
            waitingArrivals.decrementAndGet();
            runwayAccessMutex.release(); 
            
        } catch (InterruptedException e) { 
            // Important: Decrement counter if thread is interrupted
            waitingArrivals.decrementAndGet();
            runwayAccessMutex.release(); 
            Thread.currentThread().interrupt(); 
        }
    }

    @Override
    public void finishLandingAndDock(int planeId) {
        try {
            // 1. Acquire a Gate
            gates.acquire();
            
            // 2. Release the Runway
            runways.release(); 
            
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // --- DEPARTURE LOGIC (LOW PRIORITY) ---

    @Override
    public void requestRunwayForTakeoff(int planeId) {
        try {
            // 1. Try to acquire the Mutex (allows check of waiting arrivals)
            runwayAccessMutex.acquire(); 
            
            // 2. CHECK PRIORITY: If high-priority Arrivals are waiting, block access to the runway.
            //    Departures must yield the runway resource if an Arrival is waiting.
            while (waitingArrivals.get() > 0) {
                 // Release the Mutex so Arrivals can proceed and wait on the runways semaphore
                 runwayAccessMutex.release(); 
                 
                 // Small wait before re-checking to prevent busy-waiting
                 Thread.sleep(50); 
                 
                 // Reacquire the Mutex to check the state again
                 runwayAccessMutex.acquire();
            }
            
            // 3. Acquire the actual Runway resource (if no Arrivals are waiting)
            runways.acquire(); 
            
            // 4. If successful, release the Gate and the Mutex
            gates.release();   
            runwayAccessMutex.release(); 
            
        } catch (InterruptedException e) { 
            runwayAccessMutex.release();
            Thread.currentThread().interrupt(); 
        }
    }
    @Override
    public int getFreeRunways() {
        return runways.availablePermits();
    }
    
    @Override
    public int getTotalRunways() {
        return totalRunways;
    }
    
    public int getFreeGates() {
        return gates.availablePermits();
    }

    public int getTotalGates() {
        return totalGates;
    }

    
    @Override
    public void finishTakeoff(int planeId) {
        // Just releases the runway and exits the system
        runways.release(); 
    }
}