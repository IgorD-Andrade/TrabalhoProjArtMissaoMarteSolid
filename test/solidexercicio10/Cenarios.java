package solidexercicio10;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import solidexercicio10.model.Astronauta;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Engenheiro;
import solidexercicio10.model.Mapa;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;
import solidexercicio10.model.Professor;
import solidexercicio10.service.Comando;
import solidexercicio10.service.FabricaPassageiro;
import solidexercicio10.service.Partida;

/** Montagem de cenários controlados para os testes. */
final class Cenarios {
    static final Clock RELOGIO_FIXO = Clock.fixed(Instant.parse("2026-09-22T13:00:00Z"), ZoneId.of("America/Fortaleza"));

    static final List<FabricaPassageiro> TIPOS_PADRAO = List.of(
            (x, y) -> new Professor("Dr. Silva", x, y),
            (x, y) -> new Engenheiro("Eng. Rosa", x, y),
            (x, y) -> new Professor("Dr. Lima", x, y),
            (x, y) -> new Engenheiro("Eng. Carlos", x, y),
            (x, y) -> new Astronauta("Ast. Maria", x, y));

    private Cenarios() {
    }

    static Missao missaoVazia(int tamanho) {
        return new Missao(new Mapa(tamanho), new Nave("T-1", 0, 0));
    }

    static Partida partida(Dificuldade dificuldade, Missao missao) {
        return new Partida("Teste", dificuldade, missao, new Random(1), RELOGIO_FIXO);
    }

    /** Partida vencida: um astronauta em (1,0); direita, embarca, volta. */
    static Partida partidaVencida(Dificuldade dificuldade) {
        Missao missao = missaoVazia(2);
        missao.adicionarPassageiro(new Astronauta("Ast", 1, 0));
        Partida partida = partida(dificuldade, missao);
        partida.executar(Comando.DIREITA);
        partida.executar(Comando.EMBARCAR);
        partida.executar(Comando.ESQUERDA);
        return partida;
    }
}
