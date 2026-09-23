package solidexercicio10;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Direcao;
import solidexercicio10.model.Mapa;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Posicionavel;
import solidexercicio10.repository.RankingEmMemoria;
import solidexercicio10.service.Comando;
import solidexercicio10.service.EstadoPartida;
import solidexercicio10.service.GeradorMissao;
import solidexercicio10.service.JogoService;
import solidexercicio10.service.Partida;

/**
 * "Jogador robô" usado para gerar roteiros de partidas vitoriosas.
 *
 * <p>Com a mesma semente, o jogo real ({@code Main --seed N}) reproduz exatamente
 * a mesma partida, então as teclas planejadas aqui podem ser enviadas ao
 * programa real para testar o fluxo completo até a vitória.</p>
 *
 * <p>Uso: {@code java -cp out solidexercicio10.PilotoAutomatico <semente> <dificuldade> <tamanho>}</p>
 */
final class PilotoAutomatico {
    private static final Map<Comando, String> TECLAS = Map.of(
            Comando.CIMA, "w", Comando.BAIXO, "s", Comando.ESQUERDA, "a",
            Comando.DIREITA, "d", Comando.EMBARCAR, "c", Comando.ABORTAR, "q");

    private PilotoAutomatico() {
    }

    /** Teclas de uma partida vencida ou vazio se o robô perder com essa semente. */
    static Optional<List<String>> planejarVitoria(long semente, Dificuldade dificuldade, int tamanho) {
        Random random = new Random(semente);
        JogoService servico = new JogoService(new RankingEmMemoria(),
                new GeradorMissao(Cenarios.TIPOS_PADRAO, random), random);
        Partida partida = servico.iniciarPartida("Robo", dificuldade, tamanho);
        List<String> teclas = new ArrayList<>();
        while (partida.emAndamento() && teclas.size() < 500) {
            Comando comando = proximoComando(partida.getMissao());
            teclas.add(TECLAS.get(comando));
            partida.executar(comando);
        }
        return partida.getEstado() == EstadoPartida.VITORIA ? Optional.of(teclas) : Optional.empty();
    }

    static Comando proximoComando(Missao missao) {
        if (missao.passageiroNaPosicaoDaNave().isPresent()) {
            return Comando.EMBARCAR;
        }
        List<Posicionavel> alvos = new ArrayList<>(missao.getPassageiros());
        if (alvos.isEmpty()) {
            alvos.add(missao.getPlataforma());
        }
        return primeiroPasso(missao, alvos).orElse(Comando.EMBARCAR); // sem caminho: espera
    }

    /** Busca em largura até o alvo mais próximo, evitando asteroides e inimigos. */
    private static Optional<Comando> primeiroPasso(Missao missao, List<Posicionavel> alvos) {
        Mapa mapa = missao.getMapa();
        String inicio = chave(missao.getNave().getX(), missao.getNave().getY());
        Map<String, Comando> primeiro = new HashMap<>();
        Queue<int[]> fila = new ArrayDeque<>();
        fila.add(new int[] {missao.getNave().getX(), missao.getNave().getY()});
        primeiro.put(inicio, null);
        while (!fila.isEmpty()) {
            int[] atual = fila.poll();
            for (Comando c : new Comando[] {Comando.CIMA, Comando.BAIXO, Comando.ESQUERDA, Comando.DIREITA}) {
                Direcao d = c.getDirecao();
                int x = atual[0] + d.getDx();
                int y = atual[1] + d.getDy();
                String k = chave(x, y);
                if (!mapa.contem(x, y) || primeiro.containsKey(k) || perigo(missao, x, y)) {
                    continue;
                }
                Comando passo = primeiro.get(chave(atual[0], atual[1]));
                primeiro.put(k, passo == null ? c : passo);
                for (Posicionavel alvo : alvos) {
                    if (alvo.estaEm(x, y)) {
                        return Optional.of(primeiro.get(k));
                    }
                }
                fila.add(new int[] {x, y});
            }
        }
        return Optional.empty();
    }

    private static boolean perigo(Missao missao, int x, int y) {
        return missao.getAsteroides().stream().anyMatch(a -> a.estaEm(x, y))
                || missao.getInimigos().stream().anyMatch(i -> Math.abs(i.getX() - x) + Math.abs(i.getY() - y) <= 1);
    }

    private static String chave(int x, int y) {
        return x + "," + y;
    }

    public static void main(String[] args) {
        long semente = Long.parseLong(args[0]);
        Dificuldade dificuldade = Dificuldade.deString(args[1]);
        int tamanho = Integer.parseInt(args[2]);
        planejarVitoria(semente, dificuldade, tamanho).ifPresentOrElse(
                teclas -> teclas.forEach(System.out::println),
                () -> System.err.println("O robô não venceu com essa semente."));
    }
}
