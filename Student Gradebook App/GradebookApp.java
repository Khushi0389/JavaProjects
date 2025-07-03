
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

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

        // Table setup
        tableModel = new DefaultTableModel(new String[] {
                "Name", "Math", "Science", "English", "Total", "Average", "Grade"
        }, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                try {
                    double avg = Double.parseDouble(getValueAt(row, 5).toString());
                    if (avg < 40)
                        c.setBackground(Color.PINK);
                    else
                        c.setBackground(Color.WHITE);
                } catch (Exception e) {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Input panel
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

        // Button Panel
        JPanel controlPanel = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton saveBtn = new JButton("Save");
        JButton loadBtn = new JButton("Load");
        JButton exportBtn = new JButton("Export CSV");
        JButton printBtn = new JButton("Print");
        JButton themeBtn = new JButton("🌗 Toggle Theme");

        avgLabel = new JLabel(" ");

        controlPanel.add(addBtn);
        controlPanel.add(editBtn);
        controlPanel.add(deleteBtn);
        controlPanel.add(saveBtn);
        controlPanel.add(loadBtn);
        controlPanel.add(exportBtn);
        controlPanel.add(printBtn);
        controlPanel.add(themeBtn);
        controlPanel.add(avgLabel);

        add(controlPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addStudent());
        editBtn.addActionListener(e -> editStudent());
        deleteBtn.addActionListener(e -> deleteStudent());
        saveBtn.addActionListener(e -> saveToFile());
        loadBtn.addActionListener(e -> loadFromFile());
        exportBtn.addActionListener(e -> exportCSV());
        printBtn.addActionListener(e -> printTable());
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

    private void addStudent() {
        try {
            String name = nameField.getText().trim();
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
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input!");
        }
    }

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

    private void deleteStudent() {
        int row = table.getSelectedRow();
        if (row != -1)
            tableModel.removeRow(row);
    }

    private void saveToFile() {
        try (PrintWriter pw = new PrintWriter("grades.txt")) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    pw.print(tableModel.getValueAt(i, j) + ",");
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(this, "Saved to grades.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Save failed.");
        }
    }

    private void loadFromFile() {
        tableModel.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader("grades.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 7) {
                    tableModel.addRow(new Object[] {
                            data[0], data[1], data[2], data[3], data[4], data[5], data[6]
                    });
                }
            }
            JOptionPane.showMessageDialog(this, "Loaded grades.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Load failed.");
        }
    }

    private void exportCSV() {
        try (PrintWriter pw = new PrintWriter("grades_export.csv")) {
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                pw.print(tableModel.getColumnName(i) + ",");
            }
            pw.println();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    pw.print(tableModel.getValueAt(i, j) + ",");
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(this, "Exported to grades_export.csv");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed.");
        }
    }

    private void printTable() {
        try {
            table.print();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Print failed.");
        }
    }

    private void filter() {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText(), 0));
    }

    private void clearInputs() {
        nameField.setText("");
        m1Field.setText("");
        m2Field.setText("");
        m3Field.setText("");
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
                UIManager.put("control", null);
                UIManager.put("nimbusLightBackground", null);
                UIManager.put("text", null);
            } else {
                UIManager.put("control", new Color(40, 40, 40));
                UIManager.put("nimbusLightBackground", new Color(30, 30, 30));
                UIManager.put("text", Color.WHITE);
            }
            SwingUtilities.updateComponentTreeUI(this);
            isDarkMode = !isDarkMode;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Theme toggle failed.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GradebookApp::new);
    }
}
