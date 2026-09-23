package solidexercicio10.presentation;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import solidexercicio10.model.EntidadeMapa;
import solidexercicio10.model.Mapa;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Passageiro;

/**
 * Desenha o mapa da missão no console.
 *
 * <p>SRP: só apresenta; não altera a missão nem calcula pontos.</p>
 *
 * <p>OCP: o símbolo vem de {@link EntidadeMapa#getSimbolo()} e a legenda é
 * montada a partir das entidades presentes. Um novo tipo de passageiro aparece
 * no mapa e na legenda sem nenhuma alteração nesta classe (no original e no
 * tutorial havia {@code instanceof}/comparação de texto por tipo).</p>
 */
public class MapaRenderer {
    private static final char VAZIO = '.';
    private final PrintStream saida;

    public MapaRenderer(PrintStream saida) {
        this.saida = saida;
    }

    public void desenhar(Missao missao, String piloto, int pontuacao) {
        Mapa mapa = missao.getMapa();
        saida.println();
        saida.printf("Mapa da Missão (Pontos: %d) - Piloto: %s%n", pontuacao, piloto);
        desenharCabecalho(mapa);
        for (int y = mapa.getMinY(); y <= mapa.getMaxY(); y++) {
            saida.printf("%3d|", y);
            for (int x = mapa.getMinX(); x <= mapa.getMaxX(); x++) {
                char simbolo = missao.entidadeEm(x, y).map(EntidadeMapa::getSimbolo).orElse(VAZIO);
                saida.printf(" %2c", simbolo);
            }
            saida.println();
        }
        saida.println("Legenda: " + montarLegenda(missao));
        saida.println("Comandos: w/s/a/d (mover), c (embarcar), q (sair)");
        saida.println("Passageiros na superfície marciana:");
        for (Passageiro p : missao.getPassageiros()) {
            saida.printf(" - %s (%s) em (%d,%d)%n", p.getNome(), p.getTipo(), p.getX(), p.getY());
        }
    }

    private void desenharCabecalho(Mapa mapa) {
        StringBuilder numeros = new StringBuilder("    ");
        StringBuilder linha = new StringBuilder("    ");
        for (int x = mapa.getMinX(); x <= mapa.getMaxX(); x++) {
            numeros.append(String.format(" %2d", x));
            linha.append(" __");
        }
        saida.println(numeros);
        saida.println(linha);
    }

    private String montarLegenda(Missao missao) {
        Map<Character, String> legenda = new LinkedHashMap<>();
        legenda.put(missao.getNave().getSimbolo(), "Nave");
        Stream.concat(missao.getPassageiros().stream(), missao.getNave().getPassageiros().stream())
                .forEach(p -> legenda.putIfAbsent(p.getSimbolo(), p.getTipo()));
        adicionar(legenda, missao.getAsteroides(), "Asteroide");
        adicionar(legenda, missao.getInimigos(), "Inimigo");
        legenda.put(missao.getPlataforma().getSimbolo(), "Plataforma de Pouso");
        legenda.put(VAZIO, "Vazio");

        StringBuilder texto = new StringBuilder();
        legenda.forEach((simbolo, nome) -> texto.append(texto.length() == 0 ? "" : ", ")
                .append(simbolo).append('=').append(nome));
        return texto.toString();
    }

    private void adicionar(Map<Character, String> legenda, List<? extends EntidadeMapa> entidades, String nome) {
        if (!entidades.isEmpty()) {
            legenda.putIfAbsent(entidades.get(0).getSimbolo(), nome);
        }
    }
}
