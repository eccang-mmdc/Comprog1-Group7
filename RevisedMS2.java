package revisedms2;


// Import necessary Java libraries
import java.io.BufferedReader;       // For reading text files
import java.io.FileReader;            // For file handling
import java.io.IOException;           // For input/output error handling
import java.text.ParseException;      // For date parsing exceptions
import java.text.SimpleDateFormat;    // For date formatting
import java.util.Date;                // For date object handling the hours worked
import java.util.HashMap;             // For hash map data structure
import java.util.Map;                 // For map interface
import java.util.Scanner;             // For user inputs

/**
 * Represents an employee with personal and payroll details
 */
class Employee {
    // Class properties with final keyword for immutability
    private final String employeeNumber;  // Unique employee identifier
    private final String fullName;        // Combined first + last name
    private final String birthday;        // Date of birth
    private final double basicSalary;     // Monthly base salary-basis of net pay
    private final double hourlyRate;      // Hourly wage rate

    // Constructor to initialize employee object
    public Employee(String employeeNumber, String fullName, String birthday, 
                   double basicSalary, double hourlyRate) {
        this.employeeNumber = employeeNumber;
        this.fullName = fullName;
        this.birthday = birthday;
        this.basicSalary = basicSalary;
        this.hourlyRate = hourlyRate;
    }

    // Getter methods for encapsulated properties
    public String getEmployeeNumber() { return employeeNumber; }
    public String getFullName() { return fullName; }
    public String getBirthday() { return birthday; }
    public double getBasicSalary() { return basicSalary; }
    public double getHourlyRate() { return hourlyRate; }
}

/**
 * Manages employee attendance records with date-based tracking
 */
class AttendanceRecord {
    // Nested map structure: Employee Number -> Date -> [LogIn, LogOut]
    private final Map<String, Map<String, String[]>> attendanceData;

    // Constructor initializes empty attendance structure
    public AttendanceRecord() {
        this.attendanceData = new HashMap<>();
    }

    /**
     * Adds attendance entry for an employee
     * @param empNumber - Employee ID
     * @param date - Attendance date (MM/dd/yyyy)
     * @param logIn - Clock-in time (HH:mm)
     * @param logOut - Clock-out time (HH:mm)
     */
    public void addAttendance(String empNumber, String date, String logIn, String logOut) {
        // Create employee entry if not exists
        attendanceData.putIfAbsent(empNumber, new HashMap<>());
        // Add date entry with time data
        attendanceData.get(empNumber).put(date, new String[]{logIn, logOut});
    }

    /**
     * Retrieves filtered attendance records within date range
     * @param empNumber - Target employee ID
     * @param startDate - Range start date
     * @param endDate - Range end date
     * @return Filtered map of date -> [logIn, logOut]
     */
    public Map<String, String[]> getAttendanceInRange(String empNumber, Date startDate, Date endDate) {
        Map<String, String[]> filteredRecords = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        
        // Process each attendance record for employee
        attendanceData.getOrDefault(empNumber, new HashMap<>()).forEach((date, times) -> {
            try {
                Date currentDate = dateFormat.parse(date);
                // Check if date falls within range
                if (!currentDate.before(startDate) && !currentDate.after(endDate)) {
                    filteredRecords.put(date, times);
                }
            } catch (ParseException e) {
                System.err.println("Error parsing date: " + date);
            }
        });
        return filteredRecords;
    }

    // Getter for raw attendance data
    public Map<String, Map<String, String[]>> getAttendanceData() {
        return attendanceData;
    }
}

/**
 * Computation of deductions and net pay
 */
class PayrollCalculator {
    // SSS contribution calculation based on salary brackets
    public static double calculateSSSContribution(double basicSalary) {
        if (basicSalary < 3250) return 135.0;         // Minimum bracket
        if (basicSalary >= 24750) return 1125.0;      // Maximum bracket
        double steps = Math.floor((basicSalary - 3250) / 500);  // Calculate tier steps
        return 135.0 + (steps + 1) * 22.50;          // Base + tier increments
    }

    // PhilHealth contribution calculation
    public static double calculatePhilHealthContribution(double basicSalary) {
        if (basicSalary <= 10000) return 150.0;       // Fixed rate for income P10000 and below
        if (basicSalary < 60000) return basicSalary * 0.015;  // 1.5% as employee contrubution
        return 900.0;                                 // Maximum contribution
    }

    // Pag-IBIG contribution calculation
    public static double calculatePagIBIGContribution(double basicSalary) {
        if (basicSalary >= 1000 && basicSalary <= 1500) 
            return basicSalary * 0.01;                // 1% employee contribution for basic salary 1000 to 1500
        if (basicSalary > 1500) 
            return basicSalary * 0.02;                // 2% employee contribution for basic salary over 1500
        return 0.0;                                   // No contribution
    }

