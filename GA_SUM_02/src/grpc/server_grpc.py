import grpc
from concurrent import futures

import messaging_pb2
import messaging_pb2_grpc

HOST = "localhost:5001"

reloj_lamport = 0


class MessagingService(
    messaging_pb2_grpc.MessagingServiceServicer
):

    def EnviarMensaje(self, request, context):

        global reloj_lamport

        reloj_lamport = max(
            reloj_lamport,
            request.timestamp
        ) + 1

        print(
            f"Recibido de {request.sender} "
            f"| Lamport: {request.timestamp} "
            f"| Mensaje: {request.message}"
        )

        return messaging_pb2.Response(
            sender="ServidorGRPC",
            timestamp=reloj_lamport,
            message=f"ACK gRPC: {request.message}"
        )


server = grpc.server(
    futures.ThreadPoolExecutor(max_workers=10)
)

messaging_pb2_grpc.add_MessagingServiceServicer_to_server(
    MessagingService(),
    server
)

server.add_insecure_port(HOST)

server.start()

print(f"Servidor gRPC escuchando en {HOST}")

server.wait_for_termination()