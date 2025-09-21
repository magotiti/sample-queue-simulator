import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class SimuladorRede {
    private double minChegada, maxChegada;
    private final int MAX_ALEATORIOS = 100000;

    private double tempoGlobal;
    private List<Fila> redeDeFilas;
    private long perdas;
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
        this.tempoNosEstadosPorFila.add(new double[fila.getCapacidade() + 1]);
    }
    
    private double nextRandom() {
        if (numerosAleatoriosUsados >= MAX_ALEATORIOS) return -1;
        semente = (a * semente + c) % M;
        numerosAleatoriosUsados++;
        return (double) semente / M;
    }

    private double gerarTempo(double min, double max) {
        double aleatorio = nextRandom();
        if (aleatorio == -1) return -1;
        return min + (max - min) * aleatorio;
    }

    private void agendarSaida(Fila filaOrigem) {
        double tempoServico = gerarTempo(filaOrigem.getMinServico(), filaOrigem.getMaxServico());
        if (tempoServico != -1) {
            escalonadorEventos.add(new Evento(Evento.TipoEvento.SAIDA, tempoGlobal + tempoServico, filaOrigem.getId()));
        }
    }

    private void processarChegada(Evento evento) {
        Fila filaAtual = redeDeFilas.get(evento.filaDestino);

        if (filaAtual.estaCheia()) {
            perdas++;
        } else {
            boolean servidorEstavaLivre = filaAtual.temServidorLivre();
            filaAtual.chegarCliente();
            if (servidorEstavaLivre) {
                agendarSaida(filaAtual);
            }
        }
    }

    // alteracao crucial para implementar a logica de rede
    private void processarSaida(Evento evento) {
        Fila filaOrigem = redeDeFilas.get(evento.filaDestino);
        filaOrigem.finalizarServico();
        
        // logica de roteamento
        double aleatorio = nextRandom();
        if (aleatorio != -1) {
            double probAcumulada = 0.0;
            for (Rota rota : filaOrigem.getRotasDeSaida()) {
                probAcumulada += rota.getProbabilidade();
                if (aleatorio <= probAcumulada) {
                    int idDestino = rota.getFilaDestinoId();
                    // -1 eh uma convencao para "saida do sistema"
                    if (idDestino != -1) { 
                        // cria um evento de chegada na proxima fila, no tempo atual
                        escalonadorEventos.add(new Evento(Evento.TipoEvento.CHEGADA, tempoGlobal, idDestino));
                    }
                    break;
                }
            }
        }

        // se ha cliente na fila, inicia o proximo servico
        if (filaOrigem.getClientesNaFila() > 0) {
            filaOrigem.iniciarServico();
            agendarSaida(filaOrigem);
        }
    }

    public void executar() {
        // inicializacao
        tempoGlobal = 0.0;
        perdas = 0;
        numerosAleatoriosUsados = 0;
        escalonadorEventos = new PriorityQueue<>();
        
        // evento inicial: primeira chegada na primeira fila (id 0)
        escalonadorEventos.add(new Evento(Evento.TipoEvento.CHEGADA, 2.0, 0));
        
        double tempoProximaChegada = gerarTempo(minChegada, maxChegada);
        if(tempoProximaChegada != -1){
            escalonadorEventos.add(new Evento(Evento.TipoEvento.CHEGADA, 2.0 + tempoProximaChegada, 0));
        }

        // laco principal da simulacao
        while (numerosAleatoriosUsados < MAX_ALEATORIOS && !escalonadorEventos.isEmpty()) {
            Evento eventoAtual = escalonadorEventos.poll();
            double tempoDecorrido = eventoAtual.tempo - tempoGlobal;

            // atualiza estatisticas para todas as filas da rede
            for (Fila fila : redeDeFilas) {
                tempoNosEstadosPorFila.get(fila.getId())[fila.getEstado()] += tempoDecorrido;
            }

            tempoGlobal = eventoAtual.tempo;

            if (eventoAtual.tipo == Evento.TipoEvento.CHEGADA) {
                processarChegada(eventoAtual);
            } else { // SAIDA
                processarSaida(eventoAtual);
            }
        }
        printResultados();
    }

    private void printResultados() {
        for (Fila fila : redeDeFilas) {
            System.out.printf("\nfila %d: G/G/%d/%d / atendimento: %.1f-%.1f\n",
                    fila.getId(), fila.getServidores(), fila.getCapacidade(), fila.getMinServico(), fila.getMaxServico());
            System.out.println("ESTADO / TEMPO ACUMULADO / PROBABILIDADE");

            double[] tempoNosEstados = tempoNosEstadosPorFila.get(fila.getId());
            for (int i = 0; i <= fila.getCapacidade(); i++) {
                double tempo = tempoNosEstados[i];
                double probabilidade = (tempo / tempoGlobal) * 100.0;
                System.out.printf("%-9d / %-18.4f / %.2f%%\n", i, tempo, probabilidade);
            }
        }
        System.out.println("\n---------------------------------------------------");
        System.out.printf(" - perdas de clientes (total): %d\n", perdas);
        System.out.printf(" - tempo global da simulacao: %.4f\n", tempoGlobal);
    }

    public static void main(String[] args) {
        System.out.println("iniciando simulacao: Rede com Roteamento...\n");

        // testes
        SimuladorRede simulacaoRede = new SimuladorRede(1.0, 3.0);
        Fila filaTriagem = new Fila(0, 1, 5, 0.5, 1.0);
        filaTriagem.adicionarRota(1, 0.70);
        filaTriagem.adicionarRota(2, 0.30); 
        simulacaoRede.adicionarFila(filaTriagem);

        Fila filaAtendimentoLento = new Fila(1, 1, 5, 4.0, 8.0);
        filaAtendimentoLento.adicionarRota(-1, 1.0);
        simulacaoRede.adicionarFila(filaAtendimentoLento);

        Fila filaAtendimentoRapido = new Fila(2, 1, 5, 1.0, 2.0);
        filaAtendimentoRapido.adicionarRota(-1, 1.0);
        simulacaoRede.adicionarFila(filaAtendimentoRapido);

        simulacaoRede.executar();
    }
}