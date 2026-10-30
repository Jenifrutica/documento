package courier.domain;

import java.util.ArrayList;
import java.util.List;

//doubly-linked-list
public class DeliveryRoute {
    private RouteStopLink first;
    private RouteStopLink last;
    private RouteStopLink current;
    private int size;

    public void append(Shipment shipment) {
        RouteStopLink link = new RouteStopLink(new RouteStop(shipment));
        if (last == null) {
            first = link;
            last = link;
        } else {
            link.previous = last;
            last.next = link;
            last = link;
        }
        size++;
        if (current == null) {
            current = link;
        }
    }

    public void insertAt(int position, Shipment shipment) {
        if (position < 1 || position > size + 1) {
            throw new IllegalArgumentException("Posicion fuera de la ruta");
        }
        RouteStopLink link = new RouteStopLink(new RouteStop(shipment));
        if (position == 1) {
            link.next = first;
            if (first != null) {
                first.previous = link;
            } else {
                last = link;
            }
            first = link;
        } else {
            RouteStopLink cursor = linkAt(position - 1);
            link.previous = cursor;
            link.next = cursor.next;
            if (cursor.next != null) {
                cursor.next.previous = link;
            } else {
                last = link;
            }
            cursor.next = link;
        }
        size++;
        if (current == null) {
            current = link;
        }
    }

    public Shipment remove(String guide) {
        RouteStopLink link = findLink(guide);
        if (link == null) {
            return null;
        }
        RouteStopLink next = link.next;
        RouteStopLink previous = link.previous;
        unlink(link);
        if (current == link) {
            current = next != null ? next : previous;
        }
        size--;
        return link.stop.getShipment();
    }

    public boolean move(String guide, int position) {
        if (position < 1 || position > size) {
            throw new IllegalArgumentException("Posicion fuera de la ruta");
        }
        RouteStopLink link = findLink(guide);
        if (link == null) {
            return false;
        }
        int originalPosition = positionOf(link);
        if (originalPosition == position) {
            return true;
        }
        unlink(link);
        size--;
        insertLinkAt(position, link);
        size++;
        return true;
    }

    public RouteStop currentStop() {
        return current == null ? null : current.stop;
    }

    public void advanceCurrent() {
        if (current != null) {
            current = current.next;
        }
    }

    public RouteStop moveCurrentToEnd() {
        if (current == null) {
            return null;
        }
        if (size == 1) {
            return current.stop;
        }
        RouteStopLink moving = current;
        RouteStopLink next = nextPendingAfter(moving);
        if (next == null) {
            next = firstPendingBefore(moving);
        }
        unlink(moving);
        size--;
        moving.previous = null;
        moving.next = null;
        if (last == null) {
            first = moving;
            last = moving;
        } else {
            moving.previous = last;
            last.next = moving;
            last = moving;
        }
        size++;
        current = next == null ? moving : next;
        return current.stop;
    }

    public RouteStop findStop(String guide) {
        RouteStopLink link = findLink(guide);
        return link == null ? null : link.stop;
    }

    public RouteStop stopAt(int position) {
        RouteStopLink link = linkAt(position);
        return link == null ? null : link.stop;
    }

    public int positionOf(String guide) {
        RouteStopLink link = findLink(guide);
        return link == null ? 0 : positionOf(link);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsGuide(String guide) {
        return findLink(guide) != null;
    }

    public List<RouteStop> firstToLast() {
        List<RouteStop> result = new ArrayList<>();
        RouteStopLink cursor = first;
        while (cursor != null) {
            result.add(cursor.stop);
            cursor = cursor.next;
        }
        return result;
    }

    public List<RouteStop> lastToFirst() {
        List<RouteStop> result = new ArrayList<>();
        RouteStopLink cursor = last;
        while (cursor != null) {
            result.add(cursor.stop);
            cursor = cursor.previous;
        }
        return result;
    }

    public void clear() {
        first = null;
        last = null;
        current = null;
        size = 0;
    }

    private void insertLinkAt(int position, RouteStopLink link) {
        if (position == 1) {
            link.previous = null;
            link.next = first;
            if (first != null) {
                first.previous = link;
            } else {
                last = link;
            }
            first = link;
            return;
        }
        RouteStopLink cursor = linkAt(position - 1);
        link.previous = cursor;
        link.next = cursor.next;
        if (cursor.next != null) {
            cursor.next.previous = link;
        } else {
            last = link;
        }
        cursor.next = link;
    }

    private void unlink(RouteStopLink link) {
        if (link.previous != null) {
            link.previous.next = link.next;
        } else {
            first = link.next;
        }
        if (link.next != null) {
            link.next.previous = link.previous;
        } else {
            last = link.previous;
        }
        link.previous = null;
        link.next = null;
    }

    private RouteStopLink linkAt(int position) {
        if (position < 1 || position > size) {
            return null;
        }
        RouteStopLink cursor = first;
        for (int index = 1; index < position; index++) {
            cursor = cursor.next;
        }
        return cursor;
    }

    private int positionOf(RouteStopLink link) {
        int position = 1;
        RouteStopLink cursor = first;
        while (cursor != null) {
            if (cursor == link) {
                return position;
            }
            cursor = cursor.next;
            position++;
        }
        return 0;
    }

    private RouteStopLink nextPendingAfter(RouteStopLink reference) {
        RouteStopLink cursor = reference.next;
        while (cursor != null) {
            if (cursor.stop.getState() != StopState.ENTREGADA) {
                return cursor;
            }
            cursor = cursor.next;
        }
        return null;
    }

    private RouteStopLink firstPendingBefore(RouteStopLink reference) {
        RouteStopLink cursor = first;
        while (cursor != null && cursor != reference) {
            if (cursor.stop.getState() != StopState.ENTREGADA) {
                return cursor;
            }
            cursor = cursor.next;
        }
        return null;
    }

    private RouteStopLink findLink(String guide) {
        RouteStopLink cursor = first;
        while (cursor != null) {
            if (cursor.stop.getShipment().getGuide().equalsIgnoreCase(guide)) {
                return cursor;
            }
            cursor = cursor.next;
        }
        return null;
    }
}
