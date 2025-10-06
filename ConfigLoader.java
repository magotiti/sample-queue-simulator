import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {

    public static SimuladorRede carregarModelo(String caminhoArquivo) {
        SimuladorRede simulador = null;
        Map<String, Integer> mapaNomesFilas = new HashMap<>();
        int idAtual = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            String secaoAtual = "";

            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("#")) {
                    continue;
                }

                if (linha.startsWith("[")) {
                    secaoAtual = linha.toUpperCase();
                    continue;
                }

                String[] partes = linha.split(",");

                switch (secaoAtual) {
                    case "[CHEGADAS]":
                        double minChegada = Double.parseDouble(partes[1]);
                        double maxChegada = Double.parseDouble(partes[2]);
                        // Converter para segundos se necessário
                        minChegada = converterParaSegundos(minChegada);
                        maxChegada = converterParaSegundos(maxChegada);
                        simulador = new SimuladorRede(minChegada, maxChegada);
                        break;

                    case "[FILAS]":
                        if (simulador == null)
                            throw new IllegalStateException("A seção [CHEGADAS] deve vir antes de [FILAS]");

                        String nomeFila = partes[0];
                        int servidores = Integer.parseInt(partes[1]);
                        int capacidade = Integer.parseInt(partes[2]);
                        double minServico = Double.parseDouble(partes[3]);
                        double maxServico = Double.parseDouble(partes[4]);
                        
                        // Converter tempos de serviço para segundos se necessário
                        minServico = converterParaSegundos(minServico);
                        maxServico = converterParaSegundos(maxServico);

                        mapaNomesFilas.put(nomeFila, idAtual);
                        Fila fila = new Fila(idAtual, servidores, capacidade, minServico, maxServico);
                        simulador.adicionarFila(fila);
                        idAtual++;
                        break;

                    case "[REDE]":
                        if (simulador == null)
                            throw new IllegalStateException("As seções [CHEGADAS] e [FILAS] devem vir antes de [REDE]");

                        String origemNome = partes[0];
                        String destinoNome = partes[1];
                        double probabilidade = Double.parseDouble(partes[2]);

                        int origemId = mapaNomesFilas.get(origemNome);

                        int destinoId = mapaNomesFilas.getOrDefault(destinoNome, -1);

                        simulador.getFilaPorId(origemId).adicionarRota(destinoId, probabilidade);
                        break;
                        
                    case "[CONFIG]":
                        if (simulador == null)
                            throw new IllegalStateException("As seções [CHEGADAS] e [FILAS] devem vir antes de [CONFIG]");
                            
                        String parametro = partes[0].toLowerCase();
                        double valor = Double.parseDouble(partes[1]);
                        
                        switch (parametro) {
                            case "warmup":
                                simulador.configurarWarmUp(valor);
                                break;
                            case "observacao":
                                simulador.configurarTempoObservacao(valor);
                                break;
                            case "replicacoes":
                                simulador.configurarReplicacoes((int) valor);
                                break;
                        }
                        break;
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Erro ao ler ou processar o arquivo de configuração: " + e.getMessage());
            return null;
        }
        return simulador;
    }
    
    /**
     * Converte valores para segundos assumindo que estão em minutos
     * Se o valor for muito pequeno (< 0.1), assume que já está em segundos
     */
    private static double converterParaSegundos(double valor) {
        if (valor < 0.1) {
            // Provavelmente já está em segundos
            return valor;
        } else {
            // Assumir que está em minutos, converter para segundos
            return valor * 60.0;
        }
    }
}