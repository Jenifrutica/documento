package courier.domain;

final class TrunkLink {
    final Shipment shipment;
    TrunkLink below;

    TrunkLink(Shipment shipment, TrunkLink below) {
        this.shipment = shipment;
        this.below = below;
    }
}
