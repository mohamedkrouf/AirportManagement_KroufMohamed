package com.airport.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.airport.common.*;
import com.airport.semaphore.SemaphoreAirport;
import com.airport.lock.LockAirport;
import com.airport.monitor.MonitorAirport;
import com.airport.performance.PerformanceTester;

public class AirportFrame extends JFrame implements MainController {

    private static final long serialVersionUID = 1L;

    // Components
    private JTextArea logsArea;
    private JPanel runwayPanel;
    private JPanel gatePanel;
    private JList<String> queueList;
    private DefaultListModel<String> queueModel;

    // Logic
    private AirportManager airportManager;
    private int planeIdCounter = 1;

    // UI: Title + Toggle Buttons
    private JLabel algoTitleLabel;
    private JToggleButton btnSemaphore;
    private JToggleButton btnLock;
    private JToggleButton btnMonitor;
    private ButtonGroup algoGroup;

    // Performance tester
    private final PerformanceTester tester;

    public AirportFrame() {

        setTitle("Airport Management - Projet 2026");
        setSize(1050, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 247)); // Apple light gray

        // Default Algorithm
        airportManager = new MonitorAirport(1, 3);

        // Init tester (Hybrid: 3 arrivals + 3 departures per algorithm)
        tester = new PerformanceTester(this, 3, 3);

        // Apply Apple window look
        getContentPane().setBackground(new Color(245, 245, 247));

