package gay.pancake.fluff.utils;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * This class is used to create a tray icon
 *
 * @author Pancake
 */
public class Tray {

    /**
     * Initialize the tray
     */
    public Tray() {
        try {
            var trayIcon = new TrayIcon(new ImageIcon(Objects.requireNonNull(Tray.class.getResource("/fluff.png"))).getImage(), "Fluff");
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> System.exit(0));
            SystemTray.getSystemTray().add(trayIcon);
        } catch (Exception ignored) {

        }
    }

}
