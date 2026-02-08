package repository;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import classes.*;

public class EnrollmentRepository {

    public static List<Course> getAllCoursesTakenByFailedStudents(){
        Set<Course> uniqueCourse = new HashSet<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(ResourceManager.getGradeDataPath()))){
            reader.readLine(); 
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\t");
                String courseID = values[2];
                
                Course course = CourseRepository.findCoursesByID(courseID);
                if (course != null && values[3].matches("[DEF][+-]?")) {
                    uniqueCourse.add(course);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>(uniqueCourse);
    }

    public static List<Student> getEnrolledStudents(){
        // 1. Use a Set to store unique students
        Set<Student> uniqueStudents = new HashSet<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(ResourceManager.getGradeDataPath()))){
            reader.readLine(); 
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\t");
                String studentID = values[1];
                
                Student student = StudentRepository.findStudentByStudentID(studentID);
                if (student != null) {
                    uniqueStudents.add(student); // Duplicate students will be ignored automatically
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>(uniqueStudents);
    }

    public static List<Course> findCoursesTakenByStudents(Student student){
        List<Course> courses = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ResourceManager.getGradeDataPath()))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] value = line.split("\t");
                if(student.getStudentID().equals(value[1])){
                    courses.add(CourseRepository.findCoursesByID(value[2]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public static List<Enrollement> getStudentEnrollments(Student student){
        List<Enrollement> enrollements = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ResourceManager.getGradeDataPath()))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] value = line.split("\t");

                if(student.getStudentID().equals(value[1])){
                    enrollements.add(new Enrollement(value[0], student, CourseRepository.findCoursesByID(value[2]), value[3]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return enrollements;
    }

    public static List<Enrollement> getStudentEnrollmentsbySem(Student student, String sem){
        List<Enrollement> enrollements = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ResourceManager.getGradeDataPath()))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] value = line.split("\t");

                Course course = CourseRepository.findCoursesByID(value[2]);

                if(student.getStudentID().equals(value[1]) && course.getSemester().equalsIgnoreCase(sem)){
                    enrollements.add(new Enrollement(value[0], student, course, value[3]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return enrollements;
    }

    public static List<Enrollement> findGradeBasedOnCourse(Course course){
        List<Enrollement> enrollements = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ResourceManager.getGradeDataPath()))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] value = line.split("\t");
                if(course.getCourseID().equals(value[2])){
                    System.out.println(line);
                    enrollements.add(new Enrollement(value[0], StudentRepository.findStudentByStudentID(value[1]), CourseRepository.findCoursesByID(value[2]), value[3]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return enrollements;
    }

    public static Enrollement findEnrollementByID(String id){
        try (BufferedReader reader = new BufferedReader(new FileReader(ResourceManager.getGradeDataPath()))) {
            reader.readLine();//for skipping the header
            String line;
            while ((line = reader.readLine()) != null){
                String[] enrollmentData = line.split("\t");
                if (enrollmentData[0].equals(id)){
                    Enrollement enrollement = new Enrollement(
                        enrollmentData[0], 
                        StudentRepository.findStudentByStudentID(enrollmentData[1]), 
                        CourseRepository.findCoursesByID(enrollmentData[2]), 
                        enrollmentData[3]
                    );
                    return enrollement;
                }
                
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
