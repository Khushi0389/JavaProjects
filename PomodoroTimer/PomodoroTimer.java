import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import javax.swing.Timer;
import javax.sound.sampled.*;

public class PomodoroTimer extends JFrame {
    private static final int WORK_TIME = 25 * 60; // 25 mins
    private static final int BREAK_TIME = 5 * 60; // 5 mins

    private int remainingSeconds = WORK_TIME;
    private int sessionDuration = WORK_TIME;

    private boolean isRunning = false;
    private boolean isWorkSession = true;

    private JLabel timerLabel;
    private JLabel pomodoroCountLabel;
    private int pomodoroCount = 0;

    private JProgressBar progressBar;
    private JButton startButton;
    private JButton resetButton;
    private DefaultListModel<String> taskListModel;
    private JTextField taskInputField;
    private JList<String> taskList;
    private Timer swingTimer;

    public PomodoroTimer() {
        setTitle("Pomodoro Timer");
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        applyDarkMode();

        // Top: Timer label and pomodoro counter
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.setBackground(Color.BLACK);

        timerLabel = new JLabel(formatTime(remainingSeconds), SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 48));
        timerLabel.setForeground(Color.GREEN);
        topPanel.add(timerLabel);

        pomodoroCountLabel = new JLabel("Completed Pomodoros: 0", SwingConstants.CENTER);
        pomodoroCountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        pomodoroCountLabel.setForeground(Color.LIGHT_GRAY);
        topPanel.add(pomodoroCountLabel);

        add(topPanel, BorderLayout.NORTH);

        // Progress bar
        progressBar = new JProgressBar(0, WORK_TIME);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(Color.GREEN);
        progressBar.setBackground(Color.DARK_GRAY);
        add(progressBar, BorderLayout.AFTER_LAST_LINE);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        startButton = new JButton("Start");
        resetButton = new JButton("Reset");

        styleButton(startButton);
        styleButton(resetButton);

        startButton.addActionListener(e -> toggleTimer());
        resetButton.addActionListener(e -> resetTimer());

        buttonPanel.add(startButton);
        buttonPanel.add(resetButton);
        add(buttonPanel, BorderLayout.CENTER);

        // Task panel
        JPanel taskPanel = new JPanel(new BorderLayout(5, 5));
        taskPanel.setBorder(BorderFactory.createTitledBorder("Tasks"));
        taskPanel.setBackground(Color.BLACK);

        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        taskList.setForeground(Color.WHITE);
        taskList.setBackground(Color.DARK_GRAY);

        JScrollPane scrollPane = new JScrollPane(taskList);
        taskPanel.add(scrollPane, BorderLayout.CENTER);

        // Task input and buttons
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBackground(Color.BLACK);
        taskInputField = new JTextField();
        taskInputField.setBackground(Color.DARK_GRAY);
        taskInputField.setForeground(Color.WHITE);

        JButton addTaskButton = new JButton("Add Task");
        JButton doneTaskButton = new JButton("Mark Done");

        styleButton(addTaskButton);
        styleButton(doneTaskButton);

        addTaskButton.addActionListener(e -> {
            String task = taskInputField.getText().trim();
            if (!task.isEmpty()) {
                taskListModel.addElement(task);
                taskInputField.setText("");
            }
        });

        doneTaskButton.addActionListener(e -> {
            int index = taskList.getSelectedIndex();
            if (index != -1) {
                taskListModel.remove(index);
            }
        });

        inputPanel.add(taskInputField, BorderLayout.CENTER);

        JPanel taskBtnPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        taskBtnPanel.setBackground(Color.BLACK);
        taskBtnPanel.add(addTaskButton);
        taskBtnPanel.add(doneTaskButton);

        inputPanel.add(taskBtnPanel, BorderLayout.SOUTH);
        taskPanel.add(inputPanel, BorderLayout.SOUTH);

        add(taskPanel, BorderLayout.EAST);

        // Timer logic
        swingTimer = new Timer(1000, e -> {
            remainingSeconds--;
            timerLabel.setText(formatTime(remainingSeconds));
            progressBar.setValue(sessionDuration - remainingSeconds);

            if (remainingSeconds <= 0) {
                swingTimer.stop();
                isRunning = false;
                playSound("cheer.wav");

                if (isWorkSession) {
                    pomodoroCount++;
                    pomodoroCountLabel.setText("Completed Pomodoros: " + pomodoroCount);
                    JOptionPane.showMessageDialog(this, "Work session over! Take a break.");
                    remainingSeconds = BREAK_TIME;
                    sessionDuration = BREAK_TIME;
                    isWorkSession = false;
                    progressBar.setMaximum(BREAK_TIME);
                } else {
                    JOptionPane.showMessageDialog(this, "Break over! Back to work.");
                    remainingSeconds = WORK_TIME;
                    sessionDuration = WORK_TIME;
                    isWorkSession = true;
                    progressBar.setMaximum(WORK_TIME);
                }

                progressBar.setValue(0);
                timerLabel.setText(formatTime(remainingSeconds));
                startButton.setText("Start");
            }
        });

        setVisible(true);
    }

    private void toggleTimer() {
        if (isRunning) {
            swingTimer.stop();
            startButton.setText("Start");
        } else {
            swingTimer.start();
            startButton.setText("Pause");
        }
        isRunning = !isRunning;
    }

    private void resetTimer() {
        swingTimer.stop();
        isRunning = false;
        isWorkSession = true;
        remainingSeconds = WORK_TIME;
        sessionDuration = WORK_TIME;
        progressBar.setMaximum(WORK_TIME);
        progressBar.setValue(0);
        pomodoroCount = 0;
        pomodoroCountLabel.setText("Completed Pomodoros: 0");
        timerLabel.setText(formatTime(remainingSeconds));
        startButton.setText("Start");
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    private void applyDarkMode() {
        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("Label.foreground", Color.GREEN);
        UIManager.put("Button.background", Color.DARK_GRAY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("TitledBorder.titleColor", Color.GREEN);
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }

    private void playSound(String filename) {
        try {
            File soundFile = new File(filename);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.out.println("Error playing sound: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PomodoroTimer::new);
    }
}
