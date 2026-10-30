package courier.ui;

import courier.domain.DispatchService;
import courier.domain.LoadValidation;
import courier.domain.RouteStopView;
import courier.domain.ShipmentView;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

public class TrunkPanel extends JPanel {
    private final DispatchWindow window;
    private final DispatchService service;

    private final JPanel trunkContainer = new JPanel();
    private final JPanel trunkFrame = new JPanel(new BorderLayout(0, 6));
    private final JPanel routeContainer = new JPanel();
    private final JPanel validationBanner = new JPanel(new BorderLayout());
    private final JLabel validationLabel = new JLabel();
    private final JLabel validationDetail = new JLabel();
    private final JLabel currentLabel = new JLabel();
    private final JButton departButton = Palette.primaryButton("Salir a repartir (validar R4)");

    public TrunkPanel(DispatchWindow window, DispatchService service) {
        this.window = window;
        this.service = service;
        setLayout(new BorderLayout(0, 10));
        setBackground(Palette.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTrunkView(), buildRouteSide());
        split.setResizeWeight(0.52);
        split.setDividerLocation(640);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(Palette.BACKGROUND);
        add(split, BorderLayout.CENTER);

        add(buildButtons(), BorderLayout.SOUTH);
        refresh();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Maletero");
        title.setFont(Palette.TITLE);
        title.setForeground(Palette.TEXT);
        JLabel subtitle = Palette.muted(
                "R3 y R4: se carga de la ultima parada a la primera; el tope debe ser la primera parada de la ruta.");
        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);
        currentLabel.setFont(Palette.BODY_BOLD);
        currentLabel.setForeground(Palette.ACCENT);
        header.add(currentLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildTrunkView() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Palette.PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.LINE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel title = new JLabel("MALETERO - pila en vertical, tope arriba");
        title.setFont(Palette.SECTION);
        title.setForeground(Palette.TEXT);
        panel.add(title, BorderLayout.NORTH);

        trunkContainer.setLayout(new BoxLayout(trunkContainer, BoxLayout.Y_AXIS));
        trunkContainer.setBackground(Palette.PANEL);
        trunkContainer.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        JScrollPane scroll = new JScrollPane(trunkContainer,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Palette.PANEL);
        trunkFrame.add(scroll, BorderLayout.CENTER);

        JLabel bottom = new JLabel("FONDO DEL MALETERO");
        bottom.setFont(Palette.SMALL);
        bottom.setForeground(Palette.MUTED);
        trunkFrame.add(bottom, BorderLayout.SOUTH);
        return trunkFrame;
    }

