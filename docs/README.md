# Simulador de Redes de Filas por Eventos Discretos

Este projeto implementa um simulador de redes de filas usando simulação por eventos discretos, com cálculo de métricas de desempenho conforme especificações acadêmicas.

## Estrutura do Projeto

```
sample-queue-simulator-1/
├── src/main/java/           # Código fonte Java
│   ├── SimuladorRede.java  # Motor principal da simulação
│   ├── Fila.java           # Classe que representa uma fila
│   ├── Evento.java         # Classe para eventos discretos
│   ├── Rota.java           # Classe para rotas entre filas
│   ├── ConfigLoader.java   # Carregador de configurações (YAML)
│   ├── MetricasCalculadora.java # Cálculo das métricas
│   └── ExecutorSimulacao.java   # Executor principal
├── config/                  # Arquivos de configuração (YAML)
│   ├── model_baseline.yml   # Cenário baseline
│   └── model_improved.yml  # Cenário melhorado
├── outputs/                 # Resultados da simulação
│   ├── baseline_summary.csv
│   ├── improved_summary.csv
│   └── comparison.csv
├── lib/                     # Bibliotecas (auto-gerenciadas)
│   └── snakeyaml-2.2.jar   # Parser YAML
├── scripts/                 # Scripts utilitários
│   └── run.sh             # Script de execução
└── docs/                   # Documentação
```

## Métricas Implementadas

O simulador calcula as seguintes métricas de desempenho usando as fórmulas obrigatórias:

### Por Fila:
- **N** - População média: `N = Σ(πᵢ × i)` para i = 1..K
- **D** - Vazão/Throughput: `D = Σ(πᵢ × μᵢ)` onde μᵢ = min(i,C) × μ
- **U** - Utilização: `U = Σ(πᵢ × min(i,C)/C)`
- **W** - Tempo de resposta: `W = N/D` (em segundos)

### Conversão para SI:
- Tempos convertidos automaticamente para segundos
- Taxas de atendimento em s⁻¹
- Vazão também exibida em h⁻¹ para leitura humana

## Como Executar

### Opção 1: Script automatizado (Recomendado)
```bash
./scripts/run.sh
```

O script automaticamente:
- ✓ Baixa a biblioteca SnakeYAML (se necessário)
- ✓ Compila o código Java
- ✓ Executa as simulações
- ✓ Gera os resultados em CSV

### Opção 2: Compilação manual
```bash
# Compilar (requer SnakeYAML no classpath)
javac -cp ".:lib/snakeyaml-2.2.jar" -d . src/main/java/*.java

# Executar
java -cp ".:lib/snakeyaml-2.2.jar" ExecutorSimulacao
```

## Configuração (Formato YAML)

Os arquivos de configuração estão em `config/` e agora usam o formato YAML para maior legibilidade:

```yaml
chegadas:
  - fila_entrada: Fila1
    min_chegada: 2.0  # minutos
    max_chegada: 4.0  # minutos

filas:
  - nome: Fila1
    servidores: 1
    capacidade: 5
    min_servico: 1.0  # minutos
    max_servico: 2.0  # minutos

rede:
  - origem: Fila1
    destino: Fila2
    probabilidade: 0.8

config:
  warmup: 1000.0       # Tempo de aquecimento (minutos)
  observacao: 10000.0  # Tempo de observação (minutos)
  replicacoes: 5       # Número de replicações
```

## Resultados

Os resultados são salvos em `outputs/`:
- `baseline_summary.csv` - Métricas do cenário baseline
- `improved_summary.csv` - Métricas do cenário melhorado
- `comparison.csv` - Comparação com deltas percentuais

## Características Técnicas

- ✅ Simulação por eventos discretos
- ✅ Warm-up configurável
- ✅ Múltiplas replicações com estatísticas
- ✅ Conversão automática para unidades SI
- ✅ Normalização das probabilidades de estado
- ✅ Cálculo de perdas por capacidade
- ✅ Suporte a redes complexas com múltiplas filas
- ✅ **Configuração em YAML** (mais legível e estruturado)
- ✅ **Gerenciamento automático de dependências** (SnakeYAML)

## Dependências

- **Java 8+** - Linguagem de programação
- **SnakeYAML 2.2** - Parser YAML (baixado automaticamente pelo script)
