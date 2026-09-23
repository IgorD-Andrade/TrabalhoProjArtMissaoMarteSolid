package solidexercicio10.service;

import solidexercicio10.model.Direcao;

/**
 * Ações que o piloto pode executar em um turno.
 *
 * <p>Independente de teclado: a camada de apresentação decide que "w" é
 * {@link #CIMA}. Assim a regra do turno pode ser testada sem console.</p>
 */
public enum Comando {
    CIMA(Direcao.CIMA),
    BAIXO(Direcao.BAIXO),
    ESQUERDA(Direcao.ESQUERDA),
    DIREITA(Direcao.DIREITA),
    EMBARCAR(null),
    ABORTAR(null);

    private final Direcao direcao;

    Comando(Direcao direcao) {
        this.direcao = direcao;
    }

    public boolean isMovimento() {
        return direcao != null;
    }

    public Direcao getDirecao() {
        return direcao;
    }
}
