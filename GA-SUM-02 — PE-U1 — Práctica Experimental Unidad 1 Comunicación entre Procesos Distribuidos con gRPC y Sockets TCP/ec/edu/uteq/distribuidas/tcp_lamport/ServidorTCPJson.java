package ec.edu.uteq.distribuidas.tcp_lamport;

import com.google.gson.Gson;
import ec.edu.uteq.distribuidas.model.Mensaje;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorTCPJson {

    private static final int PUERTO = 5000;

    public static void main(String[] args) {

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {

            System.out.println("Servidor TCP JSON iniciado en puerto " + PUERTO);

            while (true) {
                Socket cliente = servidor.accept();
                new Thread(() -> manejarCliente(cliente)).start();
            }

        } catch (Exception e) {
            System.out.println("Error en servidor: " + e.getMessage());
        }
    }

    private static void manejarCliente(Socket socket) {

        Gson gson = new Gson();

        try (
                BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                PrintWriter salida = new PrintWriter(
                        socket.getOutputStream(), true)
        ) {

            String json;

            while ((json = entrada.readLine()) != null) {

                Mensaje mensaje = gson.fromJson(json, Mensaje.class);

                System.out.println(
                        "Recibido de " + mensaje.getSender()
                                + " | Lamport: " + mensaje.getTimestamp()
                                + " | Mensaje: " + mensaje.getMessage()
                );

                Mensaje respuesta = new Mensaje(
                        "ServidorTCP",
                        mensaje.getTimestamp() + 1,
                        "ACK recibido: " + mensaje.getMessage()
                );

                salida.println(gson.toJson(respuesta));
            }

        } catch (Exception e) {
            System.out.println("Cliente desconectado.");
        }
    }
}