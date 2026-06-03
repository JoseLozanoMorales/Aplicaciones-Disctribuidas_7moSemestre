package src.ec.edu.uteq.distribuidas.lamport;

import java.util.concurrent.atomic.AtomicInteger;

public class LamportClock {

    private final AtomicInteger contador = new AtomicInteger
            (0);
    private final String nombreProceso;

    public LamportClock(String nombre) {
        this.nombreProceso = nombre;
    }

    public int eventoInterno(String descripcion) {
        int t = contador.incrementAndGet();
        System.out.printf("[ %s] EVENTO INTERNO %-15s C= %d %n", nombreProceso, descripcion, t);
        return t;
    }

    public int enviar(String destino, String mensaje) {
        int t = contador.incrementAndGet();
        System.out.printf("[ %s] ENVIO -> %-8s msg=’ %-20s’ C = %d %n", nombreProceso, destino, mensaje, t);
        return t; // esta marca viaja con el mensaje
    }

    public int recibir(String origen, int marcaMensaje,
                       String mensaje) {
        int nuevo;
        while (true) {
            int actual = contador.get();
            nuevo = Math.max(actual, marcaMensaje) + 1;
            if (contador.compareAndSet(actual, nuevo)) break
                    ;
        }
        System.out.printf("[ %s] RECEPCION <- %-6s msg=’ %-20s’ " + " marcaMsg= %d C= %d %n", nombreProceso, origen, mensaje, marcaMensaje, nuevo);
        return nuevo;
    }

    public int getContador() {
        return contador.get();
    }

    public String getNombre() {
        return nombreProceso;
    }
}