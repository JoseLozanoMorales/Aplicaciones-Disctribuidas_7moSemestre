import grpc
import time
import csv
import os
import sys

import messaging_pb2
import messaging_pb2_grpc

HOST = "localhost:5001"

nombre_nodo = sys.argv[1] if len(sys.argv) > 1 else "NodoGRPC1"

reloj_lamport = 0

channel = grpc.insecure_channel(HOST)

stub = messaging_pb2_grpc.MessagingServiceStub(channel)


def enviar_mensaje(numero):

    global reloj_lamport

    reloj_lamport += 1

    request = messaging_pb2.Request(
        sender=nombre_nodo,
        timestamp=reloj_lamport,
        message=f"Mensaje gRPC {numero} desde {nombre_nodo}"
    )

    inicio = time.perf_counter()

    response = stub.EnviarMensaje(request)

    fin = time.perf_counter()

    reloj_lamport = max(
        reloj_lamport,
        response.timestamp
    ) + 1

    latencia_ms = (fin - inicio) * 1000

    print(
        f"Enviado: {request.message} | "
        f"Respuesta: {response.message} | "
        f"Lamport actual: {reloj_lamport} | "
        f"Latencia: {latencia_ms:.4f} ms"
    )

    return latencia_ms


for i in range(1, 21):
    enviar_mensaje(i)
    time.sleep(0.3)

os.makedirs("data", exist_ok=True)

latencias = []

for i in range(1, 101):
    latencia = enviar_mensaje(i)
    latencias.append(latencia)

promedio = sum(latencias) / len(latencias)

with open("data/latency_grpc.csv", "w", newline="", encoding="utf-8") as archivo:
    writer = csv.writer(archivo)
    writer.writerow(["envio", "sender", "latencia_ms"])

    for i, latencia in enumerate(latencias, start=1):
        writer.writerow([i, nombre_nodo, latencia])

print(f"Latencia promedio gRPC: {promedio:.4f} ms")
print("Archivo generado: data/latency_grpc.csv")