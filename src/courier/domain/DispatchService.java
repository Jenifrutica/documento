package courier.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//facade
public class DispatchService {
    private static final int DEFAULT_TRUNK_CAPACITY = 8;
    private static final int DEFAULT_PENDING_CAPACITY = 30;

    private final ShipmentLine pending = new ShipmentLine();
    private final ShipmentLine retries = new ShipmentLine();
    private final DeliveryRoute route = new DeliveryRoute();
    private final DeliveryTrunk trunk = new DeliveryTrunk();
    private final DispatchMetrics metrics = new DispatchMetrics();
    private final OperationLog log = new OperationLog();

    private int trunkCapacity = DEFAULT_TRUNK_CAPACITY;
    private int pendingCapacity = DEFAULT_PENDING_CAPACITY;
    private int latestMinute;
    private boolean loadInvalid;

    public int getTrunkCapacity() {
        return trunkCapacity;
    }

    public int getPendingCapacity() {
        return pendingCapacity;
    }

    public int getLatestMinute() {
        return latestMinute;
    }

    public String configureCapacities(int newTrunkCapacity, int newPendingCapacity) {
        String operation = "configurarCapacidades";
        if (newTrunkCapacity < 1 || newPendingCapacity < 1) {
            throw fail(operation, "RF-01", "Las capacidades deben ser mayores que cero.");
        }
        if (route.size() > newTrunkCapacity || trunk.size() > newTrunkCapacity) {
            throw fail(operation, "R9", "La nueva capacidad del maletero es menor que la carga actual.");
        }
        if (pending.size() > newPendingCapacity || retries.size() > newPendingCapacity) {
            throw fail(operation, "RF-01", "La nueva capacidad de la cola es menor que su ocupacion actual.");
        }
        trunkCapacity = newTrunkCapacity;
        pendingCapacity = newPendingCapacity;
        success(operation, "Capacidad del maletero=" + trunkCapacity + ", capacidad de colas=" + pendingCapacity);
        return "Capacidades actualizadas.";
    }

    public String receiveShipment(String guide, String destination, ShipmentType type, int minute) {
        String operation = "recibirEnvio";
        String cleanGuide = guide == null ? "" : guide.trim();
        String cleanDestination = destination == null ? "" : destination.trim();
        if (cleanGuide.isEmpty()) {
            throw fail(operation, "RF-01", "La guia no puede estar vacia.");
        }
        if (cleanDestination.isEmpty()) {
            throw fail(operation, "RF-01", "El destino no puede estar vacio.");
        }
        if (type == null) {
            throw fail(operation, "RF-01", "El tipo debe ser SOBRE o PAQUETE.");
        }
        if (minute < 0 || minute > 1439) {
            throw fail(operation, "RF-01", "El minuto debe estar entre 0 y 1439.");
        }
        if (guideExists(cleanGuide)) {
            throw fail(operation, "RF-01", "La guia " + cleanGuide + " ya existe en el sistema.");
        }
        if (pending.size() >= pendingCapacity) {
            throw fail(operation, "RF-01", "La cola de pendientes alcanzo su capacidad de " + pendingCapacity + ".");
        }
        Shipment shipment = new Shipment(cleanGuide, cleanDestination, type, minute);
        pending.addRear(shipment);
        latestMinute = Math.max(latestMinute, minute);
        metrics.shipmentReceived();
        success(operation, cleanGuide + " recibido. Pendientes=" + pending.size());
        return "Envio " + cleanGuide + " recibido.";
    }

    public String buildRoute() {
        String operation = "armarRuta";
        if (!route.isEmpty()) {
            throw fail(operation, "R2", "Ya hay una ruta abierta; cierrela antes de armar otra.");
        }
        if (!trunk.isEmpty()) {
            throw fail(operation, "R3", "El maletero no esta vacio; descarguelo antes de armar otra ruta.");
        }
        if (pending.isEmpty()) {
            throw fail(operation, "R1", "No hay envios pendientes para armar la ruta.");
        }
        int taken = 0;
        while (!pending.isEmpty() && route.size() < trunkCapacity) {
            Shipment shipment = pending.pollFront();
            recordWait(shipment);
            route.append(shipment);
            metrics.shipmentAssigned();
            taken++;
        }
        loadInvalid = false;
        success(operation, "Ruta armada con " + taken + " paradas.");
        return "Ruta armada con " + taken + " paradas.";
    }

