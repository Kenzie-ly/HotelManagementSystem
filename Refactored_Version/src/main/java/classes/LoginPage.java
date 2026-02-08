package classes;
import javax.swing.*;

import controller.LoginController;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

public class LoginPage extends JFrame {
    private LoginController session;

    public LoginPage(){
        session = new LoginController();
        initFrame();
    }

    private void initFrame() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900,600);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));

        // Left panel side
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.BLACK);
        leftPanel.setLayout(new GridBagLayout());

        JLabel title = new JLabel("Java Fun & Good");
        title.setFont(new Font("Poppin", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        leftPanel.add(title);

        // Right panel side
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        // Center the title
        JLabel loginLabel = new JLabel("Login");
        loginLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        loginLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // Username label and field
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setBackground(Color.WHITE);
        userPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userPanel.setMaximumSize(new Dimension(300, 80));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField userField = new JTextField();
        userField.setPreferredSize(new Dimension(200, 35));
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // Password label and field
        JPanel passPanel = new JPanel();
        passPanel.setLayout(new BoxLayout(passPanel, BoxLayout.Y_AXIS));
        passPanel.setBackground(Color.WHITE);
        passPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passPanel.setMaximumSize(new Dimension(300, 80));

        JLabel passLabel = new JLabel("Password:");
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPasswordField passField = new JPasswordField();
        passField.setPreferredSize(new Dimension(200, 35));
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // Buttons
        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(Color.BLACK);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setPreferredSize(new Dimension(300, 42));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton forgotBtn = new JButton("Forgot Password");
        forgotBtn.setBackground(Color.WHITE);
        forgotBtn.setForeground(Color.BLACK);
        forgotBtn.setFocusPainted(false);
        forgotBtn.setPreferredSize(new Dimension(300, 42));
        forgotBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        forgotBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginBtn.addActionListener((ActionEvent e) -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            User user = session.authenticateUser(new User(username, password));

            if (user != null) {
                System.out.println("Login successful!");
                System.out.println(user.getUsername() + " (" + user.getRole() + ")");
                System.out.println("Last login: " + user.getLastLogin());
                System.out.println("Last logout: " + user.getLastLogout());
                roleNavigator(session);
            } else {
                System.out.println("Invalid");
                JOptionPane.showMessageDialog(LoginPage.this, "Invalid username or password");
            }
        });

        forgotBtn.addActionListener(e -> {
            forgetBtn(userField);
        });

        // Layout
        userPanel.add(userLabel);
        userPanel.add(userField);

        passPanel.add(passLabel);
        passPanel.add(passField);

        rightPanel.add(loginLabel);
        rightPanel.add(userPanel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(passPanel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(loginBtn);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(forgotBtn);

        add(leftPanel);
        add(rightPanel);
    }

    private void roleNavigator(LoginController session){
        this.dispose();
        if(session.getUser().getRole().equals("Admin")){
            new CourseAdminPage(session).setVisible(true);
            System.out.println("successful login for admin");
        } else if (session.getUser().getRole().equals("AcademicOfficer")){
            new AcademicOfficerPage(session).setVisible(true);
            System.out.println("successfult login for academic officer");
        } else {
            System.out.println("Error: Role not found");
        }
    }

    private void forgetBtn(JTextField userField){
        User user;
        String username = userField.getText();
        String verifCode = verifCodeGen();
        System.out.println(verifCode);
        
        if((user = session.emailRetrieval(username,verifCode)) != null){
            this.dispose();
            new ForgetPassPage(username, user.getEmail(), verifCode, session).setVisible(true);
        }
    }

    // Generate 6 digit Verification Code
    private static String verifCodeGen() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

}