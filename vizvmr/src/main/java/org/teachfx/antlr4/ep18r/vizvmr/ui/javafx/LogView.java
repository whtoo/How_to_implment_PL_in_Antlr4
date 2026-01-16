package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.teachfx.antlr4.common.visualization.ui.javafx.JFXPanelBase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Log panel - JavaFX version
 * Displays VM and GUI log messages with different log levels
 */
public class LogView extends JFXPanelBase {

    private final TextArea logTextArea = new TextArea();
    private final Button clearButton = new Button("清除日志");
    private final CheckBox autoScrollCheckBox = new CheckBox("自动滚动");
    private final Label statusLabel = new Label("就绪");
    private final ObservableList<LogEntry> logEntries;
    private final PrintStream originalOut;
    private final PrintStream originalErr;
    private static final int MAX_LOG_ENTRIES = 1000;

    public LogView() {
        super("LogView");
        this.logEntries = FXCollections.observableArrayList();
        this.originalOut = System.out;
        this.originalErr = System.err;
    }

    @Override
    protected void initializeComponents() {
        setTitle("日志");
        setMinSize(600, 200);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        // Create toolbar
        HBox toolbar = createToolbar();
        mainLayout.getChildren().add(toolbar);

        // Create log text area
        logTextArea.setEditable(false);
        logTextArea.setFont(Font.font("Monospaced", 12));
        logTextArea.setWrapText(true);

        ScrollPane scrollPane = new ScrollPane(logTextArea);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        mainLayout.getChildren().add(scrollPane);

        setCenter(mainLayout);
    }

    /**
     * Create toolbar with controls
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        clearButton.setOnAction(e -> clearLog());

        autoScrollCheckBox.setSelected(true);

        toolbar.getChildren().addAll(clearButton, autoScrollCheckBox, statusLabel);

        return toolbar;
    }

    /**
     * Add log message
     */
    public void addLog(String level, String message) {
        Platform.runLater(() -> {
            // Add to observable list for potential filtering/display
            if (logEntries.size() >= MAX_LOG_ENTRIES) {
                logEntries.remove(0);
            }
            LogEntry entry = new LogEntry(level, message);
            logEntries.add(entry);

            // Get color based on level
            String color = getColorForLevel(level);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            String logLine = String.format("[%s] [%s] %s%n", timestamp, level, message);

            // Append to text area with ANSI-like color codes or just the message
            logTextArea.appendText(logLine);

            // Auto-scroll to bottom
            if (autoScrollCheckBox.isSelected()) {
                logTextArea.positionCaret(logTextArea.getText().length());
            }

            // Update status label
            statusLabel.setText("日志已更新: " + level);
        });
    }

    /**
     * Get color for log level
     */
    private String getColorForLevel(String level) {
        switch (level.toUpperCase()) {
            case "WARN":
                return "#FF8C00";  // Dark orange
            case "ERROR":
                return "#FF0000";  // Red
            case "DEBUG":
                return "#0000FF";  // Blue
            default:
            case "INFO":
                return "#000000";  // Black
        }
    }

    /**
     * Add INFO level log
     */
    public void info(String message) {
        addLog("INFO", message);
    }

    /**
     * Add WARN level log
     */
    public void warn(String message) {
        addLog("WARN", message);
    }

    /**
     * Add ERROR level log
     */
    public void error(String message) {
        addLog("ERROR", message);
    }

    /**
     * Add ERROR level log with exception
     */
    public void error(String message, Throwable t) {
        error(message + ": " + t.getMessage());
        // Add stack trace
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        t.printStackTrace(ps);
        error("堆栈跟踪: " + baos.toString());
    }

    /**
     * Add DEBUG level log
     */
    public void debug(String message) {
        addLog("DEBUG", message);
    }

    /**
     * Clear all logs
     */
    public void clearLog() {
        Platform.runLater(() -> {
            logTextArea.clear();
            logEntries.clear();
            statusLabel.setText("日志已清除");
        });
    }

    /**
     * Get log content
     */
    public String getLogContent() {
        return logTextArea.getText();
    }

    /**
     * Redirect System.out and System.err to log panel
     */
    public void redirectSystemStreams() {
        // Redirect System.out
        System.setOut(new PrintStream(new ByteArrayOutputStream() {
            @Override
            public void flush() {
                String output = this.toString();
                if (!output.trim().isEmpty()) {
                    info("STDOUT: " + output);
                }
                reset(); // Clear buffer
            }
        }));

        // Redirect System.err
        System.setErr(new PrintStream(new ByteArrayOutputStream() {
            @Override
            public void flush() {
                String output = this.toString();
                if (!output.trim().isEmpty()) {
                    error("STDERR: " + output);
                }
                reset(); // Clear buffer
            }
        }));
    }

    /**
     * Restore original System.out and System.err
     */
    public void restoreSystemStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Get log text area for external access
     */
    public TextArea getLogTextArea() {
        return logTextArea;
    }

    /**
     * Inner class for log entry
     */
    public static class LogEntry {
        private final String level;
        private final String message;
        private final LocalDateTime timestamp;

        public LogEntry(String level, String message) {
            this.level = level;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }

        public String getLevel() {
            return level;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
