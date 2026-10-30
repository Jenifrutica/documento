package courier.ui;

import courier.domain.DispatchService;
import courier.domain.LogEntry;
import courier.domain.MetricsSnapshot;
import courier.domain.RouteClosure;
import courier.domain.WaitRecord;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

public class ReportsPanel extends JPanel {
    private final DispatchWindow window;
    private final DispatchService service;

    private final JLabel closureLabel = new JLabel();
    private final JLabel receivedValue = valueLabel();
    private final JLabel assignedValue = valueLabel();
    private final JLabel deliveredValue = valueLabel();
    private final JLabel returnedValue = valueLabel();
    private final JLabel failedValue = valueLabel();
    private final JLabel reorganizationsValue = valueLabel();
    private final JLabel invalidationsValue = valueLabel();
    private final JLabel reloadsValue = valueLabel();
    private final JLabel retriesValue = valueLabel();
    private final JLabel averageWaitValue = valueLabel();
    private final JLabel maximumWaitValue = valueLabel();
    private final JTextArea waitsArea = new JTextArea();
    private final JTextArea logArea = new JTextArea();
    private RouteClosure lastClosure;

    public ReportsPanel(DispatchWindow window, DispatchService service) {
        this.window = window;
        this.service = service;
        setLayout(new BorderLayout(0, 10));
        setBackground(Palette.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildMetricsPanel(), buildLogPanel());
        split.setResizeWeight(0.46);
        split.setDividerLocation(560);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(Palette.BACKGROUND);
        add(split, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Cierre y reportes");
        title.setFont(Palette.TITLE);
        title.setForeground(Palette.TEXT);
        JLabel subtitle = Palette.muted(
                "RF-09 y RF-10: cierre de ruta, metricas actualizadas y bitacora cronologica de operaciones.");
        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

        JButton closeButton = Palette.primaryButton("Cerrar ruta (RF-09)");
        closeButton.addActionListener(event -> closeRoute());
        header.add(closeButton, BorderLayout.EAST);

        closureLabel.setFont(Palette.BODY_BOLD);
        closureLabel.setForeground(Palette.TEXT);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        bottom.add(closureLabel, BorderLayout.WEST);
        header.add(bottom, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildMetricsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Palette.PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.LINE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel title = new JLabel("Metricas del reporte");
        title.setFont(Palette.SECTION);
        title.setForeground(Palette.TEXT);
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Palette.PANEL);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        int row = 0;
        addMetric(grid, row++, "Envios recibidos", receivedValue);
        addMetric(grid, row++, "Envios asignados a ruta", assignedValue);
        addMetric(grid, row++, "Envios entregados", deliveredValue);
        addMetric(grid, row++, "Devueltos a la cola al cerrar", returnedValue);
        addMetric(grid, row++, "Entregas fallidas", failedValue);
        addMetric(grid, row++, "Movimientos de reorganizacion", reorganizationsValue);
        addMetric(grid, row++, "Cargas invalidadas (R5)", invalidationsValue);
        addMetric(grid, row++, "Recargas realizadas", reloadsValue);
        addMetric(grid, row++, "Envios a reintentos (R8)", retriesValue);
        addMetric(grid, row++, "Espera promedio en pendientes", averageWaitValue);
        addMetric(grid, row++, "Espera maxima en pendientes", maximumWaitValue);
        content.add(grid);

        content.add(Box.createVerticalStrut(8));
        content.add(Palette.muted("Espera por envio, desde su ingreso a pendientes hasta la asignacion:"));
        waitsArea.setEditable(false);
        waitsArea.setFont(Palette.MONO);
        waitsArea.setBackground(new Color(0xFAFAFA));
        waitsArea.setForeground(Palette.TEXT);
        waitsArea.setRows(6);
        JScrollPane waitsScroll = new JScrollPane(waitsArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        waitsScroll.setPreferredSize(new Dimension(100, 130));
        content.add(waitsScroll);

        JScrollPane scroll = new JScrollPane(content,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Palette.PANEL);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Palette.PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.LINE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel title = new JLabel("Bitacora cronologica (RFE-08)");
        title.setFont(Palette.SECTION);
        title.setForeground(Palette.TEXT);
        panel.add(title, BorderLayout.NORTH);

        logArea.setEditable(false);
        logArea.setFont(Palette.MONO);
        logArea.setBackground(new Color(0xFAFAFA));
        logArea.setForeground(Palette.TEXT);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(logArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createLineBorder(Palette.LINE));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void closeRoute() {
        window.perform("cerrarRuta", () -> {
            RouteClosure closure = service.closeRoute();
            lastClosure = closure;
            return "Ruta cerrada: entregados " + closure.delivered()
                    + ", devueltos " + closure.returnedToPending()
                    + ", a reintentos " + closure.sentToRetries() + ".";
        });
    }

    public void refresh() {
        MetricsSnapshot metrics = service.metricsView();
        receivedValue.setText(String.valueOf(metrics.received()));
        assignedValue.setText(String.valueOf(metrics.assigned()));
        deliveredValue.setText(String.valueOf(metrics.delivered()));
        returnedValue.setText(String.valueOf(metrics.returned()));
        failedValue.setText(String.valueOf(metrics.failed()));
        reorganizationsValue.setText(String.valueOf(metrics.reorganizations()));
        invalidationsValue.setText(String.valueOf(metrics.invalidations()));
        reloadsValue.setText(String.valueOf(metrics.reloads()));
        retriesValue.setText(String.valueOf(metrics.sentToRetries()));
        averageWaitValue.setText(String.format("%.1f min", metrics.averageWait()));
        maximumWaitValue.setText(metrics.maximumWait() + " min");

        if (lastClosure == null) {
            closureLabel.setText("Aun no se ha cerrado una ruta en esta sesion.");
        } else {
            closureLabel.setText("Ultimo cierre: entregados " + lastClosure.delivered()
                    + " - devueltos " + lastClosure.returnedToPending()
                    + " - a reintentos " + lastClosure.sentToRetries()
                    + " - pendientes " + lastClosure.pendingSize()
                    + " - reintentos " + lastClosure.retriesSize() + ".");
        }

        StringBuilder waits = new StringBuilder();
        List<WaitRecord> records = metrics.waits();
        if (records.isEmpty()) {
            waits.append("Sin registros de espera.");
        } else {
            for (WaitRecord record : records) {
                waits.append(record.guide()).append(": ").append(record.minutes()).append(" min\n");
            }
        }
        waitsArea.setText(waits.toString());

        StringBuilder log = new StringBuilder();
        List<LogEntry> entries = service.logView();
        if (entries.isEmpty()) {
            log.append("Sin operaciones registradas.");
        } else {
            for (LogEntry entry : entries) {
                log.append('[').append(entry.formattedTime()).append("] ")
                        .append(entry.operation()).append(" | ")
                        .append(entry.success() ? "OK" : "ERROR").append(" | ")
                        .append(entry.detail()).append('\n');
            }
        }
        String logText = log.toString();
        if (!logArea.getText().equals(logText)) {
            logArea.setText(logText);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }

    private static JLabel valueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(Palette.BODY_BOLD);
        label.setForeground(Palette.TEXT);
        return label;
    }

    private void addMetric(JPanel panel, int row, String name, JLabel value) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 8);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        JLabel nameLabel = Palette.label(name);
        panel.add(nameLabel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        panel.add(value, constraints);
    }
}
