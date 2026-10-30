package courier.domain;

import java.util.ArrayList;
import java.util.List;

//stack
public class DeliveryTrunk {
    private TrunkLink top;
    private int size;

    public void push(Shipment shipment) {
        top = new TrunkLink(shipment, top);
        size++;
    }

    public Shipment pop() {
        if (top == null) {
            return null;
        }
        Shipment shipment = top.shipment;
        top = top.below;
        size--;
        return shipment;
    }

    public Shipment peek() {
        return top == null ? null : top.shipment;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public List<Shipment> topToBottom() {
        List<Shipment> result = new ArrayList<>();
        TrunkLink cursor = top;
        while (cursor != null) {
            result.add(cursor.shipment);
            cursor = cursor.below;
        }
        return result;
    }

    public int moveTopToBottom() {
        if (size < 2) {
            return 0;
        }
        DeliveryTrunk auxiliary = new DeliveryTrunk();
        int movements = 0;
        Shipment failing = pop();
        while (!isEmpty()) {
            auxiliary.push(pop());
            movements++;
        }
        push(failing);
        movements++;
        while (!auxiliary.isEmpty()) {
            push(auxiliary.pop());
            movements++;
        }
        return movements;
    }

    public void clear() {
        top = null;
        size = 0;
    }
}
