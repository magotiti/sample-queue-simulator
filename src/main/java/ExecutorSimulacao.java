import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class ExecutorSimulacao {
    
    public static void main(String[] args) {
        System.out.println("=== SIMULADOR DE REDES DE FILAS - ANÁLISE DE DESEMPENHO ===\n");
        
        // Executar cenário baseline
        System.out.println("1. EXECUTANDO CENÁRIO BASELINE");
        System.out.println("=====================================");
        SimuladorRede simuladorBaseline = ConfigLoader.carregarModelo("config/model_baseline.txt");
        if (simuladorBaseline != null) {
            simuladorBaseline.executar();
            simuladorBaseline.salvarResultadosCSV("outputs/baseline_summary.csv");
        }
        
        System.out.println("\n\n2. EXECUTANDO CENÁRIO IMPROVED");
        System.out.println("=====================================");
        SimuladorRede simuladorImproved = ConfigLoader.carregarModelo("config/model_improved.txt");
        if (simuladorImproved != null) {
            simuladorImproved.executar();
            simuladorImproved.salvarResultadosCSV("outputs/improved_summary.csv");
        }
        
        // Criar arquivo de comparação
        criarArquivoComparacao(simuladorBaseline, simuladorImproved);
        
        System.out.println("\n=== SIMULAÇÃO CONCLUÍDA ===");
    }
    
    private static void criarArquivoComparacao(SimuladorRede baseline, SimuladorRede improved) {
        try {
            FileWriter writer = new FileWriter("outputs/comparison.csv");
            writer.write("fila,metric,N_delta_pct,D_delta_pct,U_delta_pct,W_delta_pct\n");
            
            DecimalFormat df = new DecimalFormat("0.0000");
            
            // Por simplicidade, vamos comparar apenas as últimas replicações
            // Em uma implementação completa, seria necessário calcular médias e ICs
            
            for (int i = 0; i < baseline.redeDeFilas.size(); i++) {
                Fila fila = baseline.redeDeFilas.get(i);
                
                // Obter resultados da última replicação (simplificado)
                MetricasCalculadora.ResultadoMetricas resultadoBaseline = 
                    baseline.resultadosReplicacoes.get(baseline.resultadosReplicacoes.size() - 1)[i];
                MetricasCalculadora.ResultadoMetricas resultadoImproved = 
                    improved.resultadosReplicacoes.get(improved.resultadosReplicacoes.size() - 1)[i];
                
                // Calcular deltas percentuais
                double deltaN = ((resultadoImproved.N - resultadoBaseline.N) / resultadoBaseline.N) * 100;
                double deltaD = ((resultadoImproved.D - resultadoBaseline.D) / resultadoBaseline.D) * 100;
                double deltaU = ((resultadoImproved.U - resultadoBaseline.U) / resultadoBaseline.U) * 100;
                double deltaW = ((resultadoImproved.W - resultadoBaseline.W) / resultadoBaseline.W) * 100;
                
                writer.write(String.format("%d,N,%.2f\n", fila.getId() + 1, deltaN));
                writer.write(String.format("%d,D,%.2f\n", fila.getId() + 1, deltaD));
                writer.write(String.format("%d,U,%.2f\n", fila.getId() + 1, deltaU));
                writer.write(String.format("%d,W,%.2f\n", fila.getId() + 1, deltaW));
            }
            
            writer.close();
            System.out.println("Arquivo de comparação salvo em: outputs/comparison.csv");
            
        } catch (IOException e) {
            System.err.println("Erro ao criar arquivo de comparação: " + e.getMessage());
        }
    }
}
