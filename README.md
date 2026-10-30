# Despacho de mensajería urbana en moto: ruta y maletero

Aplicación de escritorio en Java para armar la ruta de un mensajero urbano, cargar el maletero de la moto y verificar que la carga sea exactamente el inverso del orden de la ruta antes de salir a repartir.

El proyecto usa únicamente Java 17 y la biblioteca estándar (Swing para la interfaz). No requiere Maven, Gradle ni dependencias externas.

## Qué hace el sistema

- Mantiene una **cola de pendientes** con los envíos recibidos y una **cola de reintentos** para los envíos con dos intentos fallidos.
- Arma una **ruta** como lista doblemente enlazada de paradas, con apuntador a la parada actual.
- Carga el **maletero** como una pila LIFO, recorriendo la ruta desde la última parada hasta la primera.
- Valida que el tope del maletero corresponda a la primera parada de la ruta, sin vaciar la pila.
- Registra entregas y entregas fallidas, reorganizando el maletero cuando el destinatario no está.
- Cierra la ruta, devuelve los envíos no entregados a la cola correspondiente y calcula las métricas del reporte.
- Muestra en pantalla el estado de las tres estructuras, los errores con la regla violada y una bitácora cronológica.

## Requisitos

- JDK 17 o superior (`javac` y `java`).
- Sistema con entorno gráfico para ejecutar la interfaz Swing.
- No se necesitan bibliotecas externas.

## Instalación

No hay dependencias que instalar. Basta con tener el JDK 17 disponible en el `PATH`. El proyecto se compila directamente con `javac`.

## Uso

1. **Bandeja de envíos**: registre envíos con guía, destino, tipo (`SOBRE` o `PAQUETE`) y minuto de recepción. También puede cargar el juego de datos de prueba con el botón `Cargar datos de ejemplo` y armar la ruta.
2. **Editor de ruta**: mueva, inserte o elimine paradas. Use `Recorrer hacia adelante` y `Recorrer hacia atrás` para ver la lista en ambos sentidos con la parada actual marcada.
3. **Maletero**: cargue la ruta, valide la carga, entregue la parada actual o registre una entrega fallida. El indicador verde/rojo muestra en vivo si la salida está habilitada o bloqueada.
4. **Cierre y reportes**: cierre la ruta y consulte las métricas, la espera por envío y la bitácora de operaciones.

Los contadores de capacidad se configuran en la bandeja de envíos. El valor inicial del maletero es 8 paradas y el de cada cola es 30 envíos.

## Reglas implementadas

- **R1**: los envíos se atienden desde el frente de la cola de pendientes; no se extraen del medio.
- **R2**: armar la ruta toma hasta `N` envíos del frente de pendientes, en el mismo orden, y los convierte en paradas.
- **R3**: el maletero se carga desde la última parada hacia la primera; el tope queda siendo la primera parada.
- **R4**: antes de salir se valida que el maletero sea el inverso de la ruta. La validación no modifica la pila.
- **R5**: mover, insertar o eliminar una parada después de cargar invalida la carga. La salida queda bloqueada hasta descargar y volver a cargar.
- **R6**: solo se entrega en la parada actual; se desapila el tope y el apuntador avanza.
- **R7**: una entrega fallida pasa el paquete del tope al fondo usando un maletero auxiliar, conserva el orden relativo de los demás paquetes, marca la parada como `FALLIDA` y la reubica al final de la ruta. Cada movimiento de reorganización se cuenta.
- **R8**: al cerrar la ruta, los envíos con dos intentos fallidos pasan a la cola de reintentos.
- **R9**: la ruta no supera la capacidad del maletero y un envío no puede estar en dos lugares a la vez.

## Decisiones documentadas

- **Maletero no vacío al cargar**: `Cargar maletero` se rechaza si la pila tiene paquetes. El usuario debe usar `Descargar maletero` antes de una nueva carga (RF-05).
- **Modificación de ruta con carga**: la carga se marca como inválida, pero no se vacía automáticamente. El botón de salida se bloquea y se exige descargar y recargar (R5).
- **Inserción de paradas**: la pantalla del editor incluye insertar una parada tomando el envío del frente de pendientes; es la única forma compatible con R1.
- **Entregas previas**: después de una entrega, la validación compara el maletero con las paradas aún no entregadas, en el orden de la ruta. Antes de la primera entrega coincide con la ruta completa.
- **Tiempo de espera**: se mide desde el ingreso del envío a pendientes hasta su asignación a la ruta. Se usa como reloj de la simulación el mayor minuto de recepción registrado.
- **Datos de ejemplo**: el botón `Cargar datos de ejemplo` usa un archivo semilla separado en `src/courier/seed/DemoShipments.java` para cumplir RFE-10. Solo se puede cargar con el sistema vacío y no se usa automáticamente.

## Estructura

```text
src/
  courier/
    Application.java                 Punto de entrada Swing
    domain/
      DispatchService.java           Fachada de operaciones y reglas de negocio
      DeliveryRoute.java             Ruta como lista doblemente enlazada
      ShipmentLine.java              Cola FIFO propia para pendientes y reintentos
      DeliveryTrunk.java             Pila LIFO propia del maletero
      Shipment.java, RouteStop.java  Modelo de envíos y paradas
      DispatchMetrics.java           Métricas del reporte
      OperationLog.java              Bitácora cronológica
      ...                            Vistas inmutables y resultados
    seed/
      DemoShipments.java             Juego de datos de prueba de RFE-10
    ui/
      DispatchWindow.java            Ventana con las cuatro secciones
      ShipmentTrayPanel.java         Bandeja de envíos y colas
      RouteEditorPanel.java          Editor y recorrido de la ruta
      TrunkPanel.java                Maletero, ruta y validación
      ReportsPanel.java              Cierre, métricas y bitácora
      Palette.java                   Colores y estilos neutros
test/
  courier/verification/
    DispatchVerification.java        Verificación de reglas sin dependencias
```

## Métricas del reporte

- Envíos recibidos, asignados, entregados y devueltos a la cola al cerrar.
- Entregas fallidas y movimientos de reorganización del maletero.
- Cargas invalidadas por modificaciones de ruta (R5) y recargas realizadas.
- Espera de cada envío en pendientes, con promedio y máximo.
- Envíos con dos intentos que pasaron a la cola de reintentos (R8).

## Comandos (Git Bash)

```bash
# Dependencias: no hay. Verifique el JDK 17.
java -version
javac -version

# Compilar el proyecto
rm -rf out
javac -d out $(find src -name "*.java")

# Ejecutar la aplicación
java -cp out courier.Application

# Compilar y ejecutar las verificaciones
javac -cp out -d out $(find test -name "*.java")
java -cp out courier.verification.DispatchVerification
```
