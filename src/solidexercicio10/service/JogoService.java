package solidexercicio10.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Mapa;
import solidexercicio10.repository.RankingEntry;
import solidexercicio10.repository.RankingRepository;

/**
 * Casos de uso do jogo: iniciar partida, registrar resultado, consultar e
 * resetar o ranking.
 *
 * <p>DIP: recebe {@link RankingRepository} pelo construtor e não sabe se o
 * ranking está em JSON, em memória ou em um banco.</p>
 *
 * <p>Diferença para o tutorial: aqui não há {@code Scanner} nem
 * {@code System.out}. O menu e as mensagens ficam em
 * {@code presentation.JogoConsole}.</p>
 */
public class JogoService {
    public static final int TAMANHO_TOP = 5;
    public static final String PILOTO_PADRAO = "Piloto Anônimo";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RankingRepository rankingRepository;
    private final GeradorMissao geradorMissao;
    private final Random random;
    private final Clock relogio;

    public JogoService(RankingRepository rankingRepository, GeradorMissao geradorMissao, Random random) {
        this(rankingRepository, geradorMissao, random, Clock.systemDefaultZone());
    }

    public JogoService(RankingRepository rankingRepository, GeradorMissao geradorMissao,
                       Random random, Clock relogio) {
        this.rankingRepository = rankingRepository;
        this.geradorMissao = geradorMissao;
        this.random = random;
        this.relogio = relogio;
    }

    public Partida iniciarPartida(String piloto, Dificuldade dificuldade, int tamanhoMapa) {
        String nome = (piloto == null || piloto.isBlank()) ? PILOTO_PADRAO : piloto.trim();
        return new Partida(nome, dificuldade, geradorMissao.gerar(dificuldade, new Mapa(tamanhoMapa)),
                random, relogio);
    }

    /** Top 5 em ordem decrescente de pontuação. */
    public List<RankingEntry> listarRanking() {
        return rankingRepository.listar().stream()
                .sorted(Comparator.comparingInt(RankingEntry::getPontuacao).reversed())
                .limit(TAMANHO_TOP)
                .collect(Collectors.toList());
    }

    public Optional<RankingEntry> recordeAtual() {
        return listarRanking().stream().findFirst();
    }

    /**
     * Salva a partida se for vitória com pontuação positiva e entrar no Top 5
     * (mesma regra do exercício 10).
     *
     * @return {@code true} se o piloto entrou no ranking
     */
    public boolean registrarResultado(Partida partida) {
        if (partida.getEstado() != EstadoPartida.VITORIA || partida.getPontuacao() <= 0) {
            return false;
        }
        if (!entraNoTop(partida.getPontuacao())) {
            return false;
        }
        rankingRepository.salvar(new RankingEntry(
                partida.getPiloto(),
                partida.getPontuacao(),
                partida.getDificuldade(),
                partida.getPassageirosResgatados(),
                LocalDateTime.now(relogio).format(FORMATO_DATA),
                partida.getDuracaoSegundos()));
        return true;
    }

    public void resetarRanking() {
        rankingRepository.limpar();
    }

    private boolean entraNoTop(int pontuacao) {
        List<RankingEntry> top = listarRanking();
        return top.size() < TAMANHO_TOP || pontuacao > top.get(top.size() - 1).getPontuacao();
    }
}