    // Withholding tax calculation using progressive rates
    public static double calculateWithholdingTax(double taxableIncome) {
        // Tax bracket calculations
        if (taxableIncome <= 20832) return 0.0;                          // 20,832 and below
        if (taxableIncome <= 33333) return (taxableIncome - 20833) * 0.20; // 20,833 to below 33,333
        if (taxableIncome <= 66667) return 2500 + (taxableIncome - 33333) * 0.25; // 33,333 to below 66,667
        if (taxableIncome <= 166667) return 10833 + (taxableIncome - 66667) * 0.30; // 66,667 to below 166,667
        if (taxableIncome <= 666667) return 40833.33 + (taxableIncome - 166667) * 0.32; // 166,667 to below 666,667
        return 200833.33 + (taxableIncome - 666667) * 0.35;               // 666,667 and above
    }
}

/**
 * Handles data loading from CSV files
 */
class DataLoader {
    /**
     * Loads employee data from CSV file
     * @param filePath - Path to employee CSV
     * @return Map of Employee objects keyed by ID
     * @throws IOException - File read errors
     */
    public static Map<String, Employee> loadEmployees(String filePath) throws IOException {
        Map<String, Employee> employees = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header row
            String line;
            // Process each CSV line
            while ((line = br.readLine()) != null) {
                // Split CSV line while handling quoted fields
                String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (row.length >= 19) {
                    // Extract and clean data fields
                    String empNumber = row[0].trim();
                    String fullName = row[2].trim() + " " + row[1].trim(); // Format name
                    String basicSalaryStr = row[13].trim().replaceAll("[,\"]", ""); // Clean numeric
                    String hourlyRateStr = row[18].trim().replaceAll("[,\"]", "");
                    
                    // Create and store Employee object
                    employees.put(empNumber, new Employee(
                        empNumber,
                        fullName,
                        row[3].trim(),
                        Double.parseDouble(basicSalaryStr),
                        Double.parseDouble(hourlyRateStr)
                    ));
                }
            }
        }
        return employees;
    }

    /**
     * Loads attendance records from CSV
     * @param attendance - AttendanceRecord to populate
     * @param filePath - Path to attendance CSV
     * @throws IOException - File read errors
     */
    public static void loadAttendance(AttendanceRecord attendance, String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 6) {
                    // Extract and store attendance data
                    String empNumber = data[0].trim();
                    String date = data[3].trim();
                    String logIn = data[4].trim();
                    String logOut = data[5].trim();
                    attendance.addAttendance(empNumber, date, logIn, logOut);
                }
            }
        }
    }
}

/**
 * Manages user interface and menu operations
 */
class MenuManager {
    private final Scanner scanner;         // User input handler
    private final Map<String, Employee> employees;  // Employee data cache
    private final AttendanceRecord attendance;     // Attendance data cache

    // Initialize with data sources
    public MenuManager(Map<String, Employee> employees, AttendanceRecord attendance) {
        this.scanner = new Scanner(System.in);
        this.employees = employees;
        this.attendance = attendance;
    }

    // Main menu display loop
    public void showMenu() {
        int choice;
        do {
            // Print menu options
            System.out.println("\nWelcome to MotorPH Menu:");
            System.out.println("1. Display Employee Information");
            System.out.println("2. Compute Hours Worked");
            System.out.println("3. Compute Gross Salary");
            System.out.println("4. Compute Net Salary");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            processChoice(choice);
        } while (choice != 5);
    }

    // Route user choice to appropriate handler
    private void processChoice(int choice) {
        switch (choice) {
            case 1 -> displayEmployeeInfo();
            case 2 -> computeHoursWorked();
            case 3 -> computeGrossSalary();
            case 4 -> computeNetSalary();
            case 5 -> System.out.println("Exiting...");
            default -> System.out.println("Invalid choice.");
        }
    }

    // Option 1: Display employee details
    private void displayEmployeeInfo() {
        System.out.print("Enter Employee Number: ");
        String empNumber = scanner.nextLine().trim();
        Employee emp = employees.get(empNumber);
        
        if (emp != null) {
            // Format and display employee information
            System.out.println("\nEmployee Details:");
            System.out.println("Employee Number: " + emp.getEmployeeNumber());
            System.out.println("Full Name: " + emp.getFullName());
            System.out.println("Birthday: " + emp.getBirthday());
            System.out.printf("Basic Salary: PHP %.2f%n", emp.getBasicSalary());
            System.out.printf("Hourly Rate: PHP %.2f%n", emp.getHourlyRate());
        } else {
            System.out.println("Employee not found.");
        }
    }

