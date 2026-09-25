package solidexercicio10.model;


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
