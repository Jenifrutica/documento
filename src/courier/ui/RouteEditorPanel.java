package courier.ui;

import courier.domain.DispatchService;
import courier.domain.RouteDirection;
import courier.domain.RouteStopView;
import courier.domain.StopState;
import courier.domain.TraversalRow;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

public class RouteEditorPanel extends JPanel {
    private final DispatchWindow window;
    private final DispatchService service;

    private final JPanel routeContainer = new JPanel();
    private final JLabel routeCount = new JLabel();
    private final JComboBox<String> moveGuide = new JComboBox<>();
    private final JSpinner movePosition = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
    private final JSpinner insertPosition = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
    private final JComboBox<String> deleteGuide = new JComboBox<>();
    private final JTextArea traversalArea = new JTextArea();
    private RouteDirection lastDirection;

    public RouteEditorPanel(DispatchWindow window, DispatchService service) {
        this.window = window;
        this.service = service;
        setLayout(new BorderLayout(0, 10));
        setBackground(Palette.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildRouteView(), buildControls());
        split.setResizeWeight(0.66);
        split.setDividerLocation(800);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(Palette.BACKGROUND);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Editor de ruta");
        title.setFont(Palette.TITLE);
        title.setForeground(Palette.TEXT);
        JLabel subtitle = Palette.muted(
                "RF-03 y RF-04: mover, insertar, eliminar y recorrer la lista en ambos sentidos. Toda modificacion invalida la carga (R5).");
        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);
        routeCount.setFont(Palette.BODY_BOLD);
        routeCount.setForeground(Palette.ACCENT);
        header.add(routeCount, BorderLayout.EAST);
        return header;
    }

    private JPanel buildRouteView() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Palette.PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.LINE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Lista doblemente enlazada - PRIMERA -> ULTIMA");
        title.setFont(Palette.SECTION);
        title.setForeground(Palette.TEXT);
        header.add(title, BorderLayout.WEST);
        header.add(Palette.muted("Apuntador de parada actual resaltado"), BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        routeContainer.setLayout(new BoxLayout(routeContainer, BoxLayout.Y_AXIS));
        routeContainer.setBackground(Palette.PANEL);
        routeContainer.setBorder(BorderFactory.createEmptyBorder(6, 2, 6, 2));
        JScrollPane scroll = new JScrollPane(routeContainer,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Palette.PANEL);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Palette.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        panel.add(buildMoveSection());
        panel.add(Box.createVerticalStrut(8));
        panel.add(buildInsertSection());
        panel.add(Box.createVerticalStrut(8));
        panel.add(buildDeleteSection());
        panel.add(Box.createVerticalStrut(8));
        panel.add(buildTraversalSection());
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel section(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Palette.PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.LINE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JLabel label = new JLabel(title);
        label.setFont(Palette.SECTION);
        label.setForeground(Palette.TEXT);
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildMoveSection() {
        JPanel panel = section("Mover parada (RF-03)");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 4);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        form.add(moveGuide, constraints);
        constraints.gridx = 1;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        form.add(Palette.label("Posicion:"), constraints);
        constraints.gridx = 2;
        movePosition.setPreferredSize(new Dimension(65, 26));
        form.add(movePosition, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.NONE;
        JButton moveButton = Palette.primaryButton("Mover parada");
        moveButton.addActionListener(event -> moveStop());
        form.add(moveButton, constraints);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildInsertSection() {
        JPanel panel = section("Insertar parada desde el frente de pendientes");
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        form.setOpaque(false);
        form.add(Palette.label("Posicion:"));
        insertPosition.setPreferredSize(new Dimension(65, 26));
        form.add(insertPosition);
        JButton insertButton = Palette.button("Insertar parada");
        insertButton.addActionListener(event -> insertStop());
        form.add(insertButton);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDeleteSection() {
        JPanel panel = section("Eliminar parada (RF-03)");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 4);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        form.add(deleteGuide, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;
        JButton deleteButton = Palette.button("Eliminar y devolver al frente");
        deleteButton.addActionListener(event -> removeStop());
        form.add(deleteButton, constraints);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTraversalSection() {
        JPanel panel = section("Recorrido de la ruta (RF-04)");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        buttons.setOpaque(false);
        JButton forwardButton = Palette.button("Recorrer hacia adelante");
        forwardButton.addActionListener(event -> traverse(RouteDirection.FORWARD));
        JButton backwardButton = Palette.button("Recorrer hacia atras");
        backwardButton.addActionListener(event -> traverse(RouteDirection.BACKWARD));
        buttons.add(forwardButton);
        buttons.add(backwardButton);

        traversalArea.setEditable(false);
        traversalArea.setFont(Palette.MONO);
        traversalArea.setBackground(new Color(0xFAFAFA));
        traversalArea.setForeground(Palette.TEXT);
        traversalArea.setRows(8);
        traversalArea.setText("Seleccione un sentido de recorrido.");
        JScrollPane scroll = new JScrollPane(traversalArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(100, 150));

        JPanel content = new JPanel(new BorderLayout(0, 4));
        content.setOpaque(false);
        content.add(buttons, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void moveStop() {
        String guide = (String) moveGuide.getSelectedItem();
        if (guide == null) {
            window.setStatus("[RF-03] No hay paradas en la ruta para mover.", true);
            return;
        }
        int position = (Integer) movePosition.getValue();
        window.perform("moverParada", () -> service.moveStop(guide, position));
    }

    private void insertStop() {
        int position = (Integer) insertPosition.getValue();
        window.perform("insertarParada", () -> service.insertStop(position));
    }

    private void removeStop() {
        String guide = (String) deleteGuide.getSelectedItem();
        if (guide == null) {
            window.setStatus("[RF-03] No hay paradas en la ruta para eliminar.", true);
            return;
        }
        window.perform("eliminarParada", () -> service.removeStop(guide));
    }

    private void traverse(RouteDirection direction) {
        lastDirection = direction;
        List<TraversalRow> rows = service.traverseAndLog(direction);
        String header = direction == RouteDirection.FORWARD
                ? "HACIA ADELANTE - PRIMERA -> ULTIMA\n"
                : "HACIA ATRAS - ULTIMA -> PRIMERA\n";
        StringBuilder text = new StringBuilder(header);
        for (TraversalRow row : rows) {
            text.append(row.position()).append(". ").append(row.guide())
                    .append(" - ").append(row.destination())
                    .append(" - ").append(row.state());
            if (row.current()) {
                text.append("   <== PARADA ACTUAL");
            }
            text.append('\n');
        }
        if (rows.isEmpty()) {
            text.append("La ruta esta vacia.");
        }
        traversalArea.setText(text.toString());
        traversalArea.setCaretPosition(0);
        window.refreshAll();
    }

    public void refresh() {
        List<RouteStopView> stops = service.routeView();
        routeCount.setText(stops.size() + " paradas");
        rebuildRoute(stops);
        syncCombo(moveGuide, stops, false);
        syncCombo(deleteGuide, stops, false);
        updateSpinner(movePosition, Math.max(1, stops.size()));
        updateSpinner(insertPosition, Math.max(1, stops.size() + 1));
        if (lastDirection != null) {
            renderTraversal(lastDirection);
        }
    }

    private void renderTraversal(RouteDirection direction) {
        List<TraversalRow> rows = service.traverse(direction);
        String header = direction == RouteDirection.FORWARD
                ? "HACIA ADELANTE - PRIMERA -> ULTIMA\n"
                : "HACIA ATRAS - ULTIMA -> PRIMERA\n";
        StringBuilder text = new StringBuilder(header);
        for (TraversalRow row : rows) {
            text.append(row.position()).append(". ").append(row.guide())
                    .append(" - ").append(row.destination())
                    .append(" - ").append(row.state());
            if (row.current()) {
                text.append("   <== PARADA ACTUAL");
            }
            text.append('\n');
        }
        if (rows.isEmpty()) {
            text.append("La ruta esta vacia.");
        }
        traversalArea.setText(text.toString());
    }

    private void rebuildRoute(List<RouteStopView> stops) {
        routeContainer.removeAll();
        if (stops.isEmpty()) {
            JLabel empty = new JLabel("La ruta esta vacia. Armela desde la bandeja de envios.");
            empty.setFont(Palette.BODY);
            empty.setForeground(Palette.DISABLED);
            empty.setBorder(BorderFactory.createEmptyBorder(30, 12, 30, 12));
            routeContainer.add(empty);
            routeContainer.revalidate();
            routeContainer.repaint();
            return;
        }
        JLabel first = Palette.muted("PRIMERA PARADA");
        first.setAlignmentX(LEFT_ALIGNMENT);
        routeContainer.add(first);
        for (int index = 0; index < stops.size(); index++) {
            RouteStopView stop = stops.get(index);
            routeContainer.add(buildStopCard(stop));
            if (index < stops.size() - 1) {
                JLabel connector = new JLabel("<->");
                connector.setFont(new Font("SansSerif", Font.PLAIN, 16));
                connector.setForeground(Palette.LINE);
                connector.setAlignmentX(LEFT_ALIGNMENT);
                routeContainer.add(connector);
            }
        }
        JLabel last = Palette.muted("ULTIMA PARADA");
        last.setAlignmentX(LEFT_ALIGNMENT);
        routeContainer.add(last);
        routeContainer.revalidate();
        routeContainer.repaint();
    }

    private JPanel buildStopCard(RouteStopView stop) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(Palette.PANEL);
        card.setBorder(stop.current() ? Palette.accentCardBorder() : Palette.cardBorder());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JLabel position = new JLabel(String.format("%02d", stop.position()));
        position.setFont(new Font("Monospaced", Font.BOLD, 16));
        position.setForeground(stop.current() ? Palette.ACCENT : Palette.MUTED);
        position.setPreferredSize(new Dimension(34, 20));
        card.add(position, BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel guide = new JLabel(stop.guide() + "  -  " + stop.type());
        guide.setFont(Palette.BODY_BOLD);
        guide.setForeground(Palette.TEXT);
        JLabel destination = new JLabel(stop.destination());
        destination.setFont(Palette.SMALL);
        destination.setForeground(Palette.MUTED);
        center.add(guide);
        center.add(destination);
        card.add(center, BorderLayout.CENTER);

        JPanel state = new JPanel();
        state.setOpaque(false);
        state.setLayout(new BoxLayout(state, BoxLayout.Y_AXIS));
        JLabel stateLabel = new JLabel(stop.state().name());
        stateLabel.setFont(Palette.BODY_BOLD);
        stateLabel.setForeground(stateColor(stop.state()));
        state.add(stateLabel);
        if (stop.current()) {
            JLabel current = new JLabel("ACTUAL");
            current.setFont(new Font("SansSerif", Font.BOLD, 10));
            current.setForeground(Palette.ACCENT);
            state.add(current);
        }
        if (stop.attempts() > 0) {
            JLabel attempts = new JLabel("intentos " + stop.attempts());
            attempts.setFont(Palette.SMALL);
            attempts.setForeground(Palette.MUTED);
            state.add(attempts);
        }
        card.add(state, BorderLayout.EAST);
        return card;
    }

    private Color stateColor(StopState state) {
        return switch (state) {
            case ENTREGADA -> Palette.MUTED;
            case FALLIDA -> Palette.ACCENT;
            case PENDIENTE -> Palette.MUTED;
        };
    }

    private void syncCombo(JComboBox<String> combo, List<RouteStopView> stops, boolean includeDelivered) {
        String selected = (String) combo.getSelectedItem();
        List<String> guides = new ArrayList<>();
        for (RouteStopView stop : stops) {
            if (includeDelivered || stop.state() != StopState.ENTREGADA) {
                guides.add(stop.guide());
            }
        }
        if (guides.equals(currentItems(combo))) {
            return;
        }
        combo.removeAllItems();
        for (String guide : guides) {
            combo.addItem(guide);
        }
        if (selected != null && guides.contains(selected)) {
            combo.setSelectedItem(selected);
        }
    }

    private List<String> currentItems(JComboBox<String> combo) {
        List<String> items = new ArrayList<>();
        for (int index = 0; index < combo.getItemCount(); index++) {
            items.add(combo.getItemAt(index));
        }
        return items;
    }

    private void updateSpinner(JSpinner spinner, int maximum) {
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        int value = (Integer) model.getValue();
        model.setMaximum(Math.max(1, maximum));
        if (value > maximum) {
            model.setValue(maximum);
        }
    }
}
