--Ejecutar--

Para probar el sistema completo, ejecutar las siguientes clases desde IntelliJ IDEA:

N1.java
N2.java
N3.java
ClienteTCP.java

Debe asegurarse de ejecutar en ese orden las clases ya que en ClienteTCP puede seleccionar a que Nodo conectarse y si manda una acción a los nodos apagados le indicará que no pudo conectarse al nodo

Cada nodo usa un puerto diferente:

N1 -> 9001
N2 -> 9002
N3 -> 9003

----------------------------------------------------------------------------------------------
--Uso del cliente--

Al iniciar ClienteTCP.java, se debe seleccionar el nodo al que se desea conectar:

1. N1 - puerto 9001
2. N2 - puerto 9002
3. N3 - puerto 9003

Luego se envía una operación con el siguiente formato:

TOKEN123|Operacion

Ejemplo:

TOKEN123|Registrar entrada de paquete 001

Si la operación se registra correctamente, el sistema responderá algo similar a:

OK:N2: operación registrada: Lamport=932: coordinador=N3

----------------------------------------------------------------------------------------------
--Prueba de fallos--

Para probar la tolerancia a fallos:

Ejecutar N1, N2 y N3.
Ejecutar ClienteTCP.
Enviar una operación.
Detener uno de los nodos, por ejemplo N3.
Esperar unos segundos.
Verificar en consola que los demás nodos detectan la caída y eligen un nuevo coordinador.

Ejemplo esperado:

N2 detectó caída de N3 por falta de heartbeat.
Elección Bully en N2: coordinador anterior=N3, nuevo coordinador=N2