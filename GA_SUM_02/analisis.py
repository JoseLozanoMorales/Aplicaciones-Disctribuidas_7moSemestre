import os
import pandas as pd
import matplotlib.pyplot as plt

RUTA_TCP = "data/latency_sockets.csv"
RUTA_GRPC = "data/latency_grpc.csv"
CARPETA_FIGURAS = "docs/figures"

os.makedirs(CARPETA_FIGURAS, exist_ok=True)

tcp = pd.read_csv(RUTA_TCP)
grpc = pd.read_csv(RUTA_GRPC)

tcp["tecnologia"] = "Sockets TCP"
grpc["tecnologia"] = "gRPC"

datos = pd.concat([tcp, grpc], ignore_index=True)

resumen = datos.groupby("tecnologia")["latencia_ms"].agg(
    media="mean",
    mediana="median",
    desviacion_estandar="std",
    percentil_95=lambda x: x.quantile(0.95)
).reset_index()

print("\nResumen estadístico:")
print(resumen)

resumen.to_csv("data/resumen_estadistico.csv", index=False)

plt.figure(figsize=(8, 5))
datos.boxplot(column="latencia_ms", by="tecnologia")
plt.title("Comparación de latencias: TCP vs gRPC")
plt.suptitle("")
plt.xlabel("Tecnología")
plt.ylabel("Latencia (ms)")
plt.savefig("docs/figures/boxplot_latencias.png", dpi=300, bbox_inches="tight")
plt.close()

plt.figure(figsize=(8, 5))
plt.bar(resumen["tecnologia"], resumen["media"])
plt.title("Latencia promedio por tecnología")
plt.xlabel("Tecnología")
plt.ylabel("Latencia promedio (ms)")
plt.savefig("docs/figures/latencia_promedio.png", dpi=300, bbox_inches="tight")
plt.close()

print("\nArchivos generados:")
print("data/resumen_estadistico.csv")
print("docs/figures/boxplot_latencias.png")
print("docs/figures/latencia_promedio.png")