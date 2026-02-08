package classes;

import javax.swing.*;

import controller.LoginController;


public class AcademicOfficerPage extends BaseHomePage {

    public AcademicOfficerPage(LoginController session){
        super(session);
    }

    @Override
    protected void addRoleButtons(JPanel jpanel) {
        JButton aprBtn = new JButton("Performance Report");
        aprBtn.addActionListener(e -> {
            dispose();
            new PerformanceReportPage(session).setVisible(true);
        });

        JButton studentManageBtn = new JButton("Student Management");
        studentManageBtn.addActionListener(e -> {
            dispose();
            new StudentManagementPage(session).setVisible(true);
        });

        JButton eligiBtn = new JButton("Student Eligibility");
        eligiBtn.addActionListener(e -> {
            dispose();
            new StudentEligibilityPage(session).setVisible(true);
        });

        jpanel.add(aprBtn);
        jpanel.add(studentManageBtn);
        jpanel.add(eligiBtn);

        jpanel.add(new JLabel());
        jpanel.add(new JLabel());
        jpanel.add(new JLabel());
    }


}