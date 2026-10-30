package courier.domain;

import java.util.ArrayList;
import java.util.List;

//queue
public class ShipmentLine {
    private ShipmentLink front;
    private ShipmentLink rear;
    private int size;

    public void addRear(Shipment shipment) {
        ShipmentLink link = new ShipmentLink(shipment);
        if (rear == null) {
            front = link;
            rear = link;
        } else {
            rear.next = link;
            rear = link;
        }
        size++;
    }

    public void addFront(Shipment shipment) {
        ShipmentLink link = new ShipmentLink(shipment);
        link.next = front;
        front = link;
        if (rear == null) {
            rear = link;
        }
        size++;
    }

    public Shipment pollFront() {
        if (front == null) {
            return null;
        }
        ShipmentLink link = front;
        front = link.next;
        if (front == null) {
            rear = null;
        }
        size--;
        return link.shipment;
    }

    public Shipment peekFront() {
        return front == null ? null : front.shipment;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public boolean containsGuide(String guide) {
        ShipmentLink cursor = front;
        while (cursor != null) {
            if (cursor.shipment.getGuide().equalsIgnoreCase(guide)) {
                return true;
            }
            cursor = cursor.next;
        }
        return false;
    }

    public List<Shipment> frontToBack() {
        List<Shipment> result = new ArrayList<>();
        ShipmentLink cursor = front;
        while (cursor != null) {
            result.add(cursor.shipment);
            cursor = cursor.next;
        }
        return result;
    }
}
