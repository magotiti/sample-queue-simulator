import simulador.SimuladorTandem;
import simulador.fila.Fila;

public class Main {
    public static void main(String[] args) {
        Fila f1 = new Fila(1, 2, 3, 3.0, 4.0);
        Fila f2 = new Fila(2, 1, 5, 2.0, 3.0);

        SimuladorTandem simulador = new SimuladorTandem(f1, f2, 1.0, 4.0);
        simulador.executar();
    }
}
