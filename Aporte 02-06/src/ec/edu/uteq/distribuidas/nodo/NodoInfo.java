package src.ec.edu.uteq.distribuidas.nodo;

public class NodoInfo {

    private int id;
    private String host;
    private int puerto;

    private volatile long ultimoHeartbeat;

}