    // Option 2: Calculate worked hours
    private void computeHoursWorked() {
        System.out.print("Enter employee number: ");
        String empNumber = scanner.nextLine();
        
        // Validate employee exists
        if (!attendance.getAttendanceData().containsKey(empNumber)) {
            System.out.println("Employee not found in attendance records.");
            return;
        }

        // Get date range from user
        System.out.print("Enter start date (MM/dd/yyyy): ");
        String startDateStr = scanner.nextLine();
        System.out.print("Enter end date (MM/dd/yyyy): ");
        String endDateStr = scanner.nextLine();

        try {
            // Parse dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);
            
            // Get filtered attendance records
            Map<String, String[]> attendanceData = 
                attendance.getAttendanceInRange(empNumber, startDate, endDate);
            
            long totalMinutes = 0;
            // Process each attendance entry
            for (Map.Entry<String, String[]> entry : attendanceData.entrySet()) {
                // Calculate time difference
                long minutes = calculateTimeDifferenceMinutes(
                    entry.getValue()[0], entry.getValue()[1]
                );
                totalMinutes += minutes;
                // Display daily hours
                System.out.printf("Date: %s, Hours: %s%n", 
                    entry.getKey(), formatTimeDifference(minutes));
            }
            
            // Display total hours
            System.out.printf("Total Hours: %s%n", formatTimeDifference(totalMinutes));
        } catch (ParseException e) {
            System.out.println("Invalid date format.");
        }
    }

    // Option 3: Calculate gross salary
    private void computeGrossSalary() {
        System.out.print("Enter employee number: ");
        String empNumber = scanner.nextLine();
        Employee emp = employees.get(empNumber);
        
        if (emp == null) {
            System.out.println("Employee not found.");
            return;
        }

        // Get date range
        System.out.print("Enter start date (MM/dd/yyyy): ");
        String startDateStr = scanner.nextLine();
        System.out.print("Enter end date (MM/dd/yyyy): ");
        String endDateStr = scanner.nextLine();

        try {
            // Parse dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);
            
            // Get attendance data
            Map<String, String[]> attendanceData = 
                attendance.getAttendanceInRange(empNumber, startDate, endDate);
            
            long totalMinutes = 0;
            // Calculate total minutes worked
            for (Map.Entry<String, String[]> entry : attendanceData.entrySet()) {
                totalMinutes += calculateTimeDifferenceMinutes(
                    entry.getValue()[0], entry.getValue()[1]
                );
            }
            
            // Calculate and display gross salary
            double totalHours = totalMinutes / 60.0;
            double grossSalary = totalHours * emp.getHourlyRate();
            System.out.printf("Gross salary for %s: PHP %.2f%n", 
                emp.getFullName(), grossSalary);
        } catch (ParseException e) {
            System.out.println("Invalid date format.");
        }
    }

    // Option 4: Calculate net salary
    private void computeNetSalary() {
        System.out.print("Enter employee number: ");
        String empNumber = scanner.nextLine();
        Employee emp = employees.get(empNumber);
        
        if (emp == null) {
            System.out.println("Employee not found.");
            return;
        }

        // Get base salary
        double basicSalary = emp.getBasicSalary();
        
        // Calculate deductions
        double sss = PayrollCalculator.calculateSSSContribution(basicSalary);
        double philhealth = PayrollCalculator.calculatePhilHealthContribution(basicSalary);
        double pagibig = PayrollCalculator.calculatePagIBIGContribution(basicSalary);
        
        // Calculate taxable income
        double taxableIncome = basicSalary - (sss + philhealth + pagibig);
        
        // Calculate tax
        double withholdingTax = PayrollCalculator.calculateWithholdingTax(taxableIncome);
        
        // Calculate final net salary
        double netSalary = taxableIncome - withholdingTax;

        // Display detailed breakdown
        System.out.println("\nNet Salary Calculation:");
        System.out.printf("Basic Salary: PHP %.2f%n", basicSalary);
        System.out.printf("SSS Contribution: PHP %.2f%n", sss);
        System.out.printf("PhilHealth Contribution: PHP %.2f%n", philhealth);
        System.out.printf("Pag-IBIG Contribution: PHP %.2f%n", pagibig);
        System.out.printf("Taxable Income: PHP %.2f%n", taxableIncome);
        System.out.printf("Withholding Tax: PHP %.2f%n", withholdingTax);
        System.out.printf("Net Salary: PHP %.2f%n", netSalary);
    }

    /**
     * Calculates minutes between two time strings
     * @param logIn - Start time (HH:mm)
     * @param logOut - End time (HH:mm)
     * @return Minutes difference
     */
    private long calculateTimeDifferenceMinutes(String logIn, String logOut) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            Date timeIn = format.parse(logIn);
            Date timeOut = format.parse(logOut);
            return (timeOut.getTime() - timeIn.getTime()) / (60 * 1000);
        } catch (ParseException e) {
            return -1; // Error indicator
        }
    }

    /**
     * Formats minutes into HH:mm string
     * @param minutes - Total minutes
     * @return Formatted time string
     */
    private String formatTimeDifference(long minutes) {
        return (minutes < 0) ? "Invalid" : 
            String.format("%d:%02d", minutes / 60, minutes % 60);
    }
}

/**
 * Main application class
 */
public class RevisedMS2 {
    public static void main(String[] args) {
        try {
            // Initialize data stores
            Map<String, Employee> employees = 
                DataLoader.loadEmployees("src/motorph_employee_data_complete.csv");
            AttendanceRecord attendance = new AttendanceRecord();
            DataLoader.loadAttendance(attendance, "src/attendance_record.csv");
            
            // Start user interface
            new MenuManager(employees, attendance).showMenu();
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
}