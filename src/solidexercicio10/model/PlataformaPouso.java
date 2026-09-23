package solidexercicio10.model;

/**
 * Plataforma "L" onde a nave precisa pousar para concluir a missão.
 *
 * <p>No original, a coordenada (0,0) aparecia "solta" na regra de vitória e no
 * desenho do mapa. Como entidade, ela passa a ter um único dono: a {@link Missao}.</p>
 */
public class PlataformaPouso extends EntidadeMapa {
    public PlataformaPouso(int x, int y) {
        super(x, y);
    }

    @Override
    public char getSimbolo() {
        return 'L';
    }
}
