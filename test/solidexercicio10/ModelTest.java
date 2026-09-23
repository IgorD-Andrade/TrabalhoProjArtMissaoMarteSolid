package solidexercicio10;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import solidexercicio10.model.Asteroide;
import solidexercicio10.model.Astronauta;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Direcao;
import solidexercicio10.model.Engenheiro;
import solidexercicio10.model.Inimigo;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;
import solidexercicio10.model.Passageiro;
import solidexercicio10.model.Professor;

final class ModelTest {
    private ModelTest() {
    }

    static void registrar(Map<String, Verifica.Acao> t) {
        t.put("LSP: subclasses usadas como Passageiro mantêm o contrato (10/15/20)", ModelTest::pontuacaoPolimorfica);
        t.put("Dificuldade: conversão de texto com acento, caixa e valor inválido", ModelTest::dificuldadeDeString);
        t.put("Dificuldade: parâmetros e tamanho mínimo de mapa", ModelTest::dificuldadeParametros);
        t.put("Nave: capacidade de 5 passageiros", ModelTest::naveCapacidade);
        t.put("Nave: 3 vidas e nunca negativa", ModelTest::naveVidas);
        t.put("Missao: nave não sai do mapa", ModelTest::naveNaoSaiDoMapa);
        t.put("Missao: inimigo aleatório nunca sai do mapa (10.000 turnos)", ModelTest::inimigoNaoSaiDoMapa);
        t.put("Missao: rejeita entidade fora do mapa ou em posição ocupada", ModelTest::rejeitaPosicaoInvalida);
        t.put("Missao: prioridade de desenho igual ao original", ModelTest::prioridadeEntidades);
        t.put("Missao: embarque remove passageiro da superfície", ModelTest::embarqueRemove);
        t.put("Missao: colisão com asteroide e com inimigo", ModelTest::colisao);
    }

    private static void pontuacaoPolimorfica() {
        List<Passageiro> passageiros = List.of(
                new Professor("P", 0, 0), new Engenheiro("E", 0, 0), new Astronauta("T", 0, 0));
        int[] esperado = {10, 15, 20};
        Set<Character> simbolos = new HashSet<>();
        for (int i = 0; i < passageiros.size(); i++) {
            Passageiro p = passageiros.get(i);
            Verifica.igual(esperado[i], p.getPontuacao(), "pontuação de " + p.getTipo());
            Verifica.falso(p.getTipo().isBlank(), "tipo vazio");
            Verifica.verdadeiro(simbolos.add(p.getSimbolo()), "símbolo repetido: " + p.getSimbolo());
        }
        int total = passageiros.stream().mapToInt(Passageiro::getPontuacao).sum();
        Verifica.igual(45, total, "soma polimórfica");
    }

    private static void dificuldadeDeString() {
        Verifica.igual(Dificuldade.FACIL, Dificuldade.deString("fácil"), "fácil");
        Verifica.igual(Dificuldade.FACIL, Dificuldade.deString("FACIL"), "FACIL");
        Verifica.igual(Dificuldade.DIFICIL, Dificuldade.deString("  Difícil "), "Difícil com espaços");
        Verifica.igual(Dificuldade.MEDIO, Dificuldade.deString("médio"), "médio");
        Verifica.igual(Dificuldade.MEDIO, Dificuldade.deString("xyz"), "inválido vira médio");
        Verifica.igual(Dificuldade.MEDIO, Dificuldade.deString(null), "null vira médio");
    }

    private static void dificuldadeParametros() {
        Verifica.igual(30, Dificuldade.FACIL.getPontuacaoInicial(), "pontos fácil");
        Verifica.igual(20, Dificuldade.MEDIO.getPontuacaoInicial(), "pontos médio");
        Verifica.igual(15, Dificuldade.DIFICIL.getPontuacaoInicial(), "pontos difícil");
        Verifica.igual(6, Dificuldade.FACIL.getTotalEntidades(), "entidades fácil (4+1+1)");
        Verifica.igual(9, Dificuldade.MEDIO.getTotalEntidades(), "entidades médio (5+2+2)");
        Verifica.igual(11, Dificuldade.DIFICIL.getTotalEntidades(), "entidades difícil (5+3+3)");
        Verifica.igual(1, Dificuldade.FACIL.getTamanhoMinimoMapa(), "mínimo fácil");
        Verifica.igual(2, Dificuldade.MEDIO.getTamanhoMinimoMapa(), "mínimo médio");
        Verifica.igual(2, Dificuldade.DIFICIL.getTamanhoMinimoMapa(), "mínimo difícil");
    }

