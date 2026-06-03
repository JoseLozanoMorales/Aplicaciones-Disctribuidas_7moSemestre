package src.ec.edu.uteq.distribuidas.nodo;

public class NodoInfo {

    private final int id;
    private final String host;
    private final int puerto;

    private volatile long ultimoHeartbeat;
    private volatile boolean activo;

    public NodoInfo(int id, String host, int puerto) {
        this.id = id;
        this.host = host;
        this.puerto = puerto;
        this.ultimoHeartbeat = System.currentTimeMillis();
        this.activo = true;
    }

    public int getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPuerto() {
        return puerto;
    }

    public long getUltimoHeartbeat() {
        return ultimoHeartbeat;
    }

    public boolean isActivo() {
        return activo;
    }

    public void actualizarHeartbeat() {
        this.ultimoHeartbeat = System.currentTimeMillis();
        this.activo = true;
    }

    public void marcarCaido() {
        this.activo = false;
    }

    @Override
    public String toString() {
        return "NodoInfo{" +
                "id=" + id +
                ", host='" + host + '\'' +
                ", puerto=" + puerto +
                ", ultimoHeartbeat=" + ultimoHeartbeat +
                ", activo=" + activo +
                '}';
    }
}