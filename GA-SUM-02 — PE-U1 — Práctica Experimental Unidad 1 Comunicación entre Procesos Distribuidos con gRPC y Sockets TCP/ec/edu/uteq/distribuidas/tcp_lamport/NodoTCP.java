package ec.edu.uteq.distribuidas.tcp_lamport;

import com.google.gson.Gson;
import ec.edu.uteq.distribuidas.model.Mensaje;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NodoTCP {

    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;

    private static int relojLamport = 0;

    public static void main(String[] args) {

        String nombreNodo = args.length > 0 ? args[0] : "Nodo1";

        Gson gson = new Gson();

        try (
                Socket socket = new Socket(HOST, PUERTO);

                BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                PrintWriter salida = new PrintWriter(
                        socket.getOutputStream(), true)
        ) {

            System.out.println(nombreNodo + " conectado al servidor TCP.");

            for (int i = 1; i <= 20; i++) {

                relojLamport++;

                Mensaje mensaje = new Mensaje(
                        nombreNodo,
                        relojLamport,
                        "Mensaje " + i + " desde " + nombreNodo
                );

                String json = gson.toJson(mensaje);

                salida.println(json);

                String respuestaJson = entrada.readLine();
                Mensaje respuesta = gson.fromJson(respuestaJson, Mensaje.class);

                relojLamport = Math.max(relojLamport, respuesta.getTimestamp()) + 1;

                System.out.println(
                        "Enviado: " + mensaje.getMessage()
                                + " | Respuesta: " + respuesta.getMessage()
                                + " | Lamport actual: " + relojLamport
                );

                Thread.sleep(300);
            }

        } catch (Exception e) {
            System.out.println("Error en nodo TCP: " + e.getMessage());
        }
    }
}