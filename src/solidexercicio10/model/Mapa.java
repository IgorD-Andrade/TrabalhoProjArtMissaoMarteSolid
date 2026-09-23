package solidexercicio10.model;

/**
 * Limites do mapa quadrado de {@code -tamanho} a {@code +tamanho} nos dois eixos.
 *
 * <p>No original, {@code minX, maxX, minY, maxY} viajavam juntos por seis métodos.
 * Reunir esses valores em um objeto elimina parâmetros repetidos e concentra a
 * regra "está dentro do mapa?" em um único lugar.</p>
 */
public final class Mapa {
    private final int tamanho;

    public Mapa(int tamanho) {
        if (tamanho < 1) {
            throw new IllegalArgumentException("O tamanho do mapa deve ser maior que zero");
        }
        this.tamanho = tamanho;
    }

    public int getTamanho() {
        return tamanho;
    }

    public int getMinX() {
        return -tamanho;
    }

    public int getMaxX() {
        return tamanho;
    }

    public int getMinY() {
        return -tamanho;
    }

    public int getMaxY() {
        return tamanho;
    }

    public boolean contem(int x, int y) {
        return x >= getMinX() && x <= getMaxX() && y >= getMinY() && y <= getMaxY();
    }

    public int getQuantidadeCelulas() {
        int lado = 2 * tamanho + 1;
        return lado * lado;
    }
}
