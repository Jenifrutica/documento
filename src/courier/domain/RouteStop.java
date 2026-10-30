package courier.domain;

public class RouteStop {
    private final Shipment shipment;
    private StopState state;

    public RouteStop(Shipment shipment) {
        this.shipment = shipment;
        this.state = StopState.PENDIENTE;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public StopState getState() {
        return state;
    }

    public void markDelivered() {
        state = StopState.ENTREGADA;
    }

    public void markFailed() {
        state = StopState.FALLIDA;
    }
}
