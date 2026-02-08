package classes;

import javax.swing.*;
import javax.swing.Timer;

import controller.LoginController;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


public class UserManagementPage extends JFrame {
    private User user;
    private JPanel userPanel;
    private JTextField searchField;
    private LoginController session;

    JButton undoBtn;
    private Timer undoTimer;
    private User recentDeletedUser;
    private static final int timeout = 20000;

    public UserManagementPage(LoginController session){
        this.session = session;
        this.user = session.getUser();
        initFrame();
    }

    private void initFrame() {
        setTitle("User Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Section
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backBtn = new JButton("Back");
        backBtn.setPreferredSize(new Dimension(80, 20));
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
        JButton addBtn = new JButton("Add User");
        addBtn.addActionListener(e -> addUserDialog());

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
        searchField.addActionListener(e -> filterUsers());
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterUsers(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterUsers(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterUsers(); }
        });

        // Center
        userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        loadUserList();

        JScrollPane scrollPane = new JScrollPane(userPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        addWindowListener(new java.awt.event.WindowAdapter(){
            @Override
            public void windowActivated(java.awt.event.WindowEvent e){
                // refreshUser();
            }
        });

        add(mainPanel);
    }

    private void filterUsers() {
        String searchText = searchField.getText().toLowerCase().trim();

        userPanel.removeAll();

        List<User> filtered = session.getAllUsers().stream().filter(user -> user.getRole().equals("Admin") || user.getRole().equals("AcademicOfficer")).filter(user -> {
                    if (searchText.isEmpty()) {
                        return true;
                    }
                    String name = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
                    return name.contains(searchText) ||
                            user.getEmail().toLowerCase().contains(searchText) ||
                            user.getRole().toLowerCase().contains(searchText);
                })
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            JLabel nothingOutput = new JLabel("No users found");
            nothingOutput.setAlignmentX(Component.CENTER_ALIGNMENT);
            userPanel.add(Box.createVerticalStrut(20));
            userPanel.add(nothingOutput);
        } else {
            for (User user : filtered) {
                userPanel.add(createBox(user));
                userPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        userPanel.revalidate();
        userPanel.repaint();
    }

    private JPanel createBox(User user) {
        JPanel containerPanel = new JPanel(new BorderLayout(10, 0));
        containerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        containerPanel.setBackground(Color.WHITE);

        JPanel box = new JPanel(new GridLayout(6, 2, 5, 5));
        box.setBackground(Color.WHITE);

        box.add(new JLabel("Username:"));
        box.add(new JLabel(user.getUsername()));
        box.add(new JLabel("Name:"));
        box.add(new JLabel(user.getFirstName() + " " + user.getLastName()));
        box.add(new JLabel("Email:"));
        box.add(new JLabel(user.getEmail()));
        box.add(new JLabel("Role:"));
        box.add(new JLabel(user.getRole()));
        box.add(new JLabel("Last Login:"));
        box.add(new JLabel(user.getLastLoginFormatted()));
        box.add(new JLabel("Last Logout:"));
        box.add(new JLabel(user.getLastLogoutFormatted()));

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
            editUserDialog(user);
        });

        deleteBtn.addActionListener(e ->{
            deleteUserDialog(user);
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

    private void addUserDialog(){
        JDialog dialog = new JDialog(this, "Add User", true);
        dialog.setSize(450, 450);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setLocationRelativeTo(this);

        JPanel jPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        jPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JPanel btnPanel = new JPanel();
        JButton addbtn = new JButton("Add");

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleField = new JComboBox<>(new String[]{"Admin", "AcademicOfficer"});
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField emailField = new JTextField();

        jPanel.add(new JLabel("Username:"));
        jPanel.add(usernameField);
        jPanel.add(new JLabel("Password:"));
        jPanel.add(passwordField);
        jPanel.add(new JLabel("Role:"));
        jPanel.add(roleField);
        jPanel.add(new JLabel("First Name:"));
        jPanel.add(firstNameField);
        jPanel.add(new JLabel("Last Name:"));
        jPanel.add(lastNameField);
        jPanel.add(new JLabel("Email:"));
        jPanel.add(emailField);

        addbtn.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();

            // Check if all the fields are filled up
            if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Fill up all the fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate password
            if (password.length() < 8) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 8 characters long", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.matches("[a-zA-Z0-9]+")) {
                JOptionPane.showMessageDialog(dialog, "Password must not have any special characters", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            session.addNewUser(user);
            dialog.dispose();
            filterUsers();//stil don't know
        });

        btnPanel.add(addbtn);

        dialog.add(jPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void undoDelete() {
        if (recentDeletedUser == null) {
            JOptionPane.showMessageDialog(this, "No deletion to undo", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }else{
             // Stop the timer
            if (undoTimer != null) {undoTimer.stop();}

            session.addNewUser(recentDeletedUser);
            filterUsers();

            // Hide undo button
            undoBtn.setVisible(false);
            JOptionPane.showMessageDialog(this, String.format("User '%s %s' is restored", recentDeletedUser.getFirstName(), recentDeletedUser.getLastName()), "Successful", JOptionPane.INFORMATION_MESSAGE);

            recentDeletedUser = null;
        }
    }

    private void editUserDialog(User user){
        JDialog dialog = new JDialog(this, "Update/Edit User", true);
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
        JTextField usernameField = new JTextField(user.getUsername());
        usernameField.setEditable(false);
        usernameField.setBackground(Color.LIGHT_GRAY);

        JPasswordField passwordField = new JPasswordField(user.getPassword());
        JComboBox<String> roleField = new JComboBox<>(new String[]{"Admin", "AcademicOfficer"});
        roleField.setSelectedItem(user.getRole());

        JTextField firstNameField = new JTextField(user.getFirstName());
        JTextField lastNameField = new JTextField(user.getLastName());
        JTextField emailField = new JTextField(user.getEmail());

        jPanel.add(new JLabel("Username:"));
        jPanel.add(usernameField);
        jPanel.add(new JLabel("Password:"));
        jPanel.add(passwordField);
        jPanel.add(new JLabel("Role:"));
        jPanel.add(roleField);
        jPanel.add(new JLabel("First Name:"));
        jPanel.add(firstNameField);
        jPanel.add(new JLabel("Last Name:"));
        jPanel.add(lastNameField);
        jPanel.add(new JLabel("Email:"));
        jPanel.add(emailField);

        saveBtn.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String role = roleField.getSelectedItem().toString();

            // defaults to existing values if empty
            if (password.isEmpty()) password = user.getPassword();
            if (firstName.isEmpty()) firstName = user.getFirstName();
            if (lastName.isEmpty()) lastName = user.getLastName();
            if (email.isEmpty()) email = user.getEmail();

            // check for new changes
            if (password.equals(user.getPassword()) &&
                    firstName.equals(user.getFirstName()) &&
                    lastName.equals(user.getLastName()) &&
                    email.equals(user.getEmail()) &&
                    role.equals(user.getRole())) {
                JOptionPane.showMessageDialog(dialog, "No changes", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }else{
                user.setPassword(password);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setEmail(email);
                session.updateUserDetails(user);
                JOptionPane.showMessageDialog(dialog, "changes are successfully made", "Info", JOptionPane.INFORMATION_MESSAGE);
            }

            dialog.dispose();
            filterUsers();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(jPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void deleteUserDialog(User user){
        String message = String.format(
                "Are you sure you want to delete user:\n\n" +
                        "Name: %s %s\n" +
                        "Email: %s\n" +
                        "Role: %s",
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole()
        );

        int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION){
            try{
                this.recentDeletedUser = session.removeUser(user);
                filterUsers();
                undoOption(); // show undo after deleting user
                JOptionPane.showMessageDialog(this, "User deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
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
                recentDeletedUser = null;
            }
        });
        undoTimer.start();
    }

    private void loadUserList() {
        userPanel.removeAll();

        for (User user : session.getAllUsers()) {
            if (user.getRole().equals("Admin") || user.getRole().equals("AcademicOfficer")) {
                JPanel box = createBox(user);
                userPanel.add(box);
                userPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        userPanel.revalidate();
        userPanel.repaint();
    }

    private void goBack() {
        // Stop timer
        if (undoTimer != null){
            undoTimer.stop();
        }

        this.dispose();
        new CourseAdminPage(session).setVisible(true);
    }
}
