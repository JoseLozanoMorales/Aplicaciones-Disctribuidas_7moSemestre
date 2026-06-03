1. Ante una interrupción de comunicación entre dos nodos, ¿qué propiedad del teorema CAP privilegia su implementación y por qué? Justifíquelo con el comportamiento observado en su prototipo.

Ante una interrupción de comunicación, el sistema privilegia la disponibilidad y la tolerancia a particiones. Si un nodo cae, los demás continúan funcionando y detectan la falla mediante heartbeats.

Por ejemplo, cuando N3 se apagó, N1 y N2 detectaron la caída y eligieron a N2 como nuevo coordinador:

N1 detectó caída de N3 por falta de heartbeat.
Elección Bully en N1: coordinador anterior=N3, nuevo coordinador=N2

Esto demuestra que el sistema sigue disponible mientras exista al menos un nodo activo. Sin embargo, no garantiza consistencia fuerte, porque cada nodo mantiene su propio log local y no existe todavía un consenso formal entre réplicas. Por eso, el prototipo se acerca más a un modelo AP.


2. ¿Qué falacias de la computación distribuida tuvo que considerar al delimitar los mensajes y al definir los tiempos de espera de los latidos?

La implementación tuvo que considerar que la red no siempre es confiable. Por eso los nodos no asumen que otro nodo está activo permanentemente, sino que lo verifican mediante heartbeats.

También se consideró que la latencia no es cero. Por eso un nodo no se marca como caído con un solo fallo de conexión, sino después de superar un tiempo de espera.

Además, se evitó asumir que existe un estado global perfecto. Cada nodo tiene su propia vista del sistema y puede detectar fallos en momentos distintos.

Los mensajes usados fueron delimitados para diferenciar operaciones de cliente y mensajes internos:

TOKEN123|Operacion
TOKEN123|HEARTBEAT|idNodoOrigen|marcaLamport


3. ¿Qué tipos de transparencia (ubicación, acceso, fallos, replicación) ofrece o no ofrece su solución? Argumente cada caso.

La solución ofrece transparencia de acceso parcial, porque el cliente puede enviar el mismo formato de operación a cualquier nodo:

TOKEN123|Registrar entrada de paquete 001

También ofrece transparencia de fallos parcial, porque los nodos detectan caídas automáticamente y eligen un nuevo coordinador.

Sin embargo, no hay transparencia total para el cliente. Si el cliente intenta conectarse a un nodo apagado, la conexión falla:

No se pudo conectar con el nodo en puerto 9001
Detalle: Connection refused: connect

La transparencia de replicación también es parcial, porque existen tres nodos réplica, pero no se replica completamente el log entre todos. LamportClock.java y Evento.java ayudan a ordenar eventos, pero no reemplazan un protocolo de consenso.


4. Proponga un acuerdo de nivel de servicio (SLA) de disponibilidad para este sistema y calcule el tiempo de inactividad anual admisible que implicaría.

Para este prototipo se propone un SLA de 99.5% anual, ya que el sistema tolera la caída de nodos, pero todavía no tiene balanceador automático, replicación fuerte ni consenso formal.

Un año tiene:

365 × 24 = 8760 horas

Si la disponibilidad es 99.5%, la inactividad permitida es 0.5%:

8760 × 0.005 = 43.8 horas

Por lo tanto, el sistema podría estar inactivo como máximo aproximadamente 43.8 horas al año, es decir, cerca de 1 día y 19 horas.


5. Si reemplazara el algoritmo Bully por un consenso tipo Raft, ¿qué ganaría y qué costo introduciría?

Si se reemplazara Bully por Raft, el sistema ganaría consistencia más fuerte. Raft permitiría elegir un líder formalmente y replicar operaciones en una mayoría de nodos antes de confirmarlas.

Esto ayudaría a que todos los nodos mantengan un log más consistente y a mejorar la recuperación después de fallos.

El costo sería una mayor complejidad. Habría que implementar términos, votos, líder, seguidores, replicación de log, confirmaciones por mayoría y recuperación de nodos. Además, las operaciones podrían tardar más porque deberían confirmarse en varios nodos.

En este prototipo, Bully es suficiente para demostrar elección de coordinador y tolerancia básica a fallos. Raft sería más adecuado para un sistema real que necesite consistencia fuerte.