    private static void naveCapacidade() {
        Nave nave = new Nave("A-1", 0, 0);
        for (int i = 0; i < 5; i++) {
            Verifica.verdadeiro(nave.embarcar(new Professor("P" + i, 0, 0)), "embarque " + i);
        }
        Verifica.verdadeiro(nave.estaCheia(), "deveria estar cheia");
        Verifica.falso(nave.embarcar(new Professor("Extra", 0, 0)), "sexto passageiro");
        Verifica.igual(5, nave.getQuantidadeABordo(), "a bordo");
        Verifica.lanca(UnsupportedOperationException.class,
                () -> nave.getPassageiros().clear(), "lista exposta deve ser somente leitura");
    }

    private static void naveVidas() {
        Nave nave = new Nave("A-1", 0, 0);
        Verifica.igual(3, nave.getVidas(), "vidas iniciais");
        nave.perderVida();
        nave.perderVida();
        Verifica.falso(nave.estaDestruida(), "ainda com 1 vida");
        nave.perderVida();
        nave.perderVida();
        Verifica.igual(0, nave.getVidas(), "não fica negativa");
        Verifica.verdadeiro(nave.estaDestruida(), "destruída");
    }

    private static void naveNaoSaiDoMapa() {
        Missao missao = Cenarios.missaoVazia(1);
        Verifica.verdadeiro(missao.moverNave(Direcao.CIMA), "primeiro movimento");
        Verifica.falso(missao.moverNave(Direcao.CIMA), "segundo movimento bate na borda");
        Verifica.igual(-1, missao.getNave().getY(), "y na borda");
    }

    private static void inimigoNaoSaiDoMapa() {
        Missao missao = Cenarios.missaoVazia(2);
        missao.adicionarInimigo(new Inimigo(2, 2));
        missao.adicionarInimigo(new Inimigo(-2, -2));
        Random random = new Random(42);
        for (int i = 0; i < 10_000; i++) {
            missao.moverInimigos(random);
            for (Inimigo inimigo : missao.getInimigos()) {
                Verifica.verdadeiro(missao.getMapa().contem(inimigo.getX(), inimigo.getY()),
                        "inimigo fora do mapa em (" + inimigo.getX() + "," + inimigo.getY() + ")");
            }
        }
    }

    private static void rejeitaPosicaoInvalida() {
        Missao missao = Cenarios.missaoVazia(2);
        missao.adicionarAsteroide(new Asteroide(1, 0));
        Verifica.lanca(IllegalArgumentException.class,
                () -> missao.adicionarInimigo(new Inimigo(1, 0)), "posição ocupada");
        Verifica.lanca(IllegalArgumentException.class,
                () -> missao.adicionarAsteroide(new Asteroide(0, 0)), "posição da nave/plataforma");
        Verifica.lanca(IllegalArgumentException.class,
                () -> missao.adicionarPassageiro(new Professor("P", 3, 0)), "fora do mapa");
    }

    private static void prioridadeEntidades() {
        Missao missao = Cenarios.missaoVazia(2);
        Verifica.igual('@', missao.entidadeEm(0, 0).get().getSimbolo(), "nave sobre a plataforma");
        missao.moverNave(Direcao.DIREITA);
        Verifica.igual('L', missao.entidadeEm(0, 0).get().getSimbolo(), "plataforma visível");
        missao.adicionarPassageiro(new Astronauta("T", -1, 0));
        Verifica.igual('T', missao.entidadeEm(-1, 0).get().getSimbolo(), "astronauta");
        Verifica.verdadeiro(missao.entidadeEm(2, 2).isEmpty(), "vazio");
    }

    private static void embarqueRemove() {
        Missao missao = Cenarios.missaoVazia(2);
        Passageiro p = new Engenheiro("E", 1, 0);
        missao.adicionarPassageiro(p);
        Verifica.falso(missao.embarcar(p), "não embarca à distância");
        missao.moverNave(Direcao.DIREITA);
        Verifica.verdadeiro(missao.embarcar(p), "embarca na mesma posição");
        Verifica.verdadeiro(missao.todosResgatados(), "superfície vazia");
        Verifica.igual(1, missao.getNave().getQuantidadeABordo(), "a bordo");
    }

    private static void colisao() {
        Missao missao = Cenarios.missaoVazia(2);
        missao.adicionarAsteroide(new Asteroide(1, 0));
        missao.adicionarInimigo(new Inimigo(-1, 0));
        Verifica.falso(missao.naveColidiu(), "sem colisão");
        missao.moverNave(Direcao.DIREITA);
        Verifica.verdadeiro(missao.naveColidiu(), "colisão com asteroide");
        missao.moverNave(Direcao.ESQUERDA);
        missao.moverNave(Direcao.ESQUERDA);
        Verifica.verdadeiro(missao.naveColidiu(), "colisão com inimigo");
    }
}
