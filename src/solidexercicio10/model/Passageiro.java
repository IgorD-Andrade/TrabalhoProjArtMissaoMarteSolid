package solidexercicio10.model;

/**
 * Passageiro a ser resgatado.
 *
 * <p>Contrato (LSP): toda subclasse deve devolver uma pontuação positiva e fixa,
 * um tipo não vazio e um símbolo próprio. Quem recebe um {@code Passageiro}
 * (a partida, o renderizador) nunca precisa saber a classe concreta.</p>
 *
 * <p>A classe é abstrata porque "passageiro genérico" não existe no domínio;
 * no original ela era concreta e tinha pontuação default.</p>
 */
public abstract class Passageiro extends EntidadeMapa {
    private final String nome;

    protected Passageiro(String nome, int x, int y) {
        super(x, y);
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O passageiro precisa de um nome");
        }
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    /** Nome da categoria exibido ao jogador (ex.: "Professor"). */
    public abstract String getTipo();

    /** Pontos ganhos ao embarcar este passageiro. Sempre positivo. */
    public abstract int getPontuacao();
}
