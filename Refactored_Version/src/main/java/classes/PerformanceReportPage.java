package classes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controller.*;
import repository.*;

public class PerformanceReportPage extends JFrame{
    private LoginController session;
    private String savedFolderPath;
    private String chosenDocumentLogoPath;
    private JTextField programTextField;
    private JTextField yearStudyTextField;
    private JLabel statusResult;
    private JRadioButton semesterRadioButton;
    private JLabel chosenDocumentLogoLabel;
    private List<Student> students;

    public PerformanceReportPage(LoginController session)
    {
        this.session = session;
        
        //JFRAME Configuration
        this.setSize(1000,600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo((Component)null);
        this.setTitle("Performance Report Page");

        JPanel mainPanel = new JPanel(new BorderLayout(10, 20));
        createHeaderAndBody(mainPanel);
        add(mainPanel);
    }

    // If student exist, get their Student ID
    private Student getStudentByID(String studentID) {
        for (Student student : students) {
            if (student.getStudentID().equals(studentID)) {
                return student;
            }
        }
        return null;
    }

    private void sendEmailAttachments(String folderPath) {
        if (folderPath == null){
            return;
        }

        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name)-> name.endsWith(".pdf"));

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                String studentID = fileName.split("_")[0];

                Student student = getStudentByID(studentID);
                if (student != null) {
                    try {
                        String studentEmail = student.getEmail();
                        String subject = "Academic Performance Report";
                        String bodyText = "Attached is the Academic Performance Report";
                        EmailSender.sendEmail(studentEmail, subject, bodyText, file.getAbsolutePath());
                        JOptionPane.showMessageDialog(PerformanceReportPage.this, "Email sent successfully to " + studentEmail + "!");
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(PerformanceReportPage.this, "Failed to send email!");
                        e.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(PerformanceReportPage.this, "Student with the Student ID '" + studentID + "' not found!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(PerformanceReportPage.this, "No files found in the folder");
            System.out.println("No files found in the folder: " + folderPath);
        }
    }

    private void exportToPdf(){
        //check in case the document has been generated
        if (this.statusResult.getText().equals("Available")) {
            if (!(createConfirmDiaglog("The documents have been generated. Do you want to overwrite all documents with the new generated ones?"))){return;}
        }

        //check in case logoPath is not found
        String defaultLogoPath = ResourceManager.getDefultLogoDataPath();
        if (!(chosenDocumentLogoPath.equalsIgnoreCase(defaultLogoPath))) {
            File file = new File(chosenDocumentLogoPath);
            //if not found, use default logo
            if (!(file.exists())){
                if (!(createConfirmDiaglog("The selected logo file was not found. Do you want to continue with the default logo?"))){return;}
                this.chosenDocumentLogoPath = defaultLogoPath;
                this.chosenDocumentLogoLabel.setText("defaultLogo.png");
            }
        }

        //check in case the folder name is not correctly formated
        String folderPath = getAPRFolderPath();
        if (folderPath == null){
            JOptionPane.showMessageDialog(null, "No students found matching the specified criteria, please try again!", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        

        //get filter, yearStudy, program, folderPath
        String filter = checkSemYearStatus();
        String yearStudy = yearStudyTextField.getText().toLowerCase();
        String program = programTextField.getText().toLowerCase();
        
        File directory = new File(folderPath);
        //create performance report
        try {
            PdfFile pdfFile = new StudentPerformanceReport(filter);
            PdfController performanceReport = new PdfController(pdfFile, true);
            //successfully generated reports
            if (performanceReport.generateManyPerformanceReports(chosenDocumentLogoPath,yearStudy, program, directory)){
                JOptionPane.showMessageDialog(null, "The document has been successfuly generated!", "Information", JOptionPane.INFORMATION_MESSAGE);
                updateStatus(); 
            //unsuccessfully generated reports
            }else{
                JOptionPane.showMessageDialog(null, "No students found matching the specified criteria, please try again!", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            //if error, delete the new created folder
            directory.delete();
            JOptionPane.showMessageDialog(null, e, "Error message", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatusAsync() {
        String folderPath = getAPRFolderPath();
        if (folderPath == null){
            statusResult.setText("Not Available");
            statusResult.setForeground(Color.red);
            return;
        }

        try{
            File folder = new File(folderPath);
            if(folder.exists() && folder.isDirectory()){
                statusResult.setText("Available");
                statusResult.setForeground(Color.green);
            }else{
                statusResult.setText("Not Available");
                statusResult.setForeground(Color.red);
            }
        }catch (Exception error){
            error.printStackTrace();
        }
    }
        

    private void checkProgram(){
        //update status when programTextField get modified
        programTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateStatusAsync(); }
            public void removeUpdate(DocumentEvent e) { updateStatusAsync(); }
            public void changedUpdate(DocumentEvent e) { updateStatusAsync(); }
        });
    }

    private void checkYearStudy(){
        //update status when yearStudy get modified
        yearStudyTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateStatusAsync(); }
            public void removeUpdate(DocumentEvent e) { updateStatusAsync(); }
            public void changedUpdate(DocumentEvent e) { updateStatusAsync(); }
        });
    }


    private void browseFolderPath(JLabel folderPathLabel){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        //open a window to choose file
        int option = chooser.showOpenDialog(this);

        //if folder is selected
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = chooser.getSelectedFile();
            folderPathLabel.setText(selectedFolder.getAbsolutePath());
            this.savedFolderPath = selectedFolder.getAbsolutePath();
            updateStatus();
        }
    }

    private boolean browseLogoPath(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        //open a window to choose file
        int option = chooser.showOpenDialog(this);

        //if logo is selected
        if (option == JFileChooser.APPROVE_OPTION){
            File selectedFile = chooser.getSelectedFile();
            try{
                //check if the image can be opened
                BufferedImage chosenImage = ImageIO.read(selectedFile);
                this.chosenDocumentLogoPath = selectedFile.getAbsolutePath();
                this.chosenDocumentLogoLabel.setText(selectedFile.getName());

                return chosenImage != null;
            }catch(IOException e){
                //errors means cannot be opened
                System.out.println(e);
                return false;
            }
        }else{
            return false;
        }
    }
    private void resetSettings(JLabel folderPathLabel,JLabel chosenDocumentLogoLabel){
        //set labels to its default content
        folderPathLabel.setText("D:\\");
        chosenDocumentLogoLabel.setText("defaultLogo.png");

        //set fields to its default content, following the content's labels
        this.savedFolderPath = "D:\\";
        this.chosenDocumentLogoPath = ResourceManager.getDefultLogoDataPath();
    }

    private void goBack(){
        this.dispose();
        String role = session.getUser().getRole();

        if (role.equals("Admin")) {
            new CourseAdminPage(session).setVisible(true);
        } else if (role.equals("AcademicOfficer")) {
            new AcademicOfficerPage(session).setVisible(true);
        }
    }

    private String getAPRFolderPath(){
        String filter = checkSemYearStatus();
        String program = programTextField.getText().toLowerCase().trim();
        String yearStudy = yearStudyTextField.getText().toLowerCase().trim();

        //if there are no inputs or there are illegal symbols
        if(!(program.matches("[a-zA-Z ]+") && yearStudy.matches("[a-zA-Z ]+"))){
            return null;
        }

        return Paths.get(savedFolderPath, "Academic Performance Reports", yearStudy, program, filter).toString();
    }

    private String checkSemYearStatus(){
        String filter;

        //by default semester is always true
        if(semesterRadioButton.isSelected()){
            filter = "semester";
        }else{
            filter = "year";
        }

        return filter;
    }

    private void updateStatus(){
        if (getAPRFolderPath() == null){
            return;
        }

        File folder = new File(getAPRFolderPath());

        //check if local device has the folder
        if(folder.exists() && folder.isDirectory()){
            //has the folder = has the document file
            statusResult.setText("Available");
            statusResult.setForeground(Color.GREEN);
        }else{
            //doens't have the folder = doesn't have the document file
            statusResult.setText("Not Available");
            statusResult.setForeground(Color.RED);
        }
    }

    private boolean createConfirmDiaglog(String message){
        int result = JOptionPane.showConfirmDialog(null,message,"Confirmation",JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    private JPanel createElementForEmail(){
        JPanel emailPanel = new JPanel();
        emailPanel.setLayout(new BoxLayout(emailPanel, BoxLayout.Y_AXIS));
        emailPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 250));

        JLabel emailDescriptionLabel = new JLabel("Email Description: ");
        emailDescriptionLabel.setFont(new Font("Serif", Font.BOLD, 25));
        emailDescriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea emailDescriptionTextArea = new JTextArea(8, 1);
        emailDescriptionTextArea.setLineWrap(true);
        emailDescriptionTextArea.setWrapStyleWord(true);
        JScrollPane emailDescriptionScrollPane = new JScrollPane(emailDescriptionTextArea);
        emailDescriptionScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        emailDescriptionScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton sendEmailButton = new JButton("Send Email");
        JPanel sendBtnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sendBtnWrap.add(sendEmailButton);
        sendBtnWrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        sendEmailButton.addActionListener(e -> {
            if (statusResult.getText().equals("Available")) {
                String folderPath = getAPRFolderPath();
                sendEmailAttachments(folderPath);
            }
        });

        emailPanel.add(emailDescriptionLabel);
        emailPanel.add(Box.createVerticalStrut(8));
        emailPanel.add(emailDescriptionScrollPane);
        emailPanel.add(Box.createVerticalStrut(8));
        emailPanel.add(sendBtnWrap);

        return emailPanel;
    }

    private JPanel createElementForFilter(Font labelFont, JButton exportButton){
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));

