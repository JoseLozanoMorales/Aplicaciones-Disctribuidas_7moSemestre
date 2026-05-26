import socket
import json

HOST = "localhost"
PORT = 5000

reloj_lamport = 0

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind((HOST, PORT))
server.listen()

print(f"Servidor TCP escuchando en {HOST}:{PORT}")

while True:
    conn, addr = server.accept()

    with conn:
        print(f"Conexión desde {addr}")

        while True:

            data = conn.recv(1024)

            if not data:
                break

            mensaje = json.loads(data.decode())

            reloj_lamport = max(
                reloj_lamport,
                mensaje["timestamp"]
            ) + 1

            print(
                f"Recibido de {mensaje['sender']} "
                f"| Lamport: {mensaje['timestamp']} "
                f"| Mensaje: {mensaje['message']}"
            )

            respuesta = {
                "sender": "ServidorTCP",
                "timestamp": reloj_lamport,
                "message": f"ACK recibido: {mensaje['message']}"
            }

            conn.send(json.dumps(respuesta).encode())