package solidexercicio10;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.repository.JsonRankingRepository;
import solidexercicio10.repository.RankingEntry;

final class RepositoryTest {
    private RepositoryTest() {
    }

    static void registrar(Map<String, Verifica.Acao> t) {
        t.put("JSON: salvar e carregar preserva todos os campos (nome com vírgula e aspas)", RepositoryTest::idaEVolta);
        t.put("JSON: arquivo inexistente ou vazio resulta em ranking vazio", RepositoryTest::arquivoAusente);
        t.put("JSON: arquivo corrompido não quebra e é preservado ao salvar", RepositoryTest::arquivoCorrompido);
        t.put("JSON: lê arquivo no formato do exercício 10", RepositoryTest::formatoOriginal);
        t.put("JSON: limpar remove o arquivo", RepositoryTest::limpar);
    }

    private static Path arquivoTemporario() throws Exception {
        return Files.createTempDirectory("ranking-json").resolve("ranking.json");
    }

    private static void idaEVolta() throws Exception {
        JsonRankingRepository repo = new JsonRankingRepository(arquivoTemporario());
        String nome = "Silva, \"J.\" \\ çã";
        repo.salvar(new RankingEntry(nome, 77, Dificuldade.DIFICIL, 5, "2026-09-22 10:00:00", 42));
        repo.salvar(new RankingEntry("Ana", 50, Dificuldade.FACIL, 4, "2026-09-22 11:00:00", 30));
        List<RankingEntry> lidos = new JsonRankingRepository(repo.getArquivo()).listar();
        Verifica.igual(2, lidos.size(), "quantidade");
        RankingEntry e = lidos.get(0);
        Verifica.igual(nome, e.getNomePiloto(), "nome");
        Verifica.igual(77, e.getPontuacao(), "pontos");
        Verifica.igual(Dificuldade.DIFICIL, e.getDificuldade(), "dificuldade");
        Verifica.igual(5, e.getPassageirosResgatados(), "passageiros");
        Verifica.igual("2026-09-22 10:00:00", e.getDataHora(), "data/hora");
        Verifica.igual(42L, e.getTempoJogoSegundos(), "tempo");
    }

    private static void arquivoAusente() throws Exception {
        Path arquivo = arquivoTemporario();
        Verifica.verdadeiro(new JsonRankingRepository(arquivo).listar().isEmpty(), "inexistente");
        Files.writeString(arquivo, "   ");
        Verifica.verdadeiro(new JsonRankingRepository(arquivo).listar().isEmpty(), "vazio");
        Files.writeString(arquivo, "[]");
        Verifica.verdadeiro(new JsonRankingRepository(arquivo).listar().isEmpty(), "array vazio");
    }

    private static void arquivoCorrompido() throws Exception {
        Path arquivo = arquivoTemporario();
        Files.writeString(arquivo, "[{\"name\":\"Ana\",\"score\":");
        JsonRankingRepository repo = new JsonRankingRepository(arquivo);
        Verifica.verdadeiro(repo.listar().isEmpty(), "listar não lança exceção");
        repo.salvar(new RankingEntry("Bia", 10, Dificuldade.MEDIO, 1, "", 1));
        Verifica.igual(1, repo.listar().size(), "novo arquivo válido");
        Path copia = arquivo.resolveSibling("ranking.json.corrompido");
        Verifica.verdadeiro(Files.exists(copia), "cópia do corrompido preservada");
    }

    private static void formatoOriginal() throws Exception {
        Path arquivo = arquivoTemporario();
        Files.writeString(arquivo, "[{\"name\":\"Igor\",\"score\":40,\"dificuldade\":\"FACIL\","
                + "\"passageirosColetados\":4,\"dataHora\":\"2026-09-01 10:00:00\",\"tempoJogo\":35}]",
                StandardCharsets.UTF_8);
        List<RankingEntry> lidos = new JsonRankingRepository(arquivo).listar();
        Verifica.igual(1, lidos.size(), "registro lido");
        Verifica.igual("Igor", lidos.get(0).getNomePiloto(), "nome");
        Verifica.igual(Dificuldade.FACIL, lidos.get(0).getDificuldade(), "dificuldade");
    }

    private static void limpar() throws Exception {
        Path arquivo = arquivoTemporario();
        JsonRankingRepository repo = new JsonRankingRepository(arquivo);
        repo.limpar(); // não existe ainda: não deve falhar
        repo.salvar(new RankingEntry("Ana", 10, Dificuldade.MEDIO, 1, "", 1));
        repo.limpar();
        Verifica.falso(Files.exists(arquivo), "arquivo removido");
        Verifica.verdadeiro(repo.listar().isEmpty(), "vazio");
    }
}
