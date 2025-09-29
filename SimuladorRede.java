import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class SimuladorRede {
    private double minChegada, maxChegada;
    private final int MAX_ALEATORIOS = 100000;
    private double tempoGlobal;
    private List<Fila> redeDeFilas;
    private List<double[]> tempoNosEstadosPorFila;
    private PriorityQueue<Evento> escalonadorEventos;
    private long semente = System.currentTimeMillis();
    private final long a = 1664525;
    private final long c = 1013904223;
    private final long M = (long) Math.pow(2, 32);
    private int numerosAleatoriosUsados = 0;

    public SimuladorRede(double minChegada, double maxChegada) {
        this.minChegada = minChegada;
        this.maxChegada = maxChegada;
        this.redeDeFilas = new ArrayList<>();
        this.tempoNosEstadosPorFila = new ArrayList<>();
    }

    public void adicionarFila(Fila fila) {
        this.redeDeFilas.add(fila);

        int tamanhoArray = fila.getCapacidade() < Integer.MAX_VALUE / 2 ? fila.getCapacidade() + 1 : 20;
        this.tempoNosEstadosPorFila.add(new double[tamanhoArray]);
    }

    public Fila getFilaPorId(int id) {
        return redeDeFilas.stream().filter(f -> f.getId() == id).findFirst().orElse(null);
    }

    private double nextRandom() {
        if (numerosAleatoriosUsados >= MAX_ALEATORIOS)
            return -1;
        semente = (a * semente + c) % M;
        numerosAleatoriosUsados++;
        return (double) semente / M;
    }

    private double gerarTempo(double min, double max) {
        double aleatorio = nextRandom();
        if (aleatorio == -1)
            return -1;
        return min + (max - min) * aleatorio;
    }

    private void agendarSaida(Fila filaOrigem) {
        double tempoServico = gerarTempo(filaOrigem.getMinServico(), filaOrigem.getMaxServico());
        if (tempoServico != -1) {
            escalonadorEventos.add(new Evento(Evento.TipoEvento.SAIDA, tempoGlobal + tempoServico, filaOrigem.getId()));
        }
    }

    private void processarChegada(Evento evento) {
        Fila filaAtual = getFilaPorId(evento.filaDestino);

        if (filaAtual.getId() == 0) {
            double proximaChegada = gerarTempo(minChegada, maxChegada);
            if (proximaChegada != -1) {
                escalonadorEventos.add(new Evento(Evento.TipoEvento.CHEGADA, tempoGlobal + proximaChegada, 0));
            }
        }

        if (filaAtual.estaCheia()) {
            filaAtual.registrarPerda();
        } else {
            boolean servidorEstavaLivre = filaAtual.temServidorLivre();
            filaAtual.chegarCliente();
            if (servidorEstavaLivre) {
                agendarSaida(filaAtual);
            }
        }
    }

    private void processarSaida(Evento evento) {
        Fila filaOrigem = getFilaPorId(evento.filaDestino);
        filaOrigem.finalizarServico();

        double aleatorio = nextRandom();
        if (aleatorio != -1) {
            double probAcumulada = 0.0;
            for (Rota rota : filaOrigem.getRotasDeSaida()) {
                probAcumulada += rota.getProbabilidade();
                if (aleatorio <= probAcumulada) {
                    int idDestino = rota.getFilaDestinoId();
                    if (idDestino != -1) {
                        escalonadorEventos.add(new Evento(Evento.TipoEvento.CHEGADA, tempoGlobal, idDestino));
                    }
                    break;
                }
            }
        }

        if (filaOrigem.getClientesNaFila() > 0) {
            filaOrigem.iniciarServico();
            agendarSaida(filaOrigem);
        }
    }

    public void executar() {
        tempoGlobal = 0.0;
        numerosAleatoriosUsados = 0;
        escalonadorEventos = new PriorityQueue<>();

        escalonadorEventos.add(new Evento(Evento.TipoEvento.CHEGADA, 2.0, 0));

        while (numerosAleatoriosUsados < MAX_ALEATORIOS && !escalonadorEventos.isEmpty()) {
            Evento eventoAtual = escalonadorEventos.poll();
            double tempoDecorrido = eventoAtual.tempo - tempoGlobal;

            if (tempoDecorrido > 0) {
                for (Fila fila : redeDeFilas) {
                    int estado = fila.getEstado();
                    if (estado < tempoNosEstadosPorFila.get(fila.getId()).length) {
                        tempoNosEstadosPorFila.get(fila.getId())[estado] += tempoDecorrido;
                    }
                }
            }

            tempoGlobal = eventoAtual.tempo;

            if (eventoAtual.tipo == Evento.TipoEvento.CHEGADA) {
                processarChegada(eventoAtual);
            } else {
                processarSaida(eventoAtual);
            }
        }
        printResultados();
    }

    private void printResultados() {
        System.out.println("==================================================");
        System.out.println("           RELATÓRIO FINAL DA SIMULAÇÃO           ");
        System.out.println("==================================================");
        for (Fila fila : redeDeFilas) {
            System.out.printf("\nResultados para a Fila %d: G/G/%d/%d (atendimento: %.1f-%.1f)\n",
                    fila.getId() + 1, fila.getServidores(), fila.getCapacidade(), fila.getMinServico(),
                    fila.getMaxServico());
            System.out.println("--------------------------------------------------");
            System.out.println("ESTADO / TEMPO ACUMULADO / PROBABILIDADE");

            double[] tempoNosEstados = tempoNosEstadosPorFila.get(fila.getId());
            for (int i = 0; i < tempoNosEstados.length; i++) {
                double tempo = tempoNosEstados[i];
                double probabilidade = (tempoGlobal > 0) ? (tempo / tempoGlobal) * 100.0 : 0.0;
                System.out.printf("%-6d / %-18.4f / %.2f%%\n", i, tempo, probabilidade);
            }
            System.out.printf(" - Perdas de clientes: %d\n", fila.getPerdas());
        }
        System.out.println("\n--------------------------------------------------");
        System.out.printf(" - Tempo global da simulação: %.4f\n", tempoGlobal);
        System.out.println("==================================================");
    }

    public static void main(String[] args) {
        String arquivoConfig = "model.txt";
        System.out.println("Iniciando simulação a partir de '" + arquivoConfig + "'...\n");

        SimuladorRede simulador = ConfigLoader.carregarModelo(arquivoConfig);

        if (simulador != null) {
            simulador.executar();
        } else {
            System.out.println("Falha ao carregar o modelo. Encerrando.");
        }
    }
}