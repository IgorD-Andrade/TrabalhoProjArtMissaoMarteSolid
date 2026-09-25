// PRINCIPIO OCP
package solidexercicio10.model;

public class Engenheiro extends Passageiro {
    public Engenheiro(String nome, int x, int y) {
        super(nome, x, y);
    }

    @Override
    public String getTipo() {
        return "Engenheiro";
    }

    @Override
    public int getPontuacao() {
        return 15;
    }

    @Override
    public char getSimbolo() {
        return 'E';
    }
}
