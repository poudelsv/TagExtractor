import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class TagExtractorGUI extends JFrame {

    private JTextField selectedTextFileField;
    private JTextField selectedStopWordsFileField;
    private JTextArea outputArea;

    private File selectedTextFile;
    private File selectedStopWordsFile;

    public TagExtractorGUI() {
        setTitle("Tag / Keyword Extractor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeGUI();
    }

    private void initializeGUI() {
        JPanel topPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JPanel textFilePanel = new JPanel(new BorderLayout(5, 5));
        JButton chooseTextFileButton = new JButton("Choose Text File");
        selectedTextFileField = new JTextField();
        selectedTextFileField.setEditable(false);
        textFilePanel.add(chooseTextFileButton, BorderLayout.WEST);
        textFilePanel.add(selectedTextFileField, BorderLayout.CENTER);

        JPanel stopWordsPanel = new JPanel(new BorderLayout(5, 5));
        JButton chooseStopWordsButton = new JButton("Choose Stop Words File");
        selectedStopWordsFileField = new JTextField();
        selectedStopWordsFileField.setEditable(false);
        stopWordsPanel.add(chooseStopWordsButton, BorderLayout.WEST);
        stopWordsPanel.add(selectedStopWordsFileField, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        JButton extractButton = new JButton("Extract Tags");
        JButton saveButton = new JButton("Save Output");
        actionPanel.add(extractButton);
        actionPanel.add(saveButton);

        topPanel.add(textFilePanel);
        topPanel.add(stopWordsPanel);
        topPanel.add(actionPanel);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        chooseTextFileButton.addActionListener(e -> chooseTextFile());
        chooseStopWordsButton.addActionListener(e -> chooseStopWordsFile());
        extractButton.addActionListener(e -> extractTags());
        saveButton.addActionListener(e -> saveOutput());
    }

    private void chooseTextFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedTextFile = chooser.getSelectedFile();
            selectedTextFileField.setText(selectedTextFile.getAbsolutePath());
        }
    }

    private void chooseStopWordsFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedStopWordsFile = chooser.getSelectedFile();
            selectedStopWordsFileField.setText(selectedStopWordsFile.getAbsolutePath());
        }
    }

    private void extractTags() {
        if (selectedTextFile == null || selectedStopWordsFile == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select both a text file and a stop words file.",
                    "Missing File",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Set<String> stopWords = loadStopWords(selectedStopWordsFile);
            Map<String, Integer> frequencyMap = extractWordFrequencies(selectedTextFile, stopWords);

            StringBuilder result = new StringBuilder();
            result.append("File: ").append(selectedTextFile.getName()).append("\n\n");
            result.append("Tags and Frequencies:\n");
            result.append("-----------------------------\n");

            for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
                result.append(String.format("%-20s %d%n", entry.getKey(), entry.getValue()));
            }

            outputArea.setText(result.toString());

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error reading files: " + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Set<String> loadStopWords(File stopWordsFile) throws IOException {
        Set<String> stopWords = new TreeSet<>();

        try (Scanner scanner = new Scanner(stopWordsFile)) {
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine().trim().toLowerCase();
                if (!word.isEmpty()) {
                    stopWords.add(word);
                }
            }
        }

        return stopWords;
    }

    private Map<String, Integer> extractWordFrequencies(File textFile, Set<String> stopWords) throws IOException {
        Map<String, Integer> frequencyMap = new TreeMap<>();

        Pattern nonLetters = Pattern.compile("[^a-zA-Z]");

        try (Scanner scanner = new Scanner(textFile)) {
            while (scanner.hasNext()) {
                String word = scanner.next();

                word = nonLetters.matcher(word).replaceAll("").toLowerCase();

                if (!word.isEmpty() && !stopWords.contains(word)) {
                    frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
                }
            }
        }

        return frequencyMap;
    }

    private void saveOutput() {
        if (outputArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "There is no output to save.",
                    "No Output",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File outputFile = chooser.getSelectedFile();

            try (PrintWriter writer = new PrintWriter(outputFile)) {
                writer.print(outputArea.getText());
                JOptionPane.showMessageDialog(this,
                        "Output saved successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving file: " + ex.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TagExtractorGUI gui = new TagExtractorGUI();
            gui.setVisible(true);
        });
    }
}