    public String moveStop(String guide, int position) {
        String operation = "moverParada";
        if (route.isEmpty()) {
            throw fail(operation, "R2", "No hay una ruta abierta.");
        }
        RouteStop stop = route.findStop(guide);
        if (stop == null) {
            throw fail(operation, "RF-03", "La guia " + guide + " no esta en la ruta.");
        }
        if (stop.getState() == StopState.ENTREGADA) {
            throw fail(operation, "RF-03", "No se puede mover una parada ya entregada.");
        }
        if (position < 1 || position > route.size()) {
            throw fail(operation, "RF-03", "La posicion debe estar entre 1 y " + route.size() + ".");
        }
        int previousPosition = route.positionOf(guide);
        if (previousPosition == position) {
            success(operation, guide + " permanece en la posicion " + position + ".");
            return "La parada no cambio de posicion.";
        }
        route.move(guide, position);
        boolean invalidated = invalidateLoadIfNeeded();
        success(operation, guide + " movida de " + previousPosition + " a " + position
                + (invalidated ? ". Carga invalidada (R5)." : "."));
        return "Parada " + guide + " movida a la posicion " + position + ".";
    }

    public String insertStop(int position) {
        String operation = "insertarParada";
        if (route.size() >= trunkCapacity) {
            throw fail(operation, "R9", "La ruta ya tiene la capacidad maxima del maletero (" + trunkCapacity + ").");
        }
        if (position < 1 || position > route.size() + 1) {
            throw fail(operation, "RF-03", "La posicion debe estar entre 1 y " + (route.size() + 1) + ".");
        }
        if (pending.isEmpty()) {
            throw fail(operation, "R1", "No hay envios pendientes para insertar.");
        }
        Shipment shipment = pending.pollFront();
        recordWait(shipment);
        route.insertAt(position, shipment);
        metrics.shipmentAssigned();
        boolean invalidated = invalidateLoadIfNeeded();
        success(operation, shipment.getGuide() + " insertada en la posicion " + position
                + (invalidated ? ". Carga invalidada (R5)." : "."));
        return "Parada " + shipment.getGuide() + " insertada en la posicion " + position + ".";
    }

    public String removeStop(String guide) {
        String operation = "eliminarParada";
        RouteStop stop = route.findStop(guide);
        if (stop == null) {
            throw fail(operation, "RF-03", "La guia " + guide + " no esta en la ruta.");
        }
        if (stop.getState() == StopState.ENTREGADA) {
            throw fail(operation, "RF-03", "No se puede eliminar una parada ya entregada.");
        }
        if (pending.size() >= pendingCapacity) {
            throw fail(operation, "R1", "La cola de pendientes esta llena; no es posible devolver el envio.");
        }
        Shipment removed = route.remove(guide);
        removed.waitFrom(latestMinute);
        pending.addFront(removed);
        boolean invalidated = invalidateLoadIfNeeded();
        success(operation, guide + " eliminada y devuelta al frente de pendientes"
                + (invalidated ? ". Carga invalidada (R5)." : "."));
        return "Parada " + guide + " eliminada; el envio volvio al frente de pendientes.";
    }

    public List<TraversalRow> traverse(RouteDirection direction) {
        List<RouteStop> stops = direction == RouteDirection.FORWARD
                ? route.firstToLast()
                : route.lastToFirst();
        RouteStop current = route.currentStop();
        List<TraversalRow> rows = new ArrayList<>();
        for (RouteStop stop : stops) {
            rows.add(new TraversalRow(
                    route.positionOf(stop.getShipment().getGuide()),
                    stop.getShipment().getGuide(),
                    stop.getShipment().getDestination(),
                    stop.getState(),
                    stop == current));
        }
        return rows;
    }

