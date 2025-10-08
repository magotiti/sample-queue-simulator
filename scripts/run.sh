#!/bin/bash

# Script para compilar e executar o simulador de redes de filas

echo "=== SIMULADOR DE REDES DE FILAS ==="

# Verificar se a biblioteca SnakeYAML existe, se não, baixar
LIB_DIR="lib"
SNAKEYAML_JAR="$LIB_DIR/snakeyaml-2.2.jar"
SNAKEYAML_URL="https://repo1.maven.org/maven2/org/yaml/snakeyaml/2.2/snakeyaml-2.2.jar"

if [ ! -f "$SNAKEYAML_JAR" ]; then
    echo "Biblioteca SnakeYAML não encontrada. Baixando..."
    mkdir -p "$LIB_DIR"
    
    if command -v curl &> /dev/null; then
        curl -L -o "$SNAKEYAML_JAR" "$SNAKEYAML_URL"
    elif command -v wget &> /dev/null; then
        wget -O "$SNAKEYAML_JAR" "$SNAKEYAML_URL"
    else
        echo "Erro: curl ou wget não encontrado. Por favor, instale um deles."
        exit 1
    fi
    
    if [ $? -eq 0 ]; then
        echo "✓ SnakeYAML baixado com sucesso!"
    else
        echo "✗ Erro ao baixar SnakeYAML"
        exit 1
    fi
else
    echo "✓ Biblioteca SnakeYAML encontrada"
fi

echo ""
echo "Compilando projeto..."

# Compilar todos os arquivos Java com SnakeYAML no classpath
javac -cp ".:$SNAKEYAML_JAR" -d . src/main/java/*.java

if [ $? -eq 0 ]; then
    echo "✓ Compilação bem-sucedida!"
    echo ""
    echo "Executando simulação..."
    echo ""
    
    # Executar o simulador com SnakeYAML no classpath
    java -cp ".:$SNAKEYAML_JAR" ExecutorSimulacao
    
    echo ""
    echo "=== SIMULAÇÃO CONCLUÍDA ==="
    echo "Resultados salvos em: outputs/"
    echo "- baseline_summary.csv"
    echo "- improved_summary.csv" 
    echo "- comparison.csv"
else
    echo "✗ Erro na compilação!"
    exit 1
fi
