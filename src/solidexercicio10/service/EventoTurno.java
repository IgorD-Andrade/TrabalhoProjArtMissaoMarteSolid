package solidexercicio10.service;

/**
 * O que aconteceu em um turno. A partida produz eventos; a apresentação decide
 * o texto de cada um (SRP: regra não formata mensagem).
 */
public enum EventoTurno {
    PASSAGEIRO_EMBARCADO,
    SEM_PASSAGEIRO,
    NAVE_CHEIA,
    COLISAO,
    NAVE_DESTRUIDA,
    SEM_COMBUSTIVEL,
    TODOS_RESGATADOS,
    MISSAO_CUMPRIDA,
    MISSAO_ABORTADA
}