    public List<TraversalRow> traverseAndLog(RouteDirection direction) {
        String operation = "recorrerRuta";
        List<TraversalRow> rows = traverse(direction);
        String sense = direction == RouteDirection.FORWARD ? "hacia adelante" : "hacia atras";
        success(operation, "Recorrido " + sense + " con " + rows.size() + " paradas.");
        return rows;
    }

    public LoadValidation validateLoad() {
        if (loadInvalid) {
            return LoadValidation.invalid(
                    "La carga fue invalidada al modificar la ruta (R5). Descargue el maletero y vuelva a cargarlo.");
        }
        if (route.isEmpty()) {
            return LoadValidation.invalid("No hay una ruta abierta para validar.");
        }
        if (trunk.isEmpty()) {
            return LoadValidation.invalid("El maletero esta vacio; primero cargue la ruta.");
        }
        List<RouteStop> stops = route.firstToLast();
        List<RouteStop> remaining = new ArrayList<>();
        for (RouteStop stop : stops) {
            if (stop.getState() != StopState.ENTREGADA) {
                remaining.add(stop);
            }
        }
        List<Shipment> packages = trunk.topToBottom();
        int maximum = Math.max(remaining.size(), packages.size());
        for (int index = 0; index < maximum; index++) {
            String expected = index < remaining.size() ? remaining.get(index).getShipment().getGuide() : null;
            String actual = index < packages.size() ? packages.get(index).getGuide() : null;
            if (!Objects.equals(expected, actual)) {
                return LoadValidation.mismatch(index + 1, expected, actual);
            }
        }
        return LoadValidation.success();
    }

    public LoadValidation validateAndLog() {
        String operation = "validarCarga";
        LoadValidation validation = validateLoad();
        log.add(operation, validation.valid(), validation.message());
        return validation;
    }

    public String authorizeDeparture() {
        String operation = "salirARepartir";
        LoadValidation validation = validateLoad();
        if (!validation.valid()) {
            throw fail(operation, "R4", validation.message());
        }
        success(operation, "Salida autorizada: la carga es el inverso exacto de la ruta.");
        return "Salida autorizada.";
    }

    public String loadTrunk() {
        String operation = "cargarMaletero";
        if (route.isEmpty()) {
            throw fail(operation, "R3", "No hay una ruta abierta para cargar.");
        }
        if (!trunk.isEmpty()) {
            throw fail(operation, "R3", "El maletero no esta vacio; descarguelo antes de recargar.");
        }
        if (route.size() > trunkCapacity) {
            throw fail(operation, "R9", "La ruta supera la capacidad del maletero.");
        }
        List<RouteStop> stops = route.lastToFirst();
        int loadedCount = 0;
        for (RouteStop stop : stops) {
            if (stop.getState() != StopState.ENTREGADA) {
                trunk.push(stop.getShipment());
                loadedCount++;
            }
        }
        if (loadedCount == 0) {
            throw fail(operation, "R3", "No quedan paquetes pendientes en la ruta para cargar.");
        }
        loadInvalid = false;
        metrics.trunkLoaded();
        success(operation, "Maletero cargado con " + trunk.size()
                + " paquetes; el tope es " + trunk.peek().getGuide() + ".");
        return "Maletero cargado; tope: " + trunk.peek().getGuide() + ".";
    }

    public String unloadTrunk() {
        String operation = "descargarMaletero";
        int amount = trunk.size();
        trunk.clear();
        loadInvalid = false;
        success(operation, amount == 0 ? "El maletero ya estaba vacio." : "Maletero descargado (" + amount + ").");
        return amount == 0 ? "El maletero ya estaba vacio." : "Maletero descargado.";
    }

    public String deliverCurrent() {
        String operation = "entregar";
        RouteStop stop = route.currentStop();
        if (stop == null) {
            throw fail(operation, "R6", "No hay una parada actual sobre la cual entregar.");
        }
        if (stop.getState() == StopState.ENTREGADA) {
            throw fail(operation, "R6", "La parada actual ya fue entregada.");
        }
        LoadValidation validation = validateLoad();
        if (!validation.valid()) {
            throw fail(operation, "R4", validation.message());
        }
        Shipment top = trunk.peek();
        if (top == null || !top.getGuide().equalsIgnoreCase(stop.getShipment().getGuide())) {
            throw fail(operation, "R4", "El tope del maletero no corresponde a la parada actual.");
        }
        trunk.pop();
        stop.markDelivered();
        route.advanceCurrent();
        metrics.shipmentDelivered();
        success(operation, stop.getShipment().getGuide() + " entregado. Entregados=" + metrics.snapshot().delivered() + ".");
        return "Envio " + stop.getShipment().getGuide() + " entregado.";
    }

