import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigLoader {

    public static SimuladorRede carregarModelo(String caminhoArquivo) {
        SimuladorRede simulador = null;
        Map<String, Integer> mapaNomesFilas = new HashMap<>();
        int idAtual = 0;

        try (InputStream inputStream = new FileInputStream(caminhoArquivo)) {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(inputStream);

            // Processar seção de CHEGADAS
            List<Map<String, Object>> chegadas = (List<Map<String, Object>>) config.get("chegadas");
            if (chegadas != null && !chegadas.isEmpty()) {
                Map<String, Object> chegada = chegadas.get(0);
                double minChegada = getDouble(chegada.get("min_chegada"));
                double maxChegada = getDouble(chegada.get("max_chegada"));
                
                // Converter para segundos se necessário
                minChegada = converterParaSegundos(minChegada);
                maxChegada = converterParaSegundos(maxChegada);
                
                simulador = new SimuladorRede(minChegada, maxChegada);
            }

            if (simulador == null) {
                throw new IllegalStateException("Não foi possível criar o simulador - verifique a seção 'chegadas'");
            }

            // Processar seção de FILAS
            List<Map<String, Object>> filas = (List<Map<String, Object>>) config.get("filas");
            if (filas != null) {
                for (Map<String, Object> filaConfig : filas) {
                    String nomeFila = (String) filaConfig.get("nome");
                    int servidores = getInt(filaConfig.get("servidores"));
                    int capacidade = getInt(filaConfig.get("capacidade"));
                    double minServico = getDouble(filaConfig.get("min_servico"));
                    double maxServico = getDouble(filaConfig.get("max_servico"));
                    
                    // Converter tempos de serviço para segundos se necessário
                    minServico = converterParaSegundos(minServico);
                    maxServico = converterParaSegundos(maxServico);

                    mapaNomesFilas.put(nomeFila, idAtual);
                    Fila fila = new Fila(idAtual, servidores, capacidade, minServico, maxServico);
                    simulador.adicionarFila(fila);
                    idAtual++;
                }
            }

            // Processar seção de REDE
            List<Map<String, Object>> rede = (List<Map<String, Object>>) config.get("rede");
            if (rede != null) {
                for (Map<String, Object> rota : rede) {
                    String origemNome = (String) rota.get("origem");
                    String destinoNome = (String) rota.get("destino");
                    double probabilidade = getDouble(rota.get("probabilidade"));

                    Integer origemId = mapaNomesFilas.get(origemNome);
                    if (origemId == null) {
                        throw new IllegalStateException("Fila origem não encontrada: " + origemNome);
                    }

                    int destinoId = mapaNomesFilas.getOrDefault(destinoNome, -1);

                    simulador.getFilaPorId(origemId).adicionarRota(destinoId, probabilidade);
                }
            }

            // Processar seção de CONFIG
            Map<String, Object> configParams = (Map<String, Object>) config.get("config");
            if (configParams != null) {
                if (configParams.containsKey("warmup")) {
                    simulador.configurarWarmUp(getDouble(configParams.get("warmup")));
                }
                if (configParams.containsKey("observacao")) {
                    simulador.configurarTempoObservacao(getDouble(configParams.get("observacao")));
                }
                if (configParams.containsKey("replicacoes")) {
                    simulador.configurarReplicacoes(getInt(configParams.get("replicacoes")));
                }
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo de configuração: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Erro ao processar o arquivo YAML: " + e.getMessage());
            e.printStackTrace();
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
    
    /**
     * Extrai um valor double de um Object (pode ser Integer ou Double)
     */
    private static double getDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }
    
    /**
     * Extrai um valor int de um Object (pode ser Integer ou Double)
     */
    private static int getInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }
}