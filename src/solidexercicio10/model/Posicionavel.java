package solidexercicio10.model;

public interface Posicionavel {
    int getX();

    int getY();

    default boolean estaEm(int x, int y) {
        return getX() == x && getY() == y;
    }

    default boolean mesmaPosicaoQue(Posicionavel outro) {
        return estaEm(outro.getX(), outro.getY());
    }
}
