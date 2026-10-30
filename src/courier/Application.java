package courier;

import courier.domain.DispatchService;
import courier.ui.DispatchWindow;
import courier.ui.Palette;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

public final class Application {
    private Application() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configureLookAndFeel();
            DispatchService service = new DispatchService();
            DispatchWindow window = new DispatchWindow(service);
            window.setVisible(true);
        });
    }

    private static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception exception) {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
        }
        UIManager.put("TabbedPane.background", Palette.BACKGROUND);
        UIManager.put("TabbedPane.foreground", Palette.TEXT);
        UIManager.put("TabbedPane.selected", Palette.PANEL);
        UIManager.put("TabbedPane.contentAreaColor", Palette.PANEL);
        UIManager.put("TabbedPane.light", Palette.PANEL);
        UIManager.put("TabbedPane.highlight", Palette.LINE);
        UIManager.put("TabbedPane.shadow", Palette.LINE);
        UIManager.put("TabbedPane.darkShadow", Palette.LINE);
        UIManager.put("TabbedPane.focus", Palette.LINE);
        UIManager.put("Button.background", Palette.PANEL);
        UIManager.put("Button.foreground", Palette.TEXT);
        UIManager.put("Button.select", Palette.SOFT);
        UIManager.put("Button.focus", Palette.LINE);
        UIManager.put("ComboBox.background", Palette.PANEL);
        UIManager.put("ComboBox.foreground", Palette.TEXT);
        UIManager.put("ComboBox.selectionBackground", Palette.SOFT);
        UIManager.put("ComboBox.selectionForeground", Palette.TEXT);
        UIManager.put("TextField.selectionBackground", Palette.SOFT);
        UIManager.put("TextField.selectionForeground", Palette.TEXT);
        UIManager.put("Spinner.background", Palette.PANEL);
        UIManager.put("Spinner.foreground", Palette.TEXT);
        UIManager.put("List.selectionBackground", Palette.SOFT);
        UIManager.put("List.selectionForeground", Palette.TEXT);
        UIManager.put("SplitPane.background", Palette.BACKGROUND);
        UIManager.put("Panel.background", Palette.BACKGROUND);
        UIManager.put("Label.foreground", Palette.TEXT);
        UIManager.put("OptionPane.background", Palette.PANEL);
        UIManager.put("OptionPane.messageForeground", Palette.TEXT);
        UIManager.put("ScrollBar.thumb", Palette.LINE);
        UIManager.put("ScrollBar.track", Palette.SOFT);
        UIManager.put("activeCaption", Color.WHITE);
        UIManager.put("activeCaptionText", Palette.TEXT);
    }
}
