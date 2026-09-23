package solidexercicio10;

import java.util.Objects;

/** Asserções mínimas para os testes sem dependência externa (sem JUnit). */
final class Verifica {
    private Verifica() {
    }

    static void verdadeiro(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    static void falso(boolean condicao, String mensagem) {
        verdadeiro(!condicao, mensagem);
    }

    static void igual(Object esperado, Object obtido, String mensagem) {
        if (!Objects.equals(esperado, obtido)) {
            throw new AssertionError(mensagem + " — esperado: " + esperado + ", obtido: " + obtido);
        }
    }

    static void contem(String texto, String trecho) {
        if (!texto.contains(trecho)) {
            throw new AssertionError("Saída não contém: \"" + trecho + "\"");
        }
    }

    interface Acao {
        void executar() throws Exception;
    }

    static <T extends Throwable> void lanca(Class<T> tipo, Acao acao, String mensagem) {
        try {
            acao.executar();
        } catch (Throwable t) {
            if (tipo.isInstance(t)) {
                return;
            }
            throw new AssertionError(mensagem + " — lançou " + t.getClass().getSimpleName());
        }
        throw new AssertionError(mensagem + " — nenhuma exceção lançada");
    }
}
