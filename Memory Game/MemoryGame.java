import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;

public class MemoryGame extends JFrame {
    private JButton[][] buttons = new JButton[4][4];
    private String[] icons = {
            "🍎", "🍌", "🍓", "🍇", "🍍", "🥝", "🍒", "🥥",
            "🍎", "🍌", "🍓", "🍇", "🍍", "🥝", "🍒", "🥥"
    };
    private String firstIcon = null;
    private JButton firstButton = null;
    private boolean busy = false;
    private javax.swing.Timer flipBackTimer;

    public MemoryGame() {
        setTitle("🧠 Memory Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 400);
        setLayout(new GridLayout(4, 4));
        java.util.List<String> shuffledIcons = new java.util.ArrayList<>(java.util.Arrays.asList(icons));
        Collections.shuffle(shuffledIcons);

        int index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                JButton button = new JButton("");
                button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
                String icon = shuffledIcons.get(index++);
                button.putClientProperty("icon", icon);
                button.addActionListener(e -> handleButtonClick(button));
                buttons[i][j] = button;
                add(button);
            }
        }

        setVisible(true);
    }

    private void handleButtonClick(JButton button) {
        if (busy || button.getText().length() > 0)
            return;

        String icon = (String) button.getClientProperty("icon");
        button.setText(icon);

        if (firstIcon == null) {
            firstIcon = icon;
            firstButton = button;
        } else {
            if (firstButton == button)
                return; // clicking same button again

            if (firstIcon.equals(icon)) {
                firstIcon = null;
                firstButton = null;
            } else {
                busy = true;
                flipBackTimer = new javax.swing.Timer(500, e -> {
                    firstButton.setText("");
                    button.setText("");
                    firstIcon = null;
                    firstButton = null;
                    busy = false;
                    flipBackTimer.stop();
                });
                flipBackTimer.start();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MemoryGame());
    }
}
