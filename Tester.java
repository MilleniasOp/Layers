import java.util.List;

import controller.ReportController;
import controller.UserController;
import entity.Report;
import dorkbox.systemTray.SystemTray;

public class Tester {

    public static void main(String[] args) {

        SystemTray tray = SystemTray.get();

        if (tray == null) {
            System.out.println("Tray not supported");
            return;
        }

        tray.setStatus("Task Reminder");

        tray.notify("Reminder", "It finally works 🎉");
    }
}

