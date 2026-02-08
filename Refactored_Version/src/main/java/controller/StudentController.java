package controller;

import java.util.List;

import classes.Student;
import repository.StudentRepository;

public class StudentController {
    
    public void updateStudent(Student student){
        try {
            StudentRepository.updateStudent(student);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addNewStudent(Student student){
        try {
            StudentRepository.addNewStudent(student);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteStudent(Student student){
        try {
            StudentRepository.deleteStudent(student);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Student> getAllStudents(){
        return StudentRepository.loadAllStudents();
    }

}
