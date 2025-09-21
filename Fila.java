import java.util.ArrayList;
import java.util.List;

public class Fila {
    private int id;
    private int servidores;
    private int capacidade;
    private double minServico;
    private double maxServico;
    private long perdas;

    private int clientesNaFila;
    private int servidoresOcupados;

    // alteracao: cada fila agora tem sua propria lista de rotas de saida
    private List<Rota> rotasDeSaida;

    public Fila(int id, int servidores, int capacidade, double minServico, double maxServico) {
        this.id = id;
        this.servidores = servidores;
        this.capacidade = capacidade;
        this.minServico = minServico;
        this.maxServico = maxServico;
        this.clientesNaFila = 0;
        this.servidoresOcupados = 0;
        this.rotasDeSaida = new ArrayList<>();
        this.perdas = 0;
    }

    // novo metodo para configurar a rede
    public void adicionarRota(int filaDestinoId, double probabilidade) {
        this.rotasDeSaida.add(new Rota(filaDestinoId, probabilidade));
    }

    public boolean temServidorLivre() {
        return servidoresOcupados < servidores;
    }

    public boolean estaCheia() {
        return (clientesNaFila + servidoresOcupados) == capacidade;
    }

    public void chegarCliente() {
        if (temServidorLivre()) {
            servidoresOcupados++;
        } else {
            clientesNaFila++;
        }
    }

    public void iniciarServico() {
        clientesNaFila--;
        servidoresOcupados++;
    }

    public void finalizarServico() {
        servidoresOcupados--;
    }
    
    public void registrarPerda() {
        this.perdas++;
    }

    // getters
    public int getId() { return id; }
    public int getClientesNaFila() { return clientesNaFila; }
    public int getServidoresOcupados() { return servidoresOcupados; }
    public double getMinServico() { return minServico; }
    public double getMaxServico() { return maxServico; }
    public int getCapacidade() { return capacidade; }
    public int getServidores() { return servidores; }
    public int getEstado() { return clientesNaFila + servidoresOcupados; }
    public List<Rota> getRotasDeSaida() { return rotasDeSaida; }
    public long getPerdas() { return perdas; }
}