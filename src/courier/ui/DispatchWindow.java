package courier.ui;

import courier.domain.DispatchException;
import courier.domain.DispatchService;
import courier.domain.MetricsSnapshot;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.function.Supplier;

public class DispatchWindow extends JFrame {
    private final DispatchService service;
    private final ShipmentTrayPanel trayPanel;
    private final RouteEditorPanel routePanel;
    private final TrunkPanel trunkPanel;
    private final ReportsPanel reportsPanel;
    private final JTabbedPane tabs;
    private final JLabel statusLabel;
    private final JLabel metricsFooter;
    private final JLabel capacityLabel;

    public DispatchWindow(DispatchService service) {
        super("Despacho de mensajeria urbana en moto");
        this.service = service;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1150, 650));
        setSize(1320, 710);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Palette.BACKGROUND);
        setLayout(new BorderLayout());

        capacityLabel = new JLabel();
        capacityLabel.setFont(Palette.SMALL);
        capacityLabel.setForeground(new Color(0xCCCCCC));
        capacityLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        add(buildHeader(), BorderLayout.NORTH);

        trayPanel = new ShipmentTrayPanel(this, service);
        routePanel = new RouteEditorPanel(this, service);
        trunkPanel = new TrunkPanel(this, service);
        reportsPanel = new ReportsPanel(this, service);

        tabs = new JTabbedPane();
        tabs.setFont(Palette.SECTION);
        tabs.setBackground(Palette.BACKGROUND);
        tabs.addTab("1. Bandeja de envios", trayPanel);
        tabs.addTab("2. Editor de ruta", routePanel);
        tabs.addTab("3. Maletero", trunkPanel);
        tabs.addTab("4. Cierre y reportes", reportsPanel);
        add(tabs, BorderLayout.CENTER);

        statusLabel = new JLabel("Sistema listo. Comience recibiendo envios o cargando los datos de ejemplo.");
        statusLabel.setFont(Palette.BODY_BOLD);
        statusLabel.setForeground(Palette.TEXT);
        metricsFooter = new JLabel();
        metricsFooter.setFont(Palette.SMALL);
        metricsFooter.setForeground(Palette.MUTED);
        add(buildFooter(), BorderLayout.SOUTH);

        refreshAll();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Palette.HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Despacho de mensajeria urbana en moto");
        title.setFont(Palette.TITLE);
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Ruta, maletero y colas - lista doblemente enlazada + cola + pila");
        subtitle.setFont(Palette.SMALL);
        subtitle.setForeground(new Color(0xBBBBBB));
        titles.add(title);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);
        header.add(capacityLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(Palette.PANEL);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Palette.LINE),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        footer.add(statusLabel);
        footer.add(Box.createVerticalStrut(2));
        footer.add(metricsFooter);
        return footer;
    }

    public boolean perform(String description, Supplier<String> action) {
        try {
            String result = action.get();
            setStatus(result == null || result.isEmpty() ? description : result, false);
            refreshAll();
            return true;
        } catch (DispatchException exception) {
            setStatus("[" + exception.getRule() + "] " + exception.getMessage(), true);
        } catch (RuntimeException exception) {
            setStatus("Error: " + exception.getMessage(), true);
        }
        refreshAll();
        return false;
    }

    public void refreshAll() {
        trayPanel.refresh();
        routePanel.refresh();
        trunkPanel.refresh();
        reportsPanel.refresh();
        MetricsSnapshot metrics = service.metricsView();
        capacityLabel.setText("Capacidad maletero: " + service.getTrunkCapacity()
                + " - Capacidad colas: " + service.getPendingCapacity()
                + " - Ruta: " + service.routeSize() + "/" + service.getTrunkCapacity());
        metricsFooter.setText("Recibidos: " + metrics.received()
                + "   |   Asignados: " + metrics.assigned()
                + "   |   Entregados: " + metrics.delivered()
                + "   |   Fallidos: " + metrics.failed()
                + "   |   Devueltos: " + metrics.returned()
                + "   |   Reorganizaciones: " + metrics.reorganizations()
                + "   |   Invalidaciones R5: " + metrics.invalidations()
                + "   |   Recargas: " + metrics.reloads());
        tabs.repaint();
    }

    public void setStatus(String text, boolean error) {
        statusLabel.setText(text);
        statusLabel.setForeground(error ? Palette.ACCENT : Palette.TEXT);
    }

    public void showRouteTab() {
        tabs.setSelectedIndex(1);
    }

    public void showTrunkTab() {
        tabs.setSelectedIndex(2);
    }

    public void showReportsTab() {
        tabs.setSelectedIndex(3);
    }
}
