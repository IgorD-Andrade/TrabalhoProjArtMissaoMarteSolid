package solidexercicio10.model;

public class Astronauta extends Passageiro {
    public Astronauta(String nome, int x, int y) {
        super(nome, x, y);
        
    }

    @Override
    public String getTipo() {
        
        return "Astronauta";
    }

    @Override
    public int getPontuacao() {
        
        return 20;
    }

    @Override
    public char getSimbolo() {
        
        return 'T';
    }
}
