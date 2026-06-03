package src.ec.edu.uteq.distribuidas.evento;

public class Evento implements Comparable<Evento> {

    private final int timestampLamport;
    private final int nodoId;
    private final String descripcion;

    public Evento(int timestampLamport, int nodoId, String descripcion) {
        this.timestampLamport = timestampLamport;
        this.nodoId = nodoId;
        this.descripcion = descripcion;
    }

    public int getTimestampLamport() {
        return timestampLamport;
    }

    public int getNodoId() {
        return nodoId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public int compareTo(Evento otro) {
        int comparacionTiempo = Integer.compare(this.timestampLamport, otro.timestampLamport);

        if (comparacionTiempo != 0) {
            return comparacionTiempo;
        }

        return Integer.compare(this.nodoId, otro.nodoId);
    }

    @Override
    public String toString() {
        return "[L=" + timestampLamport + ", Nodo=" + nodoId + "] " + descripcion;
    }
}