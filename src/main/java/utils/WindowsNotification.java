package utils;

import java.awt.*;
import java.awt.TrayIcon.MessageType;

public class WindowsNotification {
    public static void show(String title, String message) {
        System.out.println("[DEBUG] Appel de WindowsNotification.show : title=" + title + ", message=" + message);

        if (!SystemTray.isSupported()) {
            System.err.println("[DEBUG] System tray not supported!");
            return;
        }
        System.out.println("[DEBUG] System tray supporté, préparation de la notification...");
        SystemTray tray = SystemTray.getSystemTray();
        // Utilise une icône vide pour éviter les erreurs si icon.png est absent
        Image image = Toolkit.getDefaultToolkit().createImage(new byte[0]);
        TrayIcon trayIcon = new TrayIcon(image, "Notification");
        trayIcon.setImageAutoSize(true);
        try {
            tray.add(trayIcon);
            System.out.println("[DEBUG] trayIcon ajouté, affichage de la notification...");
            trayIcon.displayMessage(title, message, MessageType.INFO);
            System.out.println("[DEBUG] displayMessage appelé.");
            Thread.sleep(3000);
            tray.remove(trayIcon);
            System.out.println("[DEBUG] trayIcon retiré.");
        } catch (Exception e) {
            System.err.println("[DEBUG] Exception lors de l'affichage de la notification : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