        // ----------------------------------------------------
        // HEADER BAR — APPLE STYLE
        // ----------------------------------------------------
        JPanel header = new JPanel(new BorderLayout()) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(250, 250, 250),
                        0, getHeight(), new Color(230, 230, 232)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        algoTitleLabel = new JLabel("Algorithm: Monitor", SwingConstants.CENTER);
        algoTitleLabel.setFont(new Font("SF Pro Display", Font.BOLD, 20));
        algoTitleLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));

        // Segmented buttons
        btnSemaphore = createSegmentButton("Semaphore");
        btnLock = createSegmentButton("Lock");
        btnMonitor = createSegmentButton("Monitor");
        btnMonitor.setSelected(true);

        algoGroup = new ButtonGroup();
        algoGroup.add(btnSemaphore);
        algoGroup.add(btnLock);
        algoGroup.add(btnMonitor);

        JPanel segmented = new JPanel();
        segmented.setOpaque(false);
        segmented.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 3));
        segmented.add(btnSemaphore);
        segmented.add(btnLock);
        segmented.add(btnMonitor);

        header.add(algoTitleLabel, BorderLayout.NORTH);
        header.add(segmented, BorderLayout.CENTER);

        // Benchmark button on the top-right
        JButton benchmarkBtn = createMacButton("Run Test");
        benchmarkBtn.setPreferredSize(new Dimension(140, 34));
        JPanel rightContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightContainer.setOpaque(false);
        rightContainer.add(benchmarkBtn);
        header.add(rightContainer, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Toggle listeners
        btnSemaphore.addActionListener(e -> switchAlgorithm("Semaphore"));
        btnLock.addActionListener(e -> switchAlgorithm("Lock"));
        btnMonitor.addActionListener(e -> switchAlgorithm("Monitor"));

        // Benchmark button listener
        benchmarkBtn.addActionListener(e -> {
            if (!tester.isRunning()) {
                // Disable controls while benchmark runs
                setAllControlsEnabled(false);
                new Thread(() -> {
                    tester.runAllBenchmarks();
                    // Re-enable after run
                    SwingUtilities.invokeLater(() -> setAllControlsEnabled(true));
                }).start();
            } else {
                log("Benchmark already running.");
            }
        });

        // -----------------------------
        // SIDEBAR CONTROLS
        // -----------------------------
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));
        controls.setBackground(new Color(248, 248, 249));

        JButton addArrivalBtn = createMacButton("Add Arrival Plane");
        JButton addDepartureBtn = createMacButton("Add Departure Plane");

        addArrivalBtn.addActionListener(ev -> spawnPlane(true));
        addDepartureBtn.addActionListener(ev -> spawnPlane(false));

        JLabel ctrlTitle = new JLabel("Control Tower");
        ctrlTitle.setFont(new Font("SF Pro Display", Font.BOLD, 16));
        ctrlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        controls.add(ctrlTitle);
        controls.add(Box.createVerticalStrut(25));
        controls.add(addArrivalBtn);
        controls.add(Box.createVerticalStrut(10));
        controls.add(addDepartureBtn);

        add(controls, BorderLayout.WEST);

        // -----------------------------
        // DASHBOARD (4 QUADRANTS)
        // -----------------------------
        JPanel dashboard = new JPanel(new GridLayout(2, 2, 12, 12));
        dashboard.setBackground(new Color(245, 245, 247));
        dashboard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Queue
        queueModel = new DefaultListModel<>();
        queueList = new JList<>(queueModel);
        queueList.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        JPanel q1 = createShadowCard("Queue", new JScrollPane(queueList));

        // Runways
        runwayPanel = new JPanel();
        JPanel q2 = createShadowCard("Runways", runwayPanel);

        // Logs
        logsArea = new JTextArea();
        logsArea.setEditable(false);
        logsArea.setFont(new Font("SF Mono", Font.PLAIN, 13));
        JPanel q3 = createShadowCard("Event Logs", new JScrollPane(logsArea));

        // Gates
        gatePanel = new JPanel();
        JPanel q4 = createShadowCard("Gates", gatePanel);

        dashboard.add(q1);
        dashboard.add(q2);
        dashboard.add(q3);
        dashboard.add(q4);

        add(dashboard, BorderLayout.CENTER);

        // Initialize visuals
        updateRunwayVisuals();
        updateGateStatus();
    }

    // ----------------  UI helpers ----------------

    private JToggleButton createSegmentButton(String text) {
        JToggleButton btn = new JToggleButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
        btn.setPreferredSize(new Dimension(120, 35));
        btn.setBackground(new Color(240, 240, 242));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!btn.isSelected())
                    btn.setBackground(new Color(225, 225, 227));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!btn.isSelected())
                    btn.setBackground(new Color(240, 240, 242));
            }
        });

        btn.addActionListener(e -> {
            btnSemaphore.setBackground(new Color(240, 240, 242));
            btnLock.setBackground(new Color(240, 240, 242));
            btnMonitor.setBackground(new Color(240, 240, 242));

            btn.setBackground(new Color(200, 200, 205));
        });

        return btn;
    }

    private JButton createMacButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SF Pro Text", Font.PLAIN, 15));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(245, 245, 246));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    private JPanel createShadowCard(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(Color.WHITE);
            }
        };

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 227)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SF Pro Text", Font.BOLD, 16));
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        card.add(lbl, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    // ---------------- core logic ----------------

    private void spawnPlane(boolean isArrival) {
        Plane p = new Plane(planeIdCounter++, airportManager, isArrival, this);
        new Thread(p).start();
    }

    /**
     * Switch algorithm, called by toggle buttons or externally via forceSwitchAlgorithm.
     */
    private void switchAlgorithm(String selected) {
        switch (selected) {
            case "Semaphore":
                airportManager = new SemaphoreAirport(1, 3);
                algoTitleLabel.setText("Algorithm: Semaphore");
                log("Switched to Semaphore Algorithm");
                break;

            case "Lock":
                airportManager = new LockAirport(1, 3);
                algoTitleLabel.setText("Algorithm: ReentrantLock");
                log("Switched to ReentrantLock Algorithm");
                break;

            case "Monitor":
                airportManager = new MonitorAirport(1, 3);
                algoTitleLabel.setText("Algorithm: Monitor");
                log("Switched to Monitor Algorithm");
                break;
        }

        updateRunwayVisuals();
        updateGateStatus();
    }

    /**
     * Force switch used by PerformanceTester: apply the provided AirportManager instance and
     * update the UI to show the provided algorithm label.
     */
    public void forceSwitchAlgorithm(String algoName, AirportManager manager) {
        // Update internal airport manager to the provided instance
        this.airportManager = manager;

        // Update toggle visuals and title on EDT
        SwingUtilities.invokeLater(() -> {
            algoTitleLabel.setText("Algorithm: " + (algoName.equals("ReentrantLock") ? "ReentrantLock" : algoName));
            if ("Semaphore".equals(algoName)) btnSemaphore.setSelected(true);
            else if ("ReentrantLock".equals(algoName)) btnLock.setSelected(true);
            else btnMonitor.setSelected(true);

            // Ensure segmented colors update
            btnSemaphore.doClick(0); // triggers color update; harmless
            btnLock.doClick(0);
            btnMonitor.doClick(0);

            updateRunwayVisuals();
            updateGateStatus();
        });
    }

    /**
     * Called by the PerformanceTester when it wants to switch algorithm using only the algorithm name.
     * This variant will construct the AirportManager itself (used by manual UI toggles).
     */
    public void forceSwitchAlgorithm(String algoName) {
        switchAlgorithm(algoName);
    }

    /**
     * Disable/enable controls while benchmark runs
     */
    private void setAllControlsEnabled(boolean enabled) {
        btnSemaphore.setEnabled(enabled);
        btnLock.setEnabled(enabled);
        btnMonitor.setEnabled(enabled);
    }

    // ---------------- MainController implementation ----------------

    @Override
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logsArea.append(message + "\n");
            logsArea.setCaretPosition(logsArea.getDocument().getLength());
        });
    }

    @Override
    public void updateRunwayVisuals() {
        int free = airportManager.getFreeRunways();
        int total = airportManager.getTotalRunways();
        int occupied = total - free;

        SwingUtilities.invokeLater(() -> {
            runwayPanel.removeAll();
            runwayPanel.setLayout(new GridLayout(total, 1, 5, 5));

            for (int i = 0; i < occupied; i++) {
                JLabel block = new JLabel("Runway " + (i + 1) + " (Busy)");
                block.setHorizontalAlignment(SwingConstants.CENTER);
                block.setOpaque(true);
                block.setBackground(new Color(255, 100, 100));
                block.setForeground(Color.WHITE);
                runwayPanel.add(block);
            }

            for (int i = 0; i < free; i++) {
                JLabel block = new JLabel("Runway " + (occupied + i + 1) + " (Free)");
                block.setHorizontalAlignment(SwingConstants.CENTER);
                block.setOpaque(true);
                block.setBackground(new Color(180, 255, 180));
                runwayPanel.add(block);
            }

            runwayPanel.revalidate();
            runwayPanel.repaint();
        });
    }

    @Override
    public void updateGateStatus() {
        SwingUtilities.invokeLater(() -> {
            int free = airportManager.getFreeGates();
            int total = airportManager.getTotalGates();
            int occupied = total - free;

            gatePanel.removeAll();
            gatePanel.setLayout(new GridLayout(total, 1, 5, 5));

            for (int i = 0; i < occupied; i++) {
                JLabel g = new JLabel("Gate " + (i + 1) + " (Occupied)");
                g.setHorizontalAlignment(SwingConstants.CENTER);
                g.setOpaque(true);
                g.setBackground(new Color(255, 200, 120));
                gatePanel.add(g);
            }

            for (int i = 0; i < free; i++) {
                JLabel g = new JLabel("Gate " + (occupied + i + 1) + " (Free)");
                g.setHorizontalAlignment(SwingConstants.CENTER);
                g.setOpaque(true);
                g.setBackground(new Color(200, 255, 200));
                gatePanel.add(g);
            }

            gatePanel.revalidate();
            gatePanel.repaint();
        });
    }

    @Override
    public void updateQueue(int planeId, String status) {
        SwingUtilities.invokeLater(() -> {
            queueModel.addElement("Plane " + planeId + ": " + status);
        });
    }

    /**
     * This method receives plane-finished notifications from Plane threads and forwards them.
     * PerformanceTester installs its own short-lived controller bridge when it runs tests,
     * so this method primarily keeps the UI aware.
     */
    @Override
    public void notifyPlaneFinished(Plane p) {
        // We keep this method lightweight: simply log a short message for visibility.
        SwingUtilities.invokeLater(() -> log("Plane " + p.getId() + " finished (latency: " + (p.getLatencyNs() / 1_000_000) + " ms)"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AirportFrame().setVisible(true));
    }
}
