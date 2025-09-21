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

        if (filaAtual.getId() == 0) { 
            double proximaChegada = gerarTempo(minChegada, maxChegada);
            if (proximaChegada != -1) {
                escalonadorEventos.add(new Evento(Evento.TipoEvento.CHEGADA, tempoGlobal + proximaChegada, 0));
            }
        }

        if (filaAtual.estaCheia())
            filaAtual.registrarPerda();
        else {
            boolean servidorEstavaLivre = filaAtual.temServidorLivre();
            filaAtual.chegarCliente();
            if (servidorEstavaLivre) {
                agendarSaida(filaAtual);
            }
        }
    }

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
            System.out.printf(" - perdas de clientes (total): %d\n", fila.getPerdas());
        }
        System.out.println("\n---------------------------------------------------");
        System.out.printf(" - tempo global da simulacao: %.4f\n", tempoGlobal);
    }

    public static void main(String[] args) {
        System.out.println("iniciando simulacao: Rede com Roteamento...\n");

        SimuladorRede simulador = new SimuladorRede(2.0, 4.0);

        // fila 1 G/G/1/5
        // capacidade nao informada
        Fila fila1 = new Fila(0, 1, 5, 1.0, 2.0);
        fila1.adicionarRota(1, 0.80);
        fila1.adicionarRota(2, 0.20); 
        simulador.adicionarFila(fila1);

        // fila 2 G/G/2/5
        Fila fila2 = new Fila(1, 2, 5, 4.0, 6.0);
        fila2.adicionarRota(2, 0.50);
        fila2.adicionarRota(0, 0.30); 
        fila2.adicionarRota(-1, 0.20);
        simulador.adicionarFila(fila2);

        // fila 3 G/G/2/10
        Fila fila3 = new Fila(2, 2, 10, 5.0, 15.0);
        fila3.adicionarRota(1, 0.70);  
        fila3.adicionarRota(-1, 0.30);
        simulador.adicionarFila(fila3);

        simulador.executar();
    }
}