import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ExpenseTracker extends JFrame {
    private JTextField amountField, dateField;
    private JComboBox<String> categoryBox, filterCategoryBox, filterMonthBox;
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private List<Expense> expenses = new ArrayList<>();
    private final String[] categories = { "Food", "Travel", "Utilities", "Shopping", "Other" };
    private JPanel chartPanel;

    public ExpenseTracker() {
        setTitle("Expense Tracker");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Expense"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Amount
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Amount (₹)"), gbc);
        gbc.gridx = 1;
        amountField = new JTextField(10);
        inputPanel.add(amountField, gbc);

        // Row 1: Date
        gbc.gridx = 2;
        inputPanel.add(new JLabel("Date (yyyy-MM-dd)"), gbc);
        gbc.gridx = 3;
        dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 10);
        inputPanel.add(dateField, gbc);

        // Row 1: Category
        gbc.gridx = 4;
        inputPanel.add(new JLabel("Category"), gbc);
        gbc.gridx = 5;
        categoryBox = new JComboBox<>(categories);
        inputPanel.add(categoryBox, gbc);

        // Row 2: Buttons
        gbc.gridy = 1;
        gbc.gridx = 0;
        JButton addButton = new JButton("Add");
        inputPanel.add(addButton, gbc);

        gbc.gridx = 1;
        JButton saveButton = new JButton("Save");
        inputPanel.add(saveButton, gbc);

        gbc.gridx = 2;
        JButton loadButton = new JButton("Load");
        inputPanel.add(loadButton, gbc);

        gbc.gridx = 3;
        JButton exportButton = new JButton("Export CSV");
        inputPanel.add(exportButton, gbc);

        add(inputPanel, BorderLayout.NORTH);

        // Action Listeners
        addButton.addActionListener(e -> addExpense());
        saveButton.addActionListener(e -> saveExpenses());
        loadButton.addActionListener(e -> loadExpenses());
        exportButton.addActionListener(e -> exportToCSV());

        // Table
        tableModel = new DefaultTableModel(new String[] { "Amount", "Date", "Category" }, 0);
        expenseTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(expenseTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("All Expenses"));
        add(tableScrollPane, BorderLayout.CENTER);

        // Chart
        chartPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBarChart(g);
            }
        };
        chartPanel.setPreferredSize(new Dimension(300, 300));
        chartPanel.setBorder(BorderFactory.createTitledBorder("Filtered Expense Summary"));
        add(chartPanel, BorderLayout.EAST);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Expenses"));
        filterPanel.add(new JLabel("Category:"));
        filterCategoryBox = new JComboBox<>();
        filterCategoryBox.addItem("All");
        for (String cat : categories)
            filterCategoryBox.addItem(cat);
        filterPanel.add(filterCategoryBox);

        filterPanel.add(new JLabel("Month:"));
        filterMonthBox = new JComboBox<>();
        filterMonthBox.addItem("All");
        for (int i = 1; i <= 12; i++)
            filterMonthBox.addItem(String.format("%02d", i));
        filterPanel.add(filterMonthBox);

        JButton applyFilter = new JButton("Apply Filter");
        filterPanel.add(applyFilter);
        applyFilter.addActionListener(e -> {
            updateTable();
            chartPanel.repaint();
        });

        add(filterPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addExpense() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            String date = dateField.getText();
            String category = categoryBox.getSelectedItem().toString();
            expenses.add(new Expense(amount, date, category));
            updateTable();
            chartPanel.repaint();
            amountField.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please check amount and date.");
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        String selectedCategory = filterCategoryBox.getSelectedItem().toString();
        String selectedMonth = filterMonthBox.getSelectedItem().toString();

        for (Expense exp : expenses) {
            boolean matchCategory = selectedCategory.equals("All") || exp.category.equals(selectedCategory);
            boolean matchMonth = selectedMonth.equals("All") || exp.date.substring(5, 7).equals(selectedMonth);
            if (matchCategory && matchMonth) {
                tableModel.addRow(new Object[] { exp.amount, exp.date, exp.category });
            }
        }
    }

    private void drawBarChart(Graphics g) {
        Map<String, Double> totals = new HashMap<>();
        for (String cat : categories)
            totals.put(cat, 0.0);

        String selectedCategory = filterCategoryBox.getSelectedItem().toString();
        String selectedMonth = filterMonthBox.getSelectedItem().toString();

        for (Expense exp : expenses) {
            boolean matchCategory = selectedCategory.equals("All") || exp.category.equals(selectedCategory);
            boolean matchMonth = selectedMonth.equals("All") || exp.date.substring(5, 7).equals(selectedMonth);
            if (matchCategory && matchMonth) {
                totals.put(exp.category, totals.getOrDefault(exp.category, 0.0) + exp.amount);
            }
        }

        int x = 20, barWidth = 40, maxHeight = 200;
        double max = Math.max(1.0, Collections.max(totals.values()));

        for (String cat : categories) {
            int height = (int) ((totals.get(cat) / max) * maxHeight);
            g.setColor(Color.GREEN);
            g.fillRect(x, 250 - height, barWidth, height);
            g.setColor(Color.BLACK);
            g.drawString(cat, x, 270);
            g.drawString("₹" + (int) totals.get(cat).doubleValue(), x, 240 - height);
            x += 60;
        }
    }

    private void saveExpenses() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("expenses.txt"))) {
            for (Expense exp : expenses) {
                writer.write(exp.amount + "," + exp.date + "," + exp.category);
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Expenses saved to expenses.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage());
        }
    }

    private void loadExpenses() {
        JOptionPane.showMessageDialog(this,
                "Please select a file to load expenses.\nFormat: amount,date,category",
                "Load Expenses", JOptionPane.INFORMATION_MESSAGE);

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            expenses.clear();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        double amount = Double.parseDouble(parts[0]);
                        String date = parts[1];
                        String category = parts[2];
                        expenses.add(new Expense(amount, date, category));
                    }
                }
                updateTable();
                chartPanel.repaint();
                JOptionPane.showMessageDialog(this, "Expenses loaded successfully.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading: " + e.getMessage());
            }
        }
    }

    private void exportToCSV() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("export.csv"))) {
            writer.println("Amount,Date,Category");
            for (Expense exp : expenses) {
                writer.println(exp.amount + "," + exp.date + "," + exp.category);
            }
            JOptionPane.showMessageDialog(this, "Exported to export.csv successfully.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTracker::new);
    }

    class Expense {
        double amount;
        String date;
        String category;

        Expense(double amount, String date, String category) {
            this.amount = amount;
            this.date = date;
            this.category = category;
        }
    }
}
