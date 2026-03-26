import java.util.List;

public class Tester {
    public static void main (String[] args){
        List<Reports> reports = ReportsController.generateReports("john_doe");

        System.out.println("Total reports: " + reports.size());

        for (Reports r : reports) {
            System.out.println(r); // uses toString()
        }
    
    }
}
