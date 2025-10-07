#!/bin/bash

# Script para compilar e executar o simulador de redes de filas

echo "=== SIMULADOR DE REDES DE FILAS ==="
echo "Compilando projeto..."

# Compilar todos os arquivos Java
javac -d . src/main/java/*.java

if [ $? -eq 0 ]; then
    echo "Compilação bem-sucedida!"
    echo ""
    echo "Executando simulação..."
    echo ""
    
    # Executar o simulador
    java ExecutorSimulacao
    
    echo ""
    echo "=== SIMULAÇÃO CONCLUÍDA ==="
    echo "Resultados salvos em: outputs/"
    echo "- baseline_summary.csv"
    echo "- improved_summary.csv" 
    echo "- comparison.csv"
else
    echo "Erro na compilação!"
    exit 1
fi
