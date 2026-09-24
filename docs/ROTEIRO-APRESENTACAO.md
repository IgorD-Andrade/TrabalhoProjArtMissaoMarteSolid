# Roteiro da apresentação (≈ 12 min)

Equipe: Igor Damasceno Andrade (2113413), Anderson Silveiro de Oliveira (2517361) e Asafe Campos Damasceno (2510525).

Preparação: compilar as duas versões antes (`javac -encoding UTF-8 -d out src/exercicio10/*.java` e `javac -encoding UTF-8 -d out $(find src/solidexercicio10 test/solidexercicio10 -name "*.java")`) e apagar `ranking-solid-exercicio10.json`. No Windows, os comandos equivalentes estão no README.

| Ordem | Quem | Tempo | Parte | O que mostrar |
|---|---|---|---|---|
| 1 | Igor | 1 min | Abertura | README no GitHub |
| 2 | Igor | 1 min | Antes | `java -cp out exercicio10.Main` → médio, mapa **1** → trava (Ctrl+C) |
| 3 | Igor | 2 min | Depois | `java -cp out solidexercicio10.Main --seed 5` → médio, mapa 1 não trava; **fechar e reabrir**; fácil, mapa 3: `d`, `c`, `c`, `x`, `s`, `s`, `d`, `q`; menu `2`, `3` + `n` |
| 4 | Igor | 1 min | Estrutura | árvore de pastas e diagrama de pacotes (README, seções 2 e 4) |
| 5 | Igor | 2 min | `REVISAO-SOLID.md`: validação e SRP | Partida sem `System.out`; `Main` com 69 linhas |
| 6 | Anderson | 2 min | OCP e LSP | `getSimbolo()`, `FabricaPassageiro` + teste do `Medico`, enum `Dificuldade`, `Passageiro` abstrato |
| 7 | Asafe | 2 min | ISP, DIP e melhorias adicionais | `RankingRepository` com 3 métodos; mesmo teste com JSON e memória; `--seed` |
| 8 | Anderson | 0,5 min | Decisões com que concordamos | `Main` como raiz de composição |
| 9 | Asafe | 1 min | Decisões com que discordamos e tamanho da arquitetura | ranking em texto, pontuações trocadas, 10 → 34 arquivos |
| 10 | Igor | 1 min | Testes e encerramento | `java -cp out solidexercicio10.ExecutarTestes` → 39/39 |

## Perguntas prováveis

| Pergunta | Quem responde | Resposta curta |
|---|---|---|
| O que muda para trocar o arquivo por banco de dados? | Asafe | Uma classe que implemente `RankingRepository` e uma linha no `Main`. |
| Como adicionar um novo tipo de passageiro? | Anderson | Subclasse de `Passageiro` + uma fábrica na lista do `Main`. |
| Por que `Partida` e `JogoService` separados? | Igor | `Partida` = regras de uma partida; `JogoService` = ações que envolvem o ranking. |
| Por que não criaram interface para `MapaRenderer`? | Igor | Só existe uma implementação; seria abstração sem problema a resolver. |
| Por que `Passageiro` é agregação no diagrama? | Anderson | O passageiro troca de dono: sai da `Missao` e vai para a `Nave`. |
| Onde está a única referência a `JsonRankingRepository`? | Asafe | No `Main`. |
