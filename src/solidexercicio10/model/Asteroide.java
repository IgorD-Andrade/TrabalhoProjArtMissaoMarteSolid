package solidexercicio10.model;

/** Obstáculo fixo. Colidir com ele  */
public class Asteroide extends EntidadeMapa {
    public Asteroide(int x, int y) {
        super(x, y);
    }

    @Override
    public char getSimbolo() {
        return '#';
    }
}