    public String failCurrentDelivery() {
        String operation = "entregaFallida";
        RouteStop stop = route.currentStop();
        if (stop == null) {
            throw fail(operation, "R7", "No hay una parada actual para registrar la falla.");
        }
        Shipment shipment = stop.getShipment();
        if (shipment.getAttempts() >= 2) {
            throw fail(operation, "R8", "El envio ya tiene 2 intentos fallidos; saldra a reintentos al cerrar la ruta.");
        }
        LoadValidation validation = validateLoad();
        if (!validation.valid()) {
            throw fail(operation, "R4", validation.message());
        }
        Shipment top = trunk.peek();
        if (top == null || !top.getGuide().equalsIgnoreCase(shipment.getGuide())) {
            throw fail(operation, "R4", "El tope del maletero no corresponde a la parada actual.");
        }
        shipment.registerAttempt();
        stop.markFailed();
        int movements = trunk.moveTopToBottom();
        metrics.deliveryFailed();
        metrics.reorganizationMovements(movements);
        route.moveCurrentToEnd();
        success(operation, shipment.getGuide() + " fallida (intento " + shipment.getAttempts()
                + "). Movimientos de reorganizacion=" + movements + ".");
        return "Entrega fallida de " + shipment.getGuide() + "; movimientos: " + movements + ".";
    }

    public RouteClosure closeRoute() {
        String operation = "cerrarRuta";
        if (route.isEmpty()) {
            throw fail(operation, "RF-09", "No hay una ruta abierta para cerrar.");
        }
        List<RouteStop> stops = route.firstToLast();
        int deliveredCount = 0;
        int returnedCount = 0;
        int retryCount = 0;
        for (RouteStop stop : stops) {
            if (stop.getState() == StopState.ENTREGADA) {
                deliveredCount++;
            } else if (stop.getShipment().getAttempts() >= 2) {
                retryCount++;
            } else {
                returnedCount++;
            }
        }
        if (pending.size() + returnedCount > pendingCapacity) {
            throw fail(operation, "R1", "La cola de pendientes no tiene espacio para los envios no entregados.");
        }
        if (retries.size() + retryCount > pendingCapacity) {
            throw fail(operation, "R1", "La cola de reintentos no tiene espacio para los envios con 2 intentos.");
        }
        for (RouteStop stop : stops) {
            if (stop.getState() == StopState.ENTREGADA) {
                continue;
            }
            Shipment shipment = stop.getShipment();
            if (shipment.getAttempts() >= 2) {
                retries.addRear(shipment);
                metrics.shipmentSentToRetries();
            } else {
                shipment.waitFrom(latestMinute);
                pending.addRear(shipment);
            }
        }
        metrics.shipmentsReturned(returnedCount);
        route.clear();
        trunk.clear();
        loadInvalid = false;
        success(operation, "Ruta cerrada. Entregados=" + deliveredCount + ", devueltos=" + returnedCount
                + ", a reintentos=" + retryCount + ".");
        return new RouteClosure(deliveredCount, returnedCount, retryCount, pending.size(), retries.size());
    }

