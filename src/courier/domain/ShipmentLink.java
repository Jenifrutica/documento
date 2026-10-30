package courier.domain;

final class ShipmentLink {
    final Shipment shipment;
    ShipmentLink next;

    ShipmentLink(Shipment shipment) {
        this.shipment = shipment;
    }
}
