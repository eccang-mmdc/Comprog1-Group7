/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package motorphhourworked;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

 
public class Motorphhourworked {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
    // Import the csv file
        String csvFile = "C:\\\\Users\\\\jonad\\\\OneDrive\\\\Documents\\\\NetBeansProjects\\\\Motorphhourworked\\\\attendance_record.csv";
        String line;
        // Change this to "\t" if the file is tab-separated
        String csvSplitBy = ","; 

        // Map to store employee data
        Map<String, Map<String, String[]>> employeeMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Read the header line
            br.readLine();

            // Read file line by line
            while ((line = br.readLine()) != null) {
                // Split the line by the delimiter
                String[] employeeData = line.split(csvSplitBy);

                // Ensure the line has the expected number of columns
                if (employeeData.length == 6) {
                    // Extract data from the CSV
                    String empNumber = employeeData[0];
                    String lastName = employeeData[1];
                    String firstName = employeeData[2];
                    String date = employeeData[3];
                    String logIn = employeeData[4];
                    String logOut = employeeData[5];

                    // Store the employee data in the map
                    employeeMap.putIfAbsent(empNumber, new HashMap<>());
                    employeeMap.get(empNumber).put(date, new String[]{firstName, lastName, logIn, logOut});
                } else {
                    System.out.println("Skipping invalid line: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Prompt the user to input an employee number
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter employee number: ");
        String inputEmpNumber = scanner.nextLine();

        // Check if the employee exists
        if (!employeeMap.containsKey(inputEmpNumber)) {
            System.out.println("Employee not found.");
            scanner.close();
            return;
        }

        // Prompt the user to input the start and end dates
        System.out.print("Enter start date (MM/dd/yyyy): ");
        String startDate = scanner.nextLine();
        System.out.print("Enter end date (MM/dd/yyyy): ");
        String endDate = scanner.nextLine();

        // Retrieve the employee's data
        Map<String, String[]> employeeData = employeeMap.get(inputEmpNumber);

        // Initialize total time difference in minutes
        long totalTimeDifferenceMinutes = 0;

        // Iterate through the employee's data and calculate the time difference for the covered dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        for (Map.Entry<String, String[]> entry : employeeData.entrySet()) {
            String date = entry.getKey();
            String[] data = entry.getValue();
            String logIn = data[2];
            String logOut = data[3];

            try {
                // Check if the date is within the specified range
                Date currentDate = dateFormat.parse(date);
                Date start = dateFormat.parse(startDate);
                Date end = dateFormat.parse(endDate);

                if (!currentDate.before(start) && !currentDate.after(end)) {
                    // Calculate the time difference for this date
                    long timeDifferenceMinutes = calculateTimeDifferenceMinutes(logIn, logOut);
                    totalTimeDifferenceMinutes += timeDifferenceMinutes;

                    // Display the daily time difference in hours:minutes format
                    String timeDifferenceFormatted = formatTimeDifference(timeDifferenceMinutes);
                    System.out.println("Date: " + date + ", Total hours worked: " + timeDifferenceFormatted);
                }
            } catch (ParseException e) {
                System.out.println("Error parsing date: " + date);
            }
        }

        // Display the total time difference in hours:minutes format
        String totalTimeDifferenceFormatted = formatTimeDifference(totalTimeDifferenceMinutes);
        System.out.println("Total hours worked between " + startDate + " and " + endDate + ": " + totalTimeDifferenceFormatted);

        scanner.close();
    }

    // Helper method to calculate the time difference in minutes
    private static long calculateTimeDifferenceMinutes(String logIn, String logOut) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        try {
            Date timeIn = format.parse(logIn);
            Date timeOut = format.parse(logOut);

            // Calculate the difference in milliseconds
            long difference = timeOut.getTime() - timeIn.getTime();

            // Convert milliseconds to minutes
            return difference / (60 * 1000);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Return -1 if there's an error parsing the time
        }
    }

    // Helper method to format time difference in hours:minutes
    private static String formatTimeDifference(long timeDifferenceMinutes) {
        if (timeDifferenceMinutes < 0) {
            return "Invalid time difference";
        }
        long hours = timeDifferenceMinutes / 60;
        long minutes = timeDifferenceMinutes % 60;
        return String.format("%d:%02d", hours, minutes); // Format as hours:minutes
    }
}     
        
    
    

