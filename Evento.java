public class Evento implements Comparable<Evento> {
    enum TipoEvento {
        CHEGADA, SAIDA
    }

    TipoEvento tipo;
    double tempo;
    int filaDestino; // id da fila onde o evento ocorre

    public Evento(TipoEvento tipo, double tempo, int filaDestino) {
        this.tipo = tipo;
        this.tempo = tempo;
        this.filaDestino = filaDestino;
    }

    @Override
    public int compareTo(Evento outro) {
        return Double.compare(this.tempo, outro.tempo);
    }
}