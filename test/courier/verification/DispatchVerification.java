package courier.verification;

import courier.domain.DispatchException;
import courier.domain.DispatchService;
import courier.domain.RouteClosure;
import courier.domain.ShipmentType;

public final class DispatchVerification {
    private DispatchVerification() {
    }

    public static void main(String[] args) {
        buildsRouteFromTheFront();
        loadsTrunkInInverseOrder();
        invalidatesLoadAfterRouteChange();
        deletesStopToTheFrontOfPending();
        deliversOnlyFromTheTop();
        reorganizesFailedDelivery();
        sendsTwoAttemptsToRetries();
        rejectsDuplicatedGuide();
        closesRouteAndReturnsUndelivered();
        System.out.println("OK: verificaciones de reglas de negocio completadas.");
    }

    private static DispatchService serviceWith(int count) {
        DispatchService service = new DispatchService();
        service.configureCapacities(Math.max(count, 1), 30);
        for (int index = 1; index <= count; index++) {
            service.receiveShipment("T-" + index, "Destino " + index, ShipmentType.SOBRE, index * 5);
        }
        return service;
    }

    private static void buildsRouteFromTheFront() {
        DispatchService service = serviceWith(5);
        service.configureCapacities(2, 30);
        service.buildRoute();
        check(service.routeSize() == 2, "La ruta debe tomar hasta N envios.");
        check(service.routeView().get(0).guide().equals("T-1"), "La primera parada debe ser el frente.");
        check(service.pendingView().get(0).guide().equals("T-3"), "El resto queda en pendientes.");
    }

    private static void loadsTrunkInInverseOrder() {
        DispatchService service = serviceWith(3);
        service.buildRoute();
        service.loadTrunk();
        check(service.validateLoad().valid(), "La carga inicial debe ser valida.");
        check(service.trunkView().get(0).guide().equals("T-1"), "El tope debe ser la primera parada.");
        check(service.trunkSize() == 3, "La validacion no debe vaciar el maletero.");
        service.validateLoad();
        check(service.trunkSize() == 3, "La validacion no debe vaciar el maletero.");
    }

    private static void invalidatesLoadAfterRouteChange() {
        DispatchService service = serviceWith(3);
        service.buildRoute();
        service.loadTrunk();
        service.moveStop("T-2", 1);
        check(!service.validateLoad().valid(), "Mover una parada debe invalidar la carga (R5).");
        check(!service.canDepart(), "La salida debe quedar bloqueada.");
        check(service.metricsView().invalidations() == 1, "Debe contarse la invalidacion.");
        service.unloadTrunk();
        service.loadTrunk();
        check(service.validateLoad().valid(), "Despues de recargar la salida debe habilitarse.");
        check(service.metricsView().reloads() == 2, "Deben contarse las dos cargas.");
    }

    private static void deletesStopToTheFrontOfPending() {
        DispatchService service = serviceWith(3);
        service.buildRoute();
        service.removeStop("T-2");
        check(service.pendingView().get(0).guide().equals("T-2"), "La eliminacion devuelve al frente.");
        check(service.routeSize() == 2, "La ruta debe quedar con dos paradas.");
    }

    private static void deliversOnlyFromTheTop() {
        DispatchService service = serviceWith(3);
        service.buildRoute();
        service.loadTrunk();
        service.deliverCurrent();
        check(service.metricsView().delivered() == 1, "Debe registrarse una entrega.");
        check(service.trunkView().get(0).guide().equals("T-2"), "El tope debe avanzar a la siguiente.");
        check(service.currentShipmentView().guide().equals("T-2"), "El apuntador debe avanzar.");
        check(service.validateLoad().valid(), "La carga restante sigue siendo valida.");
    }

    private static void reorganizesFailedDelivery() {
        DispatchService service = serviceWith(4);
        service.buildRoute();
        service.loadTrunk();
        service.deliverCurrent();
        service.failCurrentDelivery();
        check(service.metricsView().failed() == 1, "Debe registrarse una falla.");
        check(service.trunkView().get(0).guide().equals("T-3"), "El tope debe quedar en el siguiente paquete.");
        check(service.trunkView().get(service.trunkSize() - 1).guide().equals("T-2"),
                "El paquete fallido debe quedar al fondo.");
        check(service.routeView().get(service.routeSize() - 1).guide().equals("T-2"),
                "La parada fallida debe quedar al final de la ruta.");
        check(service.validateLoad().valid(), "La reorganizacion debe conservar la correspondencia.");
        check(service.metricsView().reorganizations() > 0, "Deben contarse los movimientos.");
    }

    private static void sendsTwoAttemptsToRetries() {
        DispatchService service = serviceWith(2);
        service.buildRoute();
        service.loadTrunk();
        service.failCurrentDelivery();
        service.deliverCurrent();
        service.failCurrentDelivery();
        RouteClosure closure = service.closeRoute();
        check(closure.sentToRetries() == 1, "El envio con dos intentos pasa a reintentos.");
        check(service.retriesSize() == 1, "La cola de reintentos debe tener el envio.");
        check(service.metricsView().sentToRetries() == 1, "Debe contarse el paso a reintentos.");
    }

    private static void rejectsDuplicatedGuide() {
        DispatchService service = serviceWith(1);
        boolean rejected = false;
        try {
            service.receiveShipment("T-1", "Otro destino", ShipmentType.PAQUETE, 50);
        } catch (DispatchException exception) {
            rejected = exception.getRule().equals("RF-01");
        }
        check(rejected, "La guia duplicada debe rechazarse citando RF-01.");
    }

    private static void closesRouteAndReturnsUndelivered() {
        DispatchService service = serviceWith(3);
        service.buildRoute();
        service.loadTrunk();
        service.deliverCurrent();
        RouteClosure closure = service.closeRoute();
        check(closure.returnedToPending() == 2, "Los no entregados vuelven a pendientes.");
        check(service.routeSize() == 0, "La ruta debe quedar vacia.");
        check(service.trunkSize() == 0, "El maletero debe quedar vacio.");
        check(service.pendingSize() == 2, "Los envios deben estar de nuevo en pendientes.");
        check(service.metricsView().returned() == 2, "Debe contarse el retorno a la cola.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
