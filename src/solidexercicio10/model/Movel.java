package solidexercicio10.model;

/**
 * Algo que pode se deslocar pelo mapa.
 *
 * <p>ISP: separado de {@link Posicionavel} porque asteroides, passageiros e a
 * plataforma não se movem. Só {@link Nave} e {@link Inimigo} implementam.</p>
 */
public interface Movel {
    /** Desloca a entidade sem validar limites (quem conhece o mapa é a {@link Missao}). */
    void mover(int dx, int dy);
}
