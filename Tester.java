import java.util.List;

import controller.ReportController;
import controller.UserController;
import entity.Report;

public class Tester {
    public static void main (String[] args){
        List<Report> reports = ReportController.generateReports("john_doe");

        System.out.println("Total reports: " + reports.size());

        for (Report r : reports) {
            System.out.println(r); // uses toString()
        }

        String Workers = UserController.fetchWorkers();
        System.out.println(Workers);
        List<String[]> list = UserController.parseWorkersJson(Workers);
        System.out.println(list);        
    
    }
}
