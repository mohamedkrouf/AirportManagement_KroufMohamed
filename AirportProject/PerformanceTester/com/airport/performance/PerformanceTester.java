package com.airport.performance;

import com.airport.common.Plane;
import com.airport.gui.AirportFrame;
import com.airport.lock.LockAirport;
import com.airport.monitor.MonitorAirport;
import com.airport.semaphore.SemaphoreAirport;
import com.airport.common.MainController;
import com.airport.common.AirportManager;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * PerformanceTester runs a hybrid benchmark for the three implementations.
 * It runs a small number of real Plane threads per algorithm (visual + measured),
 * gathers latencies, and reports a ranking.
 */
public class PerformanceTester {

    private final AirportFrame ui;
    private volatile boolean running = false;

    // Hybrid sample sizes
    private final int arrivalsPerRun;
    private final int departuresPerRun;

    // Results aggregated per algorithm (average latency in ns)
    private final Map<String, Long> resultsNs = Collections.synchronizedMap(new HashMap<>());

    public PerformanceTester(AirportFrame ui, int arrivalsPerRun, int departuresPerRun) {
        this.ui = ui;
        this.arrivalsPerRun = arrivalsPerRun;
        this.departuresPerRun = departuresPerRun;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Run all three algorithms sequentially (Semaphore, ReentrantLock, Monitor)
     */
    public void runAllBenchmarks() {
        if (running) return;
        running = true;

        new Thread(() -> {
            SwingUtilities.invokeLater(() -> ui.log("\n=== Starting Hybrid Benchmark Suite ==="));

            // Clear old results
            resultsNs.clear();

            runSingle("Semaphore");
            runSingle("ReentrantLock");
            runSingle("Monitor");

            SwingUtilities.invokeLater(() -> {
                running = false;
                ui.log("\n=== Benchmark Suite Complete ===");
                showResultsPopup();
            });
        }).start();
    }

    private void runSingle(String algoName) {
        SwingUtilities.invokeLater(() -> ui.log("\n-- Running: " + algoName + " --"));

        // Prepare AirportManager instance
        AirportManager manager;
        switch (algoName) {
            case "Semaphore":
                manager = new SemaphoreAirport(1, 3);
                break;
            case "ReentrantLock":
                manager = new LockAirport(1, 3);
                break;
            default:
                manager = new MonitorAirport(1, 3);
        }

        // Switch UI to this algorithm
        SwingUtilities.invokeLater(() -> ui.forceSwitchAlgorithm(algoName, manager));

        // Total planes
        int total = arrivalsPerRun + departuresPerRun;
        CountDownLatch latch = new CountDownLatch(total);
        List<Plane> finished = Collections.synchronizedList(new ArrayList<>());

        // A temporary controller glued to UI but also collecting plane finish events
        MainController collectorController = new MainController() {

            @Override
            public void log(String message) { ui.log(message); }

            @Override
            public void updateRunwayVisuals() { ui.updateRunwayVisuals(); }

            @Override
            public void updateGateStatus() { ui.updateGateStatus(); }

            @Override
            public void updateQueue(int planeId, String status) { ui.updateQueue(planeId, status); }

            @Override
            public void notifyPlaneFinished(Plane p) {
                finished.add(p);
                latch.countDown();
                ui.notifyPlaneFinished(p);
            }
        };

        // Start IDs randomized so benchmark runs don't spam same IDs
        int idBase = new Random().nextInt(1000);

        // Spawn ARRIVAL planes
        for (int i = 0; i < arrivalsPerRun; i++) {
            Plane p = new Plane(idBase + i + 1, manager, true, collectorController);
            new Thread(p).start();
            sleepQuiet(80);
        }

        // Spawn DEPARTURE planes
        for (int i = 0; i < departuresPerRun; i++) {
            Plane p = new Plane(idBase + arrivalsPerRun + i + 1, manager, false, collectorController);
            new Thread(p).start();
            sleepQuiet(80);
        }

        // Wait for all planes to complete
        try {
            latch.await();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        // --- Calculate results ---
        long totalNs = 0;
        long maxNs = 0;

        for (Plane p : finished) {
            long l = p.getLatencyNs();
            totalNs += l;
            if (l > maxNs) maxNs = l;
        }

        long avgNs = finished.isEmpty() ? 0 : totalNs / finished.size();
        resultsNs.put(algoName, avgNs);

        // Prepare final variables for lambda
        final long finalAvgNs = avgNs;
        final long finalMaxNs = maxNs;
        final int planeCount = finished.size();

        SwingUtilities.invokeLater(() -> {
            ui.log(String.format("Result [%s] — planes: %d, avg: %d ms, max: %d ms",
                    algoName,
                    planeCount,
                    finalAvgNs / 1_000_000,
                    finalMaxNs / 1_000_000
            ));
        });

        // Pause between algorithms
        sleepQuiet(600);
    }

    private void showResultsPopup() {
        List<Map.Entry<String, Long>> list = new ArrayList<>(resultsNs.entrySet());
        list.sort(Comparator.comparingLong(Map.Entry::getValue));

        StringBuilder sb = new StringBuilder();
        sb.append("Benchmark Ranking (lower = better avg latency):\n\n");

        int rank = 1;
        for (Map.Entry<String, Long> e : list) {
            sb.append(String.format("%d) %s — avg: %d ms\n",
                    rank++, e.getKey(), e.getValue() / 1_000_000));
        }

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(ui, sb.toString(),
                    "Benchmark Results", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void sleepQuiet(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
