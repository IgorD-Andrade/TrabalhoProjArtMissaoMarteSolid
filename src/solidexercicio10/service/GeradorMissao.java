package solidexercicio10.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import solidexercicio10.model.Asteroide;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Inimigo;
import solidexercicio10.model.Mapa;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;

/**
 * Monta uma {@link Missao} nova para a dificuldade e o mapa escolhidos.
 *
 * <p>SRP: "como o mapa é povoado" muda por motivos diferentes de "como um
 * turno é jogado", por isso fica fora de {@link Partida}.</p>
 *
 * <p>Correção em relação ao original: em vez de sortear coordenadas até achar
 * uma livre (laço infinito quando o mapa é pequeno), embaralha a lista de
 * células livres e percorre em ordem.</p>
 */
public class GeradorMissao {
    private final List<FabricaPassageiro> tiposPassageiro;
    private final Random random;

    public GeradorMissao(List<FabricaPassageiro> tiposPassageiro, Random random) {
        if (tiposPassageiro.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um tipo de passageiro");
        }
        this.tiposPassageiro = List.copyOf(tiposPassageiro);
        this.random = random;
    }

    /** @throws IllegalArgumentException se o mapa não comportar as entidades da dificuldade */
    public Missao gerar(Dificuldade dificuldade, Mapa mapa) {
        if (mapa.getTamanho() < dificuldade.getTamanhoMinimoMapa()) {
            throw new IllegalArgumentException(String.format(
                    "Mapa %d pequeno demais para a dificuldade %s (mínimo %d)",
                    mapa.getTamanho(), dificuldade, dificuldade.getTamanhoMinimoMapa()));
        }
        Missao missao = new Missao(mapa, new Nave("A-1", 0, 0));
        Iterator<int[]> livres = posicoesLivresEmbaralhadas(missao).iterator();

        for (int i = 0; i < dificuldade.getQuantidadePassageiros(); i++) {
            int[] p = livres.next();
            FabricaPassageiro fabrica = tiposPassageiro.get(i % tiposPassageiro.size());
            missao.adicionarPassageiro(fabrica.criar(p[0], p[1]));
        }
        for (int i = 0; i < dificuldade.getQuantidadeAsteroides(); i++) {
            int[] p = livres.next();
            missao.adicionarAsteroide(new Asteroide(p[0], p[1]));
        }
        for (int i = 0; i < dificuldade.getQuantidadeInimigos(); i++) {
            int[] p = livres.next();
            missao.adicionarInimigo(new Inimigo(p[0], p[1]));
        }
        return missao;
    }

    private List<int[]> posicoesLivresEmbaralhadas(Missao missao) {
        Mapa mapa = missao.getMapa();
        List<int[]> livres = new ArrayList<>();
        for (int y = mapa.getMinY(); y <= mapa.getMaxY(); y++) {
            for (int x = mapa.getMinX(); x <= mapa.getMaxX(); x++) {
                if (missao.posicaoLivre(x, y)) {
                    livres.add(new int[] {x, y});
                }
            }
        }
        Collections.shuffle(livres, random);
        return livres;
    }
}
