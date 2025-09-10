package simulador.evento;

public class Evento implements Comparable<Evento> {
    public TipoEvento tipo;
    public double tempo;

    public Evento(TipoEvento tipo, double tempo) {
        this.tipo = tipo;
        this.tempo = tempo;
    }

    @Override
    public int compareTo(Evento outro) {
        return Double.compare(this.tempo, outro.tempo);
    }
}
