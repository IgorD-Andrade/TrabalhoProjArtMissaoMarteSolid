package solidexercicio10;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import solidexercicio10.model.Asteroide;
import solidexercicio10.model.Astronauta;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.EntidadeMapa;
import solidexercicio10.model.Mapa;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;
import solidexercicio10.model.Passageiro;
import solidexercicio10.model.Professor;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.service.Comando;
import solidexercicio10.service.EstadoPartida;
import solidexercicio10.service.EventoTurno;
import solidexercicio10.service.GeradorMissao;
import solidexercicio10.service.Partida;
import solidexercicio10.service.ResultadoTurno;

final class PartidaTest {
    private PartidaTest() {
    }

    static void registrar(Map<String, Verifica.Acao> t) {
        t.put("Partida: movimento custa 1 ponto, inclusive contra a borda", PartidaTest::movimentoCusta);
        t.put("Partida: embarque soma a pontuação polimórfica", PartidaTest::embarqueSoma);
        t.put("Partida: embarcar sem passageiro não altera pontos", PartidaTest::semPassageiro);
        t.put("Partida: nave cheia recusa embarque", PartidaTest::naveCheia);
        t.put("Partida: vitória só com todos resgatados E na plataforma (0,0)", PartidaTest::vitoriaNaPlataforma);
        t.put("Partida: colisões tiram vidas até o Game Over", PartidaTest::colisoesAteGameOver);
        t.put("Partida: pontuação zerada encerra com derrota", PartidaTest::semCombustivel);
        t.put("Partida: abortar encerra e bloqueia novos comandos", PartidaTest::abortar);
        t.put("GeradorMissao: quantidades por dificuldade, sem sobreposição", PartidaTest::geradorQuantidades);
        t.put("GeradorMissao: mapa pequeno falha rápido (original travava)", PartidaTest::geradorMapaPequeno);
        t.put("GeradorMissao: rotação de tipos igual ao original", PartidaTest::geradorRotacao);
        t.put("OCP: novo passageiro (Médico) funciona sem alterar código existente", PartidaTest::novoTipoPassageiro);
    }

    private static void movimentoCusta() {
        Missao missao = Cenarios.missaoVazia(1);
        missao.adicionarPassageiro(new Professor("P", 1, 1));
        Partida partida = Cenarios.partida(Dificuldade.FACIL, missao);
        partida.executar(Comando.CIMA);
        partida.executar(Comando.CIMA); // borda
        Verifica.igual(28, partida.getPontuacao(), "30 - 2");
        Verifica.igual(2, partida.getMovimentos(), "movimentos");
        Verifica.igual(-1, missao.getNave().getY(), "posição limitada");
    }

    private static void embarqueSoma() {
        Missao missao = Cenarios.missaoVazia(2);
        missao.adicionarPassageiro(new Astronauta("Ast", 1, 0));
        missao.adicionarPassageiro(new Professor("Prof", 2, 2));
        Partida partida = Cenarios.partida(Dificuldade.FACIL, missao);
        partida.executar(Comando.DIREITA);
        ResultadoTurno r = partida.executar(Comando.EMBARCAR);
        Verifica.verdadeiro(r.contem(EventoTurno.PASSAGEIRO_EMBARCADO), "evento de embarque");
        Verifica.igual("Ast", r.getPassageiroEmbarcado().get().getNome(), "passageiro do evento");
        Verifica.igual(49, partida.getPontuacao(), "30 - 1 + 20");
    }

    private static void semPassageiro() {
        Missao missao = Cenarios.missaoVazia(2);
        missao.adicionarPassageiro(new Professor("Prof", 2, 2));
        Partida partida = Cenarios.partida(Dificuldade.MEDIO, missao);
        ResultadoTurno r = partida.executar(Comando.EMBARCAR);
        Verifica.verdadeiro(r.contem(EventoTurno.SEM_PASSAGEIRO), "evento");
        Verifica.igual(20, partida.getPontuacao(), "pontos inalterados");
    }

