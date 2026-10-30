package courier.domain;

final class RouteStopLink {
    final RouteStop stop;
    RouteStopLink previous;
    RouteStopLink next;

    RouteStopLink(RouteStop stop) {
        this.stop = stop;
    }
}
