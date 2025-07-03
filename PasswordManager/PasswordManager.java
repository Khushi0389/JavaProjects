import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class PasswordManager extends JFrame {
    private static final String MASTER_PASSWORD = "admin123"; // Change this if needed
    private static final String DATA_FILE = "passwords.txt";

    private Map<String, String> passwordMap = new HashMap<>();

    // GUI components
    private JTextField accountField;
    private JTextField passwordField;
    private JTextField searchField;
    private JLabel searchResultLabel;
    private JTextArea displayArea;

    public PasswordManager() {
        showLoginScreen();
    }

    private void showLoginScreen() {
        JPasswordField passwordField = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(
                this,
                passwordField,
                "Enter Master Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String input = new String(passwordField.getPassword());
            if (input.equals(MASTER_PASSWORD)) {
                loadPasswords();
                createMainUI();
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect Master Password. Exiting.");
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    private void loadPasswords() {
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String account = decrypt(parts[0]);
                    String password = decrypt(parts[1]);
                    passwordMap.put(account, password);
                }
            }
        } catch (IOException e) {
            // File might not exist yet
        }
    }

    private void savePasswordsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Map.Entry<String, String> entry : passwordMap.entrySet()) {
                String encAccount = encrypt(entry.getKey());
                String encPassword = encrypt(entry.getValue());
                writer.write(encAccount + ":" + encPassword);
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save file.");
        }
    }

    private void createMainUI() {
        setTitle("Password Manager");
        setSize(450, 500);
        setLayout(new BorderLayout());

        // Upper panel for adding
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add / Update Password"));

        inputPanel.add(new JLabel("Account:"));
        accountField = new JTextField();
        inputPanel.add(accountField);

        inputPanel.add(new JLabel("Password:"));
        passwordField = new JTextField();
        inputPanel.add(passwordField);

        JButton addButton = new JButton("Add / Update");
        addButton.addActionListener(e -> savePassword());
        inputPanel.add(addButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deletePassword());
        inputPanel.add(deleteButton);

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search by Account"));

        JPanel searchTop = new JPanel(new BorderLayout(5, 5));
        searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchPassword());

        searchTop.add(searchField, BorderLayout.CENTER);
        searchTop.add(searchButton, BorderLayout.EAST);

        searchPanel.add(searchTop, BorderLayout.NORTH);

        searchResultLabel = new JLabel(" ");
        searchPanel.add(searchResultLabel, BorderLayout.CENTER);

        // Display area
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Stored Passwords"));

        // Add to frame
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        updateDisplay();
        setVisible(true);
    }

    private void savePassword() {
        String account = accountField.getText().trim();
        String password = passwordField.getText().trim();

        if (account.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in both fields.");
            return;
        }

        passwordMap.put(account, password);
        savePasswordsToFile();
        updateDisplay();
        accountField.setText("");
        passwordField.setText("");
    }

    private void deletePassword() {
        String account = accountField.getText().trim();
        if (account.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an account name to delete.");
            return;
        }

        if (passwordMap.containsKey(account)) {
            passwordMap.remove(account);
            savePasswordsToFile();
            updateDisplay();
            accountField.setText("");
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Account not found.");
        }
    }

    private void searchPassword() {
        String account = searchField.getText().trim();
        if (account.isEmpty()) {
            searchResultLabel.setText("Enter an account name to search.");
            return;
        }

        if (passwordMap.containsKey(account)) {
            searchResultLabel.setText("Password for '" + account + "': " + passwordMap.get(account));
        } else {
            searchResultLabel.setText("Account not found.");
        }
    }

    private void updateDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : passwordMap.entrySet()) {
            sb.append("Account: ").append(entry.getKey())
                    .append(" | Password: ").append(entry.getValue()).append("\n");
        }
        displayArea.setText(sb.toString());
    }

    private String encrypt(String input) {
        char[] key = { 'K', 'C', 'Q' };
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            output.append((char) (input.charAt(i) ^ key[i % key.length]));
        }

        return Base64.getEncoder().encodeToString(output.toString().getBytes());
    }

    private String decrypt(String input) {
        byte[] decoded = Base64.getDecoder().decode(input);
        char[] key = { 'K', 'C', 'Q' };
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < decoded.length; i++) {
            output.append((char) (decoded[i] ^ key[i % key.length]));
        }

        return output.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PasswordManager manager = new PasswordManager();
            manager.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }
}
