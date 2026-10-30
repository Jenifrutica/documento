package courier.seed;

import courier.domain.Shipment;
import courier.domain.ShipmentType;

import java.util.List;

public final class DemoShipments {
    private DemoShipments() {
    }

    public static List<Shipment> create() {
        return List.of(
                new Shipment("G-1045", "Calle 45 #12-30", ShipmentType.SOBRE, 470),
                new Shipment("G-1046", "Barrio La Soledad", ShipmentType.PAQUETE, 472),
                new Shipment("G-1047", "Avenida 68 #24-10", ShipmentType.SOBRE, 475),
                new Shipment("G-1048", "Barrio Chapinero Alto", ShipmentType.PAQUETE, 478),
                new Shipment("G-1049", "Carrera 7 #56-20", ShipmentType.SOBRE, 481),
                new Shipment("G-1050", "Barrio Galerias", ShipmentType.PAQUETE, 484),
                new Shipment("G-1051", "Calle 80 #45-12", ShipmentType.SOBRE, 486),
                new Shipment("G-1052", "Barrio Teusaquillo", ShipmentType.PAQUETE, 489),
                new Shipment("G-1053", "Avenida Caracas #33-40", ShipmentType.SOBRE, 492),
                new Shipment("G-1054", "Barrio Restrepo", ShipmentType.PAQUETE, 495));
    }
}
