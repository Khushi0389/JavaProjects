import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class InventoryManager extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField nameField, qtyField, priceField, searchField;
    private JLabel totalValueLabel;
    private boolean isDarkMode = false;

    public InventoryManager() {
        setTitle("Inventory Management System");
        setSize(900, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table
        tableModel = new DefaultTableModel(new String[] { "Item Name", "Quantity", "Price (₹)", "Total" }, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        nameField = new JTextField();
        qtyField = new JTextField();
        priceField = new JTextField();
        searchField = new JTextField();

        inputPanel.add(new JLabel("Item Name:"));
        inputPanel.add(new JLabel("Quantity:"));
        inputPanel.add(new JLabel("Price (₹):"));
        inputPanel.add(new JLabel(""));
        inputPanel.add(new JLabel("Search:"));

        inputPanel.add(nameField);
        inputPanel.add(qtyField);
        inputPanel.add(priceField);
        inputPanel.add(new JLabel(""));
        inputPanel.add(searchField);

        add(inputPanel, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");
        JButton exportButton = new JButton("Export CSV");
        JButton toggleThemeButton = new JButton("🌗 Toggle Theme");

        totalValueLabel = new JLabel("Total Value: ₹0.00");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(toggleThemeButton);
        buttonPanel.add(totalValueLabel);

        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        addButton.addActionListener(e -> addItem());
        updateButton.addActionListener(e -> updateItem());
        deleteButton.addActionListener(e -> deleteItem());
        saveButton.addActionListener(e -> saveToFile());
        loadButton.addActionListener(e -> loadFromFile());
        exportButton.addActionListener(e -> exportCSV());
        toggleThemeButton.addActionListener(e -> toggleTheme());

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }
        });

        setVisible(true);
    }

    private void addItem() {
        String name = nameField.getText().trim();
        String qtyText = qtyField.getText().trim();
        String priceText = priceField.getText().trim();

        if (name.isEmpty() || qtyText.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required!");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyText);
            double price = Double.parseDouble(priceText);
            double total = qty * price;
            tableModel.addRow(new Object[] { name, qty, price, String.format("%.2f", total) });
            clearFields();
            updateTotalValue();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity and Price must be numbers!");
        }
    }

    private void updateItem() {
        int row = table.getSelectedRow();
        if (row != -1) {
            try {
                String name = nameField.getText().trim();
                int qty = Integer.parseInt(qtyField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
                double total = qty * price;

                tableModel.setValueAt(name, row, 0);
                tableModel.setValueAt(qty, row, 1);
                tableModel.setValueAt(price, row, 2);
                tableModel.setValueAt(String.format("%.2f", total), row, 3);

                clearFields();
                updateTotalValue();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid update data!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a row to update!");
        }
    }

    private void deleteItem() {
        int row = table.getSelectedRow();
        if (row != -1) {
            tableModel.removeRow(row);
            updateTotalValue();
        } else {
            JOptionPane.showMessageDialog(this, "Select a row to delete!");
        }
    }

    private void saveToFile() {
        try (PrintWriter pw = new PrintWriter("inventory.txt")) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                pw.println(tableModel.getValueAt(i, 0) + "," +
                        tableModel.getValueAt(i, 1) + "," +
                        tableModel.getValueAt(i, 2));
            }
            JOptionPane.showMessageDialog(this, "Saved to inventory.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Save failed.");
        }
    }

    private void loadFromFile() {
        tableModel.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader("inventory.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0];
                    int qty = Integer.parseInt(parts[1]);
                    double price = Double.parseDouble(parts[2]);
                    double total = qty * price;
                    tableModel.addRow(new Object[] { name, qty, price, String.format("%.2f", total) });
                }
            }
            updateTotalValue();
            JOptionPane.showMessageDialog(this, "Loaded inventory.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Load failed.");
        }
    }

    private void exportCSV() {
        try (PrintWriter pw = new PrintWriter("inventory_export.csv")) {
            pw.println("Item,Quantity,Price,Total");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                pw.println(tableModel.getValueAt(i, 0) + "," +
                        tableModel.getValueAt(i, 1) + "," +
                        tableModel.getValueAt(i, 2) + "," +
                        tableModel.getValueAt(i, 3));
            }
            JOptionPane.showMessageDialog(this, "Exported to inventory_export.csv");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed.");
        }
    }

    private void filterTable() {
        String search = searchField.getText().toLowerCase();
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + search, 0));
    }

    private void updateTotalValue() {
        double total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            total += Double.parseDouble(tableModel.getValueAt(i, 3).toString());
        }
        totalValueLabel.setText("Total Value: ₹" + String.format("%.2f", total));
    }

    private void clearFields() {
        nameField.setText("");
        qtyField.setText("");
        priceField.setText("");
    }

    private void toggleTheme() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }

            if (isDarkMode) {
                // Light Theme Reset
                UIManager.put("control", null);
                UIManager.put("info", null);
                UIManager.put("nimbusBase", null);
                UIManager.put("nimbusAlertYellow", null);
                UIManager.put("nimbusDisabledText", null);
                UIManager.put("nimbusFocus", null);
                UIManager.put("nimbusGreen", null);
                UIManager.put("nimbusInfoBlue", null);
                UIManager.put("nimbusLightBackground", null);
                UIManager.put("nimbusOrange", null);
                UIManager.put("nimbusRed", null);
                UIManager.put("nimbusSelectedText", null);
                UIManager.put("nimbusSelectionBackground", null);
                UIManager.put("text", null);
            } else {
                // Dark Theme
                UIManager.put("control", new Color(60, 63, 65));
                UIManager.put("info", new Color(60, 63, 65));
                UIManager.put("nimbusBase", new Color(18, 30, 49));
                UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
                UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
                UIManager.put("nimbusFocus", new Color(115, 164, 209));
                UIManager.put("nimbusGreen", new Color(176, 179, 50));
                UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
                UIManager.put("nimbusLightBackground", new Color(43, 43, 43));
                UIManager.put("nimbusOrange", new Color(191, 98, 4));
                UIManager.put("nimbusRed", new Color(169, 46, 34));
                UIManager.put("nimbusSelectedText", Color.WHITE);
                UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
                UIManager.put("text", Color.WHITE);
            }

            SwingUtilities.updateComponentTreeUI(this);
            isDarkMode = !isDarkMode;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Theme toggle failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InventoryManager::new);
    }
}
