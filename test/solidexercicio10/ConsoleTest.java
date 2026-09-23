package solidexercicio10;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.presentation.JogoConsole;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.presentation.PainelInformacoes;
import solidexercicio10.presentation.Terminal;
import solidexercicio10.repository.RankingEmMemoria;
import solidexercicio10.service.GeradorMissao;
import solidexercicio10.service.JogoService;

/** Testes de ponta a ponta: um "jogador" digita pelo Scanner e a saída é conferida. */
final class ConsoleTest {
    private ConsoleTest() {
    }

    static void registrar(Map<String, Verifica.Acao> t) {
        t.put("Console: menu, ranking vazio, reset com confirmação, opção inválida e sair", ConsoleTest::menu);
        t.put("Console: reset cancelado mantém o ranking", ConsoleTest::resetCancelado);
        t.put("Console: entradas inválidas usam padrões (dificuldade e mapa)", ConsoleTest::entradasInvalidas);
        t.put("Console: fim da entrada no meio da partida não lança exceção", ConsoleTest::fimDaEntrada);
        t.put("Console: partida completa até a vitória, Top 5 e ranking", ConsoleTest::vitoriaCompleta);
    }

    private static String jogar(String entrada, long semente, RankingEmMemoria repo) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream saida = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        Random random = new Random(semente);
        JogoService servico = new JogoService(repo, new GeradorMissao(Cenarios.TIPOS_PADRAO, random), random);
        new JogoConsole(servico, new Terminal(new Scanner(entrada), saida),
                new MapaRenderer(saida), new PainelInformacoes(saida)).executar();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static void menu() {
        String saida = jogar("2\n3\ns\n9\n4\n", 1, new RankingEmMemoria());
        Verifica.contem(saida, "1. Iniciar Nova Missão");
        Verifica.contem(saida, "Nenhum registro encontrado");
        Verifica.contem(saida, "Ranking resetado com sucesso!");
        Verifica.contem(saida, "Opção inválida. Tente novamente.");
        Verifica.contem(saida, "Obrigado por jogar");
    }

    private static void resetCancelado() {
        RankingEmMemoria repo = new RankingEmMemoria();
        repo.salvar(new solidexercicio10.repository.RankingEntry("Ana", 50, Dificuldade.FACIL, 4, "", 1));
        String saida = jogar("3\nn\n2\n4\n", 1, repo);
        Verifica.contem(saida, "Operação cancelada.");
        Verifica.contem(saida, "1. Ana - 50 pts");
    }

    private static void entradasInvalidas() {
        String saida = jogar("1\n\nqualquer\nabc\n\nq\n1\nX\ndificil\n1\n\nq\n4\n", 1, new RankingEmMemoria());
        Verifica.contem(saida, "Iniciando missão na dificuldade Médio");
        Verifica.contem(saida, "Entrada inválida, usando tamanho padrão (5).");
        Verifica.contem(saida, "Piloto: Piloto Anônimo");
        Verifica.contem(saida, "Tamanho fora do intervalo para Difícil, usando tamanho padrão (5).");
        Verifica.contem(saida, "Missão abortada pelo piloto.");
    }

    private static void fimDaEntrada() {
        String saida = jogar("1\nIgor\nfacil\n3\n\nd\n", 1, new RankingEmMemoria());
        Verifica.contem(saida, "Missão abortada pelo piloto.");
        Verifica.contem(saida, "Obrigado por jogar");
    }

    private static void vitoriaCompleta() {
        for (long semente = 1; semente < 100; semente++) {
            Optional<List<String>> teclas = PilotoAutomatico.planejarVitoria(semente, Dificuldade.FACIL, 3);
            if (teclas.isEmpty()) {
                continue;
            }
            String entrada = "1\nIgor\nfacil\n3\n\n" + String.join("\n", teclas.get()) + "\n2\n4\n";
            RankingEmMemoria repo = new RankingEmMemoria();
            String saida = jogar(entrada, semente, repo);
            Verifica.contem(saida, "Todos os passageiros resgatados!");
            Verifica.contem(saida, "Missão cumprida!");
            Verifica.contem(saida, "Estatísticas da Partida (vitória)");
            Verifica.contem(saida, "Parabéns! Você entrou para o Top 5 de pilotos!");
            Verifica.contem(saida, "1. Igor - ");
            Verifica.igual(4, repo.listar().get(0).getPassageirosResgatados(), "passageiros no ranking");
            return;
        }
        throw new AssertionError("Nenhuma semente produziu vitória do robô");
    }
}
