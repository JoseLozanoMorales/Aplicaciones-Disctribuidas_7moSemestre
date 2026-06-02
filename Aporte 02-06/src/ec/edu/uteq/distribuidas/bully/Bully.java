package src.ec.edu.uteq.distribuidas.bully;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Simulacion del Algoritmo Bully para eleccion de lider .
 * Los procesos se comunican mediante BlockingQueue ( simula mensajes en red).
 */
public class Bully {

    enum TipoMensaje {ELECTION, OK, COORDINATOR}

    record Mensaje(TipoMensaje tipo, int emisor, int destino) {
    }

    static class Proceso implements Runnable {
        final int id;
        final int totalProcesos;
        final BlockingQueue<Mensaje>[] buzones;
        volatile boolean activo;
        volatile int liderActual = -1;
        volatile boolean enEleccion = false;
        static final AtomicInteger liderId = new AtomicInteger(-1);

        @SuppressWarnings(" unchecked ")
        Proceso(int id, int total, BlockingQueue<Mensaje>[]
                buzones, boolean activo) {
            this.id = id;
            this.totalProcesos = total;
            this.buzones = buzones;
            this.activo = activo;
        }

        @Override
        public void run() {
            Thread.currentThread().setName(" Proceso -" + id);
            System.out.printf("[P %d] Iniciado . Activo= %b %n", id, activo);

            while (activo) {
                try {
                    Mensaje msg = buzones[id].poll(500, TimeUnit.MILLISECONDS);
                    if (msg != null) procesarMensaje(msg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.printf("[P %d] Finalizado. %n", id);
        }

        void procesarMensaje(Mensaje msg) {
            switch (msg.tipo()) {
                case ELECTION -> {
                    System.out.printf("[P %d] Recibe ELECTION deP %d %n", id, msg.emisor());
                    // Responder OK al emisor ( soy mas grande )
                    enviar(TipoMensaje.OK, msg.emisor());
                    // Iniciar mi propia eleccion si no latengo activa
                    if (!enEleccion) iniciarEleccion();
                }
                case OK -> {
                    System.out.printf("[P %d] Recibe OK de P %d " + " -> alguien mas grande existe %n", id, msg.emisor());
                    enEleccion = false; // otro tomara elcontrol
                }
                case COORDINATOR -> {
                    liderActual = msg.emisor();
                    liderId.set(liderActual);
                    enEleccion = false;
                    System.out.printf("[P %d] NUEVO LIDER : P %d %n ", id, liderActual);
                }
            }
        }

        void iniciarEleccion() {
            enEleccion = true;
            System.out.printf("[P %d] Iniciando ELECTION ... %n", id);
            boolean hayMasGrande = false;
            // Enviar ELECTION a todos los procesos con ID mayor
            for (int i = id + 1; i < totalProcesos; i++) {
                enviar(TipoMensaje.ELECTION, i);
                hayMasGrande = true;
            }

            // Si no hay nadie mas grande , me proclamo lider
            if (!hayMasGrande) {
                proclamarLider();
            } else {
                // Esperar respuesta OK ( timeout 1.5s)
                CompletableFuture.delayedExecutor(1500, TimeUnit.MILLISECONDS).execute(() -> {
                    if (enEleccion) {
                        // Nadie respondio -> soy el mas grande activo
                        proclamarLider();
                    }
                });
            }
        }

        void proclamarLider() {
            liderActual = id;
            liderId.set(id);
            enEleccion = false;
            System.out.printf("[P %d] Me proclamo LIDER ! " + " Enviando COORDINATOR a todos. %n", id);
            for (int i = 0; i < totalProcesos; i++) {
                if (i != id) enviar(TipoMensaje.COORDINATOR, i);
            }
        }

        void enviar(TipoMensaje tipo, int destino) {
            if (destino < totalProcesos) {
                try {
                    buzones[destino].put(new Mensaje(tipo, id,
                            destino));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        void simularFallo() {
            System.out.printf("[P %d] *** SIMULANDO FALLO *** %n", id);
            activo = false;
        }
    }

    @SuppressWarnings(" unchecked ")
    public static void main(String[] args) throws Exception {
        final int N = 5;
        BlockingQueue<Mensaje>[] buzones = new BlockingQueue[N
                ];
        for (int i = 0; i < N; i++) {
            buzones[i] = new LinkedBlockingQueue<>();
        }

        // Crear procesos ( todos activos inicialmente )
        Proceso[] procs = new Proceso[N];
        for (int i = 0; i < N; i++) {
            procs[i] = new Proceso(i, N, buzones, true);
        }

        ExecutorService exec = Executors.newFixedThreadPool(N);
        for (Proceso p : procs) exec.submit(p);

        // Inicialmente P4 (id =4, el mayor ) es lider
        procs[4].proclamarLider();
        Thread.sleep(1000);

        System.out.println("\n--- Simulando fallo del lider P4 -- -\n");
        procs[4].simularFallo();

        // P1 detecta el fallo e inicia eleccion
        Thread.sleep(500);
        procs[1].iniciarEleccion();

        Thread.sleep(3000);
        System.out.printf(" %nLider final elegido : P %d %n",
                Proceso.liderId.get());
         exec.shutdownNow();
    }
}