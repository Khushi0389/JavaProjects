
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class QuizApp extends JFrame {
    private JLabel questionLabel, scoreLabel, timerLabel;
    private JRadioButton[] options = new JRadioButton[4];
    private ButtonGroup group = new ButtonGroup();
    private JButton nextButton, restartButton;
    private java.util.List<Question> questions = new ArrayList<>();
    private int currentQuestion = 0, score = 0;
    private boolean isDarkMode = false;
    private javax.swing.Timer countdownTimer;

    private int timeLeft = 15;

    public QuizApp() {
        setTitle("Quiz App");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel: question, timer
        JPanel topPanel = new JPanel(new BorderLayout());
        questionLabel = new JLabel("Question");
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel = new JLabel("Time: 15s", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(questionLabel, BorderLayout.WEST);
        topPanel.add(timerLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center panel: options
        JPanel centerPanel = new JPanel(new GridLayout(4, 1));
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            group.add(options[i]);
            centerPanel.add(options[i]);
        }
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel: buttons + score
        JPanel bottomPanel = new JPanel();
        scoreLabel = new JLabel("Score: 0");
        nextButton = new JButton("Next");
        restartButton = new JButton("Restart");
        JButton themeToggle = new JButton("🌗 Toggle Theme");

        nextButton.addActionListener(e -> checkAnswer());
        restartButton.addActionListener(e -> restartQuiz());
        themeToggle.addActionListener(e -> toggleTheme());

        bottomPanel.add(scoreLabel);
        bottomPanel.add(nextButton);
        bottomPanel.add(restartButton);
        bottomPanel.add(themeToggle);
        add(bottomPanel, BorderLayout.SOUTH);

        loadQuestions();
        showQuestion();
        startTimer();

        setVisible(true);
    }

    private void loadQuestions() {
        questions.clear();
        // You can load from file here instead
        questions.add(new Question("Capital of France?", new String[] { "Berlin", "Madrid", "Paris", "London" }, 2));
        questions.add(new Question("5 + 7 = ?", new String[] { "10", "12", "11", "13" }, 1));
        questions.add(new Question("Color of Sun?", new String[] { "Red", "Blue", "Yellow", "Green" }, 2));
        Collections.shuffle(questions);
    }

    private void showQuestion() {
        if (currentQuestion >= questions.size()) {
            endQuiz();
            return;
        }
        group.clearSelection();
        Question q = questions.get(currentQuestion);
        questionLabel.setText("Q" + (currentQuestion + 1) + ": " + q.question);
        for (int i = 0; i < 4; i++) {
            options[i].setText(q.choices[i]);
        }
        timeLeft = 15;
        timerLabel.setText("Time: " + timeLeft + "s");
    }

    private void startTimer() {
        countdownTimer = new javax.swing.Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft + "s");
            if (timeLeft <= 0) {
                checkAnswer(); // auto-submit
            }
        });
        countdownTimer.start();
    }

    private void checkAnswer() {
        countdownTimer.stop();
        Question q = questions.get(currentQuestion);
        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) {
                if (i == q.correctIndex) {
                    score++;
                    break;
                }
            }
        }
        scoreLabel.setText("Score: " + score);
        currentQuestion++;
        showQuestion();
        startTimer();
    }

    private void endQuiz() {
        countdownTimer.stop();
        questionLabel.setText("🎉 Quiz Complete!");
        for (JRadioButton opt : options)
            opt.setVisible(false);
        nextButton.setEnabled(false);
        JOptionPane.showMessageDialog(this, "Final Score: " + score + "/" + questions.size());
    }

    private void restartQuiz() {
        score = 0;
        currentQuestion = 0;
        scoreLabel.setText("Score: 0");
        for (JRadioButton opt : options)
            opt.setVisible(true);
        nextButton.setEnabled(true);
        loadQuestions();
        showQuestion();
        startTimer();
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        Color bg = isDarkMode ? Color.BLACK : Color.WHITE;
        Color fg = isDarkMode ? Color.GREEN : Color.BLACK;
        getContentPane().setBackground(bg);
        questionLabel.setForeground(fg);
        timerLabel.setForeground(fg);
        scoreLabel.setForeground(fg);
        for (JRadioButton opt : options) {
            opt.setBackground(bg);
            opt.setForeground(fg);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuizApp::new);
    }

    static class Question {
        String question;
        String[] choices;
        int correctIndex;

        Question(String q, String[] c, int correct) {
            question = q;
            choices = c;
            correctIndex = correct;
        }
    }
}
