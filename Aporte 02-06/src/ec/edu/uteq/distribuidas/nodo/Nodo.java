package src.ec.edu.uteq.distribuidas.nodo;

import src.ec.edu.uteq.distribuidas.Evento.Evento;
import src.ec.edu.uteq.distribuidas.lamport.LamportClock;

import java.util.List;
import java.util.Map;

public class Nodo {

    private final int id;
    private final int puerto;

    private volatile int coordinador;

    private final LamportClock reloj;

    private final Map<Integer, NodoInfo> otrosNodos;

    private final List<Evento> logEventos;

    public Nodo(int id, int puerto, LamportClock reloj, Map<Integer, NodoInfo> otrosNodos, List<Evento> logEventos) {
        this.id = id;
        this.puerto = puerto;
        this.reloj = reloj;
        this.otrosNodos = otrosNodos;
        this.logEventos = logEventos;
    }
}