package simulador.fila;

public class Fila {
    private final int id;
    private final int servidores;
    private final int capacidade;
    private final double minServico;
    private final double maxServico;

    private int clientesNaFila;
    private int servidoresOcupados;
    private long perdas;
    private double[] tempoNosEstados;

    public Fila(int id, int servidores, int capacidade, double minServico, double maxServico) {
        this.id = id;
        this.servidores = servidores;
        this.capacidade = capacidade;
        this.minServico = minServico;
        this.maxServico = maxServico;
        this.tempoNosEstados = new double[capacidade + 1];
    }

    public void registrarPerda() {
        this.perdas++;
    }

    public void clienteEntraNaFila() {
        this.clientesNaFila++;
    }

    public void clienteSaiDaFila() {
        this.clientesNaFila--;
    }

    public void ocuparServidor() {
        this.servidoresOcupados++;
    }

    public void liberarServidor() {
        this.servidoresOcupados--;
    }

    public void acumularTempo(double tempoDecorrido) {
        this.tempoNosEstados[getEstadoAtual()] += tempoDecorrido;
    }

    public void resetar() {
        this.clientesNaFila = 0;
        this.servidoresOcupados = 0;
        this.perdas = 0;
        this.tempoNosEstados = new double[capacidade + 1];
    }

    public int getId() {
        return id;
    }

    public int getServidores() {
        return servidores;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public double getMinServico() {
        return minServico;
    }

    public double getMaxServico() {
        return maxServico;
    }

    public long getPerdas() {
        return perdas;
    }

    public int getEstadoAtual() {
        return clientesNaFila + servidoresOcupados;
    }

    public boolean temServidorLivre() {
        return servidoresOcupados < servidores;
    }

    public boolean estaCheia() {
        return getEstadoAtual() == capacidade;
    }

    public boolean temClienteEsperando() {
        return clientesNaFila > 0;
    }

    public void printResultado(double tempoGlobal) {
        System.out.printf("\nResultados para a Fila %d - G/G/%d/%d / atendimento: %.1f-%.1f\n",
                id, servidores, capacidade, minServico, maxServico);
        System.out.println("--------------------------------------------------");
        System.out.println("ESTADO / TEMPO ACUMULADO / PROBABILIDADE");

        for (int i = 0; i <= capacidade; i++) {
            double tempo = tempoNosEstados[i];
            double probabilidade = (tempo / tempoGlobal) * 100.0;
            System.out.printf("%-9d / %-18.4f / %.2f%%\n", i, tempo, probabilidade);
        }
        System.out.printf(" - perdas de clientes: %d\n", perdas);
    }
}
