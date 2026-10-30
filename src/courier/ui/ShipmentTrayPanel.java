package courier.ui;

import courier.domain.DispatchService;
import courier.domain.ShipmentType;
import courier.domain.ShipmentView;
import courier.seed.DemoShipments;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

public class ShipmentTrayPanel extends JPanel {
    private final DispatchWindow window;
    private final DispatchService service;

    private final JTextField guideField = new JTextField();
    private final JTextField destinationField = new JTextField();
    private final JComboBox<ShipmentType> typeCombo = new JComboBox<>(ShipmentType.values());
    private final JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(480, 0, 1439, 1));

    private final JSpinner trunkCapacitySpinner = new JSpinner(new SpinnerNumberModel(8, 1, 40, 1));
    private final JSpinner pendingCapacitySpinner = new JSpinner(new SpinnerNumberModel(30, 1, 200, 1));
    private final JButton buildRouteButton = Palette.primaryButton("Armar ruta");

    private final JPanel pendingLane = new JPanel();
    private final JPanel retriesLane = new JPanel();
    private final JLabel pendingCount = new JLabel();
    private final JLabel retriesCount = new JLabel();

    public ShipmentTrayPanel(DispatchWindow window, DispatchService service) {
        this.window = window;
        this.service = service;
        setLayout(new BorderLayout(0, 12));
        setBackground(Palette.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new GridLayout(1, 2, 12, 0));
        top.setOpaque(false);
        top.add(buildReceiveForm());
        top.add(buildActions());
        add(top, BorderLayout.NORTH);

        JPanel queues = new JPanel(new GridLayout(2, 1, 0, 10));
        queues.setOpaque(false);
        queues.add(buildQueueSection("Cola de pendientes", "R1: el primero recibido es el primero en salir.",
                pendingCount, pendingLane));
        queues.add(buildQueueSection("Cola de reintentos", "R8: envios con 2 intentos fallidos para el dia siguiente.",
                retriesCount, retriesLane));
        add(queues, BorderLayout.CENTER);

        configureLane(pendingLane);
        configureLane(retriesLane);

        refresh();
    }

    private JPanel buildReceiveForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Palette.PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.LINE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(3, 3, 3, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Recibir envio (RF-01)");
        title.setFont(Palette.SECTION);
        title.setForeground(Palette.TEXT);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 4;
        panel.add(title, constraints);

        constraints.gridwidth = 1;
        constraints.weightx = 0;
        constraints.gridy = 1;
        constraints.gridx = 0;
        panel.add(Palette.label("Guia:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(guideField, constraints);
        constraints.gridx = 2;
        constraints.weightx = 0;
        panel.add(Palette.label("Minuto:"), constraints);
        constraints.gridx = 3;
        constraints.weightx = 0;
        minuteSpinner.setPreferredSize(new Dimension(70, 26));
        panel.add(minuteSpinner, constraints);

        constraints.gridy = 2;
        constraints.gridx = 0;
        constraints.weightx = 0;
        panel.add(Palette.label("Destino:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(destinationField, constraints);
        constraints.gridx = 2;
        constraints.weightx = 0;
        panel.add(Palette.label("Tipo:"), constraints);
        constraints.gridx = 3;
        typeCombo.setPreferredSize(new Dimension(100, 26));
        panel.add(typeCombo, constraints);

        constraints.gridy = 3;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        JButton receiveButton = Palette.primaryButton("Recibir envio");
        receiveButton.addActionListener(event -> receiveShipment());
        panel.add(receiveButton, constraints);

        constraints.gridx = 2;
        constraints.gridwidth = 2;
        JLabel hint = Palette.muted("Guia unica - 0 a 1439 minutos");
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        panel.add(hint, constraints);

        return panel;
    }

    private JPanel buildActions() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Palette.PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.LINE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel title = new JLabel("Operaciones de la bandeja");
        title.setFont(Palette.SECTION);
        title.setForeground(Palette.TEXT);
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel capacities = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        capacities.setOpaque(false);
        capacities.add(Palette.label("Maletero (N):"));
        trunkCapacitySpinner.setPreferredSize(new Dimension(60, 26));
        capacities.add(trunkCapacitySpinner);
        capacities.add(Palette.label("Colas:"));
        pendingCapacitySpinner.setPreferredSize(new Dimension(65, 26));
        capacities.add(pendingCapacitySpinner);
        JButton applyButton = Palette.button("Aplicar");
        applyButton.addActionListener(event -> applyCapacities());
        capacities.add(applyButton);
        content.add(capacities);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.setOpaque(false);
        buildRouteButton.addActionListener(event -> buildRoute());
        buttons.add(buildRouteButton);
        JButton demoButton = Palette.button("Cargar datos de ejemplo");
        demoButton.addActionListener(event -> loadDemo());
        buttons.add(demoButton);
        content.add(buttons);

        content.add(Palette.muted("R2: la ruta toma hasta N envios del frente de pendientes."));
        content.add(Palette.muted("El maletero solo se carga cuando la ruta esta armada."));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildQueueSection(String title, String hint, JLabel countLabel, JPanel lane) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Palette.PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.LINE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Palette.SECTION);
        titleLabel.setForeground(Palette.TEXT);
        header.add(titleLabel, BorderLayout.WEST);
        countLabel.setFont(Palette.BODY_BOLD);
        countLabel.setForeground(Palette.ACCENT);
        header.add(countLabel, BorderLayout.EAST);

        JPanel order = new JPanel(new BorderLayout());
        order.setOpaque(false);
        order.add(Palette.muted("FRENTE  ->"), BorderLayout.WEST);
        order.add(Palette.muted(hint), BorderLayout.CENTER);
        order.add(Palette.muted("->  FINAL"), BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(order, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(lane,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(Palette.PANEL);
        scroll.getViewport().setBackground(Palette.PANEL);
        scroll.setPreferredSize(new Dimension(100, 104));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void configureLane(JPanel lane) {
        lane.setLayout(new BoxLayout(lane, BoxLayout.X_AXIS));
        lane.setBackground(Palette.PANEL);
        lane.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
    }

    private void receiveShipment() {
        String guide = guideField.getText();
        String destination = destinationField.getText();
        ShipmentType type = (ShipmentType) typeCombo.getSelectedItem();
        int minute = (Integer) minuteSpinner.getValue();
        boolean success = window.perform("recibirEnvio",
                () -> service.receiveShipment(guide, destination, type, minute));
        if (success) {
            guideField.setText("");
            destinationField.setText("");
            guideField.requestFocusInWindow();
        }
    }

    private void applyCapacities() {
        int trunk = (Integer) trunkCapacitySpinner.getValue();
        int pending = (Integer) pendingCapacitySpinner.getValue();
        window.perform("configurarCapacidades", () -> service.configureCapacities(trunk, pending));
    }

    private void buildRoute() {
        boolean success = window.perform("armarRuta", service::buildRoute);
        if (success) {
            window.showRouteTab();
        }
    }

    private void loadDemo() {
        window.perform("cargarDatosEjemplo", () -> service.loadDemoShipments(DemoShipments.create()));
    }

    public void refresh() {
        trunkCapacitySpinner.setValue(service.getTrunkCapacity());
        pendingCapacitySpinner.setValue(service.getPendingCapacity());
        buildRouteButton.setText("Armar ruta (max. " + service.getTrunkCapacity() + ")");

        List<ShipmentView> pending = service.pendingView();
        List<ShipmentView> retries = service.retriesView();
        pendingCount.setText(pending.size() + " envios");
        retriesCount.setText(retries.size() + " envios");
        fillLane(pendingLane, pending, "No hay envios pendientes.");
        fillLane(retriesLane, retries, "No hay envios en reintentos.");
        pendingLane.revalidate();
        pendingLane.repaint();
        retriesLane.revalidate();
        retriesLane.repaint();
    }

    private void fillLane(JPanel lane, List<ShipmentView> shipments, String emptyText) {
        lane.removeAll();
        if (shipments.isEmpty()) {
            JLabel empty = new JLabel(emptyText);
            empty.setFont(Palette.BODY);
            empty.setForeground(Palette.DISABLED);
            empty.setBorder(BorderFactory.createEmptyBorder(28, 12, 28, 12));
            lane.add(empty);
            return;
        }
        for (int index = 0; index < shipments.size(); index++) {
            lane.add(buildShipmentCard(shipments.get(index), index == 0, index == shipments.size() - 1));
            lane.add(Box.createHorizontalStrut(8));
        }
        lane.add(Box.createHorizontalGlue());
    }

    private JPanel buildShipmentCard(ShipmentView shipment, boolean front, boolean rear) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Palette.PANEL);
        card.setBorder(front ? Palette.accentCardBorder() : Palette.cardBorder());
        card.setPreferredSize(new Dimension(170, 84));
        card.setMinimumSize(new Dimension(170, 84));
        card.setMaximumSize(new Dimension(170, 84));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel guide = new JLabel(shipment.guide());
        guide.setFont(new Font("SansSerif", Font.BOLD, 13));
        guide.setForeground(Palette.TEXT);
        heading.add(guide, BorderLayout.WEST);
        if (front) {
            JLabel frontLabel = new JLabel("FRENTE");
            frontLabel.setFont(new Font("SansSerif", Font.BOLD, 9));
            frontLabel.setForeground(Palette.ACCENT);
            heading.add(frontLabel, BorderLayout.EAST);
        } else if (rear) {
            JLabel rearLabel = new JLabel("FINAL");
            rearLabel.setFont(new Font("SansSerif", Font.BOLD, 9));
            rearLabel.setForeground(Palette.MUTED);
            heading.add(rearLabel, BorderLayout.EAST);
        }
        card.add(heading);

        JLabel destination = new JLabel(shipment.destination());
        destination.setFont(Palette.SMALL);
        destination.setForeground(Palette.MUTED);
        destination.setToolTipText(shipment.destination());
        card.add(destination);

        JLabel detail = new JLabel(shipment.type() + " - min " + shipment.receivedMinute()
                + " - intentos " + shipment.attempts());
        detail.setFont(Palette.SMALL);
        detail.setForeground(Palette.TEXT);
        card.add(detail);
        return card;
    }
}
