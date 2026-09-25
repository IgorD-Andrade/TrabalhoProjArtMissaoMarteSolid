package solidexercicio10.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nave pilotada pelo jogador: guarda passageiros a bordo e vidas.
 *
 * <p>Composição: a nave é dona da lista de passageiros embarcados e só expõe
 * uma visão somente leitura dela.</p>
 */
public class Nave extends EntidadeMapa implements Movel {
    public static final int CAPACIDADE_PADRAO = 5;
    public static final int VIDAS_INICIAIS = 3;

    private final String id;
    private final int capacidade;
    private final List<Passageiro> passageiros = new ArrayList<>();
    private int vidas;

    public Nave(String id, int x, int y) {
        this(id, x, y, CAPACIDADE_PADRAO, VIDAS_INICIAIS);
    }

    public Nave(String id, int x, int y, int capacidade, int vidas) {
        super(x, y);
        if (capacidade < 1 || vidas < 1) {
            throw new IllegalArgumentException("Capacidade e vidas devem ser positivas");
        }
        this.id = id;
        this.capacidade = capacidade;
        this.vidas = vidas;
    }

    public String getId() {
        return id;
    }

    public int getCapacidade() {
        return capacidade;
    
    }

    public int getVidas() {
        return vidas;
    }

    public List<Passageiro> getPassageiros() {
        return Collections.unmodifiableList(passageiros);
    }

    public int getQuantidadeABordo() {
        return passageiros.size();
    }

    public boolean estaCheia() {
        return passageiros.size() >= capacidade;
    }

   
    public boolean embarcar(Passageiro passageiro) {
        if (estaCheia()) {
            return false;
        }
        passageiros.add(passageiro);
        return true;
    }

    public void perderVida() {
        if (vidas > 0) {
            vidas--;
        }
    }

    public boolean estaDestruida() {
        return vidas == 0;
    }

    @Override
    public void mover(int dx, int dy) {
        deslocar(dx, dy);
    }

    @Override
    public char getSimbolo() {
        return '@';
    }
}
