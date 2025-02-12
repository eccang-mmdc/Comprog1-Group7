/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package motorphemployeeinfo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
/**
 *
 * @author jonad
 */
public class Motorphemployeeinfo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //import csv file
        String csvFile = "C:\\Users\\jonad\\OneDrive\\Documents\\NetBeansProjects\\motorphemployeeinfo\\src\\motorph_employeedata.csv";
        String line;
        //csv delimiter
        String csvSplitBy = ",";
        //store employee data
        HashMap<String, String[]> employeeMap = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            //proceed to data and skip the header
            br.readLine();
            
            //read csv file by line
            while((line = br.readLine()) !=null) {
            //split each line by delimiter
            String[] row = line.split(csvSplitBy);
            
            //capture first 4 columns
            if (row.length == 4) {
                //employee#
                String employeeNumber = row[0].trim();
                //last name
                String lastName = row[1].trim();
                //first name
                String firstName = row[2].trim();
                //Birthday
                String birthday = row[3].trim();
                
            //store data with hashmap
            employeeMap.put(employeeNumber, new String[]{lastName, firstName, birthday});}}} 
        
        catch (IOException e) {}
        
        //input employee number
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Employee Number: ");
        String inputEmployeeNumber = scanner.nextLine().trim();
        
        //if else function to capture employee information
        if (employeeMap.containsKey(inputEmployeeNumber)) 
            {String[] employeeDetails = employeeMap.get(inputEmployeeNumber);
            String lastName = employeeDetails[0];
            String firstName = employeeDetails[1];
            String birthday = employeeDetails[2];
            
            System.out.println("Employee Details: ");
            System.out.println("Full Name: "+ firstName + " "+ lastName);
            System.out.println("Birthday: " + birthday);} 
        
        else {System.out.println("Employee not found.");}
            }
                
    }
    
