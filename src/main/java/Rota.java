public class Rota {
    private int filaDestinoId;
    private double probabilidade;

    public Rota(int filaDestinoId, double probabilidade) {
        this.filaDestinoId = filaDestinoId;
        this.probabilidade = probabilidade;
    }

    public int getFilaDestinoId() {
        return filaDestinoId;
    }

    public double getProbabilidade() {
        return probabilidade;
    }
}