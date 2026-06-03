package src.ec.edu.uteq.distribuidas.aportetcp;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClienteTCP {

    private static final String TOKEN = "TOKEN123";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Seleccione el nodo al que desea conectarse:");
        System.out.println("1. N1 - puerto 9001");
        System.out.println("2. N2 - puerto 9002");
        System.out.println("3. N3 - puerto 9003");
        System.out.print("Opción: ");

        int opcion = Integer.parseInt(scanner.nextLine());

        int puerto;

        switch (opcion) {
            case 1 -> puerto = 9001;
            case 2 -> puerto = 9002;
            case 3 -> puerto = 9003;
            default -> {
                System.out.println("Opción inválida.");
                return;
            }
        }

        String host = "localhost";

        System.out.println("Conectado a " + host + ":" + puerto);
        System.out.println("Formato: TOKEN123|Operacion");
        System.out.println("Ejemplo: TOKEN123|Registrar entrada de paquete 001");
        System.out.println("Escriba SALIR para cerrar el cliente");
        System.out.println("--------------------------------------------");

        while (true) {
            System.out.print("> ");
            String operacion = scanner.nextLine();

            if ("SALIR".equalsIgnoreCase(operacion)) {
                System.out.println("Cliente finalizado.");
                break;
            }

            try (
                    Socket socket = new Socket(host, puerto);
                    PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                salida.println(operacion);

                String respuesta = entrada.readLine();

                if (respuesta != null) {
                    System.out.println("Servidor: " + respuesta);
                } else {
                    System.out.println("Servidor sin respuesta.");
                }

            } catch (IOException e) {
                System.out.println("No se pudo conectar con el nodo en puerto " + puerto);
                System.out.println("Detalle: " + e.getMessage());
                System.out.println("Pruebe conectarse a otro nodo activo.");
            }
        }

        scanner.close();
    }
}