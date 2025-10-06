public class MetricasCalculadora {
    
    /**
     * Calcula as métricas de desempenho de uma fila usando as fórmulas obrigatórias
     * 
     * @param pi Array de probabilidades πᵢ (i = 0..K)
     * @param C Número de servidores
     * @param K Capacidade total da fila
     * @param mu Taxa de atendimento por servidor (s⁻¹)
     * @return Resultado das métricas
     */
    public static ResultadoMetricas calcularMetricas(double[] pi, int C, int K, double mu) {
        // Normalizar probabilidades para garantir que somem 1
        double somaPi = 0.0;
        for (int i = 0; i <= K; i++) {
            somaPi += pi[i];
        }
        
        // Normalizar
        for (int i = 0; i <= K; i++) {
            pi[i] = pi[i] / somaPi;
        }
        
        // Calcular métricas usando i = 1..K conforme especificado
        double N = 0.0; // População média
        double D = 0.0; // Vazão/Throughput
        double U = 0.0; // Utilização
        
        for (int i = 1; i <= K; i++) {
            // População média: N = Σ(πᵢ × i)
            N += pi[i] * i;
            
            // Taxa efetiva no estado i: μᵢ = min(i, C) × μ
            double muI = Math.min(i, C) * mu;
            
            // Vazão: D = Σ(πᵢ × μᵢ)
            D += pi[i] * muI;
            
            // Utilização: U = Σ(πᵢ × min(i,C)/C)
            U += pi[i] * (Math.min(i, C) / (double) C);
        }
        
        // Tempo de resposta: W = N/D (em segundos)
        double W = (D > 0) ? N / D : 0.0;
        
        return new ResultadoMetricas(N, D, U, W);
    }
    
    /**
     * Classe para armazenar os resultados das métricas
     */
    public static class ResultadoMetricas {
        public final double N; // População média
        public final double D; // Vazão (s⁻¹)
        public final double U; // Utilização (0-1)
        public final double W; // Tempo de resposta (s)
        
        public ResultadoMetricas(double N, double D, double U, double W) {
            this.N = N;
            this.D = D;
            this.U = U;
            this.W = W;
        }
        
        /**
         * Retorna a vazão em h⁻¹ para leitura humana
         */
        public double getDEmHoras() {
            return D * 3600.0;
        }
        
        @Override
        public String toString() {
            return String.format("N=%.4f, D=%.6f s⁻¹ (%.2f h⁻¹), U=%.4f, W=%.4f s", 
                               N, D, getDEmHoras(), U, W);
        }
    }
}
