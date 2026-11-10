package com.smartcity.traffic;

import javax.swing.*;
import java.awt.*;

/**
 * Main entry point for the Smart City Traffic Control System.
 * B.Tech Computer Science Engineering Project.
 * Initializes the modern UI theme and launches the Dashboard on the Event Dispatch Thread (EDT).
 */
public class Main {
    public static void main(String[] args) {
        // Ensure UI is constructed on the Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel for modern native styling
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Set UI font defaults for crisp presentation
                Font defaultFont = new Font("Segoe UI", Font.PLAIN, 13);
                UIManager.put("Label.font", defaultFont);
                UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
                UIManager.put("TextField.font", defaultFont);
                UIManager.put("Spinner.font", defaultFont);
            } catch (Exception ignored) {
                // Fallback to standard Swing look and feel
            }

            Dashboard dashboard = new Dashboard();
            dashboard.setVisible(true);
        });
    }
}
