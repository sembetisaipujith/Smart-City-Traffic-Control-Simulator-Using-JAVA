package com.smartcity.traffic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Main desktop application window for the Smart City Traffic Control System.
 * Developed in Java Swing with custom dark navy/slate styling, live telemetry,
 * road control cards, emergency dispatch panel, and CSV export functionality.
 */
public class Dashboard extends JFrame implements TrafficController.SimulationListener {
    private static final long serialVersionUID = 1L;

    private final TrafficController controller;
    private final CityMapPanel mapPanel;

    // Top Header & Stats Widgets
    private JLabel clockLabel;
    private JLabel statusPillLabel;
    private JLabel totalRemainingLabel;
    private JLabel totalCompletedLabel;
    private JLabel pendingEmergenciesLabel;
    private JLabel avgWaitTimeLabel;
    private JLabel activeGreenRoadLabel;

    // Road Cards UI components map
    private static class RoadCardWidgets {
        JSpinner countSpinner;
        JLabel densityBadge;
        JLabel signalBadge;
        JLabel countdownLabel;
        JLabel waitTimeLabel;
        JLabel remainingVehiclesLabel;
    }
    private final Map<RoadDirection, RoadCardWidgets> roadCards = new HashMap<>();

    // Master Control Buttons
    private JButton startBtn;
    private JButton pauseBtn;
    private JButton resumeBtn;
    private JButton resetBtn;
    private JButton exportCsvBtn;

    // Emergency Dispatch UI
    private JComboBox<RoadDirection> emergencyRoadCombo;
    private JComboBox<String> emergencyTypeCombo;
    private JButton dispatchEmergencyBtn;

