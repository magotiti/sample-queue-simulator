package simulador;

import java.util.PriorityQueue;

import simulador.evento.Evento;
import simulador.evento.TipoEvento;
import simulador.fila.Fila;

public class SimuladorTandem {

    private final Fila fila1;
    private final Fila fila2;
    private final double minChegada;
    private final double maxChegada;
    private static final int MAX_ALEATORIOS = 100000;

    private double tempoGlobal;
    private PriorityQueue<Evento> escalonador;
    private int numerosAleatoriosUsados;

    private long semente = System.currentTimeMillis();
    private static final long A = 1664525;
    private static final long C = 1013904223;
    private static final long M = (long) Math.pow(2, 32);

    public SimuladorTandem(Fila fila1, Fila fila2, double minChegada, double maxChegada) {
        this.fila1 = fila1;
        this.fila2 = fila2;
        this.minChegada = minChegada;
        this.maxChegada = maxChegada;
    }

    private double nextRandom() {
        if (numerosAleatoriosUsados >= MAX_ALEATORIOS)
            return -1;
        semente = (A * semente + C) % M;
        numerosAleatoriosUsados++;
        return (double) semente / M;
    }

    private double gerarTempo(double min, double max) {
        double aleatorio = nextRandom();
        if (aleatorio == -1)
            return -1;
        return min + (max - min) * aleatorio;
    }

    private void processarChegada() {
        if (fila1.estaCheia()) {
            fila1.registrarPerda();
        } else {
            if (fila1.temServidorLivre()) {
                fila1.ocuparServidor();
                agendarPassagem();
            } else {
                fila1.clienteEntraNaFila();
            }
        }

        double tempoProximaChegada = gerarTempo(minChegada, maxChegada);
        if (tempoProximaChegada != -1) {
            escalonador.add(new Evento(TipoEvento.CHEGADA, tempoGlobal + tempoProximaChegada));
        }
    }

    private void processarPassagem() {

        fila1.liberarServidor();
        if (fila1.temClienteEsperando()) {
            fila1.clienteSaiDaFila();
            fila1.ocuparServidor();
            agendarPassagem();
        }

        if (fila2.estaCheia()) {
            fila2.registrarPerda();
        } else {
            if (fila2.temServidorLivre()) {
                fila2.ocuparServidor();
                agendarSaida();
            } else {
                fila2.clienteEntraNaFila();
            }
        }
    }

    private void processarSaida() {
        fila2.liberarServidor();
        if (fila2.temClienteEsperando()) {
            fila2.clienteSaiDaFila();
            fila2.ocuparServidor();
            agendarSaida();
        }
    }

    private void agendarPassagem() {
        double tempoServico = gerarTempo(fila1.getMinServico(), fila1.getMaxServico());
        if (tempoServico != -1) {
            escalonador.add(new Evento(TipoEvento.PASSAGEM, tempoGlobal + tempoServico));
        }
    }

    private void agendarSaida() {
        double tempoServico = gerarTempo(fila2.getMinServico(), fila2.getMaxServico());
        if (tempoServico != -1) {
            escalonador.add(new Evento(TipoEvento.SAIDA, tempoGlobal + tempoServico));
        }
    }

    public void executar() {

        tempoGlobal = 0.0;
        numerosAleatoriosUsados = 0;
        fila1.resetar();
        fila2.resetar();

        escalonador = new PriorityQueue<>();
        escalonador.add(new Evento(TipoEvento.CHEGADA, 1.5));

        while (numerosAleatoriosUsados < MAX_ALEATORIOS && !escalonador.isEmpty()) {
            Evento eventoAtual = escalonador.poll();
            double tempoDecorrido = eventoAtual.tempo - tempoGlobal;

            fila1.acumularTempo(tempoDecorrido);
            fila2.acumularTempo(tempoDecorrido);

            tempoGlobal = eventoAtual.tempo;

            switch (eventoAtual.tipo) {
                case CHEGADA:
                    processarChegada();
                    break;
                case PASSAGEM:
                    processarPassagem();
                    break;
                case SAIDA:
                    processarSaida();
                    break;
            }
        }

        printResultadoFinal();
    }

    private void printResultadoFinal() {
        System.out.println("==================================================");
        System.out.println("           RELATÓRIO FINAL DA SIMULAÇÃO           ");
        System.out.println("==================================================");
        fila1.printResultado(tempoGlobal);
        fila2.printResultado(tempoGlobal);
        System.out.printf("\n - tempo global da simulacao: %.4f\n", tempoGlobal);
        System.out.println("==================================================");
    }
}
