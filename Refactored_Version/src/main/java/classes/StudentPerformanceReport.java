package classes;

import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.util.List;
import java.util.function.Consumer;

import controller.ModuleController;

public class StudentPerformanceReport extends PdfFile{
    private ModuleController moduleController;
    private Document document;
    private String filterBy;

    public StudentPerformanceReport(String filterBy){
        this.filterBy = filterBy;
    }

    private void createIntroduction(Student student){
        //create student personal info as the introduction
        document.add(new Paragraph("Student ID: " + student.getStudentID())
            .simulateBold()
            .setFontSize(12));

        document.add(new Paragraph("Student Name: " + student.getFirstName() + " " + student.getLastName())
            .simulateBold()
            .setFontSize(12));

        document.add(new Paragraph("Program: " + student.getMajor().getMajorName())
            .simulateBold()
            .setFontSize(12));

        document.add(new Paragraph("\n"));
    }
    
    private void generateBody(Student student){
        String sem = student.getSem();

        //Based on Semester
        if (filterBy.equalsIgnoreCase("semester")) {
            document.add(new Paragraph(sem+"\n")
                .simulateBold()
                .setFontSize(14));

            CourseAndGradeTable(moduleController.getEnrollementBySemOfStudent(student, student.getSem()));
            gpaParagraph(sem, student);
        //Based on Year
        }else{
            //Year, semester 1 and 2
            if(student.getSem().equalsIgnoreCase("Semester 2")){
                document.add(new Paragraph(student.getYear() + " Year" + ", Semester 1"+"\n")
                    .simulateBold()
                    .setFontSize(14));

                CourseAndGradeTable(moduleController.getEnrollementBySemOfStudent(student, "Semester 1"));
                gpaParagraph("Semester 1", student);

                document.add(new Paragraph("\n"));

                document.add(new Paragraph(student.getYear() + " Year" + ", Semester 2"+"\n")
                    .simulateBold()
                    .setFontSize(14));

                CourseAndGradeTable(moduleController.getEnrollementBySemOfStudent(student, "Semester 2"));
                gpaParagraph("Semester 2", student);
            //Year, semester 1
            }else{
                document.add(new Paragraph(student.getYear() + " Year" + ", Semester 1"+"\n")
                    .simulateBold()
                    .setFontSize(14));

                CourseAndGradeTable(moduleController.getEnrollementBySemOfStudent(student, "Semester 1"));
                gpaParagraph("Semester 1", student);
            }
        }

        //Cumulitative GPA
        double roundedCGPA = Math.round(moduleController.calcStudentCGPA(student) * 100.0) / 100.0;
        document.add(new Paragraph("\nCumulative CGPA: " + roundedCGPA)
            .simulateBold()
            .setFontSize(12));
    
        document.close();
    }

    public void generateReport(Student s, boolean introduction, Document document) throws Exception{ 
        this.document = document;
        this.moduleController = new ModuleController();  
        if (introduction){
            this.createIntroduction(s);
            this.generateBody(s);
        }else{
            this.generateBody(s);
        }
    }


    private void CourseAndGradeTable(List<Enrollement> enrollements){
        float[] columnWidths = {2f,4f, 2f, 2f};
        Table courseAndGradeTable = new Table(UnitValue.createPercentArray(columnWidths));
        
        //create header of a table
        String[] tableHeaders = {"Course Code", "Course title", "Credit Hours", "Grade"};
        for (String h:tableHeaders){
            courseAndGradeTable.addHeaderCell(new Cell()
                .add(new Paragraph(h)
                .simulateBold()
                .setTextAlignment(TextAlignment.CENTER)));
        }

        //create a function inside a funtion to add items per row
        Consumer<String[]> createRowCell = (data) -> {
            //for one row cell, create multiple column cells
            for (String d : data){
                courseAndGradeTable.addCell(new Cell().add(new Paragraph(d)).setTextAlignment(TextAlignment.CENTER));
            }
        };

        //for each course with grade, create a row cell, and add data to the row cell
        enrollements.forEach( (Enrollement e) -> createRowCell.accept(new String[]{
            e.getCourse().getCourseID(), 
            e.getCourse().getCourseName(), 
            String.valueOf(e.getCourse().getCredits()),
            e.getGrade()
            }
        ));   
           
        document.add(courseAndGradeTable);
    }

    private void gpaParagraph(String sem, Student student){
        double roundedGPA = Math.round(moduleController.calcStudentGPA(student, sem) * 100.0) / 100.0;
        document.add(new Paragraph("\nGPA: " + roundedGPA)
            .simulateBold()
            .setFontSize(12));
    }
}