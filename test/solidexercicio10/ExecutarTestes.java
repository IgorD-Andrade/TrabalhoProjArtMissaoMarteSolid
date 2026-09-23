package solidexercicio10;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Executa todos os testes automatizados.
 *
 * <pre>
 * javac -encoding UTF-8 -d out $(find src/solidexercicio10 test/solidexercicio10 -name "*.java")
 * java -cp out solidexercicio10.ExecutarTestes
 * </pre>
 */
public final class ExecutarTestes {

    public static void main(String[] args) {
        Map<String, Verifica.Acao> testes = new LinkedHashMap<>();
        ModelTest.registrar(testes);
        PartidaTest.registrar(testes);
        JogoServiceTest.registrar(testes);
        RepositoryTest.registrar(testes);
        ConsoleTest.registrar(testes);

        int falhas = 0;
        for (Map.Entry<String, Verifica.Acao> teste : testes.entrySet()) {
            try {
                teste.getValue().executar();
                System.out.println("[OK]    " + teste.getKey());
            } catch (Throwable t) {
                falhas++;
                System.out.println("[FALHA] " + teste.getKey() + " -> " + t);
            }
        }
        System.out.printf("%n%d testes, %d sucesso(s), %d falha(s)%n",
                testes.size(), testes.size() - falhas, falhas);
        if (falhas > 0) {
            System.exit(1);
        }
    }
}
