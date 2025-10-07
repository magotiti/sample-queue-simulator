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
│   ├── ConfigLoader.java   # Carregador de configurações
│   ├── MetricasCalculadora.java # Cálculo das métricas
│   └── ExecutorSimulacao.java   # Executor principal
├── config/                  # Arquivos de configuração
│   ├── model_baseline.txt   # Cenário baseline
│   └── model_improved.txt  # Cenário melhorado
├── outputs/                 # Resultados da simulação
│   ├── baseline_summary.csv
│   ├── improved_summary.csv
│   └── comparison.csv
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

### Opção 1: Script automatizado
```bash
./scripts/run.sh
```

### Opção 2: Compilação manual
```bash
# Compilar
javac -d . src/main/java/*.java

# Executar
java ExecutorSimulacao
```

## Configuração

Os arquivos de configuração estão em `config/` e seguem o formato:

```
[CHEGADAS]
# fila_entrada,min_chegada,max_chegada (em minutos)
Fila1,2.0,4.0

[FILAS]
# nome,servidores,capacidade,min_servico,max_servico (em minutos)
Fila1,1,5,1.0,2.0

[REDE]
# origem,destino,probabilidade
Fila1,Fila2,0.8

[CONFIG]
# parametro,valor
warmup,1000.0
observacao,10000.0
replicacoes,5
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
