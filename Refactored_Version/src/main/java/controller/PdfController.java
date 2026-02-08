package controller;

import java.io.File;
import java.util.List;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

import classes.PdfFile;
import classes.Student;
import repository.*;

public class PdfController{
    private boolean successful;
    private boolean studentPersonalInfo = false;
    private PdfFile pdfFile;
    private List<Student> studentList;

    //create document
    public PdfController(PdfFile pdfFile){
        this.pdfFile = pdfFile;
        this.studentList = StudentRepository.loadAllStudents();
    }

    //create document with studentPersonalInfo
    public PdfController(PdfFile pdfFile, boolean studentPersonalInfo){
        this.pdfFile = pdfFile;
        this.studentPersonalInfo = studentPersonalInfo;
        this.studentList = StudentRepository.loadAllStudents();
    }

    public boolean generateManyPerformanceReports(String logoPath,String yearStudy, String program, File directory)throws Exception{  
        for (Student s : studentList) {
            //filtered based the program and yearStudy 
            if (s.getMajor().getMajorName().equalsIgnoreCase(program) && s.getYear().equalsIgnoreCase(yearStudy)) {
                //check if the folder exists
                if (!directory.exists()) {directory.mkdirs();}
                String filepath = directory.getAbsolutePath() + File.separator + s.getStudentID() +"_APR.pdf";

                //create report
                Document document = new Document(new PdfDocument(new PdfWriter(filepath)), PageSize.A4);  
                pdfFile.createHeader(document,logoPath, "Academic Performance Report",12);
                pdfFile.generateReport(s, studentPersonalInfo, document);
                successful = true;
            }   
        }

        return successful;
    }
    public boolean generateRecoveryPlan(String outputPath, Student student, String logoPath) throws Exception{
        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);

        pdfFile.createHeader(document,logoPath, "Course Recovery Plan",12);
        pdfFile.generateReport(student, studentPersonalInfo, document); // closes document
        return !successful;
    }
}