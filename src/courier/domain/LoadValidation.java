package courier.domain;

public record LoadValidation(
        boolean valid,
        int position,
        String expectedGuide,
        String actualGuide,
        String message) {

    public static LoadValidation success() {
        return new LoadValidation(true, 0, null, null, "La carga coincide con el orden inverso de la ruta.");
    }

    public static LoadValidation mismatch(int position, String expectedGuide, String actualGuide) {
        String expected = expectedGuide == null ? "sin paquete" : expectedGuide;
        String actual = actualGuide == null ? "sin paquete" : actualGuide;
        String detail = "La carga no coincide en la posicion " + position
                + ": se esperaba " + expected + " y el tope tiene " + actual + ".";
        return new LoadValidation(false, position, expectedGuide, actualGuide, detail);
    }

    public static LoadValidation invalid(String message) {
        return new LoadValidation(false, 0, null, null, message);
    }
}