    private static void naveCheia() {
        Missao missao = new Missao(new Mapa(2), new Nave("T", 0, 0, 1, 3));
        missao.adicionarPassageiro(new Professor("A", 1, 0));
        missao.adicionarPassageiro(new Professor("B", 2, 0));
        Partida partida = Cenarios.partida(Dificuldade.FACIL, missao);
        partida.executar(Comando.DIREITA);
        partida.executar(Comando.EMBARCAR);
        partida.executar(Comando.DIREITA);
        ResultadoTurno r = partida.executar(Comando.EMBARCAR);
        Verifica.verdadeiro(r.contem(EventoTurno.NAVE_CHEIA), "nave cheia");
        Verifica.igual(1, missao.getPassageiros().size(), "B continua na superfície");
    }

    private static void vitoriaNaPlataforma() {
        Missao missao = Cenarios.missaoVazia(2);
        missao.adicionarPassageiro(new Astronauta("Ast", 1, 0));
        Partida partida = Cenarios.partida(Dificuldade.FACIL, missao);
        partida.executar(Comando.DIREITA);
        ResultadoTurno r = partida.executar(Comando.EMBARCAR);
        Verifica.verdadeiro(r.contem(EventoTurno.TODOS_RESGATADOS), "alerta de retorno");
        Verifica.igual(EstadoPartida.EM_ANDAMENTO, partida.getEstado(), "ainda não venceu fora de (0,0)");
        r = partida.executar(Comando.ESQUERDA);
        Verifica.verdadeiro(r.contem(EventoTurno.MISSAO_CUMPRIDA), "missão cumprida");
        Verifica.igual(EstadoPartida.VITORIA, partida.getEstado(), "vitória");
        Verifica.igual(48, partida.getPontuacao(), "30 - 2 + 20");
    }

    private static void colisoesAteGameOver() {
        Missao missao = Cenarios.missaoVazia(2);
        missao.adicionarAsteroide(new Asteroide(1, 0));
        missao.adicionarPassageiro(new Professor("P", -2, -2));
        Partida partida = Cenarios.partida(Dificuldade.FACIL, missao);
        Verifica.verdadeiro(partida.executar(Comando.DIREITA).contem(EventoTurno.COLISAO), "1ª colisão");
        Verifica.igual(2, missao.getNave().getVidas(), "2 vidas");
        Verifica.verdadeiro(partida.executar(Comando.EMBARCAR).contem(EventoTurno.COLISAO), "2ª colisão");
        ResultadoTurno r = partida.executar(Comando.EMBARCAR);
        Verifica.verdadeiro(r.contem(EventoTurno.NAVE_DESTRUIDA), "game over");
        Verifica.igual(EstadoPartida.DERROTA, partida.getEstado(), "derrota");
    }

    private static void semCombustivel() {
        Missao missao = Cenarios.missaoVazia(2);
        missao.adicionarPassageiro(new Professor("P", 2, 2));
        Partida partida = Cenarios.partida(Dificuldade.DIFICIL, missao);
        ResultadoTurno r = null;
        for (int i = 0; i < 15; i++) {
            r = partida.executar(Comando.ESQUERDA);
        }
        Verifica.verdadeiro(r.contem(EventoTurno.SEM_COMBUSTIVEL), "sem combustível");
        Verifica.igual(EstadoPartida.DERROTA, partida.getEstado(), "derrota");
    }

    private static void abortar() {
        Missao missao = Cenarios.missaoVazia(2);
        missao.adicionarPassageiro(new Professor("P", 2, 2));
        Partida partida = Cenarios.partida(Dificuldade.FACIL, missao);
        Verifica.verdadeiro(partida.executar(Comando.ABORTAR).contem(EventoTurno.MISSAO_ABORTADA), "evento");
        Verifica.igual(EstadoPartida.ABORTADA, partida.getEstado(), "estado");
        Verifica.lanca(IllegalStateException.class, () -> partida.executar(Comando.CIMA), "comando após o fim");
    }

