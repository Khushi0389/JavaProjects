import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;

public class GradebookApp extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField nameField, m1Field, m2Field, m3Field, searchField;
    private JLabel avgLabel;
    private boolean isDarkMode = false;

    public GradebookApp() {
        setTitle("Student Gradebook App");
        setSize(950, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Table setup with editable score columns ---
        tableModel = new DefaultTableModel(new String[] {
                "Name", "Math", "Science", "English", "Total", "Average", "Grade"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // allow editing of the three score columns only
                return col >= 1 && col <= 3;
            }
        };
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                try {
                    double avg = Double.parseDouble(getValueAt(row, 5).toString());
                    c.setBackground(avg < 40 ? Color.PINK : Color.WHITE);
                } catch (Exception ex) {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Recalculate total/average/grade when scores are edited
        tableModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    recalcRow(e.getFirstRow());
                }
            }
        });

        // --- Input panel ---
        JPanel inputPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        nameField = new JTextField();
        m1Field = new JTextField();
        m2Field = new JTextField();
        m3Field = new JTextField();
        searchField = new JTextField();

        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(new JLabel("Math:"));
        inputPanel.add(new JLabel("Science:"));
        inputPanel.add(new JLabel("English:"));
        inputPanel.add(new JLabel("Search:"));

        inputPanel.add(nameField);
        inputPanel.add(m1Field);
        inputPanel.add(m2Field);
        inputPanel.add(m3Field);
        inputPanel.add(searchField);

        add(inputPanel, BorderLayout.NORTH);

        // --- Control panel with buttons ---
        JPanel controlPanel = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton saveBtn = new JButton("Save");
        JButton loadBtn = new JButton("Load");
        JButton exportBtn = new JButton("Export CSV");
        JButton printBtn = new JButton("Print");
        JButton statsBtn = new JButton("Class Stats");
        JButton themeBtn = new JButton("🌗 Toggle Theme");

        avgLabel = new JLabel(" ");

        controlPanel.add(addBtn);
        controlPanel.add(editBtn);
        controlPanel.add(deleteBtn);
        controlPanel.add(saveBtn);
        controlPanel.add(loadBtn);
        controlPanel.add(exportBtn);
        controlPanel.add(printBtn);
        controlPanel.add(statsBtn);
        controlPanel.add(themeBtn);
        controlPanel.add(avgLabel);

        add(controlPanel, BorderLayout.SOUTH);

        // --- Wire up actions ---
        addBtn.addActionListener(e -> addStudent());
        editBtn.addActionListener(e -> editStudent());
        deleteBtn.addActionListener(e -> deleteStudent());
        saveBtn.addActionListener(e -> saveToFile());
        loadBtn.addActionListener(e -> loadFromFile());
        exportBtn.addActionListener(e -> exportCSV());
        printBtn.addActionListener(e -> printTable());
        statsBtn.addActionListener(e -> showClassStats());
        themeBtn.addActionListener(e -> toggleTheme());

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }
        });

        setVisible(true);
    }

    // Choose a file via dialog
    private File chooseFile(String title, boolean save) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileFilter(new FileNameExtensionFilter("Text & CSV Files", "txt", "csv"));
        int result = save
                ? chooser.showSaveDialog(this)
                : chooser.showOpenDialog(this);
        return result == JFileChooser.APPROVE_OPTION
                ? chooser.getSelectedFile()
                : null;
    }

    // Add a new student row
    private void addStudent() {
        String name = nameField.getText().trim();
        if (name.isEmpty()
                || !validMark(m1Field.getText())
                || !validMark(m2Field.getText())
                || !validMark(m3Field.getText())) {
            JOptionPane.showMessageDialog(this, "Please enter a name and valid marks (0–100).");
            return;
        }
        int m1 = Integer.parseInt(m1Field.getText().trim());
        int m2 = Integer.parseInt(m2Field.getText().trim());
        int m3 = Integer.parseInt(m3Field.getText().trim());
        int total = m1 + m2 + m3;
        double avg = total / 3.0;
        String grade = avg >= 85 ? "A" : avg >= 70 ? "B" : avg >= 50 ? "C" : "F";
        DecimalFormat df = new DecimalFormat("#.##");

        tableModel.addRow(new Object[] {
                name, m1, m2, m3, total, df.format(avg), grade
        });
        clearInputs();
    }

    // Load a row into input fields for editing
    private void editStudent() {
        int row = table.getSelectedRow();
        if (row != -1) {
            nameField.setText(tableModel.getValueAt(row, 0).toString());
            m1Field.setText(tableModel.getValueAt(row, 1).toString());
            m2Field.setText(tableModel.getValueAt(row, 2).toString());
            m3Field.setText(tableModel.getValueAt(row, 3).toString());
            tableModel.removeRow(row);
        }
    }

    // Delete selected row
    private void deleteStudent() {
        int row = table.getSelectedRow();
        if (row != -1)
            tableModel.removeRow(row);
    }

    // Save to a chosen text file
    private void saveToFile() {
        File f = chooseFile("Save Grades As…", true);
        if (f == null)
            return;
        try (PrintWriter pw = new PrintWriter(f)) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    pw.print(tableModel.getValueAt(i, j) + ",");
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(this, "Saved to " + f.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }

    // Load from a chosen file
    private void loadFromFile() {
        File f = chooseFile("Open Grades File…", false);
        if (f == null)
            return;
        tableModel.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 7) {
                    tableModel.addRow(new Object[] {
                            data[0], data[1], data[2], data[3],
                            data[4], data[5], data[6]
                    });
                }
            }
            JOptionPane.showMessageDialog(this, "Loaded " + f.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage());
        }
    }

    // Export as CSV
    private void exportCSV() {
        File f = chooseFile("Export CSV As…", true);
        if (f == null)
            return;
        try (PrintWriter pw = new PrintWriter(f)) {
            // header
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                pw.print(tableModel.getColumnName(i) + ",");
            }
            pw.println();
            // rows
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    pw.print(tableModel.getValueAt(i, j) + ",");
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(this, "Exported to " + f.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
        }
    }

    // Print the table
    private void printTable() {
        try {
            table.print();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Print failed: " + e.getMessage());
        }
    }

    // Filter table by name
    private void filter() {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText(), 0));
    }

    // Clear input fields
    private void clearInputs() {
        nameField.setText("");
        m1Field.setText("");
        m2Field.setText("");
        m3Field.setText("");
    }

    // Validate marks are integers 0–100
    private boolean validMark(String s) {
        try {
            int v = Integer.parseInt(s.trim());
            return v >= 0 && v <= 100;
        } catch (Exception e) {
            return false;
        }
    }

    // Recalculate Total/Average/Grade for a given row
    private void recalcRow(int row) {
        try {
            int m1 = Integer.parseInt(tableModel.getValueAt(row, 1).toString());
            int m2 = Integer.parseInt(tableModel.getValueAt(row, 2).toString());
            int m3 = Integer.parseInt(tableModel.getValueAt(row, 3).toString());
            int total = m1 + m2 + m3;
            double avg = total / 3.0;
            String grade = avg >= 85 ? "A" : avg >= 70 ? "B" : avg >= 50 ? "C" : "F";
            DecimalFormat df = new DecimalFormat("#.##");

            tableModel.setValueAt(total, row, 4);
            tableModel.setValueAt(df.format(avg), row, 5);
            tableModel.setValueAt(grade, row, 6);
        } catch (Exception ignored) {
        }
    }

    // Show class-wide statistics
    private void showClassStats() {
        int n = tableModel.getRowCount();
        if (n == 0) {
            JOptionPane.showMessageDialog(this, "No data to analyze.");
            return;
        }
        double sum = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            double avg = Double.parseDouble(tableModel.getValueAt(i, 5).toString());
            sum += avg;
            min = Math.min(min, avg);
            max = Math.max(max, avg);
        }
        double classAvg = sum / n;
        JOptionPane.showMessageDialog(this,
                String.format("Class Average: %.2f\nHighest Avg: %.2f\nLowest Avg: %.2f",
                        classAvg, max, min),
                "Class Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    // Toggle between default L&F and a dark theme
    private void toggleTheme() {
        try {
            if (isDarkMode) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } else {
                // switch to Nimbus + adjust colors, or use Darcula if on your classpath
                UIManager.setLookAndFeel("com.bulenkov.darcula.DarculaLaf");
            }
            SwingUtilities.updateComponentTreeUI(this);
            isDarkMode = !isDarkMode;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Theme toggle failed: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GradebookApp::new);
    }
}
