# Roteiro da apresentação (≈ 10 min)

Preparação: compilar as duas versões antes (`javac -encoding UTF-8 -d out src/exercicio10/*.java` e `javac -encoding UTF-8 -d out $(find src/solidexercicio10 test/solidexercicio10 -name "*.java")`) e apagar `ranking-solid-exercicio10.json`.

| Tempo | Tópico | O que mostrar |
|---|---|---|
| 1 min | Problema | `src/exercicio10/Main.java`: 557 linhas, 9 responsabilidades (tabela em `docs/ANALISE-INICIAL.md`) |
| 1 min | Antes | `java -cp out exercicio10.Main` → médio, mapa **1** → trava (defeito encontrado) |
| 2 min | Depois | `java -cp out solidexercicio10.Main --seed 5` → mesma entrada mostra o intervalo e não trava; jogar alguns turnos; ranking e reset com confirmação |
| 2 min | Organização | diagrama de pacotes: dependências apontam para o `model`; `service` → `RankingRepository` (interface) |
| 2 min | SOLID no código | **SRP** `Partida` sem `System.out` · **OCP** `FabricaPassageiro` + teste do `Medico` · **LSP** `Passageiro` abstrato · **ISP** `Posicionavel`/`Movel` · **DIP** mesmo teste com JSON e memória |
| 1 min | Revisão crítica | discordância: tutorial gravava ranking em texto `\|` e trocou as pontuações; melhoria pendente: restringir `Movel.mover` |
| 1 min | Testes | `java -cp out solidexercicio10.ExecutarTestes` → 39/39; limitações do README |

## Perguntas prováveis

- **Por que `Partida` e `JogoService` separados?** `Partida` é o estado e as regras de *uma* partida; `JogoService` são os casos de uso que envolvem o ranking. Assim as regras são testadas sem repositório.
- **Por que não criou interface para `MapaRenderer`?** Só existe uma implementação e o teste já injeta um `PrintStream`; a interface não resolveria nenhum problema atual.
- **Onde está a única referência a `JsonRankingRepository`?** No `Main`.
- **Por que `Passageiro` é agregação e não composição no diagrama?** Porque o passageiro troca de dono: sai da `Missao` e vai para a `Nave`.
- **Como o teste de vitória sabe as teclas?** O `PilotoAutomatico` joga com a mesma semente e registra as teclas; com a semente, o jogo real reproduz a mesma partida.
