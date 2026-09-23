package solidexercicio10.service;

import java.util.List;
import java.util.Optional;
import solidexercicio10.model.Passageiro;

/** Eventos de um turno e, se houve embarque, o passageiro embarcado. */
public final class ResultadoTurno {
    private final List<EventoTurno> eventos;
    private final Passageiro passageiroEmbarcado;

    ResultadoTurno(List<EventoTurno> eventos, Passageiro passageiroEmbarcado) {
        this.eventos = List.copyOf(eventos);
        this.passageiroEmbarcado = passageiroEmbarcado;
    }

    public List<EventoTurno> getEventos() {
        return eventos;
    }

    public boolean contem(EventoTurno evento) {
        return eventos.contains(evento);
    }

    public Optional<Passageiro> getPassageiroEmbarcado() {
        return Optional.ofNullable(passageiroEmbarcado);
    }
}
