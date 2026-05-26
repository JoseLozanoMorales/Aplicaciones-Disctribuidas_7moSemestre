import socket
import json
import time
import csv
import os
import sys

HOST = "localhost"
PORT = 5000

nombre_nodo = sys.argv[1] if len(sys.argv) > 1 else "Nodo1"
reloj_lamport = 0


def enviar_mensaje(sock, numero):
    global reloj_lamport

    reloj_lamport += 1

    mensaje = {
        "sender": nombre_nodo,
        "timestamp": reloj_lamport,
        "message": f"Mensaje {numero} desde {nombre_nodo}"
    }

    inicio = time.perf_counter()

    sock.send(json.dumps(mensaje).encode())

    data = sock.recv(1024)

    fin = time.perf_counter()

    respuesta = json.loads(data.decode())

    reloj_lamport = max(reloj_lamport, respuesta["timestamp"]) + 1

    latencia_ms = (fin - inicio) * 1000

    print(
        f"Enviado: {mensaje['message']} | "
        f"Respuesta: {respuesta['message']} | "
        f"Lamport actual: {reloj_lamport} | "
        f"Latencia: {latencia_ms:.4f} ms"
    )

    return latencia_ms


with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
    sock.connect((HOST, PORT))

    print(f"{nombre_nodo} conectado al servidor TCP.")

    for i in range(1, 21):
        enviar_mensaje(sock, i)
        time.sleep(0.3)

    os.makedirs("data", exist_ok=True)

    latencias = []

    for i in range(1, 101):
        latencia = enviar_mensaje(sock, i)
        latencias.append(latencia)

    promedio = sum(latencias) / len(latencias)

    with open("data/latency_sockets.csv", "w", newline="", encoding="utf-8") as archivo:
        writer = csv.writer(archivo)
        writer.writerow(["envio", "sender", "latencia_ms"])
        for i, latencia in enumerate(latencias, start=1):
            writer.writerow([i, nombre_nodo, latencia])

    print(f"Latencia promedio TCP: {promedio:.4f} ms")
    print("Archivo generado: data/latency_sockets.csv")