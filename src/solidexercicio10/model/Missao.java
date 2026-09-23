package solidexercicio10.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Estado do mundo de uma partida: mapa, nave, plataforma e entidades.
 *
 * <p>SRP: responde perguntas sobre o mundo (o que há nesta posição? houve
 * colisão? todos foram resgatados?) e aplica movimentos respeitando os limites.
 * Não conhece pontuação, console nem ranking.</p>
 */
public class Missao {
    private final Mapa mapa;
    private final Nave nave;
    private final PlataformaPouso plataforma;
    private final List<Passageiro> passageiros = new ArrayList<>();
    private final List<Asteroide> asteroides = new ArrayList<>();
    private final List<Inimigo> inimigos = new ArrayList<>();

    public Missao(Mapa mapa, Nave nave) {
        if (!mapa.contem(nave.getX(), nave.getY())) {
            throw new IllegalArgumentException("A nave precisa começar dentro do mapa");
        }
        this.mapa = mapa;
        this.nave = nave;
        this.plataforma = new PlataformaPouso(0, 0);
    }

    // ----------------------------------------------------------- montagem

    public void adicionarPassageiro(Passageiro passageiro) {
        validarPosicaoLivre(passageiro);
        passageiros.add(passageiro);
    }

    public void adicionarAsteroide(Asteroide asteroide) {
        validarPosicaoLivre(asteroide);
        asteroides.add(asteroide);
    }

    public void adicionarInimigo(Inimigo inimigo) {
        validarPosicaoLivre(inimigo);
        inimigos.add(inimigo);
    }

    private void validarPosicaoLivre(EntidadeMapa entidade) {
        if (!posicaoLivre(entidade.getX(), entidade.getY())) {
            throw new IllegalArgumentException(String.format(
                    "Posição (%d,%d) fora do mapa ou ocupada", entidade.getX(), entidade.getY()));
        }
    }

    // ----------------------------------------------------------- consultas

    public boolean posicaoLivre(int x, int y) {
        return mapa.contem(x, y) && entidadeEm(x, y).isEmpty();
    }

    /**
     * Entidade visível na posição, na mesma prioridade do jogo original:
     * nave, passageiro, asteroide, inimigo e, por fim, plataforma.
     */
    public Optional<EntidadeMapa> entidadeEm(int x, int y) {
        if (nave.estaEm(x, y)) {
            return Optional.of(nave);
        }
        List<List<? extends EntidadeMapa>> camadas = List.of(passageiros, asteroides, inimigos);
        for (List<? extends EntidadeMapa> camada : camadas) {
            for (EntidadeMapa entidade : camada) {
                if (entidade.estaEm(x, y)) {
                    return Optional.of(entidade);
                }
            }
        }
        return plataforma.estaEm(x, y) ? Optional.of(plataforma) : Optional.empty();
    }

    public Optional<Passageiro> passageiroNaPosicaoDaNave() {
        return passageiros.stream().filter(p -> p.mesmaPosicaoQue(nave)).findFirst();
    }

    public boolean naveColidiu() {
        return asteroides.stream().anyMatch(a -> a.mesmaPosicaoQue(nave))
                || inimigos.stream().anyMatch(i -> i.mesmaPosicaoQue(nave));
    }

    public boolean todosResgatados() {
        return passageiros.isEmpty();
    }

    public boolean naveNaPlataforma() {
        return nave.mesmaPosicaoQue(plataforma);
    }

    // ----------------------------------------------------------- ações

    /** @return {@code false} se a nave estiver cheia ou não estiver sobre o passageiro. */
    public boolean embarcar(Passageiro passageiro) {
        if (!passageiros.contains(passageiro) || !passageiro.mesmaPosicaoQue(nave)) {
            return false;
        }
        if (!nave.embarcar(passageiro)) {
            return false;
        }
        passageiros.remove(passageiro);
        return true;
    }

    /** @return {@code false} se o movimento levaria a nave para fora do mapa. */
    public boolean moverNave(Direcao direcao) {
        return moverDentroDoMapa(nave, direcao);
    }

    /** Cada inimigo tenta andar uma casa em uma das quatro direções. */
    public void moverInimigos(Random random) {
        Direcao[] direcoes = Direcao.values();
        for (Inimigo inimigo : inimigos) {
            moverDentroDoMapa(inimigo, direcoes[random.nextInt(direcoes.length)]);
        }
    }

    /** ISP na prática: o método só exige o que usa — posição e capacidade de mover. */
    private <T extends Posicionavel & Movel> boolean moverDentroDoMapa(T entidade, Direcao direcao) {
        int novoX = entidade.getX() + direcao.getDx();
        int novoY = entidade.getY() + direcao.getDy();
        if (!mapa.contem(novoX, novoY)) {
            return false;
        }
        entidade.mover(direcao.getDx(), direcao.getDy());
        return true;
    }

    // ----------------------------------------------------------- getters

    public Mapa getMapa() {
        return mapa;
    }

    public Nave getNave() {
        return nave;
    }

    public PlataformaPouso getPlataforma() {
        return plataforma;
    }

    public List<Passageiro> getPassageiros() {
        return Collections.unmodifiableList(passageiros);
    }

    public List<Asteroide> getAsteroides() {
        return Collections.unmodifiableList(asteroides);
    }

    public List<Inimigo> getInimigos() {
        return Collections.unmodifiableList(inimigos);
    }
}
