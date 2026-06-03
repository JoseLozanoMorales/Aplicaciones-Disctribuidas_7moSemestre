package src.ec.edu.uteq.distribuidas.nodo;

import src.ec.edu.uteq.distribuidas.lamport.LamportClock;

import java.util.ArrayList;
import java.util.HashMap;

public class N2 {

    public static void main(String[] args) {
        HashMap<Integer, NodoInfo> otrosNodos = new HashMap<>();

        otrosNodos.put(1, new NodoInfo(1, "localhost", 9001));
        otrosNodos.put(3, new NodoInfo(3, "localhost", 9003));

        Nodo nodo = new Nodo(
                2,
                9002,
                new LamportClock("N2"),
                otrosNodos,
                new ArrayList<>()
        );

        nodo.iniciarServidor();
    }
}