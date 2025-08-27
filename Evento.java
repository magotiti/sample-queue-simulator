public class Evento implements Comparable<Evento> {
    enum TipoEvento {
        CHEGADA, SAIDA
    }

    TipoEvento tipo;
    double tempo;

    public Evento(TipoEvento tipo, double tempo) {
        this.tipo = tipo;
        this.tempo = tempo;
    }

    @Override
    public int compareTo(Evento outro) {
        return Double.compare(this.tempo, outro.tempo);
    }
}
