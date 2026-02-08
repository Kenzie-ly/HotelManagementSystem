package repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import classes.*;

public class StudentRepository {

    public static List<Student> loadAllStudents() {
        List<Student> students = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ResourceManager.getStudentDataPath()))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] value = line.split("\t");

                students.add(new Student(value[0], value[1], value[2], CourseRepository.findMajorByName(value[3]), value[4], value[5], value[6]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return students;
    }

    public static Student findStudentByStudentID(String studentID) {
        try (BufferedReader reader = new BufferedReader(new FileReader(ResourceManager.getStudentDataPath()))) {
            reader.readLine(); // for skipping the header
            String line;
            while ((line = reader.readLine()) != null) {
                // System.out.println("Processing line: " + line); // Log the line for debugging

                if (line.trim().isEmpty()) {
                    System.out.println("Skipping empty line");
                    continue; // Skip empty lines
                }

                String[] studentData = line.split("\t");
                if (studentData.length < 6) {
                    System.out.println("Malformed line: " + line);
                    continue; // Skip malformed lines
                }

                if (studentData[0].equals(studentID)) {
                    return new Student(studentData[0], studentData[1], studentData[2], CourseRepository.findMajorByName(studentData[3]), studentData[4], studentData[5], studentData[6]);
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        return null;
    }

    public static void deleteStudent(Student student) throws IOException {
        File originalFile = new File(ResourceManager.getStudentDataPath());
        File tempFile = new File("temp.txt");

        // 1. USE TRY-WITH-RESOURCES to auto-close streams
        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) { // removed 'true' (append)

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\t");

                // CHECK: Is this the user we are updating?
                if (!(data[0].equals(student.getStudentID()))) {
                    // NO: Copy existing line
                    writer.write(line);
                    writer.newLine();
                } 
            }
            
        } // <--- Streams close AUTOMATICALLY here. The file lock is released.

        // 2. NOW it is safe to swap the files
        
        // Safety Check: Delete failed?
        if (!originalFile.delete()) {
            System.out.println("Could not delete original file. Check permissions or if file is open.");
            return; 
        }

        // Safety Check: Rename failed?
        if (!tempFile.renameTo(originalFile)) {
            System.out.println("Could not rename temp file. You might be on a different drive partition.");
        }
    }

    public static void updateStudent(Student student) throws IOException {
        File originalFile = new File(ResourceManager.getStudentDataPath());
        File tempFile = new File("temp.txt");

        // 1. USE TRY-WITH-RESOURCES to auto-close streams
        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) { // removed 'true' (append)

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\t");

                // CHECK: Is this the user we are updating?
                if (data[0].equals(student.getStudentID())) {
                    // YES: Write the NEW data
                    writer.write(formatStudentString(student));
                } else {
                    // NO: Copy existing line
                    writer.write(line);
                }
                writer.newLine();
            }
            
        } // <--- Streams close AUTOMATICALLY here. The file lock is released.

        // 2. NOW it is safe to swap the files
        
        // Safety Check: Delete failed?
        if (!originalFile.delete()) {
            System.out.println("Could not delete original file. Check permissions or if file is open.");
            return; 
        }

        // Safety Check: Rename failed?
        if (!tempFile.renameTo(originalFile)) {
            System.out.println("Could not rename temp file. You might be on a different drive partition.");
        }
    }

    public static void addNewStudent(Student student) throws IOException{
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(ResourceManager.getStudentDataPath(),true))) {
            bufferedWriter.write(formatStudentString(student));
            bufferedWriter.newLine();
        }
    }

    private static String formatStudentString(Student student){
        return(
            student.getStudentID() + "\t" +
            student.getFirstName() + "\t" +
            student.getLastName() + "\t" +
            student.getMajor().getMajorName() + "\t" +
            student.getYear() + "\t" +
            student.getSem() + "\t" +
            student.getEmail()
        );
    }
}