        //title
        JLabel filterPanelTitle = new JLabel("Generate Report by:");
        filterPanelTitle.setFont(new Font("Serif", Font.BOLD, 22));
        filterPanelTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        //left side
        JLabel programLabel = new JLabel("Program: ");
        programLabel.setFont(labelFont);
        JLabel yearOfStudyLabel = new JLabel("Year of Study: ");
        yearOfStudyLabel.setFont(labelFont);
        JLabel semesterLabel = new JLabel("Semester: ");
        semesterLabel.setFont(labelFont);
        JLabel statusLabel = new JLabel("Document's Status: ");
        statusLabel.setFont(labelFont);

        //right side
        JTextField programTextField = new JTextField("");
        programTextField.setPreferredSize(new Dimension(280,30));
        programTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        this.programTextField = programTextField;

        JTextField yearStudyTextField = new JTextField("");
        yearStudyTextField.setPreferredSize(new Dimension(280,30));
        yearStudyTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        this.yearStudyTextField = yearStudyTextField;

        ButtonGroup sem_year_group = new ButtonGroup();
        semesterRadioButton = new JRadioButton("by semester", true);
        JRadioButton yearRadioButton = new JRadioButton("by year");
        JPanel sem_yearPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        sem_yearPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sem_year_group.add(semesterRadioButton);
        sem_year_group.add(yearRadioButton);
        sem_yearPanel.add(semesterRadioButton);
        sem_yearPanel.add(yearRadioButton);

