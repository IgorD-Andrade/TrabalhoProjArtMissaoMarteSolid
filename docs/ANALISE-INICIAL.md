# Análise do código inicial (`src/exercicio10`)

Análise feita **antes** da refatoração, a partir da compilação e execução do jogo original.

```bash
javac -encoding UTF-8 -d out src/exercicio10/*.java
java -cp out exercicio10.Main
```

## 1. Responsabilidades concentradas em `Main` (≈ 560 linhas)

| Responsabilidade | Métodos em `Main` | Motivo para mudar |
|---|---|---|
| Ponto de entrada e laço do menu | `main`, `exibirMenu`, `exibirBoasVindas` | mudar opções do menu |
| Leitura e validação de entrada | `lerLinha`, `lerDificuldade`, `lerTamanhoMapa` | mudar a forma de interação (console → GUI) |
| Regras da partida | `jogarPartida` (laço de turnos, pontuação, vidas, vitória) | mudar uma regra do jogo |
| Configuração por dificuldade | `definirPontuacaoInicial`, `criarNovaMissao` (`switch`/`if` por dificuldade) | adicionar ou balancear uma dificuldade |
| Criação de passageiros | `criarPassageiroPolimorfico` (`switch` por índice) | adicionar novo tipo de passageiro |
| Posicionamento de entidades | `posicaoOcupada`, laços de sorteio em `criarNovaMissao` | mudar a geração do mapa |
| Renderização | `desenharMapa` (usa `instanceof` para escolher símbolo) | mudar a aparência do mapa |
| Estatísticas | `exibirEstatisticas` | mudar o relatório final |
| Persistência do ranking | `loadRanking`, `saveRanking`, `parseRankingJson`, `resetarRanking`, `RankingEntry` | trocar arquivo por banco/memória |

São pelo menos **nove motivos diferentes** para alterar a mesma classe — violação clara do SRP.

Outros pontos observados:

- **OCP:** um novo passageiro exige alterar `criarPassageiroPolimorfico` **e** o `instanceof` de `desenharMapa`.
- **DIP:** a regra da partida chama `saveRanking(RANKING_PATH, ...)` diretamente; não há como testar o fluxo sem gravar em disco.
- **Parâmetros repetidos:** `minX, maxX, minY, maxY` trafegam juntos por 6 métodos (o conceito "mapa" não existe).
- **`Passageiro` concreto** com pontuação *default* 10 e um campo `tipo` em texto que repete o nome da subclasse.
- **Posição (0,0) da plataforma** aparece "mágica" em dois lugares (`jogarPartida` e `desenharMapa`).

## 2. Comportamentos que precisam continuar funcionando

| # | Comportamento | Valor no original |
|---|---|---|
| C1 | Menu com 4 opções: jogar, ranking, resetar, sair | opção inválida mostra mensagem e volta ao menu |
| C2 | Nome do piloto (vazio → "Piloto Anônimo") | — |
| C3 | Dificuldade por texto (aceita acentos; inválido → Médio) | FACIL / MEDIO / DIFICIL |
| C4 | Tamanho do mapa `n` gera grade de `-n..+n` (inválido → 5) | — |
| C5 | Pontuação inicial | Fácil 30 · Médio 20 · Difícil 15 |
| C6 | Quantidade de entidades | Fácil 4 pass./1 ast./1 inim. · Médio 5/2/2 · Difícil 5/3/3 |
| C7 | Nave começa em (0,0), capacidade 5, 3 vidas | — |
| C8 | `w/s/a/d` move 1 casa sem sair do mapa; cada movimento custa 1 ponto | `w` = y−1 (y cresce para baixo) |
| C9 | `c` embarca o passageiro da posição | Professor +10 · Engenheiro +15 · Astronauta +20 |
| C10 | Mensagens: sem passageiro / nave cheia | — |
| C11 | Inimigos andam 1 casa aleatória (4 direções) após cada comando válido | comando inválido não movimenta inimigos |
| C12 | Colisão com asteroide ou inimigo tira 1 vida; 0 vidas → Game Over | — |
| C13 | Pontuação ≤ 0 → missão perdida | — |
| C14 | Todos resgatados → alerta para voltar à plataforma `L` | — |
| C15 | Vitória só com todos resgatados **e** nave em (0,0) | — |
| C16 | Estatísticas: pontuação, movimentos, tempo, passageiros, recorde | — |
| C17 | Ranking Top 5 salvo em JSON (nome, pontos, dificuldade, passageiros, data/hora, tempo) | só vitórias entram |
| C18 | Reset do ranking pede confirmação (s/n) | arquivo inexistente não gera erro |
| C19 | `q` aborta a missão e volta ao menu | — |
| C20 | Símbolos: `@` nave, `P`/`E`/`T` passageiros, `#` asteroide, `X` inimigo, `L` plataforma, `.` vazio | — |

## 3. Defeitos encontrados durante os testes do original

| Defeito | Como reproduzir | Resultado |
|---|---|---|
| **Laço infinito** na criação da missão | tamanho de mapa `1` com dificuldade Médio ou Difícil (9 células, 1 ocupada pela nave, mas 9–11 entidades a posicionar) | o programa trava (testado com `timeout 5`, saída 124) |
| **Exceção ao fim da entrada** | encerrar a entrada (Ctrl+D / entrada redirecionada) no "Pressione Enter" | `NoSuchElementException` em `Main.jogarPartida` |
| **Parser JSON frágil** | nome do piloto contendo vírgula | `split(",")` quebra o par chave/valor e o registro é descartado ao recarregar |
| Pontuação em `Passageiro` base | `new Passageiro(...)` ainda é possível | um "passageiro genérico" que não existe no domínio vale 10 pontos |

Esses defeitos foram corrigidos na versão refatorada e estão registrados em `REVISAO-SOLID.md`.
