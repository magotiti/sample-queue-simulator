import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class SimuladorRede {
    private double minChegada, maxChegada;
    private final int MAX_ALEATORIOS = 100000;
    private double tempoGlobal;
    private double tempoWarmUp;
    private double tempoObservacao;
    public List<Fila> redeDeFilas;
    private List<double[]> tempoNosEstadosPorFila;
    private PriorityQueue<Evento> escalonadorEventos;
    private long semente = System.currentTimeMillis();
    private final long a = 1664525;
    private final long c = 1013904223;
    private final long M = (long) Math.pow(2, 32);
    private int numerosAleatoriosUsados = 0;
    private int replicacaoAtual = 0;
    private int totalReplicacoes = 1;
    public List<MetricasCalculadora.ResultadoMetricas[]> resultadosReplicacoes;

    public SimuladorRede(double minChegada, double maxChegada) {
        this.minChegada = minChegada;
        this.maxChegada = maxChegada;
        this.redeDeFilas = new ArrayList<>();
        this.tempoNosEstadosPorFila = new ArrayList<>();
        this.tempoWarmUp = 1000.0; // Default warm-up de 1000 segundos
        this.tempoObservacao = 10000.0; // Default observação de 10000 segundos
        this.totalReplicacoes = 1;
        this.resultadosReplicacoes = new ArrayList<>();
    }

    public void adicionarFila(Fila fila) {
        this.redeDeFilas.add(fila);

        int tamanhoArray = fila.getCapacidade() < Integer.MAX_VALUE / 2 ? fila.getCapacidade() + 1 : 20;
        this.tempoNosEstadosPorFila.add(new double[tamanhoArray]);
    }

    public Fila getFilaPorId(int id) {
        return redeDeFilas.stream().filter(f -> f.getId() == id).findFirst().orElse(null);
    }
    
    public void configurarWarmUp(double tempoWarmUp) {
        this.tempoWarmUp = tempoWarmUp;
    }
    
    public void configurarTempoObservacao(double tempoObservacao) {
        this.tempoObservacao = tempoObservacao;
    }
    
    public void configurarReplicacoes(int totalReplicacoes) {
        this.totalReplicacoes = totalReplicacoes;
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
        System.out.println("Iniciando simulação com " + totalReplicacoes + " replicação(ões)...");
        System.out.println("Warm-up: " + tempoWarmUp + "s, Observação: " + tempoObservacao + "s");
        
        for (replicacaoAtual = 0; replicacaoAtual < totalReplicacoes; replicacaoAtual++) {
            System.out.println("\n--- Replicação " + (replicacaoAtual + 1) + " ---");
            executarReplicacao();
        }
        
        calcularEstatisticasFinais();
    }
    
    private void executarReplicacao() {
        tempoGlobal = 0.0;
        numerosAleatoriosUsados = 0;
        escalonadorEventos = new PriorityQueue<>();
        
        // Resetar contadores de tempo nos estados
        for (int i = 0; i < tempoNosEstadosPorFila.size(); i++) {
            double[] estados = tempoNosEstadosPorFila.get(i);
            for (int j = 0; j < estados.length; j++) {
                estados[j] = 0.0;
            }
        }
        
        // Resetar perdas
        for (Fila fila : redeDeFilas) {
            fila.resetarPerdas();
        }
        
        // Resetar estado das filas
        for (Fila fila : redeDeFilas) {
            fila.resetarEstado();
        }
        
        escalonadorEventos.add(new Evento(Evento.TipoEvento.CHEGADA, 2.0, 0));
        
        double tempoFinal = tempoWarmUp + tempoObservacao;
        
        while (numerosAleatoriosUsados < MAX_ALEATORIOS && !escalonadorEventos.isEmpty() && tempoGlobal < tempoFinal) {
            Evento eventoAtual = escalonadorEventos.poll();
            double tempoDecorrido = eventoAtual.tempo - tempoGlobal;
            
            // Só coletar dados após o warm-up
            if (tempoGlobal >= tempoWarmUp && tempoDecorrido > 0) {
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
        
        // Calcular métricas para esta replicação
        calcularMetricasReplicacao();
    }
    
    private void calcularMetricasReplicacao() {
        MetricasCalculadora.ResultadoMetricas[] resultados = new MetricasCalculadora.ResultadoMetricas[redeDeFilas.size()];
        
        for (int i = 0; i < redeDeFilas.size(); i++) {
            Fila fila = redeDeFilas.get(i);
            double[] tempoNosEstados = tempoNosEstadosPorFila.get(i);
            
            // Converter tempos em probabilidades
            double[] pi = new double[tempoNosEstados.length];
            for (int j = 0; j < tempoNosEstados.length; j++) {
                pi[j] = tempoNosEstados[j] / tempoObservacao;
            }
            
            // Calcular taxa de atendimento média
            double tempoMedioServico = (fila.getMinServico() + fila.getMaxServico()) / 2.0;
            double mu = 1.0 / tempoMedioServico; // s⁻¹
            
            // Calcular métricas
            resultados[i] = MetricasCalculadora.calcularMetricas(pi, fila.getServidores(), fila.getCapacidade(), mu);
        }
        
        resultadosReplicacoes.add(resultados);
    }
    
    private void calcularEstatisticasFinais() {
        System.out.println("\n==================================================");
        System.out.println("           RESULTADOS FINAIS DA SIMULAÇÃO          ");
        System.out.println("==================================================");
        
        DecimalFormat df = new DecimalFormat("0.0000");
        
        for (int i = 0; i < redeDeFilas.size(); i++) {
            Fila fila = redeDeFilas.get(i);
            
            // Calcular médias e intervalos de confiança
            double[] mediasN = new double[redeDeFilas.size()];
            double[] mediasD = new double[redeDeFilas.size()];
            double[] mediasU = new double[redeDeFilas.size()];
            double[] mediasW = new double[redeDeFilas.size()];
            double[] mediasPerdas = new double[redeDeFilas.size()];
            
            for (int j = 0; j < totalReplicacoes; j++) {
                MetricasCalculadora.ResultadoMetricas resultado = resultadosReplicacoes.get(j)[i];
                mediasN[i] += resultado.N;
                mediasD[i] += resultado.D;
                mediasU[i] += resultado.U;
                mediasW[i] += resultado.W;
            }
            
            mediasN[i] /= totalReplicacoes;
            mediasD[i] /= totalReplicacoes;
            mediasU[i] /= totalReplicacoes;
            mediasW[i] /= totalReplicacoes;
            
            // Calcular perdas médias
            long totalPerdas = 0;
            for (int j = 0; j < totalReplicacoes; j++) {
                // Aqui precisaríamos armazenar perdas por replicação
                // Por simplicidade, vamos usar a última replicação
            }
            
            System.out.printf("\nFila %d: G/G/%d/%d\n", fila.getId() + 1, fila.getServidores(), fila.getCapacidade());
            System.out.println("--------------------------------------------------");
            System.out.printf("N (população média): %s\n", df.format(mediasN[i]));
            System.out.printf("D (vazão): %s s⁻¹ (%s h⁻¹)\n", df.format(mediasD[i]), df.format(mediasD[i] * 3600));
            System.out.printf("U (utilização): %s\n", df.format(mediasU[i]));
            System.out.printf("W (tempo resposta): %s s\n", df.format(mediasW[i]));
        }
        
        System.out.println("\n==================================================");
    }
    
    public void salvarResultadosCSV(String nomeArquivo) {
        try {
            FileWriter writer = new FileWriter(nomeArquivo);
            writer.write("fila,N,D_s-1,D_h-1,U,W_s,perdas_abs,perdas_pct\n");
            
            for (int i = 0; i < redeDeFilas.size(); i++) {
                Fila fila = redeDeFilas.get(i);
                
                // Calcular médias
                double mediaN = 0, mediaD = 0, mediaU = 0, mediaW = 0;
                for (int j = 0; j < totalReplicacoes; j++) {
                    MetricasCalculadora.ResultadoMetricas resultado = resultadosReplicacoes.get(j)[i];
                    mediaN += resultado.N;
                    mediaD += resultado.D;
                    mediaU += resultado.U;
                    mediaW += resultado.W;
                }
                
                mediaN /= totalReplicacoes;
                mediaD /= totalReplicacoes;
                mediaU /= totalReplicacoes;
                mediaW /= totalReplicacoes;
                
                // Calcular perdas (usando última replicação por simplicidade)
                long perdas = fila.getPerdas();
                double perdasPct = 0.0; // Seria necessário calcular baseado nas chegadas
                
                writer.write(String.format("%d,%.6f,%.8f,%.4f,%.6f,%.6f,%d,%.2f\n",
                    fila.getId() + 1, mediaN, mediaD, mediaD * 3600, mediaU, mediaW, perdas, perdasPct));
            }
            
            writer.close();
            System.out.println("Resultados salvos em: " + nomeArquivo);
            
        } catch (IOException e) {
            System.err.println("Erro ao salvar CSV: " + e.getMessage());
        }
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