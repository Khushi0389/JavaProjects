import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class PersonalNotesApp extends JFrame {
    private DefaultListModel<Note> noteListModel;
    private JList<Note> noteList;
    private JTextArea noteContentArea;
    private JTextField titleField, tagField, searchField;
    private final java.util.List<Note> allNotes = new ArrayList<>();
    private final String SAVE_FILE = "notes.txt";

    public PersonalNotesApp() {
        setTitle("Personal Notes App");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel - Search and Title
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        JPanel titlePanel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new BorderLayout());

        titleField = new JTextField();
        titlePanel.add(new JLabel("Title: "), BorderLayout.WEST);
        titlePanel.add(titleField, BorderLayout.CENTER);

        searchField = new JTextField();
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterNotes(searchField.getText());
            }
        });

        topPanel.add(titlePanel);
        topPanel.add(searchPanel);
        add(topPanel, BorderLayout.NORTH);

        // Center Split Panel
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(250);

        // Note List
        noteListModel = new DefaultListModel<>();
        noteList = new JList<>(noteListModel);
        noteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        noteList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Note selected = noteList.getSelectedValue();
                if (selected != null) {
                    titleField.setText(selected.title);
                    noteContentArea.setText(selected.content);
                    tagField.setText(selected.tag);
                }
            }
        });

        splitPane.setLeftComponent(new JScrollPane(noteList));

        // Right Panel - Note content
        JPanel rightPanel = new JPanel(new BorderLayout());
        noteContentArea = new JTextArea();
        noteContentArea.setLineWrap(true);
        noteContentArea.setWrapStyleWord(true);
        rightPanel.add(new JScrollPane(noteContentArea), BorderLayout.CENTER);

        // Tag Panel
        JPanel tagPanel = new JPanel(new BorderLayout());
        tagField = new JTextField();
        tagPanel.add(new JLabel("Tag: "), BorderLayout.WEST);
        tagPanel.add(tagField, BorderLayout.CENTER);
        rightPanel.add(tagPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);

        // Bottom Panel - Buttons
        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");
        JButton saveButton = new JButton("Save All");
        JButton loadButton = new JButton("Load");
        JButton exportButton = new JButton("Export as TXT");

        addButton.addActionListener(e -> addNote());
        deleteButton.addActionListener(e -> deleteNote());
        saveButton.addActionListener(e -> saveToFile());
        loadButton.addActionListener(e -> loadFromFile());
        exportButton.addActionListener(e -> exportToTxt());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(exportButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void addNote() {
        String title = titleField.getText().trim();
        String content = noteContentArea.getText().trim();
        String tag = tagField.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title and Content can't be empty!");
            return;
        }

        Note note = new Note(title, content, tag);
        allNotes.add(note);
        noteListModel.addElement(note);
        clearFields();
    }

    private void deleteNote() {
        int index = noteList.getSelectedIndex();
        if (index != -1) {
            allNotes.remove(noteListModel.getElementAt(index));
            noteListModel.remove(index);
            clearFields();
        }
    }

    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_FILE))) {
            for (Note note : allNotes) {
                writer.println(note.title + "|||"
                        + note.content.replace("\n", "\\n") + "|||"
                        + note.tag);
            }
            JOptionPane.showMessageDialog(this, "Notes saved to file.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving notes: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        allNotes.clear();
        noteListModel.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|\\|\\|");
                if (parts.length == 3) {
                    String title = parts[0];
                    String content = parts[1].replace("\\n", "\n");
                    String tag = parts[2];
                    Note note = new Note(title, content, tag);
                    allNotes.add(note);
                    noteListModel.addElement(note);
                }
            }
            JOptionPane.showMessageDialog(this, "Notes loaded.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading notes: " + e.getMessage());
        }
    }

    private void exportToTxt() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Notes As");
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File outFile = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(outFile))) {
                for (Note note : allNotes) {
                    writer.println("Title: " + note.title);
                    writer.println("Tag: " + note.tag);
                    writer.println("Content:\n" + note.content);
                    writer.println("-----\n");
                }
                JOptionPane.showMessageDialog(this, "Notes exported to " + outFile.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
            }
        }
    }

    private void filterNotes(String query) {
        noteListModel.clear();
        for (Note note : allNotes) {
            if (note.title.toLowerCase().contains(query.toLowerCase()) ||
                    note.content.toLowerCase().contains(query.toLowerCase()) ||
                    note.tag.toLowerCase().contains(query.toLowerCase())) {
                noteListModel.addElement(note);
            }
        }
    }

    private void clearFields() {
        titleField.setText("");
        noteContentArea.setText("");
        tagField.setText("");
        updateList();
    }

    private void updateList() {
        noteListModel.clear();
        for (Note note : allNotes) {
            noteListModel.addElement(note);
        }
    }

    static class Note {
        String title, content, tag;

        Note(String title, String content, String tag) {
            this.title = title;
            this.content = content;
            this.tag = tag;
        }

        public String toString() {
            return title + "  [" + tag + "]";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PersonalNotesApp::new);
    }
}
