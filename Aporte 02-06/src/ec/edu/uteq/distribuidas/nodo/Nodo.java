package src.ec.edu.uteq.distribuidas.nodo;

import src.ec.edu.uteq.distribuidas.evento.Evento;
import src.ec.edu.uteq.distribuidas.lamport.LamportClock;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class Nodo {

    private static final String TOKEN_VALIDO = "TOKEN123";

    private static final int INTERVALO_HEARTBEAT_MS = 2000;
    private static final int INTERVALO_MONITOREO_MS = 3000;
    private static final int TIMEOUT_HEARTBEAT_MS = 6000;

    private final int id;
    private final int puerto;
    private final LamportClock reloj;
    private final Map<Integer, NodoInfo> nodos;
    private final List<Evento> logEventos;

    private volatile boolean ejecutando = true;
    private volatile int coordinadorId;

    private final ExecutorService poolConexiones = Executors.newCachedThreadPool();
    private final ScheduledExecutorService tareasPeriodicas = Executors.newScheduledThreadPool(2);

    public Nodo(int id, int puerto, LamportClock reloj, Map<Integer, NodoInfo> nodos, List<Evento> logEventos) {
        this.id = id;
        this.puerto = puerto;
        this.reloj = reloj;
        this.nodos = new ConcurrentHashMap<>(nodos);
        this.logEventos = logEventos;
        this.coordinadorId = calcularNodoActivoConMayorId();

        System.out.println("Nodo N" + id + " iniciado. Coordinador actual: N" + coordinadorId);
    }

    public void iniciarServidor() {
        iniciarHeartbeats();
        iniciarMonitorDeFallos();

        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Servidor TCP de N" + id + " escuchando en puerto " + puerto);

            while (ejecutando) {
                Socket cliente = servidor.accept();
                poolConexiones.submit(() -> manejarConexion(cliente));
            }

        } catch (IOException e) {
            System.out.println("Error en el servidor de N" + id + ": " + e.getMessage());
        } finally {
            detener();
        }
    }

    private void manejarConexion(Socket socket) {
        try (
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String mensaje = entrada.readLine();

            if (mensaje == null || mensaje.isBlank()) {
                salida.println("ERROR: mensaje vacío");
                return;
            }

            String[] partes = mensaje.split("\\|");

            if (partes.length < 2) {
                salida.println("ERROR: formato inválido. Use TOKEN123|Operacion");
                return;
            }

            String token = partes[0];

            if (!TOKEN_VALIDO.equals(token)) {
                salida.println("ERROR:N" + id + ": token inválido");
                return;
            }

            if ("HEARTBEAT".equals(partes[1])) {
                procesarHeartbeat(partes);
                salida.println("OK:HEARTBEAT_RECIBIDO:N" + id);
                return;
            }

            procesarOperacionCliente(partes, salida);

        } catch (IOException e) {
            System.out.println("Error manejando conexión en N" + id + ": " + e.getMessage());
        }
    }

    private void procesarOperacionCliente(String[] partes, PrintWriter salida) {
        String operacion = reconstruirOperacion(partes);

        int marcaLamport = reloj.eventoInterno("N" + id + " registra operación: " + operacion);

        Evento evento = new Evento(
                marcaLamport,
                id,
                "Operacion: " + operacion
        );

        synchronized (logEventos) {
            logEventos.add(evento);
            Collections.sort(logEventos);
        }

        imprimirLogOrdenado();

        salida.println(
                "OK:N" + id +
                        ": operación registrada: Lamport=" + marcaLamport +
                        ": coordinador=N" + coordinadorId
        );
    }

    private String reconstruirOperacion(String[] partes) {
        if (partes.length == 2) {
            return partes[1];
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < partes.length; i++) {
            if (i > 1) {
                sb.append("|");
            }
            sb.append(partes[i]);
        }

        return sb.toString();
    }

    private void procesarHeartbeat(String[] partes) {
        if (partes.length < 4) {
            System.out.println("Heartbeat inválido recibido en N" + id);
            return;
        }

        try {
            int nodoOrigen = Integer.parseInt(partes[2]);
            int marcaLamportRecibida = Integer.parseInt(partes[3]);

            reloj.recibir(
                    "N" + nodoOrigen,
                    marcaLamportRecibida,
                    "Heartbeat recibido"
            );

            NodoInfo info = nodos.get(nodoOrigen);

            if (info != null) {
                boolean estabaCaido = !info.isActivo();

                info.actualizarHeartbeat();

                if (estabaCaido) {
                    System.out.println("N" + id + " detecta que N" + nodoOrigen + " volvió a estar activo.");
                    ejecutarEleccionBully();
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Heartbeat con datos inválidos recibido en N" + id);
        }
    }

    private void iniciarHeartbeats() {
        tareasPeriodicas.scheduleAtFixedRate(() -> {
            for (NodoInfo nodoDestino : nodos.values()) {
                enviarHeartbeat(nodoDestino);
            }
        }, 1000, INTERVALO_HEARTBEAT_MS, TimeUnit.MILLISECONDS);
    }

    private void enviarHeartbeat(NodoInfo nodoDestino) {
        int marcaLamport = reloj.enviar(
                "N" + nodoDestino.getId(),
                "Heartbeat"
        );

        String mensaje = TOKEN_VALIDO + "|HEARTBEAT|" + id + "|" + marcaLamport;

        try (
                Socket socket = new Socket(nodoDestino.getHost(), nodoDestino.getPuerto());
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            salida.println(mensaje);
            entrada.readLine();

        } catch (IOException e) {
            // No marcamos caído aquí inmediatamente para evitar falsos positivos.
            // El monitor de fallos lo hará si pasa el timeout.
        }
    }

    private void iniciarMonitorDeFallos() {
        tareasPeriodicas.scheduleAtFixedRate(() -> {
            boolean huboCambio = false;

            long ahora = System.currentTimeMillis();

            for (NodoInfo info : nodos.values()) {
                if (info.isActivo()) {
                    long diferencia = ahora - info.getUltimoHeartbeat();

                    if (diferencia > TIMEOUT_HEARTBEAT_MS) {
                        info.marcarCaido();
                        huboCambio = true;

                        System.out.println(
                                "N" + id + " detectó caída de N" + info.getId() +
                                        " por falta de heartbeat."
                        );
                    }
                }
            }

            if (huboCambio) {
                ejecutarEleccionBully();
            }

        }, 3000, INTERVALO_MONITOREO_MS, TimeUnit.MILLISECONDS);
    }

    private void ejecutarEleccionBully() {
        int nuevoCoordinador = calcularNodoActivoConMayorId();

        if (nuevoCoordinador != coordinadorId) {
            System.out.println(
                    "Elección Bully en N" + id +
                            ": coordinador anterior=N" + coordinadorId +
                            ", nuevo coordinador=N" + nuevoCoordinador
            );

            coordinadorId = nuevoCoordinador;
        } else {
            System.out.println(
                    "Elección Bully en N" + id +
                            ": se mantiene coordinador=N" + coordinadorId
            );
        }
    }

    private int calcularNodoActivoConMayorId() {
        int mayor = id;

        for (NodoInfo info : nodos.values()) {
            if (info.isActivo() && info.getId() > mayor) {
                mayor = info.getId();
            }
        }

        return mayor;
    }

    private void imprimirLogOrdenado() {
        System.out.println("Log ordenado de N" + id + ":");

        synchronized (logEventos) {
            for (Evento evento : logEventos) {
                System.out.println(evento);
            }
        }
    }

    public void detener() {
        ejecutando = false;
        tareasPeriodicas.shutdownNow();
        poolConexiones.shutdownNow();
    }
}