        JLabel statusResultLabel = new JLabel("Not Available");
        statusResultLabel.setFont(labelFont);
        statusResultLabel.setForeground(Color.red);
        this.statusResult = statusResultLabel;

        //panel (left side + right side)
        JPanel rowProgram = new JPanel(new BorderLayout(8,0));
        rowProgram.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowProgram.add(programLabel, BorderLayout.WEST);
        rowProgram.add(programTextField, BorderLayout.CENTER);

        JPanel rowYearOfStudy = new JPanel(new BorderLayout());
        rowYearOfStudy.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowYearOfStudy.add(yearOfStudyLabel, BorderLayout.WEST);
        rowYearOfStudy.add(yearStudyTextField, BorderLayout.CENTER);

        JPanel rowSemester = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rowSemester.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowSemester.add(semesterLabel);
        rowSemester.add(sem_yearPanel);

        JPanel rowStatus = new JPanel(new BorderLayout(8,0));
        rowStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowStatus.add(statusLabel, BorderLayout.WEST);
        rowStatus.add(statusResultLabel, BorderLayout.CENTER);

        //add to filter panel
        filterPanel.add(filterPanelTitle);
        filterPanel.add(Box.createVerticalStrut(8));
        filterPanel.add(rowProgram);
        filterPanel.add(Box.createVerticalStrut(8));
        filterPanel.add(rowYearOfStudy);
        filterPanel.add(Box.createVerticalStrut(8));
        filterPanel.add(rowSemester);
        filterPanel.add(Box.createVerticalStrut(8));
        filterPanel.add(rowStatus);
        filterPanel.add(Box.createVerticalStrut(6));

        //button events
        exportButton.addActionListener(e -> 
            exportToPdf()
        );
        semesterRadioButton.addActionListener(e -> updateStatus());
        yearRadioButton.addActionListener(e -> updateStatus());
        checkProgram();
        checkYearStudy();

