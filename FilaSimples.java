import java.util.PriorityQueue;

public class FilaSimples {
    private int servidores;
    private int capacidade;
    private double minChegada, maxChegada;
    private double minServico, maxServico;
    private final int MAX_ALEATORIOS = 100000;

    private double tempoGlobal;
    private int clientesNaFila;
    private int servidoresOcupados;
    private long perdas;
    private double[] tempoNosEstados;
    private PriorityQueue<Evento> escalonadorEventos;

    private long semente = System.currentTimeMillis(); // seed
    private final long a = 1664525;
    private final long c = 1013904223;
    private final long M = (long) Math.pow(2, 32);
    private int numerosAleatoriosUsados = 0;

    public FilaSimples(int servidores, int capacidade, double minChegada, double maxChegada, double minServico, double maxServico) {
        this.servidores = servidores;
        this.capacidade = capacidade;
        this.minChegada = minChegada;
        this.maxChegada = maxChegada;
        this.minServico = minServico;
        this.maxServico = maxServico;
    }

    // pseudoaleatorios
    private double nextRandom() {
        if (numerosAleatoriosUsados >= MAX_ALEATORIOS) return -1;
        semente = (a * semente + c) % M;
        numerosAleatoriosUsados++;
        return (double) semente / M;
    }

    // converte aleatorio para intervalo desejado
    private double gerarTempo(double min, double max) {
        double aleatorio = nextRandom();
        if (aleatorio == -1) return -1;
        return min + (max - min) * aleatorio;
    }

    // metodo auxiliar para agendar um evento de saida
    private void agendarSaida() {
        double tempoServico = gerarTempo(minServico, maxServico);
        if (tempoServico != -1) {
            escalonadorEventos.add(new Evento(Evento.TipoEvento.SAIDA, tempoGlobal + tempoServico));
        }
    }

    // chegada de cliente conforme orientacao: perda se sistema cheio
    private void processarChegada() {
        if (servidoresOcupados + clientesNaFila == capacidade) {
            perdas++;
        } else {
            if (servidoresOcupados < servidores) {
                servidoresOcupados++;
                agendarSaida();
            } else {
                clientesNaFila++;
            }
        }
        
        // agenda proxima chegada msm com perda
        double tempoProximaChegada = gerarTempo(minChegada, maxChegada);
        if (tempoProximaChegada != -1) {
            escalonadorEventos.add(new Evento(Evento.TipoEvento.CHEGADA, tempoGlobal + tempoProximaChegada));
        }
    }

    // saida libera servidor e inicia atendimento do proximo se houver fila
    private void processarSaida() {
        servidoresOcupados--;
        if (clientesNaFila > 0) {
            clientesNaFila--;
            servidoresOcupados++;
            agendarSaida();
        }
    }

    public void executar() {
        tempoGlobal = 0.0;
        clientesNaFila = 0;
        servidoresOcupados = 0;
        perdas = 0;
        numerosAleatoriosUsados = 0;

        tempoNosEstados = new double[capacidade + 1];

        escalonadorEventos = new PriorityQueue<>();
        escalonadorEventos.add(new Evento(Evento.TipoEvento.CHEGADA, 2.0)); // 1o cliente

        while (numerosAleatoriosUsados < MAX_ALEATORIOS && !escalonadorEventos.isEmpty()) {
            Evento eventoAtual = escalonadorEventos.poll();

            // atualiza as estatisticas do estado anterior
            double tempoDecorrido = eventoAtual.tempo - tempoGlobal;
            int estadoAtual = servidoresOcupados + clientesNaFila;
            tempoNosEstados[estadoAtual] += tempoDecorrido;

            tempoGlobal = eventoAtual.tempo;

            if (eventoAtual.tipo == Evento.TipoEvento.CHEGADA) {
                processarChegada();
            } else { // SAIDA
                processarSaida();
            }
        }

        printResultado();
    }

    private void printResultado() {
        System.out.printf("\nfila: G/G/%d/%d / chegadas: %.1f-%.1f / atendimento: %.1f-%.1f\n",
                servidores, capacidade, minChegada, maxChegada, minServico, maxServico);
        System.out.println("ESTADO / TEMPO ACUMULADO / PROBABILIDADE");

        for (int i = 0; i <= capacidade; i++) {
            double tempo = tempoNosEstados[i];
            double probabilidade = (tempo / tempoGlobal) * 100.0;
            System.out.printf("%-9d / %-18.4f / %.2f%%\n", i, tempo, probabilidade);
        }

        System.out.printf(" - perdas de clientes: %d\n", perdas);
        System.out.printf(" - tempo global da simulacao: %.4f\n", tempoGlobal);
    }

    public static void main(String[] args) {
        System.out.println("iniciando simulacoes...\n");

        // TO DO: desmockar testes
        // sim. 1: G/G/1/5
        FilaSimples simulacao1 = new FilaSimples(1, 5, 2.0, 5.0, 3.0, 5.0);
        simulacao1.executar();

        FilaSimples simulacao2 = new FilaSimples(2, 5, 2.0, 5.0, 3.0, 5.0);
        simulacao2.executar();
    }
}
