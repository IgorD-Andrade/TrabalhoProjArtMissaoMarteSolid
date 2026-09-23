package solidexercicio10.repository;

import java.util.List;

/**
 * Contrato de persistência do ranking.
 *
 * <p>DIP: {@code JogoService} depende desta interface, nunca de arquivo.
 * Trocar JSON por banco de dados ou memória é criar outra implementação e
 * mudar uma linha no {@code Main}.</p>
 *
 * <p>ISP: três operações, todas usadas pelo serviço. A sobrecarga
 * {@code salvar(nome, pontuacao)} do tutorial foi removida porque nenhum
 * cliente a usava.</p>
 */
public interface RankingRepository {
    void salvar(RankingEntry entrada);

    /** Todos os registros, sem ordem garantida. Nunca retorna {@code null}. */
    List<RankingEntry> listar();

    /** Remove todos os registros. Não falha se ainda não houver registros. */
    void limpar();
}
