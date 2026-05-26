package ec.edu.uteq.distribuidas.tcp_lamport;

import com.google.gson.Gson;
import ec.edu.uteq.distribuidas.model.Mensaje;

import java.io.*;
import java.net.Socket;

public class MedidorLatenciaTCP {

    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;
    private static int relojLamport = 0;

    public static void main(String[] args) {

        Gson gson = new Gson();

        File carpeta = new File("resultados");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        try (
                Socket socket = new Socket(HOST, PUERTO);

                BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                PrintWriter salida = new PrintWriter(
                        socket.getOutputStream(), true);

                PrintWriter csv = new PrintWriter(
                        new FileWriter("resultados/latencia_tcp.csv"))
        ) {

            csv.println("envio,sender,latencia_ms,lamport");

            double sumaLatencias = 0;

            for (int i = 1; i <= 100; i++) {

                relojLamport++;

                Mensaje mensaje = new Mensaje(
                        "NodoMedidorTCP",
                        relojLamport,
                        "Prueba de latencia TCP " + i
                );

                String json = gson.toJson(mensaje);

                long inicio = System.nanoTime();

                salida.println(json);

                String respuestaJson = entrada.readLine();

                long fin = System.nanoTime();

                Mensaje respuesta = gson.fromJson(respuestaJson, Mensaje.class);

                relojLamport = Math.max(relojLamport, respuesta.getTimestamp()) + 1;

                double latenciaMs = (fin - inicio) / 1_000_000.0;
                sumaLatencias += latenciaMs;

                csv.println(i + "," + mensaje.getSender() + "," + latenciaMs + "," + relojLamport);
            }

            double promedio = sumaLatencias / 100;

            System.out.println("Medición TCP finalizada.");
            System.out.println("Latencia promedio TCP: " + promedio + " ms");
            System.out.println("Archivo generado: resultados/latencia_tcp.csv");

        } catch (Exception e) {
            System.out.println("Error midiendo latencia TCP: " + e.getMessage());
        }
    }
}