    public Dashboard() {
        super("SMART CITY TRAFFIC CONTROL SYSTEM - Intelligent Simulation");
        this.controller = new TrafficController();
        this.mapPanel = new CityMapPanel(controller);
        controller.setListener(this);

        initUi();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1340, 880);
        setMinimumSize(new Dimension(1150, 750));
        setLocationRelativeTo(null);
    }

    private void initUi() {
        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.setBackground(new Color(15, 23, 42)); // Slate 900 dark background

        // 1. Header Section
        rootPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Content Split: City Map Simulation (Left) + Control Dashboard (Right)
        JPanel contentPanel = new JPanel(new BorderLayout(12, 0));
        contentPanel.setBackground(new Color(15, 23, 42));
        contentPanel.setBorder(new EmptyBorder(10, 16, 14, 16));

        // Center map container with subtle border
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.setBackground(new Color(30, 41, 59));
        mapContainer.setBorder(new LineBorder(new Color(51, 65, 85), 1, true));
        mapContainer.add(mapPanel, BorderLayout.CENTER);
        contentPanel.add(mapContainer, BorderLayout.CENTER);

        // Right side: Master Controls + Road Cards + Emergency Manager
        contentPanel.add(createSideControlPanel(), BorderLayout.EAST);

        rootPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);

        // Initial sync
        updateTelemetry();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42));
        header.setBorder(new EmptyBorder(14, 18, 10, 18));

        // Brand & Title
        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleBox.setOpaque(false);

        JLabel iconLabel = new JLabel("🚦");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        titleBox.add(iconLabel);

        JPanel textStack = new JPanel(new GridLayout(2, 1, 0, 2));
        textStack.setOpaque(false);
        JLabel titleLabel = new JLabel("SMART CITY TRAFFIC CONTROL SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(new Color(248, 250, 252));
        JLabel subtitleLabel = new JLabel("Adaptive Signal Scheduling • Emergency FIFO Queuing • Autonomous Urban Flow");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(148, 163, 184));
        textStack.add(titleLabel);
        textStack.add(subtitleLabel);
        titleBox.add(textStack);

        header.add(titleBox, BorderLayout.WEST);

        // Right Header Widgets: Status & Real-Time Clock
        JPanel statusBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        statusBox.setOpaque(false);

        statusPillLabel = new JLabel("STOPPED");
        statusPillLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusPillLabel.setForeground(new Color(239, 68, 68));
        statusPillLabel.setOpaque(true);
        statusPillLabel.setBackground(new Color(69, 10, 10));
        statusPillLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
        statusBox.add(statusPillLabel);

        clockLabel = new JLabel("00:00:00");
        clockLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        clockLabel.setForeground(new Color(226, 232, 240));
        statusBox.add(clockLabel);

        header.add(statusBox, BorderLayout.EAST);

        // Combine with Top Stats Bar below header
        JPanel combinedHeader = new JPanel(new BorderLayout(0, 8));
        combinedHeader.setOpaque(false);
        combinedHeader.add(header, BorderLayout.NORTH);
        combinedHeader.add(createTopStatsBar(), BorderLayout.SOUTH);

        return combinedHeader;
    }

    private JPanel createTopStatsBar() {
        JPanel statsBar = new JPanel(new GridLayout(1, 5, 10, 0));
        statsBar.setOpaque(false);
        statsBar.setBorder(new EmptyBorder(0, 18, 6, 18));

        totalRemainingLabel = new JLabel("107", SwingConstants.CENTER);
        totalCompletedLabel = new JLabel("0", SwingConstants.CENTER);
        pendingEmergenciesLabel = new JLabel("0", SwingConstants.CENTER);
        avgWaitTimeLabel = new JLabel("0.0s", SwingConstants.CENTER);
        activeGreenRoadLabel = new JLabel("NORTH", SwingConstants.CENTER);

        statsBar.add(createStatCard("TOTAL REMAINING", totalRemainingLabel, new Color(56, 189, 248)));
        statsBar.add(createStatCard("COMPLETED VEHICLES", totalCompletedLabel, new Color(74, 222, 128)));
        statsBar.add(createStatCard("EMERGENCY REQUESTS", pendingEmergenciesLabel, new Color(248, 113, 113)));
        statsBar.add(createStatCard("AVG WAITING TIME", avgWaitTimeLabel, new Color(251, 191, 36)));
        statsBar.add(createStatCard("ACTIVE GREEN ROAD", activeGreenRoadLabel, new Color(168, 85, 247)));

        return statsBar;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(new Color(148, 163, 184));
        card.add(titleLbl, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setForeground(accentColor);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSideControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(460, 700));
        panel.setOpaque(false);

        // Simulation Master Controls (Start, Pause, Resume, Reset, CSV Report)
        panel.add(createMasterControlsPanel(), BorderLayout.NORTH);

        // Middle Scrollable Section: Emergency Dispatch + 4 Road Cards
        JPanel centerCards = new JPanel();
        centerCards.setLayout(new BoxLayout(centerCards, BoxLayout.Y_AXIS));
        centerCards.setOpaque(false);

        centerCards.add(createEmergencyDispatchCard());
        centerCards.add(Box.createVerticalStrut(8));

        // Add 4 Road Control Cards
        for (RoadDirection dir : RoadDirection.values()) {
            centerCards.add(createRoadCard(dir));
            centerCards.add(Box.createVerticalStrut(8));
        }

        JScrollPane scrollPane = new JScrollPane(centerCards);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMasterControlsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(new Color(30, 41, 59));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel title = new JLabel("SIMULATION CONTROL CENTER");
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(new Color(226, 232, 240));
        panel.add(title, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new GridLayout(1, 4, 6, 0));
        btnRow.setOpaque(false);

        startBtn = createStyledButton("START", new Color(34, 197, 94), Color.BLACK);
        pauseBtn = createStyledButton("PAUSE", new Color(234, 179, 8), Color.BLACK);
        resumeBtn = createStyledButton("RESUME", new Color(59, 130, 246), Color.WHITE);
        resetBtn = createStyledButton("RESET", new Color(100, 116, 139), Color.WHITE);

        btnRow.add(startBtn);
        btnRow.add(pauseBtn);
        btnRow.add(resumeBtn);
        btnRow.add(resetBtn);

        exportCsvBtn = createStyledButton("📄 GENERATE & EXPORT CSV REPORT", new Color(79, 70, 229), Color.WHITE);

        JPanel buttonStack = new JPanel(new BorderLayout(0, 6));
        buttonStack.setOpaque(false);
        buttonStack.add(btnRow, BorderLayout.CENTER);
        buttonStack.add(exportCsvBtn, BorderLayout.SOUTH);

        panel.add(buttonStack, BorderLayout.CENTER);

        // Action Listeners
        startBtn.addActionListener(e -> controller.startSimulation());
        pauseBtn.addActionListener(e -> controller.pauseSimulation());
        resumeBtn.addActionListener(e -> controller.resumeSimulation());
        resetBtn.addActionListener(e -> handleResetSimulation());
        exportCsvBtn.addActionListener(e -> handleExportCsvReport());

        return panel;
    }

    private JPanel createEmergencyDispatchCard() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(239, 68, 68), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel title = new JLabel("🚨 EMERGENCY VEHICLE PRIORITY DISPATCH");
        title.setFont(new Font("Segoe UI", Font.BOLD, 11));
        title.setForeground(new Color(248, 113, 113));
        card.add(title, BorderLayout.NORTH);

        JPanel row = new JPanel(new GridLayout(1, 3, 6, 0));
        row.setOpaque(false);

        emergencyTypeCombo = new JComboBox<>(new String[]{ "Ambulance", "Fire Engine", "Police Vehicle" });
        emergencyRoadCombo = new JComboBox<>(RoadDirection.values());
        dispatchEmergencyBtn = createStyledButton("DISPATCH", new Color(220, 38, 38), Color.WHITE);

        dispatchEmergencyBtn.addActionListener(e -> {
            String type = (String) emergencyTypeCombo.getSelectedItem();
            RoadDirection road = (RoadDirection) emergencyRoadCombo.getSelectedItem();
            if (type != null && road != null) {
                controller.getVehicleManager().spawnEmergencyVehicle(road, type);
                updateTelemetry();
            }
        });

        row.add(emergencyTypeCombo);
        row.add(emergencyRoadCombo);
        row.add(dispatchEmergencyBtn);

        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JPanel createRoadCard(RoadDirection dir) {
        Road road = controller.getRoads().get(dir);
        RoadCardWidgets widgets = new RoadCardWidgets();

        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        // Header: Road Name + Density Badge
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel nameLabel = new JLabel(dir.getDisplayName().toUpperCase());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(new Color(241, 245, 249));

        widgets.densityBadge = new JLabel("MEDIUM", SwingConstants.CENTER);
        widgets.densityBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        widgets.densityBadge.setOpaque(true);
        widgets.densityBadge.setBackground(new Color(30, 58, 138));
        widgets.densityBadge.setForeground(new Color(147, 197, 253));
        widgets.densityBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

        headerRow.add(nameLabel, BorderLayout.WEST);
        headerRow.add(widgets.densityBadge, BorderLayout.EAST);
        card.add(headerRow, BorderLayout.NORTH);

        // Middle Grid: Remaining, Signal, Wait Time
        JPanel metrics = new JPanel(new GridLayout(2, 3, 6, 4));
        metrics.setOpaque(false);

        widgets.remainingVehiclesLabel = new JLabel(String.valueOf(road.getRemainingCount()));
        widgets.remainingVehiclesLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        widgets.remainingVehiclesLabel.setForeground(new Color(56, 189, 248));

        widgets.signalBadge = new JLabel("RED");
        widgets.signalBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        widgets.signalBadge.setForeground(new Color(239, 68, 68));

        widgets.countdownLabel = new JLabel("0s");
        widgets.countdownLabel.setFont(new Font("Consolas", Font.BOLD, 12));
        widgets.countdownLabel.setForeground(new Color(203, 213, 225));

        widgets.waitTimeLabel = new JLabel("0.0s");
        widgets.waitTimeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        widgets.waitTimeLabel.setForeground(new Color(251, 191, 36));

        metrics.add(createMiniMetric("Remaining", widgets.remainingVehiclesLabel));
        metrics.add(createMiniMetric("Signal", widgets.signalBadge));
        metrics.add(createMiniMetric("Countdown", widgets.countdownLabel));
        metrics.add(createMiniMetric("Avg Wait", widgets.waitTimeLabel));

        // Vehicle Count Spinner + Update Count Button
        widgets.countSpinner = new JSpinner(new SpinnerNumberModel(road.getInitialCount(), 0, 100, 5));
        JButton updateBtn = createStyledButton("UPDATE", new Color(71, 85, 105), Color.WHITE);
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        updateBtn.addActionListener(e -> {
            int newCount = (Integer) widgets.countSpinner.getValue();
            road.setInitialCount(newCount);
            updateTelemetry();
        });

        JPanel spinnerRow = new JPanel(new BorderLayout(4, 0));
        spinnerRow.setOpaque(false);
        spinnerRow.add(widgets.countSpinner, BorderLayout.CENTER);
        spinnerRow.add(updateBtn, BorderLayout.EAST);

        metrics.add(createMiniMetric("Set Count", spinnerRow));

        // Quick Emergency Button for this road
        JButton quickEmergBtn = createStyledButton("+ EMERG", new Color(153, 27, 27), Color.WHITE);
        quickEmergBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        quickEmergBtn.addActionListener(e -> {
            controller.getVehicleManager().spawnEmergencyVehicle(dir, "Ambulance");
            updateTelemetry();
        });
        metrics.add(createMiniMetric("Priority", quickEmergBtn));

        card.add(metrics, BorderLayout.CENTER);
        roadCards.put(dir, widgets);

        return card;
    }

    private JPanel createMiniMetric(String title, JComponent component) {
        JPanel p = new JPanel(new BorderLayout(0, 1));
        p.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(new Color(148, 163, 184));
        p.add(lbl, BorderLayout.NORTH);
        p.add(component, BorderLayout.CENTER);
        return p;
    }

    

    
