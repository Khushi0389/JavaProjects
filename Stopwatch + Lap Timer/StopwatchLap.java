import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StopwatchLap {
    private long startEpoch = 0;
    private long elapsed = 0; // accumulated when paused
    private boolean running = false;
    private final DefaultListModel<String> laps = new DefaultListModel<>();
    private final JLabel timeLbl = new JLabel("00:00.00", SwingConstants.CENTER);
    private final Timer uiTimer;

    public StopwatchLap() {
        JFrame f = new JFrame("Stopwatch + Lap Timer");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(380, 420);
        f.setLocationRelativeTo(null);

        timeLbl.setFont(new Font("Consolas", Font.BOLD, 48));

        JButton startStop = new JButton("Start");
        JButton lap = new JButton("Lap");
        JButton reset = new JButton("Reset");
        JButton export = new JButton("Export Laps");

        JList<String> lapList = new JList<>(laps);
        lapList.setFont(new Font("Consolas", Font.PLAIN, 14));

        JPanel top = new JPanel(new BorderLayout());
        top.add(timeLbl, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btns.add(startStop);
        btns.add(lap);
        btns.add(reset);
        btns.add(export);

        f.setLayout(new BorderLayout(10,10));
        f.add(top, BorderLayout.NORTH);
        f.add(new JScrollPane(lapList), BorderLayout.CENTER);
        f.add(btns, BorderLayout.SOUTH);

        uiTimer = new Timer(10, e -> updateLabel());
        startStop.addActionListener(e -> {
            if (!running) {
                running = true;
                startEpoch = System.currentTimeMillis();
                uiTimer.start();
                startStop.setText("Stop");
            } else {
                running = false;
                elapsed += System.currentTimeMillis() - startEpoch;
                uiTimer.stop();
                startStop.setText("Start");
            }
        });

        lap.addActionListener(e -> {
            long ms = currentElapsed();
            laps.add(0, format(ms) + "  (Lap " + (laps.size()+1) + ")");
        });

        reset.addActionListener(e -> {
            running = false;
            elapsed = 0;
            uiTimer.stop();
            timeLbl.setText("00:00.00");
            laps.clear();
            startStop.setText("Start");
        });

        export.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("laps_" +
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt"));
            if (fc.showSaveDialog(f) == JFileChooser.APPROVE_OPTION) {
                try (PrintWriter pw = new PrintWriter(fc.getSelectedFile(), "UTF-8")) {
                    for (int i = laps.getSize() - 1; i >= 0; --i) pw.println(laps.get(i));
                    JOptionPane.showMessageDialog(f, "Exported!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        f.setVisible(true);
    }

    private long currentElapsed() {
        return running ? elapsed + (System.currentTimeMillis() - startEpoch) : elapsed;
    }

    private void updateLabel() {
        timeLbl.setText(format(currentElapsed()));
    }

    private String format(long ms) {
        long totalCentis = ms / 10;
        long minutes = (totalCentis / 6000);
        long seconds = (totalCentis / 100) % 60;
        long centis = totalCentis % 100;
        return String.format("%02d:%02d.%02d", minutes, seconds, centis);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StopwatchLap::new);
    }
}
