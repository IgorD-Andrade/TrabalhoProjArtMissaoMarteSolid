package solidexercicio10.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import solidexercicio10.model.Dificuldade;

/**
 * Converte a lista de registros para JSON e de volta, sem bibliotecas externas.
 *
 * <p>SRP: separa "formato do arquivo" de "acesso ao disco"
 * ({@link JsonRankingRepository}). Diferente do parser original, este respeita
 * aspas e vírgulas dentro de textos (ex.: piloto "Silva, J.").</p>
 */
final class RankingJson {

    private RankingJson() {
    }

    static String serializar(List<RankingEntry> entradas) {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < entradas.size(); i++) {
            RankingEntry e = entradas.get(i);
            json.append("  {")
                    .append("\"name\":").append(texto(e.getNomePiloto())).append(',')
                    .append("\"score\":").append(e.getPontuacao()).append(',')
                    .append("\"dificuldade\":").append(texto(e.getDificuldade().name())).append(',')
                    .append("\"passageirosColetados\":").append(e.getPassageirosResgatados()).append(',')
                    .append("\"dataHora\":").append(texto(e.getDataHora())).append(',')
                    .append("\"tempoJogo\":").append(e.getTempoJogoSegundos())
                    .append('}');
            json.append(i < entradas.size() - 1 ? ",\n" : "\n");
        }
        return json.append("]\n").toString();
    }

    /**
     * Lê um array de objetos simples. Objetos sem {@code name} ou {@code score}
     * válidos são ignorados.
     *
     * @throws IllegalArgumentException se o texto não for um array JSON bem formado
     */
    static List<RankingEntry> desserializar(String json) {
        List<RankingEntry> entradas = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return entradas;
        }
        Leitor leitor = new Leitor(json);
        leitor.esperar('[');
        if (!leitor.consumirSe(']')) {
            do {
                Map<String, String> objeto = leitor.lerObjeto();
                RankingEntry entrada = converter(objeto);
                if (entrada != null) {
                    entradas.add(entrada);
                }
            } while (leitor.consumirSe(','));
            leitor.esperar(']');
        }
        leitor.esperarFim();
        return entradas;
    }

    private static RankingEntry converter(Map<String, String> objeto) {
        try {
            String nome = objeto.get("name");
            if (nome == null || !objeto.containsKey("score")) {
                return null;
            }
            return new RankingEntry(
                    nome,
                    Integer.parseInt(objeto.get("score")),
                    Dificuldade.deString(objeto.get("dificuldade")),
                    Integer.parseInt(objeto.getOrDefault("passageirosColetados", "0")),
                    objeto.getOrDefault("dataHora", ""),
                    Long.parseLong(objeto.getOrDefault("tempoJogo", "0")));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String texto(String valor) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : valor.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

    /** Leitor mínimo: objetos planos com valores texto, número, booleano ou null. */
    private static final class Leitor {
        private final String json;
        private int pos;

        Leitor(String json) {
            this.json = json;
        }

        Map<String, String> lerObjeto() {
            Map<String, String> objeto = new HashMap<>();
            esperar('{');
            if (consumirSe('}')) {
                return objeto;
            }
            do {
                String chave = lerTexto();
                esperar(':');
                objeto.put(chave, lerValor());
            } while (consumirSe(','));
            esperar('}');
            return objeto;
        }

        private String lerValor() {
            pularEspacos();
            if (pos < json.length() && json.charAt(pos) == '"') {
                return lerTexto();
            }
            int inicio = pos;
            while (pos < json.length() && ",}] \t\r\n".indexOf(json.charAt(pos)) < 0) {
                pos++;
            }
            if (inicio == pos) {
                throw erro("valor esperado");
            }
            return json.substring(inicio, pos);
        }

        private String lerTexto() {
            esperar('"');
            StringBuilder sb = new StringBuilder();
            while (pos < json.length()) {
                char c = json.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                if (pos >= json.length()) {
                    break;
                }
                char escape = json.charAt(pos++);
                switch (escape) {
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        if (pos + 4 > json.length()) {
                            throw erro("escape unicode incompleto");
                        }
                        sb.append((char) Integer.parseInt(json.substring(pos, pos + 4), 16));
                        pos += 4;
                    }
                    default -> sb.append(escape); // \" \\ \/
                }
            }
            throw erro("texto não terminado");
        }

        boolean consumirSe(char esperado) {
            pularEspacos();
            if (pos < json.length() && json.charAt(pos) == esperado) {
                pos++;
                return true;
            }
            return false;
        }

        void esperar(char esperado) {
            if (!consumirSe(esperado)) {
                throw erro("'" + esperado + "' esperado");
            }
        }

        void esperarFim() {
            pularEspacos();
            if (pos != json.length()) {
                throw erro("conteúdo inesperado");
            }
        }

        private void pularEspacos() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }
        }

        private IllegalArgumentException erro(String mensagem) {
            return new IllegalArgumentException("JSON inválido na posição " + pos + ": " + mensagem);
        }
    }
}