    public String loadDemoShipments(List<Shipment> shipments) {
        String operation = "cargarDatosEjemplo";
        if (!pending.isEmpty() || !retries.isEmpty() || !route.isEmpty() || !trunk.isEmpty()) {
            throw fail(operation, "RF-01", "Los datos de ejemplo solo se cargan con el sistema vacio.");
        }
        if (shipments.size() > pendingCapacity) {
            throw fail(operation, "RF-01", "El juego de datos supera la capacidad de la cola de pendientes.");
        }
        List<String> known = new ArrayList<>();
        for (Shipment shipment : shipments) {
            if (shipment.getGuide() == null || shipment.getGuide().trim().isEmpty()
                    || shipment.getDestination() == null || shipment.getDestination().trim().isEmpty()
                    || shipment.getType() == null) {
                throw fail(operation, "RF-01", "El juego de datos contiene un envio incompleto.");
            }
            for (String guide : known) {
                if (guide.equalsIgnoreCase(shipment.getGuide())) {
                    throw fail(operation, "RF-01", "El juego de datos repite la guia " + shipment.getGuide() + ".");
                }
            }
            known.add(shipment.getGuide());
        }
        for (Shipment shipment : shipments) {
            pending.addRear(shipment);
            latestMinute = Math.max(latestMinute, shipment.getReceivedMinute());
            metrics.shipmentReceived();
        }
        success(operation, "Se cargaron " + shipments.size() + " envios de ejemplo.");
        return "Datos de ejemplo cargados: " + shipments.size() + " envios.";
    }

    public List<ShipmentView> pendingView() {
        return views(pending.frontToBack());
    }

    public List<ShipmentView> retriesView() {
        return views(retries.frontToBack());
    }

    public List<RouteStopView> routeView() {
        List<RouteStop> stops = route.firstToLast();
        RouteStop current = route.currentStop();
        List<RouteStopView> views = new ArrayList<>();
        for (int index = 0; index < stops.size(); index++) {
            RouteStop stop = stops.get(index);
            Shipment shipment = stop.getShipment();
            views.add(new RouteStopView(
                    index + 1,
                    shipment.getGuide(),
                    shipment.getDestination(),
                    shipment.getType(),
                    stop.getState(),
                    stop == current,
                    shipment.getAttempts()));
        }
        return views;
    }

    public List<ShipmentView> trunkView() {
        return views(trunk.topToBottom());
    }

    public ShipmentView currentShipmentView() {
        RouteStop current = route.currentStop();
        if (current == null) {
            return null;
        }
        Shipment shipment = current.getShipment();
        return new ShipmentView(
                shipment.getGuide(),
                shipment.getDestination(),
                shipment.getType(),
                shipment.getReceivedMinute(),
                shipment.getAttempts());
    }

    public int pendingSize() {
        return pending.size();
    }

    public int retriesSize() {
        return retries.size();
    }

    public int routeSize() {
        return route.size();
    }

    public int trunkSize() {
        return trunk.size();
    }

    public boolean isTrunkLoaded() {
        return !trunk.isEmpty();
    }

    public boolean isLoadInvalid() {
        return loadInvalid;
    }

    public boolean canDepart() {
        return validateLoad().valid();
    }

    public MetricsSnapshot metricsView() {
        return metrics.snapshot();
    }

    public List<LogEntry> logView() {
        return log.entries();
    }

    private boolean guideExists(String guide) {
        if (pending.containsGuide(guide) || retries.containsGuide(guide) || route.containsGuide(guide)) {
            return true;
        }
        for (Shipment shipment : trunk.topToBottom()) {
            if (shipment.getGuide().equalsIgnoreCase(guide)) {
                return true;
            }
        }
        return false;
    }

    private boolean invalidateLoadIfNeeded() {
        if (!trunk.isEmpty() && !loadInvalid) {
            loadInvalid = true;
            metrics.loadInvalidated();
            return true;
        }
        return false;
    }

    private void recordWait(Shipment shipment) {
        metrics.addWait(shipment.getGuide(), latestMinute - shipment.getWaitingSinceMinute());
    }

    private List<ShipmentView> views(List<Shipment> shipments) {
        List<ShipmentView> views = new ArrayList<>();
        for (Shipment shipment : shipments) {
            views.add(new ShipmentView(
                    shipment.getGuide(),
                    shipment.getDestination(),
                    shipment.getType(),
                    shipment.getReceivedMinute(),
                    shipment.getAttempts()));
        }
        return views;
    }

    private void success(String operation, String detail) {
        log.add(operation, true, detail);
    }

    private DispatchException fail(String operation, String rule, String message) {
        log.add(operation, false, "[" + rule + "] " + message);
        return new DispatchException(rule, message);
    }
}
