package courier.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

public final class Palette {
    public static final Color BACKGROUND = new Color(0xEFEFEF);
    public static final Color PANEL = Color.WHITE;
    public static final Color HEADER = new Color(0x111111);
    public static final Color TEXT = new Color(0x1A1A1A);
    public static final Color MUTED = new Color(0x666666);
    public static final Color LINE = new Color(0xBDBDBD);
    public static final Color SOFT = new Color(0xE8E8E8);
    public static final Color ACCENT = new Color(0xB00020);
    public static final Color SUCCESS = new Color(0x1B7F3B);
    public static final Color DISABLED = new Color(0x9A9A9A);

    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 20);
    public static final Font SECTION = new Font("SansSerif", Font.BOLD, 14);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font BODY_BOLD = new Font("SansSerif", Font.BOLD, 12);
    public static final Font SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font MONO = new Font("Monospaced", Font.PLAIN, 12);

    private Palette() {
    }

    public static Border lineBorder() {
        return BorderFactory.createLineBorder(LINE);
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }

    public static Border accentCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 2),
                BorderFactory.createEmptyBorder(7, 9, 7, 9));
    }

    public static JButton button(String text) {
        JButton button = new JButton(text);
        button.setFont(BODY_BOLD);
        button.setBackground(PANEL);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        return button;
    }

    public static JButton primaryButton(String text) {
        JButton button = button(text);
        button.setBackground(HEADER);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HEADER),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        return button;
    }

    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SMALL);
        label.setForeground(MUTED);
        return label;
    }

    public static JPanel sectionHeader(String text) {
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        panel.setOpaque(false);
        JLabel title = new JLabel(text);
        title.setFont(SECTION);
        title.setForeground(TEXT);
        panel.add(title, java.awt.BorderLayout.WEST);
        return panel;
    }
}
