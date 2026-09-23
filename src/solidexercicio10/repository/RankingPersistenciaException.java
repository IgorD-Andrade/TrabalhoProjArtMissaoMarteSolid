package solidexercicio10.repository;

/** Falha ao gravar ou apagar o ranking (disco cheio, sem permissão, etc.). */
public class RankingPersistenciaException extends RuntimeException {
    public RankingPersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
