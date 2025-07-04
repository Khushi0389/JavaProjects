import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class PaintApp extends JFrame {
    private BufferedImage canvas;
    private Graphics2D g2;
    private int brushSize = 5;
    private Color currentColor = Color.BLACK;
    private Point lastPoint;

    public PaintApp() {
        setTitle("🎨 Paint App");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        canvas = new BufferedImage(800, 500, BufferedImage.TYPE_INT_RGB);
        g2 = canvas.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, 800, 500);
        g2.setColor(currentColor);
        g2.setStroke(new BasicStroke(brushSize));

        JPanel controls = new JPanel();

        JButton colorBtn = new JButton("Pick Color");
        colorBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Select Color", currentColor);
            if (chosen != null) {
                currentColor = chosen;
                g2.setColor(currentColor);
            }
        });

        JLabel sizeLabel = new JLabel("Brush Size:");
        JSlider sizeSlider = new JSlider(1, 20, brushSize);
        sizeSlider.addChangeListener(e -> {
            brushSize = sizeSlider.getValue();
            g2.setStroke(new BasicStroke(brushSize));
        });

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g2.setColor(currentColor);
            repaint();
        });

        JButton saveBtn = new JButton("Save PNG");
        saveBtn.addActionListener(e -> {
            try {
                ImageIO.write(canvas, "png", new File("drawing.png"));
                JOptionPane.showMessageDialog(this, "Saved as drawing.png");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
            }
        });

        controls.add(colorBtn);
        controls.add(sizeLabel);
        controls.add(sizeSlider);
        controls.add(clearBtn);
        controls.add(saveBtn);
        add(controls, BorderLayout.SOUTH);

        JPanel drawPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(canvas, 0, 0, null);
            }
        };

        drawPanel.setPreferredSize(new Dimension(800, 500));
        drawPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }
        });

        drawPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point current = e.getPoint();
                g2.drawLine(lastPoint.x, lastPoint.y, current.x, current.y);
                lastPoint = current;
                repaint();
            }
        });

        add(drawPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PaintApp::new);
    }
}
