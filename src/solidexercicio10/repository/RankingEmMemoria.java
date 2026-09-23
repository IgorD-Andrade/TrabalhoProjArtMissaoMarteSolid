package solidexercicio10.repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação em memória, usada nos testes automatizados.
 *
 * <p>É a prova prática do DIP/LSP: o {@code JogoService} funciona igual com
 * ela ou com {@link JsonRankingRepository}, sem nenhuma alteração.</p>
 */
public class RankingEmMemoria implements RankingRepository {
    private final List<RankingEntry> entradas = new ArrayList<>();

    @Override
    public void salvar(RankingEntry entrada) {
        entradas.add(entrada);
    }

    @Override
    public List<RankingEntry> listar() {
        return new ArrayList<>(entradas);
    }

    @Override
    public void limpar() {
        entradas.clear();
    }
}
