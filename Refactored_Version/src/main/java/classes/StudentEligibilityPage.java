package classes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import controller.LoginController;
import controller.ModuleController;

import java.awt.*;

public class StudentEligibilityPage extends JFrame {
    private LoginController session;
    private DefaultTableModel failedModel;
    private DefaultTableModel passModel;
    private JTable jTablePass;
    private JTable jTableFailed;
    private ModuleController moduleController;

    public StudentEligibilityPage(LoginController session) {
        this.session = session;
        this.moduleController = new ModuleController();
        this.initFrame();
        this.loadPassedStudentData();
        this.loadFailedStudentData();
    }

    private void initFrame() {
        this.setTitle("Student Eligibility Check");
        this.setDefaultCloseOperation(3);
        this.setSize(900, 600);
        this.setLocationRelativeTo((Component)null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Eligibility Check and Enrolment", 0);

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener((e) -> this.goBack());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener((e) -> this.refreshTable());

        headerPanel.add(headerLabel, "Center");
        headerPanel.add(backBtn, "West");
        headerPanel.add(refreshBtn, "East");
        mainPanel.add(headerPanel, "North");

        this.failedModel = new DefaultTableModel(new String[]{"Student ID", "Name", "Major", "CGPA"}, 0);
        this.passModel = new DefaultTableModel(new String[]{"Student ID", "Name", "Major", "CGPA"}, 0);
        this.jTableFailed = new JTable(failedModel);
        this.jTablePass = new JTable(passModel);
        JScrollPane scrollPanePass = new JScrollPane(jTablePass);
        JScrollPane scrollPaneFailed = new JScrollPane(jTableFailed);
        scrollPanePass.setVerticalScrollBarPolicy(22);
        scrollPaneFailed.setVerticalScrollBarPolicy(22);

        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton passStudentBtn = new JButton("Passed Students");
        JButton failStudentBtn = new JButton("Failed Students");
        topButtonPanel.add(passStudentBtn);
        topButtonPanel.add(failStudentBtn);

        JPanel tablePanel = new JPanel(new CardLayout());
        tablePanel.add(scrollPanePass, "PASS");
        tablePanel.add(scrollPaneFailed, "FAIL");
        CardLayout cl = (CardLayout) (tablePanel.getLayout());

        passStudentBtn.addActionListener(e -> cl.show(tablePanel, "PASS"));
        failStudentBtn.addActionListener(e -> cl.show(tablePanel, "FAIL"));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(topButtonPanel, BorderLayout.WEST);

        JButton allowRegistrationBtn = new JButton("Allow all passed students registration");
        allowRegistrationBtn.addActionListener(e -> {
            sendEmailNotificationPass();
            JOptionPane.showMessageDialog(this, "All passed students are now allowed to register.");
        });
        bottomPanel.add(allowRegistrationBtn, BorderLayout.EAST);

        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        this.add(mainPanel);
    }

    private void sendEmailNotificationPass() {
        for (Student s : moduleController.getAllEnrolledStudents()) {
            String name = s.getFirstName() + " " + s.getLastName();
            double CGPA = moduleController.calcStudentCGPA(s);
            if(CGPA >= 2.0){
                String studentEmail = s.getEmail();
                String subject = "Pass/Fail Notice";
                String bodyText = "We are glad to inform you, " + name + " that you have passed with a CGPA of " + CGPA + ". " +
                        "You may now proceed with the registrations.";
                EmailSender.sendEmail(studentEmail, subject, bodyText);
            }else{
                String studentEmail = s.getEmail();
                String subject = "Pass/Fail Notice";
                String bodyText = "We are sorry to inform you, " + name + " that you have failed with a CGPA of " + CGPA;
                EmailSender.sendEmail(studentEmail, subject, bodyText);
            }
        }
    }

    private void goBack() {
        this.dispose();
        String role = session.getUser().getRole();

        if (role.equals("Admin")) {
            new CourseAdminPage(session).setVisible(true);
        } else if (role.equals("AcademicOfficer")) {
            new AcademicOfficerPage(session).setVisible(true);
        }
    }

     private void loadPassedStudentData(){
        passModel.setRowCount(0);

        for (Student s : moduleController.getAllEnrolledStudents()) {
            String id = s.getStudentID();
            String name = s.getFirstName() + " " + s.getLastName();
            Major major = s.getMajor();
            double CGPA = moduleController.calcStudentCGPA(s);

            double roundedCGPA = Math.round(CGPA * 100.0) / 100.0;

            // System.out.println(CGPA);

            if (roundedCGPA >= 2.0 && moduleController.canProgressToNextLevel(s)) {
                passModel.addRow(new Object[]{id, name, major.getMajorName(), roundedCGPA});
            }
        }
    }

    private void loadFailedStudentData() {
        // Clear existing rows
        failedModel.setRowCount(0);

        // Add new rows
        for (Student s : moduleController.getAllEnrolledStudents()) {
            String id = s.getStudentID();
            String name = s.getFirstName() + " " + s.getLastName();
            Major major = s.getMajor();
            double CGPA = moduleController.calcStudentCGPA(s);

            double roundedCGPA = Math.round(CGPA * 100.0) / 100.0;

            if (roundedCGPA < 2.0 || !moduleController.canProgressToNextLevel(s)) {
                failedModel.addRow(new Object[]{id, name, major.getMajorName(), roundedCGPA});
            }
        }
    }

    private void refreshTable() {
        moduleController = new ModuleController();

        loadPassedStudentData();
        loadFailedStudentData();
        JOptionPane.showMessageDialog(this, "Table refreshed!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}