package com.mdm.installer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import java.nio.file.*;
import java.io.IOException;

public class InstallerUI extends JFrame {

    private final JButton installButton;
    private final JLabel statusLabel;

    public InstallerUI() {
        // Window Setup
        setTitle("MDM Installer");
        setUndecorated(true); // Modern frameless look
        setSize(500, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Dark Gradient Background
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), 0, getHeight(), new Color(30, 41, 59));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Border
                g2d.setColor(new Color(56, 189, 248, 50));
                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        add(mainPanel);

        // Title
        JLabel titleLabel = new JLabel("MDM - CLI");
        titleLabel.setForeground(new Color(56, 189, 248)); // Cyan
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subTitleLabel = new JLabel("The Modern Dependency Manager");
        subTitleLabel.setForeground(new Color(148, 163, 184)); // Slate
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Install Button (Custom Painted)
        installButton = new JButton("INSTALL NOW") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(2, 132, 199));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(56, 189, 248));
                } else {
                    g2d.setColor(new Color(14, 165, 233));
                }
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                super.paintComponent(g);
            }
        };
        installButton.setForeground(Color.WHITE);
        installButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        installButton.setFocusPainted(false);
        installButton.setBorderPainted(false);
        installButton.setContentAreaFilled(false);
        installButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        installButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        installButton.setMaximumSize(new Dimension(200, 50));
        installButton.addActionListener(this::runInstall);

        // Status
        statusLabel = new JLabel("v1.0 Ready");
        statusLabel.setForeground(new Color(148, 163, 184));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Close Button (Little 'x' at top right for frameless window)
        // (Simplified for now, user can use Alt+F4 or we assume success closes it)

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(subTitleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        mainPanel.add(installButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalGlue());
    }

    private void runInstall(ActionEvent e) {
        installButton.setEnabled(false);
        installButton.setText("INSTALLING...");
        statusLabel.setText("Copying files...");

        // Run in background thread
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Same logic as before
                String userHome = System.getProperty("user.home");
                Path installDir = Paths.get(userHome, ".mdm", "bin");
                if (!Files.exists(installDir)) Files.createDirectories(installDir);

                // 1. Check for Native Payload (Best)
                Path currentPath = Paths.get(System.getProperty("user.dir"));
                Path payloadDir = currentPath.resolve("payload").resolve("mdm"); 
                // Note: payload/mdm contains the exe
                
                if (Files.exists(payloadDir)) {
                    // NATIVE INSTALL
                    statusLabel.setText("Installing Native Runtime...");
                    Path nativeDest = installDir.getParent().resolve("native");
                    // Clean old
                    if (Files.exists(nativeDest)) {
                        // Simple recursive delete (Java 17 doesn't have deleteRecursively util easily, using walk)
                        Files.walk(nativeDest)
                            .sorted(java.util.Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(java.io.File::delete);
                    }
                    Files.createDirectories(nativeDest);
                    
                    // Recursive Copy
                    Files.walk(payloadDir).forEach(source -> {
                        Path destination = nativeDest.resolve(payloadDir.relativize(source));
                        try {
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                    
                    // Shim points to EXE
                    boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
                    Path exePath = nativeDest.resolve("mdm.exe");
                    if (isWindows) {
                         Path shim = installDir.resolve("mdm.cmd");
                         Files.writeString(shim, "@echo off\r\n\"" + exePath.toString() + "\" %*");
                    } else {
                         // Linux logic...
                    }

                } else {
                    // JAR FALLBACK (Old logic)
                    statusLabel.setText("Installing JAR...");
                    Path currentJar = Paths.get(InstallerUI.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                    Path destJar = installDir.resolve("mdm-cli.jar");
                    Files.copy(currentJar, destJar, StandardCopyOption.REPLACE_EXISTING);
                    
                    Path shim = installDir.resolve("mdm.cmd");
                    Files.writeString(shim, "@echo off\r\njava -jar \"%~dp0mdm-cli.jar\" %*");
                }
                
                // --- PERMANENT PATH UPDATE (Windows) ---
                statusLabel.setText("Updating System PATH...");
                    
                String psCommand = String.format(
                    "$p = [Environment]::GetEnvironmentVariable('Path', 'User'); " +
                    "if ($p -notlike '*%s*') { " +
                    "[Environment]::SetEnvironmentVariable('Path', $p + ';%s', 'User') }",
                    installDir.toString(), installDir.toString()
                );
                    
                ProcessBuilder pb = new ProcessBuilder("powershell", "-NoProfile", "-Command", psCommand);
                pb.inheritIO();
                Process p = pb.start();
                p.waitFor();
                
                Thread.sleep(1000); 
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Installation Complete! (Restart Terminal)");
                    statusLabel.setForeground(new Color(74, 222, 128)); // Green
                    installButton.setText("EXIT");
                    installButton.setEnabled(true);
                    installButton.removeActionListener(installButton.getActionListeners()[0]);
                    installButton.addActionListener(ev -> System.exit(0));
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    statusLabel.setForeground(new Color(248, 113, 113)); // Red
                    installButton.setEnabled(true);
                    installButton.setText("RETRY");
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new InstallerUI().setVisible(true);
        });
    }
}
