package solidexercicio10.presentation;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.repository.RankingEntry;
import solidexercicio10.repository.RankingPersistenciaException;
import solidexercicio10.service.Comando;
import solidexercicio10.service.JogoService;
import solidexercicio10.service.Partida;
import solidexercicio10.service.ResultadoTurno;

/**
 * Menu principal e laço de interação no console.
 *
 * <p>SRP: traduz o que o jogador digita em chamadas ao {@link JogoService} e à
 * {@link Partida}, e delega a exibição para {@link MapaRenderer} e
 * {@link PainelInformacoes}. Não contém regra de pontuação, vidas ou vitória.</p>
 */
public class JogoConsole {
    static final int TAMANHO_PADRAO = 5;
    static final int TAMANHO_MAXIMO = 20;

    private static final Map<Character, Comando> TECLAS = Map.of(
            'w', Comando.CIMA,
            's', Comando.BAIXO,
            'a', Comando.ESQUERDA,
            'd', Comando.DIREITA,
            'c', Comando.EMBARCAR,
            'q', Comando.ABORTAR);

    private final JogoService jogoService;
    private final Terminal terminal;
    private final MapaRenderer mapaRenderer;
    private final PainelInformacoes painel;

    public JogoConsole(JogoService jogoService, Terminal terminal,
                       MapaRenderer mapaRenderer, PainelInformacoes painel) {
        this.jogoService = jogoService;
        this.terminal = terminal;
        this.mapaRenderer = mapaRenderer;
        this.painel = painel;
    }

    public void executar() {
        painel.boasVindas();
        boolean rodando = true;
        while (rodando) {
            painel.menu();
            Optional<String> opcao = terminal.lerLinha("Escolha uma opção: ");
            if (opcao.isEmpty()) {
                break; // entrada encerrada
            }
            switch (opcao.get()) {
                case "1" -> jogarPartida();
                case "2" -> painel.ranking(jogoService.listarRanking());
                case "3" -> resetarRanking();
                case "4" -> rodando = false;
                default -> painel.mensagem("Opção inválida. Tente novamente.");
            }
        }
        painel.mensagem("\nObrigado por jogar a Missão Marte Unifor!");
    }

    private void jogarPartida() {
        String piloto = terminal.lerLinha("\nDigite o nome do piloto: ").orElse("");
        Dificuldade dificuldade = Dificuldade.deString(
                terminal.lerLinha("Escolha a Dificuldade (" + opcoesDificuldade() + "): ").orElse("medio"));
        int tamanho = lerTamanhoMapa(dificuldade);

        painel.mensagem("\nIniciando missão na dificuldade " + dificuldade + "...");
        terminal.lerLinha("Pressione Enter para decolar!");

        Partida partida = jogoService.iniciarPartida(piloto, dificuldade, tamanho);
        while (partida.emAndamento()) {
            mapaRenderer.desenhar(partida.getMissao(), partida.getPiloto(), partida.getPontuacao());
            painel.situacaoNave(partida);
            Optional<Comando> comando = lerComando();
            if (comando.isEmpty()) {
                continue; // comando vazio ou inválido: inimigos não se movem, como no original
            }
            ResultadoTurno resultado = partida.executar(comando.get());
            painel.resultadoTurno(resultado, partida);
        }
        finalizarPartida(partida);
    }

    /** Gerado a partir do enum: uma nova dificuldade aparece aqui sem editar esta classe. */
    private static String opcoesDificuldade() {
        return Arrays.stream(Dificuldade.values())
                .map(d -> d.name().toLowerCase())
                .collect(Collectors.joining("/"));
    }

    private Optional<Comando> lerComando() {
        Optional<String> linha = terminal.lerLinha("Comando (w/s/a/d/c/q): ");
        if (linha.isEmpty()) {
            return Optional.of(Comando.ABORTAR); // entrada encerrada no meio da partida
        }
        String texto = linha.get().toLowerCase();
        if (texto.isEmpty()) {
            return Optional.empty();
        }
        Comando comando = TECLAS.get(texto.charAt(0));
        if (comando == null) {
            painel.mensagem("Comando inválido.");
        }
        return Optional.ofNullable(comando);
    }

    private int lerTamanhoMapa(Dificuldade dificuldade) {
        int minimo = dificuldade.getTamanhoMinimoMapa();
        String prompt = String.format("Tamanho do mapa (%d a %d, ex: 5 para mapa de -5 a +5): ", minimo, TAMANHO_MAXIMO);
        String texto = terminal.lerLinha(prompt).orElse("");
        try {
            int tamanho = Integer.parseInt(texto);
            if (tamanho >= minimo && tamanho <= TAMANHO_MAXIMO) {
                return tamanho;
            }
            painel.mensagem(String.format("Tamanho fora do intervalo para %s, usando tamanho padrão (%d).",
                    dificuldade, TAMANHO_PADRAO));
        } catch (NumberFormatException e) {
            painel.mensagem("Entrada inválida, usando tamanho padrão (" + TAMANHO_PADRAO + ").");
        }
        return TAMANHO_PADRAO;
    }

    private void finalizarPartida(Partida partida) {
        Optional<RankingEntry> recordeAnterior = jogoService.recordeAtual();
        painel.estatisticas(partida, recordeAnterior);
        try {
            if (jogoService.registrarResultado(partida)) {
                painel.mensagem("Parabéns! Você entrou para o Top 5 de pilotos!");
            }
        } catch (RankingPersistenciaException e) {
            painel.mensagem("Não foi possível salvar o ranking: " + e.getMessage());
        }
    }

    private void resetarRanking() {
        String resposta = terminal.lerLinha("Você realmente deseja limpar o histórico de ranking? (s/n): ")
                .orElse("n").toLowerCase();
        if (!resposta.equals("s") && !resposta.equals("sim")) {
            painel.mensagem("Operação cancelada.");
            return;
        }
        try {
            jogoService.resetarRanking();
            painel.mensagem("Ranking resetado com sucesso!");
        } catch (RankingPersistenciaException e) {
            painel.mensagem("Erro ao resetar ranking: " + e.getMessage());
        }
    }
}
