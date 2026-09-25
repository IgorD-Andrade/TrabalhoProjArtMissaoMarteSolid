package solidexercicio10.model;

/** Obstáculo que se move uma casa por turno. Cor com ele custa uma vida. */
public class Inimigo extends EntidadeMapa implements Movel {
    public Inimigo(int x, int y) {
        super(x, y);
        
    }

    @Override
    public void mover(int dx, int dy) {
        deslocar(dx, dy);
        
    }

    @Override
    public char getSimbolo() {
        return 'X';
        
    }
}
