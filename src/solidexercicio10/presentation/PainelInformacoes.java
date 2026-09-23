package solidexercicio10.presentation;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import solidexercicio10.model.Nave;
import solidexercicio10.repository.RankingEntry;
import solidexercicio10.service.EventoTurno;
import solidexercicio10.service.Partida;
import solidexercicio10.service.ResultadoTurno;

/**
 * Textos exibidos ao jogador: menu, painel da nave, mensagens de turno,
 * estatísticas e ranking.
 *
 * <p>SRP: separado de {@link MapaRenderer} porque o mapa muda por motivos
 * visuais (símbolos, grade) e estes textos mudam por motivos de comunicação
 * (idioma, redação).</p>
 */
public class PainelInformacoes {
    private static final String LINHA = "================================================================";
    private final PrintStream saida;

    public PainelInformacoes(PrintStream saida) {
        this.saida = saida;
    }

    public void boasVindas() {
        saida.println(LINHA);
        saida.println("             MISSÃO MARTE UNIFOR - VERSÃO SOLID                 ");
        saida.println(LINHA);
        saida.println("  Pilote sua nave, salve os passageiros e desvie dos perigos!   ");
        saida.println(LINHA);
    }

    public void menu() {
        saida.println("\n--- MENU PRINCIPAL ---");
        saida.println("1. Iniciar Nova Missão");
        saida.println("2. Visualizar Ranking Top 5");
        saida.println("3. Resetar Histórico de Ranking");
        saida.println("4. Sair do Jogo");
        saida.println("----------------------");
    }

    public void mensagem(String texto) {
        saida.println(texto);
    }

    public void situacaoNave(Partida partida) {
        Nave nave = partida.getMissao().getNave();
        saida.printf("Nave em (%d,%d) | Pontos: %d | Vidas: %d | A bordo: %d/%d | Restantes: %d%n",
                nave.getX(), nave.getY(), partida.getPontuacao(), nave.getVidas(),
                nave.getQuantidadeABordo(), nave.getCapacidade(),
                partida.getMissao().getPassageiros().size());
    }

    public void resultadoTurno(ResultadoTurno resultado, Partida partida) {
        for (EventoTurno evento : resultado.getEventos()) {
            saida.println(texto(evento, resultado, partida));
        }
    }

    /** Switch exaustivo: se um novo evento for criado, o compilador obriga a tratá-lo aqui. */
    private String texto(EventoTurno evento, ResultadoTurno resultado, Partida partida) {
        return switch (evento) {
            case PASSAGEIRO_EMBARCADO -> resultado.getPassageiroEmbarcado()
                    .map(p -> String.format("Passageiro %s embarcado com sucesso! +%d pontos!",
                            p.getNome(), p.getPontuacao()))
                    .orElse("Passageiro embarcado!");
            case SEM_PASSAGEIRO -> "Nenhum passageiro nesta posição.";
            case NAVE_CHEIA -> "Nave cheia! Não há espaço para mais passageiros.";
            case COLISAO -> String.format("Alerta! Colisão detectada! Vidas restantes: %d",
                    partida.getMissao().getNave().getVidas());
            case NAVE_DESTRUIDA -> "GAME OVER! A nave foi destruída.";
            case SEM_COMBUSTIVEL -> "Combustível/Pontuação zerada! Missão perdida.";
            case TODOS_RESGATADOS -> "✨ ALERTA: Todos os passageiros resgatados! "
                    + "Retorne para a Plataforma de Pouso 'L' em (0,0) para completar a missão.";
            case MISSAO_CUMPRIDA -> "\n" + LINHA
                    + "\n🚀 DECOLAGEM AUTORIZADA! Nave acoplada à plataforma em (0,0)."
                    + "\nRetornando à órbita marciana com todos os passageiros. Missão cumprida!\n" + LINHA;
            case MISSAO_ABORTADA -> "Missão abortada pelo piloto.";
        };
    }

    public void estatisticas(Partida partida, Optional<RankingEntry> recordeAnterior) {
        saida.println("Estatísticas da Partida (" + descreverResultado(partida) + "):");
        saida.printf(" - Pontuação Final: %d pontos%n", partida.getPontuacao());
        saida.printf(" - Movimentos Efetuados: %d%n", partida.getMovimentos());
        saida.printf(" - Tempo de Jogo: %d segundos%n", partida.getDuracaoSegundos());
        saida.printf(" - Passageiros Resgatados: %d%n", partida.getPassageirosResgatados());
        saida.printf(" - Vidas Restantes: %d%n", partida.getMissao().getNave().getVidas());
        recordeAnterior.ifPresent(recorde -> {
            if (partida.getPontuacao() > recorde.getPontuacao()) {
                saida.println("🏆 Novo recorde absoluto do sistema!");
            } else {
                saida.printf(" - Recorde atual a ser batido: %d pontos (Piloto: %s)%n",
                        recorde.getPontuacao(), recorde.getNomePiloto());
            }
        });
        saida.println(LINHA);
    }

    private String descreverResultado(Partida partida) {
        return switch (partida.getEstado()) {
            case VITORIA -> "vitória";
            case DERROTA -> "derrota";
            case ABORTADA -> "abortada";
            case EM_ANDAMENTO -> "em andamento";
        };
    }

    public void ranking(List<RankingEntry> ranking) {
        saida.println("\n====== RANKING TOP 5 PILOTOS ======");
        if (ranking.isEmpty()) {
            saida.println(" - Nenhum registro encontrado. Seja o primeiro a jogar!");
        }
        for (int i = 0; i < ranking.size(); i++) {
            RankingEntry e = ranking.get(i);
            saida.printf("%d. %s - %d pts | Dificuldade: %s | Coletados: %d | Tempo: %ds | %s%n",
                    i + 1, e.getNomePiloto(), e.getPontuacao(), e.getDificuldade(),
                    e.getPassageirosResgatados(), e.getTempoJogoSegundos(), e.getDataHora());
        }
        saida.println("===================================");
    }
}
