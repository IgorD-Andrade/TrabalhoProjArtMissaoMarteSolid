package solidexercicio10.service;

import solidexercicio10.model.Passageiro;

/**
 * Cria um passageiro em uma posição.
 *
 * <p>OCP: o {@link GeradorMissao} recebe uma lista destas fábricas. Para um
 * novo tipo (ex.: Médico) basta criar a subclasse e registrar
 * {@code (x, y) -> new Medico("Dra. Ana", x, y)} no {@code Main};
 * gerador, partida e renderizador não mudam.</p>
 */
@FunctionalInterface
public interface FabricaPassageiro {
    Passageiro criar(int x, int y);
}
