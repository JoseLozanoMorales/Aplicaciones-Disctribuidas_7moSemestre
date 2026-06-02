package src.ec.edu.uteq.distribuidas.lamport;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulacionLamport {

    public static <LamportClock> void main(String[] args) throws InterruptedException, ExecutionException {
        // Canales de comunicacion ( BlockingQueue simula elpaso de mensajes )
        BlockingQueue<int[]> p1aP2 = new LinkedBlockingQueue<>();
        BlockingQueue<int[]> p2aP3 = new LinkedBlockingQueue<>();
        BlockingQueue<int[]> p3aP1 = new LinkedBlockingQueue<>();

        ExecutorService exec = Executors.newFixedThreadPool(3);
        CountDownLatch inicio = new CountDownLatch(3);

        System.out.println(" === Simulacion de Relojes deLamport === ");
        System.out.println(" Formato : [ Proceso ] TIPO ... C=valor \n");

        // Proceso P1
        Future<?> f1 = exec.submit(() -> {
            LamportClock c = new LamportClock("P1");
            inicio.countDown();
            try {
                inicio.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            c.eventoInterno("a");
            int t = c.enviar("P2", " Hola P2");
            p1aP2.offer(new int[]{t}); // enviar marca alcanal P1 ->P2
            c.eventoInterno("e");
            // esperar mensaje de P3
            try {
                int[] marcaP3 = p3aP1.take();
                c.recibir("P3", marcaP3[0], " Respuesta de P3 ");
            } catch (InterruptedException e) {
                Thread.
                        currentThread().interrupt();
            }
            c.eventoInterno("f");
        });
        // Proceso P2
        Future<?> f2 = exec.submit(() -> {
            LamportClock c = new LamportClock("P2");
            inicio.countDown();
            try {
                inicio.await();
            } catch (
                    InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            c.eventoInterno("c");
            // esperar mensaje de P1
            try {
                int[] marcaP1 = p1aP2.take();
                c.recibir("P1", marcaP1[0], " Hola P2");
                c.eventoInterno("g");
                int t = c.enviar("P3", " Hola P3");
                p2aP3.offer(new int[]{t});
                c.eventoInterno("h");
            } catch (InterruptedException e) {
                Thread.
                        currentThread().interrupt();
            }
        });

        // Proceso P3
        Future<?> f3 = exec.submit(() -> {
            LamportClock c = new LamportClock("P3");
            inicio.countDown();
            try {
                inicio.await();
            } catch (
                    InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            c.eventoInterno("i");
            try {
                int[] marcaP2 = p2aP3.take();
                c.recibir("P2", marcaP2[0], " Hola P3");
                c.eventoInterno("k");
                int t = c.enviar("P1", " Hola P1");
                p3aP1.offer(new int[]{t});
            } catch (InterruptedException e) {
                Thread.
                        currentThread().interrupt();
            }
        });

        f1.get();
        f2.get();
        f3.get();
        exec.shutdown();
        System.out.println("\n === Simulacion completada === "
        );
    }
}
