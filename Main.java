import ui.MainWindow;
import ui.backend.AdvisingBackend;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the myAdvice Student Advising System.
 */
public class Main {
    public static void main(String[] args) {
        // Use system look and feel for a native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to default Swing L&F
        }

        // Launch the UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Replaced PlaceholderBackend with the actual backend implementation
            MainWindow window = new MainWindow(new AdvisingBackend());
            window.setVisible(true);
        });
    }
}