        return filterPanel;
    }

    private JPanel createFilterAndEmailSection(Font labelFont, JButton exportButton){
        JPanel filterAndEmailPanel = new JPanel();
        filterAndEmailPanel.setPreferredSize(new Dimension(650,0));
        filterAndEmailPanel.setBackground(new Color(245,245,245)); 
        filterAndEmailPanel.setBorder(BorderFactory.createTitledBorder("Filters & Email"));
        filterAndEmailPanel.setOpaque(true);
        filterAndEmailPanel.setLayout(new BoxLayout(filterAndEmailPanel, BoxLayout.Y_AXIS));

        filterAndEmailPanel.add(createElementForFilter(labelFont, exportButton));
        filterAndEmailPanel.add(Box.createVerticalStrut(12));
        filterAndEmailPanel.add(createElementForEmail());

        return filterAndEmailPanel;

    }

    private JPanel createSettingSection(Font labelFont){
        JPanel settingPanel = new JPanel();
        settingPanel.setPreferredSize(new Dimension(350,0));
        settingPanel.setLayout(new BoxLayout(settingPanel, BoxLayout.Y_AXIS));
        
        //Elements
        JLabel settingPanelTitle = new JLabel("Default Settings");
        settingPanelTitle.setFont(new Font("Serif", Font.BOLD, 25));
        settingPanelTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel folderPathLabel = new JLabel("Folder Path: ");
        folderPathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel savedFolderPathLabel = new JLabel("D:\\");
        savedFolderPathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.savedFolderPath = savedFolderPathLabel.getText();
        
        JLabel documentLogoLabel = new JLabel("Document's logo:");
        documentLogoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton browseFolderPathButton = new JButton("Browse");
        browseFolderPathButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        browseFolderPathButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel chosenDocumentLogoLabel = new JLabel("defaultLogo.png");
        chosenDocumentLogoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.chosenDocumentLogoPath = ResourceManager.getDefultLogoDataPath();
        this.chosenDocumentLogoLabel = chosenDocumentLogoLabel; 
        
        JButton browseLogoButton = new JButton("Browse");
        browseLogoButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        browseLogoButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField emailFormatTextField = new JTextField("");
        emailFormatTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        emailFormatTextField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton resetSettingsButton = new JButton("Reset");
        resetSettingsButton.setPreferredSize(new Dimension(120, 35));
        
        JPanel resetButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        resetButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        resetButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetButtonPanel.add(resetSettingsButton);

        //Set Font
        folderPathLabel.setFont(labelFont);
        documentLogoLabel.setFont(labelFont);

        //adding element to the panel
        settingPanel.add(settingPanelTitle);
        settingPanel.add(Box.createVerticalStrut(20));

        settingPanel.add(folderPathLabel);
        settingPanel.add(Box.createVerticalStrut(5));
        settingPanel.add(savedFolderPathLabel);
        settingPanel.add(Box.createVerticalStrut(5));

        settingPanel.add(browseFolderPathButton);
        settingPanel.add(Box.createVerticalStrut(15));
        settingPanel.add(documentLogoLabel);
        settingPanel.add(Box.createVerticalStrut(5));
        settingPanel.add(chosenDocumentLogoLabel);
        settingPanel.add(Box.createVerticalStrut(10));
        settingPanel.add(browseLogoButton);
        settingPanel.add(Box.createVerticalStrut(20));

        settingPanel.add(resetButtonPanel);
        settingPanel.add(Box.createVerticalGlue());

        //button events
        resetSettingsButton.addActionListener(e -> resetSettings(savedFolderPathLabel, chosenDocumentLogoLabel));
        browseFolderPathButton.addActionListener(e -> browseFolderPath(savedFolderPathLabel));
        browseLogoButton.addActionListener((actionEvent) -> browseLogoPath());

        return settingPanel;
    }

    private void createHeaderAndBody(JPanel mainPanel){
        Font labelFont = new Font("Times New Roman", Font.BOLD, 18);
        
        //Header
        JPanel headerPanel = new JPanel(new BorderLayout());

        Font headerFont = new Font("Serif", Font.BOLD, 25);
        JLabel headerLabel = new JLabel("Academic Performance Reporting", JLabel.CENTER);
        headerLabel.setFont(headerFont);

        JButton backButton = new JButton("Back");
        JButton exportButton = new JButton("Export");

        //HEADER CONFIGURATION
        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(exportButton, BorderLayout.EAST);

        //BODY
        JPanel bodyPanel = new JPanel(new BorderLayout(10,0));

        //BODY CONFIGURATION 
        bodyPanel.add(createFilterAndEmailSection(labelFont, exportButton), BorderLayout.CENTER);        
        bodyPanel.add(createSettingSection(labelFont), BorderLayout.EAST);

        //MAIN PANEL CONFIGURATION 
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(bodyPanel, BorderLayout.CENTER);
        
        //button events
        backButton.addActionListener(e -> goBack());
    }
}