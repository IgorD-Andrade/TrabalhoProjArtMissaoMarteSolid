package solidexercicio10.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Ranking gravado em um arquivo JSON.
 *
 * <p>Chamado de {@code RankingService} no tutorial; o nome foi trocado porque a
 * classe é um repositório (detalhe de infraestrutura), não um serviço.</p>
 *
 * <p>Cuidados que o original não tinha: a gravação usa um arquivo temporário
 * seguido de {@code move}, para não deixar o JSON pela metade; e um arquivo
 * corrompido é preservado como {@code .corrompido} em vez de ser sobrescrito
 * silenciosamente.</p>
 */
public class JsonRankingRepository implements RankingRepository {
    private final Path arquivo;

    public JsonRankingRepository(Path arquivo) {
        this.arquivo = arquivo;
    }

    @Override
    public void salvar(RankingEntry entrada) {
        List<RankingEntry> entradas = lerOuPreservarCorrompido();
        entradas.add(entrada);
        gravar(entradas);
    }

    @Override
    public List<RankingEntry> listar() {
        try {
            return ler();
        } catch (IOException | IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void limpar() {
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            throw new RankingPersistenciaException("Não foi possível apagar " + arquivo, e);
        }
    }

    private List<RankingEntry> ler() throws IOException {
        if (!Files.exists(arquivo)) {
            return new ArrayList<>();
        }
        String conteudo = Files.readString(arquivo, StandardCharsets.UTF_8);
        return RankingJson.desserializar(conteudo);
    }

    private List<RankingEntry> lerOuPreservarCorrompido() {
        try {
            return ler();
        } catch (IOException | IllegalArgumentException e) {
            try {
                Path copia = arquivo.resolveSibling(arquivo.getFileName() + ".corrompido");
                Files.move(arquivo, copia, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignorada) {
                // se nem mover for possível, a gravação abaixo informará o erro
            }
            return new ArrayList<>();
        }
    }

    private void gravar(List<RankingEntry> entradas) {
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            Files.createDirectories(pasta);
            Path temporario = Files.createTempFile(pasta, "ranking", ".tmp");
            Files.writeString(temporario, RankingJson.serializar(entradas), StandardCharsets.UTF_8);
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RankingPersistenciaException("Não foi possível salvar " + arquivo, e);
        }
    }

    public Path getArquivo() {
        return arquivo;
    }
}
