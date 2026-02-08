package classes;
import javax.swing.*;

import controller.LoginController;
import controller.StudentController;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class StudentManagementPage extends JFrame {
    private LoginController session;
    private StudentController studentController;
    private JPanel studentPanel;
    private JTextField searchField;

    JButton undoBtn;
    private Student recentDeletedStudent;
    private Timer undoTimer;

    public StudentManagementPage(LoginController loginController) {
        this.session = loginController;
        this.studentController = new StudentController();
        initFrame();
    }

    private void initFrame() {
        setTitle("Student Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Section
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> goBack());
        headerPanel.add(backBtn, BorderLayout.WEST);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        JLabel searchLabel = new JLabel("Search:");
        searchField = new JTextField(30);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        topPanel.add(headerPanel, BorderLayout.NORTH);
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Bottom Section
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Add Button
        JButton addBtn = new JButton("Add Student");
        addBtn.addActionListener(e -> addStudentDialog());

        // Undo Button
        undoBtn = new JButton("Undo Delete");
        undoBtn.setVisible(false);
        undoBtn.addActionListener(e -> undoDelete());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(addBtn);
        buttonPanel.add(undoBtn);

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Search Listener
        searchField.addActionListener(e -> filterStudents());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterStudents(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterStudents(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterStudents(); }
        });

        // Center
        studentPanel = new JPanel();
        studentPanel.setLayout(new BoxLayout(studentPanel, BoxLayout.Y_AXIS));
        
        loadStudentList();

        JScrollPane scrollPane = new JScrollPane(studentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void filterStudents() {
        String searchText = searchField.getText().toLowerCase().trim();

        studentPanel.removeAll();

        List<Student> filtered = this.studentController.getAllStudents().stream()
                .filter(student -> {
                    if (searchText.isEmpty()) {
                        return true;
                    }
                    String name = (student.getFirstName() + " " + student.getLastName()).toLowerCase();
                    return name.contains(searchText) ||
                            student.getStudentID().toLowerCase().contains(searchText) ||
                            student.getEmail().toLowerCase().contains(searchText) ||
                            student.getMajor().getMajorName().toLowerCase().contains(searchText) ||
                            student.getYear().toLowerCase().contains(searchText);
                })
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            JLabel nothingOutput = new JLabel("No students found");
            nothingOutput.setAlignmentX(Component.CENTER_ALIGNMENT);
            studentPanel.add(Box.createVerticalStrut(20));
            studentPanel.add(nothingOutput);
        } else {
            for (Student student : filtered) {
                studentPanel.add(createBox(student));
                studentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        studentPanel.revalidate();
        studentPanel.repaint();
    }

    private void loadStudentList() {
        studentPanel.removeAll();

        for (Student student : this.studentController.getAllStudents()) {
            JPanel box = createBox(student);
            studentPanel.add(box);
            studentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        studentPanel.revalidate();
        studentPanel.repaint();
    }

    private JPanel createBox(Student student) {
        JPanel containerPanel = new JPanel(new BorderLayout(10, 0));
        containerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        containerPanel.setBackground(Color.WHITE);

        JPanel box = new JPanel(new GridLayout(6, 2, 5, 5));
        box.setBackground(Color.WHITE);

        box.add(new JLabel("Student ID:"));
        box.add(new JLabel(student.getStudentID()));
        box.add(new JLabel("Name:"));
        box.add(new JLabel(student.getFirstName() + " " + student.getLastName()));
        box.add(new JLabel("Major:"));
        box.add(new JLabel(student.getMajor().getMajorName()));
        box.add(new JLabel("Year:"));
        box.add(new JLabel(student.getYear()));
        box.add(new JLabel("Email:"));
        box.add(new JLabel(student.getEmail()));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.WHITE);

        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");

        editBtn.setMaximumSize(new Dimension(100, 30));
        deleteBtn.setMaximumSize(new Dimension(100, 30));

        editBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        editBtn.addActionListener(e ->{
            editStudentDialog(student);
        });

        deleteBtn.addActionListener(e ->{
            deleteStudentDialog(student);
        });

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(editBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(deleteBtn);
        buttonPanel.add(Box.createVerticalGlue());

        containerPanel.add(box, BorderLayout.CENTER);
        containerPanel.add(buttonPanel, BorderLayout.EAST);

        // Prevent vertical stretching
        containerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, containerPanel.getPreferredSize().height));

        return containerPanel;
    }

    private void addStudentDialog(){
        JDialog dialog = new JDialog(this, "Add Student", true);
        dialog.setSize(450, 450);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setLocationRelativeTo(this);

        JPanel jPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        jPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JPanel btnPanel = new JPanel();
        JButton addbtn = new JButton("Add");
        String[] majors = {"Computer Science", "Engineering", "Mathematics", "Literature", "Philosophy", "Biology"};
        String[] years = {"Freshman", "Sophomore", "Junior", "Senior"};

        JTextField idField = new JTextField();
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField semField = new JTextField("Semester 1");
        semField.setEditable(false);
        JComboBox<String> majorDropdown = new JComboBox<>(majors);
        JComboBox<String> yearDropdown = new JComboBox<>(years);

        jPanel.add(new JLabel("Student ID:"));
        jPanel.add(idField);
        jPanel.add(new JLabel("First Name:"));
        jPanel.add(firstNameField);
        jPanel.add(new JLabel("Last Name:"));
        jPanel.add(lastNameField);
        jPanel.add(new JLabel("Major:"));
        jPanel.add(majorDropdown);
        jPanel.add(new JLabel("Year:"));
        jPanel.add(yearDropdown);
        jPanel.add(new JLabel("Semester:"));
        jPanel.add(semField);
        jPanel.add(new JLabel("Email:"));
        jPanel.add(emailField);

        addbtn.addActionListener(e -> {
            String id = idField.getText();
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String major = majorDropdown.getSelectedItem().toString();
            String year = yearDropdown.getSelectedItem().toString();
            String email = emailField.getText();

            if (id.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill up all the fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Student newStudent = new Student(id, firstName, lastName, new Major("12", major), year, "Semester 1", email);

            this.studentController.addNewStudent(newStudent);
            dialog.dispose();
            loadStudentList();
        });

        btnPanel.add(addbtn);

        dialog.add(jPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteStudentDialog(Student student){
        String message = String.format(
                "Are you sure you want to delete student:\n\n" +
                        "Name: %s %s\n" +
                        "Email: %s\n" +
                        "Major: %s", 
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getMajor().getMajorName()
        );

        int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION){
            this.studentController.deleteStudent(student);
            recentDeletedStudent = student;
            filterStudents();
            undoOption();
        }
    }

    private void undoOption(){
        // Stop timer if any exist
        if (undoTimer != null) {
            undoTimer.stop();
        }

        // Button
        undoBtn.setVisible(true);
        undoBtn.setText("Undo delete within 20s");

        // Timer
        undoTimer = new Timer(1000, null);
        final int[] timeLeft = {20};

        undoTimer.addActionListener(e -> {
            timeLeft[0]--;
            if (timeLeft[0] > 0) {
                undoBtn.setText("Undo delete within " + timeLeft[0] + "s");
            } else {
                undoTimer.stop();
                undoBtn.setVisible(false);
                recentDeletedStudent = null;
            }
        });
        undoTimer.start();
    }

    private void undoDelete() {
        if (recentDeletedStudent == null) {
            JOptionPane.showMessageDialog(this, "No deletion to undo", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }else{
            // Stop the timer
            if (undoTimer != null) undoTimer.stop();

        
            this.studentController.addNewStudent(recentDeletedStudent);
            filterStudents();
            
            undoBtn.setVisible(false);
            JOptionPane.showMessageDialog(this, String.format("Student '%s %s' is restored", recentDeletedStudent.getFirstName(), recentDeletedStudent.getLastName()), "Successful", JOptionPane.INFORMATION_MESSAGE);

            recentDeletedStudent = null;
        }
    }

    private void editStudentDialog(Student student){
        JDialog dialog = new JDialog(this, "Update/Edit Student", true);
        dialog.setSize(450, 450);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setLocationRelativeTo(this);

        JPanel jPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        jPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");

        // Fields
        String[] majors = {"Computer Science", "Engineering", "Mathematics", "Literature", "Philosophy", "Biology"};
        String[] years = {"Freshman", "Sophomore", "Junior", "Senior"};

        JTextField idField = new JTextField(student.getStudentID());
        idField.setEditable(false);
        idField.setBackground(Color.LIGHT_GRAY);

        JTextField firstNameField = new JTextField(student.getFirstName());
        JTextField lastNameField = new JTextField(student.getLastName());
        JComboBox<String> majorField = new JComboBox<>(majors);
        majorField.setSelectedItem(student.getMajor());
        JComboBox<String> yearField = new JComboBox<>(years);
        yearField.setSelectedItem(student.getYear());
        JTextField emailField = new JTextField(student.getEmail());

        jPanel.add(new JLabel("Student ID:"));
        jPanel.add(idField);
        jPanel.add(new JLabel("First Name:"));
        jPanel.add(firstNameField);
        jPanel.add(new JLabel("Last Name:"));
        jPanel.add(lastNameField);
        jPanel.add(new JLabel("Major:"));
        jPanel.add(majorField);
        jPanel.add(new JLabel("Year:"));
        jPanel.add(yearField);
        jPanel.add(new JLabel("Email:"));
        jPanel.add(emailField);

        saveBtn.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String major = majorField.getSelectedItem().toString();
            String year = yearField.getSelectedItem().toString();

             // check for new changes
            if (firstName.equals(student.getFirstName()) &&
                    lastName.equals(student.getLastName()) &&
                    email.equals(student.getEmail()) &&
                    major.equals(student.getMajor().getMajorName()) &&
                    year.equals(student.getYear())) {
                JOptionPane.showMessageDialog(dialog, "No changes", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            student.setFirstName(firstNameField.getText());;
            student.setLastName(lastNameField.getText());
            student.setEmail(emailField.getText());
            student.getMajor().setMajorName(majorField.getSelectedItem().toString());
            student.setYear(yearField.getSelectedItem().toString());

            this.studentController.updateStudent(student);
            dialog.dispose();
            filterStudents();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(jPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void goBack() {
        this.dispose();
        if (session.getUser().getRole().equals("Admin")) {
            new CourseAdminPage(session).setVisible(true);
        } else if (session.getUser().getRole().equals("AcademicOfficer")) {
            new AcademicOfficerPage(session).setVisible(true);
        }
    }
}