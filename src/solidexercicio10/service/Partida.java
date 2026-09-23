package solidexercicio10.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;
import solidexercicio10.model.Passageiro;

/**
 * Regras de uma partida em andamento: pontuação, vidas, derrota e vitória.
 *
 * <p>SRP: é o antigo laço de {@code jogarPartida} sem nenhum
 * {@code System.out} nem {@code Scanner}. Recebe um {@link Comando} e devolve
 * um {@link ResultadoTurno}; por isso pode ser testada de forma determinística
 * com um {@code Random} de semente fixa.</p>
 *
 * <p>LSP: o embarque soma {@code passageiro.getPontuacao()} sem saber se é
 * Professor, Engenheiro, Astronauta ou um tipo criado no futuro.</p>
 */
public class Partida {
    private final String piloto;
    private final Dificuldade dificuldade;
    private final Missao missao;
    private final Random random;
    private final Clock relogio;
    private final long inicioMillis;

    private int pontuacao;
    private int movimentos;
    private EstadoPartida estado = EstadoPartida.EM_ANDAMENTO;
    private long fimMillis = -1;

    public Partida(String piloto, Dificuldade dificuldade, Missao missao, Random random, Clock relogio) {
        this.piloto = piloto;
        this.dificuldade = dificuldade;
        this.missao = missao;
        this.random = random;
        this.relogio = relogio;
        this.pontuacao = dificuldade.getPontuacaoInicial();
        this.inicioMillis = relogio.millis();
    }

    public ResultadoTurno executar(Comando comando) {
        if (!emAndamento()) {
            throw new IllegalStateException("A partida já terminou");
        }
        List<EventoTurno> eventos = new ArrayList<>();

        if (comando == Comando.ABORTAR) {
            encerrar(EstadoPartida.ABORTADA);
            eventos.add(EventoTurno.MISSAO_ABORTADA);
            return new ResultadoTurno(eventos, null);
        }

        Passageiro embarcado = null;
        if (comando == Comando.EMBARCAR) {
            embarcado = tentarEmbarcar(eventos);
        } else {
            missao.moverNave(comando.getDirecao());
            pontuacao--;          // cada movimento consome combustível, mesmo contra a borda
            movimentos++;
        }

        missao.moverInimigos(random);
        verificarColisao(eventos);
        verificarCombustivel(eventos);
        verificarVitoria(eventos);
        return new ResultadoTurno(eventos, embarcado);
    }

    private Passageiro tentarEmbarcar(List<EventoTurno> eventos) {
        Optional<Passageiro> passageiro = missao.passageiroNaPosicaoDaNave();
        if (passageiro.isEmpty()) {
            eventos.add(EventoTurno.SEM_PASSAGEIRO);
            return null;
        }
        if (!missao.embarcar(passageiro.get())) {
            eventos.add(EventoTurno.NAVE_CHEIA);
            return null;
        }
        pontuacao += passageiro.get().getPontuacao();
        eventos.add(EventoTurno.PASSAGEIRO_EMBARCADO);
        return passageiro.get();
    }

    private void verificarColisao(List<EventoTurno> eventos) {
        if (!missao.naveColidiu()) {
            return;
        }
        Nave nave = missao.getNave();
        nave.perderVida();
        if (nave.estaDestruida()) {
            eventos.add(EventoTurno.NAVE_DESTRUIDA);
            encerrar(EstadoPartida.DERROTA);
        } else {
            eventos.add(EventoTurno.COLISAO);
        }
    }

    private void verificarCombustivel(List<EventoTurno> eventos) {
        if (emAndamento() && pontuacao <= 0) {
            eventos.add(EventoTurno.SEM_COMBUSTIVEL);
            encerrar(EstadoPartida.DERROTA);
        }
    }

    private void verificarVitoria(List<EventoTurno> eventos) {
        if (!emAndamento() || !missao.todosResgatados()) {
            return;
        }
        if (missao.naveNaPlataforma()) {
            eventos.add(EventoTurno.MISSAO_CUMPRIDA);
            encerrar(EstadoPartida.VITORIA);
        } else {
            eventos.add(EventoTurno.TODOS_RESGATADOS);
        }
    }

    private void encerrar(EstadoPartida estadoFinal) {
        estado = estadoFinal;
        fimMillis = relogio.millis();
    }

    public boolean emAndamento() {
        return estado == EstadoPartida.EM_ANDAMENTO;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public String getPiloto() {
        return piloto;
    }

    public Dificuldade getDificuldade() {
        return dificuldade;
    }

    public Missao getMissao() {
        return missao;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public int getMovimentos() {
        return movimentos;
    }

    public int getPassageirosResgatados() {
        return missao.getNave().getQuantidadeABordo();
    }

    public long getDuracaoSegundos() {
        long fim = fimMillis >= 0 ? fimMillis : relogio.millis();
        return (fim - inicioMillis) / 1000;
    }
}
