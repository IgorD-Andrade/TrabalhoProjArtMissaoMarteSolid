package solidexercicio10.model;

/**
 * Algo que pode se deslocar pelo mapa.
 *
 * <p>ISP: separado de {@link Posicionavel} porque asteroides, passageiros e a
 * plataforma não se movem. Só {@link Nave} e {@link Inimigo} implementam.</p>
 */
public interface Movel {
    void mover(int dx, int dy);
}