    private static void geradorQuantidades() {
        for (long semente = 0; semente < 30; semente++) {
            GeradorMissao gerador = new GeradorMissao(Cenarios.TIPOS_PADRAO, new Random(semente));
            for (Dificuldade d : Dificuldade.values()) {
                for (int tamanho = d.getTamanhoMinimoMapa(); tamanho <= 5; tamanho++) {
                    Missao m = gerador.gerar(d, new Mapa(tamanho));
                    Verifica.igual(d.getQuantidadePassageiros(), m.getPassageiros().size(), "passageiros " + d);
                    Verifica.igual(d.getQuantidadeAsteroides(), m.getAsteroides().size(), "asteroides " + d);
                    Verifica.igual(d.getQuantidadeInimigos(), m.getInimigos().size(), "inimigos " + d);
                    Set<String> ocupadas = new HashSet<>();
                    for (List<? extends EntidadeMapa> lista : List.of(m.getPassageiros(), m.getAsteroides(), m.getInimigos())) {
                        for (EntidadeMapa e : lista) {
                            Verifica.verdadeiro(m.getMapa().contem(e.getX(), e.getY()), "dentro do mapa");
                            Verifica.falso(e.estaEm(0, 0), "nada nasce na plataforma");
                            Verifica.verdadeiro(ocupadas.add(e.getX() + "," + e.getY()), "sobreposição");
                        }
                    }
                }
            }
        }
    }

    private static void geradorMapaPequeno() {
        GeradorMissao gerador = new GeradorMissao(Cenarios.TIPOS_PADRAO, new Random(1));
        Verifica.lanca(IllegalArgumentException.class,
                () -> gerador.gerar(Dificuldade.MEDIO, new Mapa(1)), "médio em mapa 1");
        Verifica.igual(4, gerador.gerar(Dificuldade.FACIL, new Mapa(1)).getPassageiros().size(), "fácil cabe em mapa 1");
    }

    private static void geradorRotacao() {
        Missao m = new GeradorMissao(Cenarios.TIPOS_PADRAO, new Random(3)).gerar(Dificuldade.MEDIO, new Mapa(3));
        List<String> tipos = m.getPassageiros().stream().map(Passageiro::getTipo).toList();
        Verifica.igual(List.of("Professor", "Engenheiro", "Professor", "Engenheiro", "Astronauta"), tipos, "rotação");
    }

    /** Tipo criado só no teste: nenhuma classe de src/ foi alterada para suportá-lo. */
    static final class Medico extends Passageiro {
        Medico(String nome, int x, int y) {
            super(nome, x, y);
        }

        @Override
        public String getTipo() {
            return "Médico";
        }

        @Override
        public int getPontuacao() {
            return 25;
        }

        @Override
        public char getSimbolo() {
            return 'M';
        }
    }

    private static void novoTipoPassageiro() {
        GeradorMissao gerador = new GeradorMissao(List.of((x, y) -> new Medico("Dra. Ana", x, y)), new Random(5));
        Missao missao = gerador.gerar(Dificuldade.FACIL, new Mapa(3));
        Verifica.verdadeiro(missao.getPassageiros().stream().allMatch(p -> p instanceof Medico), "gerou médicos");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new MapaRenderer(new PrintStream(buffer, true, StandardCharsets.UTF_8)).desenhar(missao, "Teste", 30);
        String saida = buffer.toString(StandardCharsets.UTF_8);
        Verifica.contem(saida, "M=Médico");
        Verifica.contem(saida, " M");

        Missao simples = Cenarios.missaoVazia(2);
        simples.adicionarPassageiro(new Medico("Dra. Ana", 1, 0));
        Partida partida = Cenarios.partida(Dificuldade.FACIL, simples);
        partida.executar(Comando.DIREITA);
        partida.executar(Comando.EMBARCAR);
        Verifica.igual(54, partida.getPontuacao(), "30 - 1 + 25");
    }
}
