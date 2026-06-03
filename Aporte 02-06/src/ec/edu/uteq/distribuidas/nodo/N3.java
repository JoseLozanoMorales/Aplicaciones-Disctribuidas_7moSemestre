package src.ec.edu.uteq.distribuidas.nodo;

import src.ec.edu.uteq.distribuidas.lamport.LamportClock;

import java.util.ArrayList;
import java.util.HashMap;

public class N3 {

    public static void main(String[] args) {
        HashMap<Integer, NodoInfo> otrosNodos = new HashMap<>();

        otrosNodos.put(1, new NodoInfo(1, "localhost", 9001));
        otrosNodos.put(2, new NodoInfo(2, "localhost", 9002));

        Nodo nodo = new Nodo(
                3,
                9003,
                new LamportClock("N3"),
                otrosNodos,
                new ArrayList<>()
        );

        nodo.iniciarServidor();
    }
}