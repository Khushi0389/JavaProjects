import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class TaskManager extends JFrame {
    private DefaultTableModel tableModel;
    private JTable taskTable;
    private JTextField titleField, dueField;
    private JComboBox<String> priorityBox;
    private final String[] priorities = { "High", "Medium", "Low" };
    private final java.util.List<Task> tasks = new java.util.ArrayList<>();
    private final java.util.Timer reminderTimer = new java.util.Timer();

    public TaskManager() {
        setTitle("Task Manager");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Task"));

        titleField = new JTextField();
        dueField = new JTextField();
        dueField.setEditable(false);
        dueField.setBackground(Color.LIGHT_GRAY);
        updateCurrentTime();

        priorityBox = new JComboBox<>(priorities);

        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(e -> addTask());

        inputPanel.add(new JLabel("Task Title:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Added On (Auto):"));
        inputPanel.add(dueField);
        inputPanel.add(new JLabel("Priority:"));
        inputPanel.add(priorityBox);
        inputPanel.add(addButton);

        add(inputPanel, BorderLayout.NORTH);

        // Task Table
        tableModel = new DefaultTableModel(new String[] { "Title", "Due", "Priority" }, 0);
        taskTable = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                try {
                    Date due = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(getValueAt(row, 1).toString());
                    if (due.before(new Date()))
                        c.setForeground(Color.RED);
                    else
                        c.setForeground(Color.BLACK);
                } catch (Exception e) {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };

        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Your Tasks"));
        add(scrollPane, BorderLayout.CENTER);

        // Control Panel
        JPanel controlPanel = new JPanel();
        JButton deleteButton = new JButton("Delete Task");
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");

        deleteButton.addActionListener(e -> deleteTask());
        saveButton.addActionListener(e -> saveTasks());
        loadButton.addActionListener(e -> loadTasks());

        controlPanel.add(deleteButton);
        controlPanel.add(saveButton);
        controlPanel.add(loadButton);

        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addTask() {
        String title = titleField.getText().trim();
        String priority = priorityBox.getSelectedItem().toString();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a task title.");
            return;
        }

        try {
            Date due = new Date(); // current time
            String dueText = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(due);
            Task task = new Task(title, due, priority);
            tasks.add(task);
            tableModel.addRow(new Object[] { title, dueText, priority });
            scheduleReminder(task);
            titleField.setText("");
            updateCurrentTime(); // refresh displayed time
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding task!");
        }
    }

    private void updateCurrentTime() {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        dueField.setText(currentTime);
    }

    private void deleteTask() {
        int row = taskTable.getSelectedRow();
        if (row != -1) {
            tableModel.removeRow(row);
            tasks.remove(row);
        }
    }

    private void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tasks.txt"))) {
            for (Task t : tasks) {
                writer.write(t.title + "," +
                        new SimpleDateFormat("yyyy-MM-dd HH:mm").format(t.due) + "," +
                        t.priority);
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Tasks saved.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage());
        }
    }

    private void loadTasks() {
        tasks.clear();
        tableModel.setRowCount(0);
        try (BufferedReader reader = new BufferedReader(new FileReader("tasks.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String title = parts[0];
                    Date due = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(parts[1]);
                    String priority = parts[2];
                    Task task = new Task(title, due, priority);
                    tasks.add(task);
                    tableModel.addRow(new Object[] { title, parts[1], priority });
                    scheduleReminder(task);
                }
            }
            JOptionPane.showMessageDialog(this, "Tasks loaded.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load failed: " + e.getMessage());
        }
    }

    private void scheduleReminder(Task task) {
        long delay = task.due.getTime() - System.currentTimeMillis();
        if (delay > 0) {
            reminderTimer.schedule(new TimerTask() {
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null,
                                "Reminder: Task \"" + task.title + "\" was added at " +
                                        new SimpleDateFormat("HH:mm").format(task.due));
                    });
                }
            }, delay);
        }
    }

    class Task {
        String title;
        Date due;
        String priority;

        Task(String title, Date due, String priority) {
            this.title = title;
            this.due = due;
            this.priority = priority;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TaskManager::new);
    }
}
