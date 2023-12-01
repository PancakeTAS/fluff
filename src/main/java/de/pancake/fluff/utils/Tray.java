package de.pancake.fluff.utils;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * This class is used to create a tray icon
 *
 * @author Pancake
 */
public class Tray {

    /** The tray icon */
    private final TrayIcon trayIcon;

    /**
     * Initialize the tray
     */
    public Tray() {
        this.trayIcon = new TrayIcon(new ImageIcon(Objects.requireNonNull(Tray.class.getResource("/fluff.png"))).getImage(), "Fluff");
        this.trayIcon.setImageAutoSize(true);
        this.trayIcon.addActionListener(e -> System.exit(0));

        try {
            SystemTray.getSystemTray().add(this.trayIcon);
        } catch (Exception ignored) {

        }
    }

}
