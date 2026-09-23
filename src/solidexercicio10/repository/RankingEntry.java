package solidexercicio10.repository;

import java.util.Objects;
import solidexercicio10.model.Dificuldade;

/**
 * Registro imutável de uma partida vencida.
 *
 * <p>Os nomes das chaves no JSON são os mesmos do exercício 10, então um
 * {@code ranking.json} antigo continua legível.</p>
 */
public final class RankingEntry {
    private final String nomePiloto;
    private final int pontuacao;
    private final Dificuldade dificuldade;
    private final int passageirosResgatados;
    private final String dataHora;
    private final long tempoJogoSegundos;

    public RankingEntry(String nomePiloto, int pontuacao, Dificuldade dificuldade,
                        int passageirosResgatados, String dataHora, long tempoJogoSegundos) {
        this.nomePiloto = Objects.requireNonNull(nomePiloto);
        this.pontuacao = pontuacao;
        this.dificuldade = Objects.requireNonNull(dificuldade);
        this.passageirosResgatados = passageirosResgatados;
        this.dataHora = dataHora == null ? "" : dataHora;
        this.tempoJogoSegundos = tempoJogoSegundos;
    }

    public String getNomePiloto() {
        return nomePiloto;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public Dificuldade getDificuldade() {
        return dificuldade;
    }

    public int getPassageirosResgatados() {
        return passageirosResgatados;
    }

    public String getDataHora() {
        return dataHora;
    }

    public long getTempoJogoSegundos() {
        return tempoJogoSegundos;
    }
}
