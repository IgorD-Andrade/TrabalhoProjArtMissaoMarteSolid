package solidexercicio10.presentation;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;

/*
 * Leitura de linhas do usuário.
 * <p>Recebe {@code Scanner} e {@code PrintStream} prontos, então os testes podem
 * simular um jogador com uma {@code String}. Retorna {@code Optional.empty()}
 * quando a entrada termina (Ctrl+D), situação em que o original lançava
 * {@code NoSuchElementException}.</p>
 */
public class Terminal {
    private final Scanner entrada;
    private final PrintStream saida;
    private boolean encerrada;

    public Terminal(Scanner entrada, PrintStream saida) {
        this.entrada = entrada;
        this.saida = saida;
    }

    public Optional<String> lerLinha(String prompt) {
        saida.print(prompt);
        saida.flush();
        if (encerrada || !entrada.hasNextLine()) {
            encerrada = true;
            saida.println();
            return Optional.empty();
        }
        return Optional.of(entrada.nextLine().trim());
    }

    public boolean entradaEncerrada() {
        return encerrada;
    }
}