private JButton createStyledButton(String text, Color bg, Color fg) {
    JButton btn = new JButton(text);

    btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());

    btn.setBackground(bg);
    btn.setForeground(fg);

    btn.setOpaque(true);
    btn.setContentAreaFilled(true);
    btn.setBorderPainted(true);
    btn.setFocusPainted(false);

    btn.setFont(new Font("Segoe UI", Font.BOLD, 12));

    btn.setBorder(BorderFactory.createCompoundBorder(
        new javax.swing.border.LineBorder(
            new Color(100, 116, 139), 1, true
        ),
        new EmptyBorder(6, 8, 6, 8)
    ));

    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    return btn;
}

    private void handleResetSimulation() {
        Map<RoadDirection, Integer> counts = new HashMap<>();
        for (RoadDirection dir : RoadDirection.values()) {
            RoadCardWidgets w = roadCards.get(dir);
            int count = (w != null) ? (Integer) w.countSpinner.getValue() : 20;
            counts.put(dir, count);
        }
        controller.resetSimulation(counts);
        updateTelemetry();
    }

    private void handleExportCsvReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("traffic_simulation_report_" + System.currentTimeMillis() + ".csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                controller.getReportManager().exportReportToFile(file, controller.getRoads(),
                        controller.getVehicleManager().getTotalEmergencyVehiclesCompleted());
                JOptionPane.showMessageDialog(this,
                        "Simulation Report successfully exported to:\n" + file.getAbsolutePath(),
                        "CSV Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to export CSV: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Updates all HUD labels, stats cards, and individual road cards on the Event Dispatch Thread.
     */
    public void updateTelemetry() {
        // Clock
        clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        // Aggregates
        int totalRem = 0;
        int totalComp = 0;
        double sumWait = 0;

        for (Road road : controller.getRoads().values()) {
            totalRem += road.getRemainingCount();
            totalComp += road.getCompletedCount();
            sumWait += road.getAverageWaitSeconds();

            RoadCardWidgets widgets = roadCards.get(road.getDirection());
            if (widgets != null) {
                // Remaining count (Requirement 6: 20 -> 19 -> 18...)
                widgets.remainingVehiclesLabel.setText(String.valueOf(road.getRemainingCount()));

                // Density Badge
                TrafficDensityCalculator.DensityLevel density = road.getDensityLevel();
                widgets.densityBadge.setText(density.getLabel().toUpperCase());
                switch (density) {
                    case LOW -> {
                        widgets.densityBadge.setBackground(new Color(20, 83, 45));
                        widgets.densityBadge.setForeground(new Color(134, 239, 172));
                    }
                    case MEDIUM -> {
                        widgets.densityBadge.setBackground(new Color(30, 58, 138));
                        widgets.densityBadge.setForeground(new Color(147, 197, 253));
                    }
                    case HIGH -> {
                        widgets.densityBadge.setBackground(new Color(120, 53, 15));
                        widgets.densityBadge.setForeground(new Color(253, 186, 116));
                    }
                    case VERY_HIGH -> {
                        widgets.densityBadge.setBackground(new Color(127, 29, 29));
                        widgets.densityBadge.setForeground(new Color(252, 165, 165));
                    }
                }

                // Signal & Countdown
                TrafficSignal signal = road.getSignal();
                widgets.signalBadge.setText(signal.getState().name());
                widgets.countdownLabel.setText(signal.getCountdownSeconds() + "s");

                switch (signal.getState()) {
                    case GREEN -> widgets.signalBadge.setForeground(new Color(34, 197, 94));
                    case YELLOW -> widgets.signalBadge.setForeground(new Color(234, 179, 8));
                    case RED -> widgets.signalBadge.setForeground(new Color(239, 68, 68));
                }

                // Avg wait time
                widgets.waitTimeLabel.setText(String.format("%.1fs", road.getAverageWaitSeconds()));
            }
        }

        totalRemainingLabel.setText(String.valueOf(totalRem));
        totalCompletedLabel.setText(String.valueOf(totalComp));
        pendingEmergenciesLabel.setText(String.valueOf(controller.getEmergencyManager().getQueueSize()));
        avgWaitTimeLabel.setText(String.format("%.1fs", sumWait / 4.0));

        RoadDirection activeDir = controller.getCurrentGreenRoad();
        activeGreenRoadLabel.setText(activeDir != null ? activeDir.name() : "NONE");
    }

    @Override
    public void onStateChanged(TrafficController.SimulationState newState) {
        SwingUtilities.invokeLater(() -> {
            switch (newState) {
                case RUNNING -> {
                    statusPillLabel.setText("RUNNING");
                    statusPillLabel.setBackground(new Color(20, 83, 45));
                    statusPillLabel.setForeground(new Color(74, 222, 128));
                    startBtn.setEnabled(false);
                    pauseBtn.setEnabled(true);
                    resumeBtn.setEnabled(false);
                }
                case PAUSED -> {
                    statusPillLabel.setText("PAUSED");
                    statusPillLabel.setBackground(new Color(113, 63, 18));
                    statusPillLabel.setForeground(new Color(250, 204, 21));
                    startBtn.setEnabled(false);
                    pauseBtn.setEnabled(false);
                    resumeBtn.setEnabled(true);
                }
                case STOPPED -> {
                    statusPillLabel.setText("STOPPED");
                    statusPillLabel.setBackground(new Color(69, 10, 10));
                    statusPillLabel.setForeground(new Color(248, 113, 113));
                    startBtn.setEnabled(true);
                    pauseBtn.setEnabled(false);
                    resumeBtn.setEnabled(false);
                }
            }
        });
    }

    @Override
    public void onTick() {
        // Repaint Graphics2D canvas and refresh HUD
        mapPanel.repaint();
        updateTelemetry();
    }
}
