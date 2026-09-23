package solidexercicio10.model;

/**
 * Algo que ocupa uma coordenada no mapa.
 *
 * <p>ISP: contrato mínimo, usado por quem só precisa consultar posição
 * (renderização, colisão, embarque). Quem precisa mover depende de {@link Movel}.</p>
 */
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
