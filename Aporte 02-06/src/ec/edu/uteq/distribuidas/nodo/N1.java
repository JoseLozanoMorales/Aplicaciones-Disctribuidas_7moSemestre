package src.ec.edu.uteq.distribuidas.nodo;

import src.ec.edu.uteq.distribuidas.lamport.LamportClock;

import java.util.ArrayList;
import java.util.HashMap;

public class N1 {

    public static void main(String[] args) {
        HashMap<Integer, NodoInfo> otrosNodos = new HashMap<>();

        otrosNodos.put(2, new NodoInfo(2, "localhost", 9002));
        otrosNodos.put(3, new NodoInfo(3, "localhost", 9003));

        Nodo nodo = new Nodo(
                1,
                9001,
                new LamportClock("N1"),
                otrosNodos,
                new ArrayList<>()
        );

        nodo.iniciarServidor();
    }
}