    private JPanel buildRouteSide() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        validationBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.LINE),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        validationLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        validationDetail.setFont(Palette.SMALL);
        JPanel validationText = new JPanel();
        validationText.setOpaque(false);
        validationText.setLayout(new BoxLayout(validationText, BoxLayout.Y_AXIS));
        validationText.add(validationLabel);
        validationText.add(validationDetail);
        validationBanner.add(validationText, BorderLayout.WEST);
        panel.add(validationBanner, BorderLayout.NORTH);

        JPanel routePanel = new JPanel(new BorderLayout(0, 6));
        routePanel.setBackground(Palette.PANEL);
        routePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.LINE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JLabel routeTitle = new JLabel("RUTA - primera parada arriba, ultima abajo");
        routeTitle.setFont(Palette.SECTION);
        routeTitle.setForeground(Palette.TEXT);
        routePanel.add(routeTitle, BorderLayout.NORTH);

        routeContainer.setLayout(new BoxLayout(routeContainer, BoxLayout.Y_AXIS));
        routeContainer.setBackground(Palette.PANEL);
        routeContainer.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        JScrollPane routeScroll = new JScrollPane(routeContainer,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        routeScroll.setBorder(BorderFactory.createEmptyBorder());
        routeScroll.getViewport().setBackground(Palette.PANEL);
        routePanel.add(routeScroll, BorderLayout.CENTER);
        panel.add(routePanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setOpaque(false);

        JButton loadButton = Palette.button("Cargar maletero");
        loadButton.addActionListener(event -> window.perform("cargarMaletero", service::loadTrunk));
        JButton unloadButton = Palette.button("Descargar maletero");
        unloadButton.addActionListener(event -> window.perform("descargarMaletero", service::unloadTrunk));
        JButton validateButton = Palette.button("Validar carga");
        validateButton.addActionListener(event -> validateLoad());
        JButton deliverButton = Palette.button("Entregar parada actual");
        deliverButton.addActionListener(event -> window.perform("entregar", service::deliverCurrent));
        JButton failButton = Palette.button("Registrar entrega fallida");
        failButton.addActionListener(event -> window.perform("entregaFallida", service::failCurrentDelivery));
        departButton.addActionListener(event -> window.perform("salirARepartir", service::authorizeDeparture));

        panel.add(loadButton);
        panel.add(unloadButton);
        panel.add(validateButton);
        panel.add(deliverButton);
        panel.add(failButton);
        panel.add(departButton);
        return panel;
    }

    private void validateLoad() {
        LoadValidation validation = service.validateAndLog();
        window.setStatus(validation.valid() ? validation.message() : "[R4] " + validation.message(),
                !validation.valid());
        window.refreshAll();
    }

    public void refresh() {
        List<ShipmentView> packages = service.trunkView();
        List<RouteStopView> stops = service.routeView();
        LoadValidation validation = service.validateLoad();
        boolean loaded = !packages.isEmpty();

        rebuildTrunk(packages);
        rebuildRoute(stops);
        updateBanner(validation, loaded, packages);
        updateTrunkBorder(validation, loaded);
        ShipmentView current = service.currentShipmentView();
        currentLabel.setText(current == null
                ? "Parada actual: ninguna"
                : "Parada actual: " + current.guide());

        departButton.setEnabled(validation.valid());
        departButton.setToolTipText(validation.valid()
                ? "La carga es el inverso exacto de la ruta."
                : "Salida bloqueada por R4/R5: " + validation.message());
    }

    private void rebuildTrunk(List<ShipmentView> packages) {
        trunkContainer.removeAll();
        if (packages.isEmpty()) {
            JLabel empty = new JLabel("Maletero vacio.");
            empty.setFont(Palette.BODY);
            empty.setForeground(Palette.DISABLED);
            empty.setBorder(BorderFactory.createEmptyBorder(30, 12, 30, 12));
            trunkContainer.add(empty);
            trunkContainer.revalidate();
            trunkContainer.repaint();
            return;
        }
        for (int index = 0; index < packages.size(); index++) {
            ShipmentView shipment = packages.get(index);
            JPanel card = new JPanel(new BorderLayout(8, 0));
            card.setBackground(Palette.PANEL);
            card.setBorder(index == 0 ? Palette.accentCardBorder() : Palette.cardBorder());
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

            JLabel marker = new JLabel(index == 0 ? "TOPE" : String.valueOf(index + 1));
            marker.setFont(new Font("SansSerif", Font.BOLD, 11));
            marker.setForeground(index == 0 ? Palette.ACCENT : Palette.MUTED);
            marker.setPreferredSize(new Dimension(44, 20));
            card.add(marker, BorderLayout.WEST);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            JLabel guide = new JLabel(shipment.guide() + " - " + shipment.type());
            guide.setFont(Palette.BODY_BOLD);
            guide.setForeground(Palette.TEXT);
            JLabel destination = new JLabel(shipment.destination());
            destination.setFont(Palette.SMALL);
            destination.setForeground(Palette.MUTED);
            text.add(guide);
            text.add(destination);
            card.add(text, BorderLayout.CENTER);
            trunkContainer.add(card);
            if (index < packages.size() - 1) {
                JLabel arrow = new JLabel("v");
                arrow.setFont(new Font("SansSerif", Font.PLAIN, 14));
                arrow.setForeground(Palette.LINE);
                arrow.setAlignmentX(LEFT_ALIGNMENT);
                trunkContainer.add(arrow);
            }
        }
        trunkContainer.revalidate();
        trunkContainer.repaint();
    }

    private void rebuildRoute(List<RouteStopView> stops) {
        routeContainer.removeAll();
        if (stops.isEmpty()) {
            JLabel empty = new JLabel("No hay ruta armada.");
            empty.setFont(Palette.BODY);
            empty.setForeground(Palette.DISABLED);
            empty.setBorder(BorderFactory.createEmptyBorder(24, 12, 24, 12));
            routeContainer.add(empty);
            routeContainer.revalidate();
            routeContainer.repaint();
            return;
        }
        for (RouteStopView stop : stops) {
            JPanel card = new JPanel(new BorderLayout(6, 0));
            card.setBackground(Palette.PANEL);
            card.setBorder(stop.current() ? Palette.accentCardBorder() : Palette.cardBorder());
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

            JLabel position = new JLabel(String.format("%02d", stop.position()));
            position.setFont(new Font("Monospaced", Font.BOLD, 13));
            position.setForeground(stop.current() ? Palette.ACCENT : Palette.MUTED);
            position.setPreferredSize(new Dimension(32, 18));
            card.add(position, BorderLayout.WEST);

            JLabel guide = new JLabel(stop.guide() + "  -  " + stop.state());
            guide.setFont(Palette.BODY_BOLD);
            guide.setForeground(stop.current() ? Palette.ACCENT : Palette.TEXT);
            card.add(guide, BorderLayout.CENTER);
            routeContainer.add(card);
        }
        routeContainer.revalidate();
        routeContainer.repaint();
    }

    private void updateTrunkBorder(LoadValidation validation, boolean loaded) {
        Color borderColor = loaded && !validation.valid() ? Palette.ACCENT : Palette.LINE;
        int thickness = loaded && !validation.valid() ? 3 : 1;
        trunkFrame.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, thickness),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
    }

    private void setDetail(String text) {
        validationDetail.setText("<html><body style='width:360px'>" + text + "</body></html>");
    }

    private void updateBanner(LoadValidation validation, boolean loaded, List<ShipmentView> packages) {
        boolean routeEmpty = service.routeSize() == 0;
        if (routeEmpty && !loaded) {
            validationBanner.setBackground(Palette.SOFT);
            validationLabel.setForeground(Palette.MUTED);
            validationLabel.setText("SIN CARGA");
            validationDetail.setForeground(Palette.MUTED);
            setDetail("Arme la ruta y cargue el maletero para habilitar la salida.");
            return;
        }
        if (validation.valid()) {
            validationBanner.setBackground(Palette.SUCCESS);
            validationLabel.setForeground(Color.WHITE);
            validationLabel.setText("CARGA VALIDA - SALIDA HABILITADA");
            validationDetail.setForeground(Color.WHITE);
            setDetail("Tope " + packages.get(0).guide() + " = primera parada de la ruta.");
            return;
        }
        validationBanner.setBackground(Palette.ACCENT);
        validationLabel.setForeground(Color.WHITE);
        validationLabel.setText("CARGA INVALIDA - SALIDA BLOQUEADA");
        validationDetail.setForeground(Color.WHITE);
        setDetail(validation.message());
    }
}
