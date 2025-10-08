# Configurações do Simulador de Redes de Filas

## Estrutura de Pastas Organizada

O projeto foi reorganizado com a seguinte estrutura:

```
sample-queue-simulator-1/
├── src/main/java/           # Código fonte Java
├── config/                  # Arquivos de configuração (YAML)
├── outputs/                 # Resultados da simulação
├── scripts/                 # Scripts utilitários
├── lib/                     # Bibliotecas externas (auto-gerenciadas)
└── docs/                    # Documentação
```

## Como Usar

### Execução Rápida
```bash
./scripts/run.sh
```

Este script automaticamente:
- ✓ Baixa a biblioteca SnakeYAML (se necessário)
- ✓ Compila o código Java
- ✓ Executa as simulações
- ✓ Gera os resultados em CSV

### Execução Manual
```bash
# Compilar (requer SnakeYAML no classpath)
javac -cp ".:lib/snakeyaml-2.2.jar" -d . src/main/java/*.java

# Executar
java -cp ".:lib/snakeyaml-2.2.jar" ExecutorSimulacao
```

## Arquivos de Configuração (Formato YAML)

O sistema utiliza arquivos YAML para configuração, proporcionando maior legibilidade e estrutura:

- `config/model_baseline.yml` - Cenário baseline
- `config/model_improved.yml` - Cenário melhorado

### Estrutura do Arquivo YAML

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

### Dependências

- **SnakeYAML 2.2** - Parser YAML para Java (baixado automaticamente pelo script)

## Resultados

Os resultados são salvos automaticamente em `outputs/`:
- `baseline_summary.csv` - Métricas do cenário baseline
- `improved_summary.csv` - Métricas do cenário melhorado
- `comparison.csv` - Comparação com deltas percentuais

## Métricas Calculadas

- **N** - População média (clientes)
- **D** - Vazão (s⁻¹ e h⁻¹)
- **U** - Utilização (0-1)
- **W** - Tempo de resposta (segundos)
- **Perdas** - Clientes perdidos por capacidade

Todas as métricas seguem as fórmulas obrigatórias especificadas e são convertidas para unidades SI.
