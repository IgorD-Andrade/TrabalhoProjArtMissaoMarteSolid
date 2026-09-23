package solidexercicio10;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.repository.JsonRankingRepository;
import solidexercicio10.repository.RankingEmMemoria;
import solidexercicio10.repository.RankingEntry;
import solidexercicio10.repository.RankingRepository;
import solidexercicio10.service.Comando;
import solidexercicio10.service.GeradorMissao;
import solidexercicio10.service.JogoService;
import solidexercicio10.service.Partida;

final class JogoServiceTest {
    private JogoServiceTest() {
    }

    static void registrar(Map<String, Verifica.Acao> t) {
        t.put("JogoService: piloto vazio vira 'Piloto Anônimo'", JogoServiceTest::pilotoPadrao);
        t.put("JogoService: vitória é registrada com data/hora, dificuldade e passageiros", JogoServiceTest::registraVitoria);
        t.put("JogoService: derrota e missão abortada não entram no ranking", JogoServiceTest::naoRegistraDerrota);
        t.put("JogoService: ranking limitado ao Top 5 em ordem decrescente", JogoServiceTest::top5);
        t.put("JogoService: resetar ranking", JogoServiceTest::resetar);
        t.put("DIP: mesmo serviço funciona com repositório em memória e em JSON", JogoServiceTest::trocaDeRepositorio);
    }

    private static JogoService servico(RankingRepository repo) {
        Random random = new Random(1);
        return new JogoService(repo, new GeradorMissao(Cenarios.TIPOS_PADRAO, random), random, Cenarios.RELOGIO_FIXO);
    }

    private static void pilotoPadrao() {
        Partida partida = servico(new RankingEmMemoria()).iniciarPartida("   ", Dificuldade.FACIL, 3);
        Verifica.igual(JogoService.PILOTO_PADRAO, partida.getPiloto(), "nome padrão");
    }

    private static void registraVitoria() {
        RankingEmMemoria repo = new RankingEmMemoria();
        Verifica.verdadeiro(servico(repo).registrarResultado(Cenarios.partidaVencida(Dificuldade.FACIL)), "registrou");
        RankingEntry e = repo.listar().get(0);
        Verifica.igual(48, e.getPontuacao(), "pontos");
        Verifica.igual(Dificuldade.FACIL, e.getDificuldade(), "dificuldade");
        Verifica.igual(1, e.getPassageirosResgatados(), "passageiros");
        Verifica.igual("2026-09-22 10:00:00", e.getDataHora(), "data/hora (relógio fixo, UTC-3)");
    }

    private static void naoRegistraDerrota() {
        RankingEmMemoria repo = new RankingEmMemoria();
        JogoService servico = servico(repo);
        Partida abortada = servico.iniciarPartida("X", Dificuldade.FACIL, 3);
        abortada.executar(Comando.ABORTAR);
        Verifica.falso(servico.registrarResultado(abortada), "abortada");
        Verifica.verdadeiro(repo.listar().isEmpty(), "ranking vazio");
    }

    private static void top5() {
        RankingEmMemoria repo = new RankingEmMemoria();
        JogoService servico = servico(repo);
        for (int pontos : new int[] {100, 300, 200, 500}) {
            repo.salvar(new RankingEntry("P" + pontos, pontos, Dificuldade.MEDIO, 5, "", 10));
        }
        Verifica.verdadeiro(servico.registrarResultado(Cenarios.partidaVencida(Dificuldade.FACIL)), "4 no ranking: entra");
        Verifica.falso(servico.registrarResultado(Cenarios.partidaVencida(Dificuldade.FACIL)), "Top 5 cheio com 48 no fim: não entra");
        List<Integer> pontos = servico.listarRanking().stream().map(RankingEntry::getPontuacao).toList();
        Verifica.igual(List.of(500, 300, 200, 100, 48), pontos, "ordem");
        Verifica.igual(500, servico.recordeAtual().get().getPontuacao(), "recorde");
    }

    private static void resetar() {
        RankingEmMemoria repo = new RankingEmMemoria();
        JogoService servico = servico(repo);
        servico.registrarResultado(Cenarios.partidaVencida(Dificuldade.MEDIO));
        servico.resetarRanking();
        Verifica.verdadeiro(servico.listarRanking().isEmpty(), "vazio após reset");
    }

    private static void trocaDeRepositorio() throws Exception {
        Path pasta = Files.createTempDirectory("ranking-teste");
        List<RankingRepository> repositorios = List.of(
                new RankingEmMemoria(), new JsonRankingRepository(pasta.resolve("ranking.json")));
        for (RankingRepository repo : repositorios) {
            JogoService servico = servico(repo);
            servico.registrarResultado(Cenarios.partidaVencida(Dificuldade.DIFICIL));
            servico.registrarResultado(Cenarios.partidaVencida(Dificuldade.FACIL));
            List<Integer> pontos = servico.listarRanking().stream().map(RankingEntry::getPontuacao).toList();
            Verifica.igual(List.of(48, 33), pontos, repo.getClass().getSimpleName());
        }
    }
}
