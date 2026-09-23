package solidexercicio10.model;

/**
 * Base de tudo que aparece no mapa.
 *
 * <p>OCP: cada entidade informa o próprio símbolo, então o renderizador não
 * precisa de {@code instanceof} nem de comparações de texto.</p>
 */
public abstract class EntidadeMapa implements Posicionavel {
    private int x;
    private int y;

    protected EntidadeMapa(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    /** Usado apenas pelas subclasses que implementam {@link Movel}. */
    protected void deslocar(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    /** Caractere usado para desenhar a entidade no mapa. */
    public abstract char getSimbolo();
}
