package com.airport.lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.airport.common.AirportManager;

public class LockAirport implements AirportManager {

    private final int totalRunways;
    private final int totalGates;
    
    // Shared State
    private int freeRunways;
    private int freeGates;
    private int waitingArrivals = 0; // To handle priority

    // Locks and Conditions
    private final Lock lock = new ReentrantLock(true); // Fair lock
    private final Condition runwayFree = lock.newCondition();
    private final Condition gateFree = lock.newCondition();

    public LockAirport(int numRunways, int numGates) {
        this.totalRunways = numRunways;
        this.totalGates = numGates;
        this.freeRunways = numRunways;
        this.freeGates = numGates;
    }

    // --- ARRIVAL LOGIC ---

    @Override
    public void requestRunwayForLanding(int planeId) {
        lock.lock();
        try {
            // 1. Indicate a high-priority arrival is waiting
            waitingArrivals++;

            // 2. Wait if no runways are available
            while (freeRunways == 0) {
                runwayFree.await();
            }

            // 3. Acquire runway
            freeRunways--;
            
            // 4. We got the runway, so we are no longer "waiting" for it
            waitingArrivals--;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void finishLandingAndDock(int planeId) {
        lock.lock();
        try {
            // 1. Wait for a gate (holding the runway!)
            while (freeGates == 0) {
                gateFree.await();
            }

            // 2. Acquire gate
            freeGates--;

            // 3. Release runway
            freeRunways++;
            runwayFree.signalAll(); // Wake up waiting planes (Arrivals or Departures)

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    // --- DEPARTURE LOGIC ---

    @Override
    public void requestRunwayForTakeoff(int planeId) {
        lock.lock();
        try {
            // 1. Wait if:
            //    a) No runways are free OR
            //    b) There are arrivals waiting (PRIORITY CHECK)
            while (freeRunways == 0 || waitingArrivals > 0) {
                runwayFree.await();
            }

            // 2. Acquire runway
            freeRunways--;

            // 3. Release gate (departure leaves the gate now)
            freeGates++;
            gateFree.signalAll(); // Wake up planes waiting for gates

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void finishTakeoff(int planeId) {
        lock.lock();
        try {
            // 1. Release runway
            freeRunways++;
            runwayFree.signalAll();

        } finally {
            lock.unlock();
        }
    }

    // --- GETTERS FOR UI ---

    @Override
    public int getFreeRunways() {
        lock.lock();
        try {
            return freeRunways;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getTotalRunways() {
        return totalRunways;
    }

    @Override
    public int getFreeGates() {
        lock.lock();
        try {
            return freeGates;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getTotalGates() {
        return totalGates;
    }
}