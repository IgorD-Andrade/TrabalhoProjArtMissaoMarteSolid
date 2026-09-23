package solidexercicio10.model;

import java.text.Normalizer;

/**
 * Níveis de dificuldade e os parâmetros de cada um.
 *
 * <p>OCP: no original, pontuação inicial e quantidades ficavam em {@code switch}
 * e {@code if} espalhados pela {@code Main}. Aqui cada constante carrega seus
 * próprios valores; criar uma nova dificuldade é adicionar uma linha.</p>
 */
public enum Dificuldade {
    //      rótulo    pontos  passageiros  asteroides  inimigos
    FACIL("Fácil", 30, 4, 1, 1),
    MEDIO("Médio", 20, 5, 2, 2),
    DIFICIL("Difícil", 15, 5, 3, 3);

    private final String rotulo;
    private final int pontuacaoInicial;
    private final int quantidadePassageiros;
    private final int quantidadeAsteroides;
    private final int quantidadeInimigos;

    Dificuldade(String rotulo, int pontuacaoInicial, int quantidadePassageiros,
                int quantidadeAsteroides, int quantidadeInimigos) {
        this.rotulo = rotulo;
        this.pontuacaoInicial = pontuacaoInicial;
        this.quantidadePassageiros = quantidadePassageiros;
        this.quantidadeAsteroides = quantidadeAsteroides;
        this.quantidadeInimigos = quantidadeInimigos;
    }

    public int getPontuacaoInicial() {
        return pontuacaoInicial;
    }

    public int getQuantidadePassageiros() {
        return quantidadePassageiros;
    }

    public int getQuantidadeAsteroides() {
        return quantidadeAsteroides;
    }

    public int getQuantidadeInimigos() {
        return quantidadeInimigos;
    }

    public int getTotalEntidades() {
        return quantidadePassageiros + quantidadeAsteroides + quantidadeInimigos;
    }

    /**
     * Menor tamanho de mapa com células livres para todas as entidades
     * (a célula (0,0) fica reservada para a nave e a plataforma).
     * Evita o laço infinito que o original tinha com mapas muito pequenos.
     */
    public int getTamanhoMinimoMapa() {
        int tamanho = 1;
        while (new Mapa(tamanho).getQuantidadeCelulas() - 1 < getTotalEntidades()) {
            tamanho++;
        }
        return tamanho;
    }

    /** Converte texto digitado (com ou sem acento, maiúsculo ou não). Inválido vira MEDIO. */
    public static Dificuldade deString(String valor) {
        if (valor == null) {
            return MEDIO;
        }
        String normalizado = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase();
        for (Dificuldade dificuldade : values()) {
            if (dificuldade.name().equals(normalizado)) {
                return dificuldade;
            }
        }
        return MEDIO;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
