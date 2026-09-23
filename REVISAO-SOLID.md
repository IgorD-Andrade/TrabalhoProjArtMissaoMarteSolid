# Revisão SOLID — Missão Marte Unifor

**Nome:** Igor Damasceno Andrade — matrícula 2113413
**Data:** 22/09/2026
**Escopo:** código inicial `src/exercicio10`, solução de referência do tutorial (`src/README.md`) e minha versão em `src/solidexercicio10`.

Cada observação indica se foi **resolvida** na minha versão ou se continua **pendente** (melhoria para uma próxima versão). A prioridade considera o impacto no jogo e o custo de fazer a mudança.

---

## Como validei a solução

- [x] compilação do código inicial — `javac -d out src/exercicio10/*.java` (sem erros)
- [x] compilação da versão refatorada — `javac -Xlint:all -d out $(find src/solidexercicio10 -name "*.java")` (sem erros e sem avisos)
- [x] início de uma missão — [evidência 03](docs/evidencias/03-refatorado-vitoria.txt)
- [x] movimentação, embarque e conclusão da missão — [evidência 03](docs/evidencias/03-refatorado-vitoria.txt) (vitória com 5 passageiros e 70 pontos)
- [x] consulta e reset do ranking — [evidência 04](docs/evidencias/04-refatorado-ranking-e-reset.txt) (inclui reset cancelado e reset com arquivo inexistente)
- [x] outros testes: colisões até o Game Over, entradas inválidas, fim de entrada, travamento do original e 39 testes automatizados (detalhes na seção [Testes realizados](#testes-realizados))

---

## Achados da revisão

### SRP — Responsabilidade única

```text
Local: exercicio10/Main (557 linhas)
Princípio relacionado: SRP
Observação: a classe tinha pelo menos nove motivos para mudar: menu, leitura de entrada,
  regras do turno, parâmetros de dificuldade, criação de passageiros, sorteio de posições,
  desenho do mapa, estatísticas e persistência do ranking (lista completa em
  docs/ANALISE-INICIAL.md).
Impacto para manutenção, testes ou evolução: qualquer mudança (ex.: trocar um símbolo)
  exigia abrir o mesmo arquivo que contém a regra de vitória; nenhuma regra podia ser
  testada sem simular o console e gravar em disco.
Proposta: dividir em Main (composição), JogoConsole/Terminal/MapaRenderer/PainelInformacoes
  (apresentação), JogoService/Partida/GeradorMissao (serviço), JsonRankingRepository/
  RankingJson (persistência). — RESOLVIDO
Prioridade: alta
```

```text
Local: tutorial — service/JogoService
Princípio relacionado: SRP
Observação: o JogoService do tutorial ainda contém o menu, o Scanner, todos os
  System.out e as regras do turno no mesmo método jogarPartida (≈100 linhas). Ele só
  tirou a persistência e o desenho de dentro da Main; o problema principal continuou.
Impacto para manutenção, testes ou evolução: para testar "embarcar soma 20 pontos" é
  preciso digitar um roteiro inteiro no Scanner; trocar o console por uma interface
  gráfica exigiria reescrever o serviço.
Proposta: separei em três classes: Partida (regras de um turno, recebe Comando e devolve
  ResultadoTurno com eventos), JogoService (casos de uso e ranking, sem console) e
  JogoConsole (menu e interação). Os testes de regra agora rodam sem console. — RESOLVIDO
Prioridade: alta
```

```text
Local: presentation/JogoConsole
Princípio relacionado: SRP
Observação: ainda reúne dois fluxos: o menu principal e o laço da partida (inclusive a
  leitura do tamanho do mapa). É a maior classe de apresentação (≈155 linhas).
Impacto para manutenção, testes ou evolução: baixo hoje; se o menu ganhar mais opções
  (configurações, créditos) a classe tende a crescer.
Proposta: extrair um ControladorPartida quando surgir a próxima opção de menu. Não fiz
  agora porque o ganho seria pequeno e criaria mais um arquivo sem problema concreto. — PENDENTE
Prioridade: baixa
```

### OCP — Aberto/fechado

```text
Local: exercicio10/Main.desenharMapa e tutorial — presentation/MapaRenderer
Princípio relacionado: OCP
Observação: o original escolhia o símbolo com instanceof; o tutorial trocou por
  comparação de texto (getTipo().equals("Engenheiro")). Nos dois casos, um passageiro
  novo exige editar o renderizador. Curiosamente, o tutorial criou getSimbolo() nas
  entidades, mas o renderizador não o utiliza.
Impacto para manutenção, testes ou evolução: esquecer de atualizar o renderizador faz o
  novo tipo aparecer com o símbolo errado, sem erro de compilação.
Proposta: MapaRenderer usa missao.entidadeEm(x, y).map(EntidadeMapa::getSimbolo) e monta
  a legenda com os tipos presentes no mapa. — RESOLVIDO (teste "OCP: novo passageiro
  (Médico)" cria uma subclasse só no teste e verifica "M=Médico" na legenda)
Prioridade: média
```

```text
Local: exercicio10/Main.criarPassageiroPolimorfico e tutorial — JogoService.posicionarPassageiros
Princípio relacionado: OCP
Observação: a criação usa switch/if por índice com os tipos concretos embutidos no serviço.
Impacto para manutenção, testes ou evolução: todo novo tipo exige alterar o serviço.
Proposta: interface funcional FabricaPassageiro; o GeradorMissao recebe a lista pelo
  construtor e o Main registra (x, y) -> new Professor(...). Novo tipo = nova classe +
  1 linha no Main. — RESOLVIDO
Prioridade: média
```

```text
Local: exercicio10/Main (definirPontuacaoInicial, criarNovaMissao) e tutorial
Princípio relacionado: OCP
Observação: os parâmetros de cada dificuldade estavam espalhados em switch/if. O
  texto do prompt "facil/medio/dificil" também era fixo.
Impacto para manutenção, testes ou evolução: criar um nível "INSANO" exigia mexer em três
  métodos e no texto da tela.
Proposta: Dificuldade virou um enum com pontuacaoInicial e quantidades; o prompt é gerado
  a partir de Dificuldade.values(). Criar um nível = adicionar uma constante. — RESOLVIDO
Prioridade: média
```

```text
Local: presentation/PainelInformacoes.texto(EventoTurno)
Princípio relacionado: OCP
Observação: um evento novo exige adicionar um caso ao switch.
Impacto para manutenção, testes ou evolução: é uma edição, mas controlada: o switch é
  exaustivo, então o compilador aponta o caso faltante.
Proposta: manter. Colocar o texto dentro do enum EventoTurno misturaria regra e
  apresentação (violaria SRP), e um mapa de mensagens perderia a checagem do compilador.
Prioridade: baixa
```

### LSP — Substituição de Liskov

```text
Local: exercicio10/Passageiro
Princípio relacionado: LSP
Observação: a classe base era concreta e tinha getPontuacao() = 10 "por padrão", além
  de um campo tipo em texto que repetia o nome da subclasse. Era possível criar um
  "passageiro genérico" que não existe no domínio.
Impacto para manutenção, testes ou evolução: uma subclasse nova que esquecesse de
  sobrescrever getPontuacao() valeria 10 silenciosamente.
Proposta: Passageiro abstrata com getTipo(), getPontuacao() e getSimbolo() abstratos e o
  contrato documentado (pontuação positiva e fixa). O compilador obriga a implementação.
  — RESOLVIDO (concordo com o tutorial neste ponto)
Prioridade: média
```

```text
Local: tutorial — model/Nave.embarcar e model/Missao.embarcarPassageiroNaPosicao
Princípio relacionado: LSP / encapsulamento
Observação: no tutorial, Nave.embarcar é void e ignora silenciosamente quando está cheia;
  por isso Missao repete a verificação de capacidade antes de chamar. A regra "cabe mais
  um?" fica duplicada e quem chama Nave.embarcar diretamente não sabe se funcionou.
Impacto para manutenção, testes ou evolução: se a capacidade mudar de regra (ex.:
  astronauta ocupa 2 lugares), é preciso lembrar dos dois lugares.
Proposta: Nave.embarcar retorna boolean e é a única dona da regra; Missao apenas repassa.
  — RESOLVIDO
Prioridade: média
```

```text
Local: model/Movel.mover(dx, dy)
Princípio relacionado: LSP
Observação: o contrato de Movel não garante que a entidade continue dentro do mapa: quem
  garante é a Missao (moverDentroDoMapa). Um cliente que chame nave.mover(10, 0) direto
  tira a nave do mapa.
Impacto para manutenção, testes ou evolução: o contrato é mais fraco do que o esperado
  pelo jogo; hoje só a Missao chama mover, então não há defeito, mas nada impede.
Proposta: tornar mover visível só para o pacote model (a Missao está no mesmo pacote) ou
  fazer Movel receber o Mapa. Não mudei porque a interface pública Movel faz parte do
  diagrama pedido. — PENDENTE
Prioridade: média
```

### ISP — Segregação de interfaces

```text
Local: tutorial — repository/RankingRepository.salvar(String nome, int pontuacao)
Princípio relacionado: ISP
Observação: a interface tinha uma sobrecarga que nenhum cliente usava. Ela só existia
  para preencher valores falsos (MEDIO, 0 passageiros, 0 segundos) e obrigava qualquer
  implementação nova (banco, memória) a implementá-la. Da mesma forma, JogoService tinha
  métodos públicos registrarPontuacao/listarRanking que ninguém chamava.
Impacto para manutenção, testes ou evolução: mais código a manter em cada implementação
  e possibilidade de gravar registros incompletos no ranking.
Proposta: RankingRepository com apenas salvar(RankingEntry), listar() e limpar() — todos
  usados pelo JogoService. — RESOLVIDO
Prioridade: média
```

```text
Local: model/Posicionavel e model/Movel
Princípio relacionado: ISP
Observação: a separação faz sentido: Asteroide, Passageiro e PlataformaPouso só têm
  posição; Nave e Inimigo também se movem. Missao.moverDentroDoMapa declara
  <T extends Posicionavel & Movel>, exigindo exatamente o que usa.
Impacto para manutenção, testes ou evolução: evita que um asteroide seja obrigado a ter
  um mover() vazio ou que lance exceção (o que também violaria LSP).
Proposta: manter. Ressalva honesta: com apenas duas classes móveis, o ganho é pequeno;
  a interface se paga se surgirem novos móveis (ex.: um passageiro que foge). — CONCORDO
Prioridade: baixa
```

### DIP — Inversão de dependência

```text
Local: service/JogoService → repository/RankingRepository
Princípio relacionado: DIP
Observação: o serviço recebe o contrato pelo construtor e não importa
  JsonRankingRepository. Só o Main conhece a implementação concreta.
Impacto para manutenção, testes ou evolução: trocar o arquivo por banco de dados ou
  memória é criar uma classe e mudar uma linha no Main. O teste "DIP: mesmo serviço
  funciona com repositório em memória e em JSON" executa o mesmo cenário com as duas.
Proposta: manter. — CONCORDO (decisão do tutorial)
Prioridade: alta
```

```text
Local: tutorial — JogoService (construtor) e Missao.moverInimigos
Princípio relacionado: DIP
Observação: o JogoService do tutorial faz new MapaRenderer() e new Random() dentro do
  construtor, e a Missao usa Math.random(). São dependências escondidas: não dá para
  trocar o renderizador nem fixar a aleatoriedade em um teste.
Impacto para manutenção, testes ou evolução: movimentos dos inimigos não reproduzíveis;
  testes de colisão ficam dependentes da sorte.
Proposta: Random, Clock, GeradorMissao e renderizadores são injetados pelo Main. Isso
  permitiu testes determinísticos, a data/hora testável e a opção --seed para a
  apresentação. — RESOLVIDO
Prioridade: média
```

```text
Local: presentation/JogoConsole e PainelInformacoes → repository.RankingEntry /
  RankingPersistenciaException
Princípio relacionado: DIP
Observação: a apresentação ainda importa dois tipos do pacote de persistência para
  exibir o ranking e tratar falhas de gravação (aparece no diagrama de pacotes).
Impacto para manutenção, testes ou evolução: se o repositório mudar o formato do
  registro, a tela precisa mudar junto.
Proposta: o JogoService devolver um DTO próprio (ex.: service.PosicaoRanking) e traduzir a
  exceção. Não fiz agora porque o RankingEntry já é imutável e simples; a mudança
  dobraria as classes de ranking sem benefício imediato. — PENDENTE
Prioridade: baixa
```

---

## Melhorias adicionais identificadas

```text
Local: exercicio10/Main.criarNovaMissao
Princípio relacionado: — (defeito de robustez)
Observação: com mapa tamanho 1 em Médio ou Difícil o original entra em laço infinito
  (9 células, a nave ocupa 1, mas há 9 ou 11 entidades a posicionar). A solução do
  tutorial troca o laço por uma exceção, mas a exceção não é tratada e derruba o jogo.
Impacto para manutenção, testes ou evolução: o jogo trava por uma entrada válida.
Proposta: Dificuldade calcula o tamanho mínimo; o console mostra o intervalo aceito e usa
  o padrão fora dele; o GeradorMissao embaralha as células livres (nunca repete sorteio).
  — RESOLVIDO (evidências 02 e 05)
Prioridade: alta
```

```text
Local: exercicio10/Main.lerLinha / tutorial — JogoService.lerLinha
Princípio relacionado: — (robustez)
Observação: fim de entrada (Ctrl+D) causa NoSuchElementException em ambos.
Proposta: Terminal.lerLinha devolve Optional; fim de entrada aborta a partida e sai do
  menu normalmente. — RESOLVIDO (evidência 07)
Prioridade: média
```

```text
Local: exercicio10/Main.parseRankingJson
Princípio relacionado: SRP (formato separado do acesso a disco)
Observação: o parser divide o texto por vírgulas; um piloto chamado "Silva, J." perde o
  registro ao recarregar. Um arquivo corrompido é sobrescrito sem aviso.
Proposta: RankingJson com leitor que respeita aspas e escapes; gravação atômica
  (temporário + move) e cópia .corrompido. — RESOLVIDO
Prioridade: média
```

```text
Local: repository/JsonRankingRepository.salvar
Princípio relacionado: SRP
Observação: o arquivo guarda todas as vitórias que entraram no Top 5 no momento em que
  foram jogadas; o corte em 5 é feito só na leitura, então o arquivo cresce aos poucos.
Proposta: o serviço poder pedir ao repositório para manter só os N melhores
  (ex.: método manterMelhores(int)), ou aceitar o crescimento, que é insignificante aqui.
  — PENDENTE
Prioridade: baixa
```

```text
Local: projeto (build e testes)
Princípio relacionado: —
Observação: sem Maven/Gradle, os testes usam um mini-framework próprio (Verifica) e o JSON
  usa um parser próprio.
Proposta: numa próxima versão, adotar Maven + JUnit 5 e uma biblioteca JSON (Gson). Mantive
  sem dependências porque a disciplina pede compilação direta com javac. — PENDENTE
Prioridade: média
```

---

## Decisões do tutorial com as quais concordo

1. **Main como raiz de composição (Passo 1).** É o único lugar que conhece implementações concretas. Benefício direto: a troca de persistência e o teste com `RankingEmMemoria` foram triviais. Ficou com 69 linhas, só de montagem.
2. **`RankingRepository` como abstração (Passo 4).** Benefício comprovado por teste: o mesmo `JogoService` roda com JSON e com memória. É a decisão que mais ajuda a testar.
3. **`Passageiro` abstrato e hierarquia `EntidadeMapa` (Passo 5).** Tira o "passageiro genérico" e faz o compilador cobrar pontuação e símbolo de cada tipo novo.
4. **Separar `Posicionavel` de `Movel`.** Evita métodos vazios em entidades fixas (com a ressalva de tamanho registrada acima).
5. **Pacotes `model / service / presentation / repository`.** Mantive a estrutura sugerida porque ela deixa as dependências apontarem para o `model`, que ficou sem nenhuma dependência.

## Decisões com as quais não concordo (e o que fiz)

| # | Decisão do tutorial | Por que discordo | O que fiz |
|---|---|---|---|
| 1 | Ranking gravado como texto `nome\|pontos\|...` | A atividade anterior exigia `ranking.json`; o formato de texto quebra se o nome tiver `\|` e não lê o arquivo do exercício 10. A refatoração deveria preservar o comportamento. | JSON com as mesmas chaves do original |
| 2 | `JogoService` com `Scanner`, `System.out` e menu | É a mesma mistura da `Main` original, apenas com outro nome; impede testar as regras. | `Partida` + `JogoService` + `JogoConsole` |
| 3 | Pontuações Professor 15, Engenheiro 20, Astronauta 10 | Altera a regra do jogo sem justificativa (original: 10/15/20). Uma refatoração não deve mudar comportamento. | Mantive 10/15/20, com teste |
| 4 | `moverInimigos` com `Math.random()`, diagonais e sem limites | Inimigos saem do mapa e somem; o movimento não é reproduzível em teste. | Movimento em 4 direções, limitado pelo `Mapa`, com `Random` injetado (teste de 10.000 turnos) |
| 5 | Reset do ranking sem confirmação | Regressão em relação ao original, que pedia (s/n). | Confirmação mantida |
| 6 | Implementação chamada `RankingService` no pacote `repository` | O nome sugere camada de serviço e confunde com `JogoService`. | `JsonRankingRepository` |
| 7 | `EntidadeMapa` com `x`/`y` `protected` | Qualquer subclasse altera a posição livremente (encapsulamento fraco). | Campos `private` + `protected deslocar()` |
| 8 | Capacidade da nave = quantidade de passageiros da dificuldade; Difícil com 6 passageiros | Muda duas regras do original (capacidade fixa 5; Difícil com 5). | Capacidade 5 e quantidades do original, centralizadas no enum |
| 9 | Eixo Y invertido (`w` soma 1 e o mapa é desenhado de cima para baixo a partir de `maxY`) | É coerente, mas muda as coordenadas mostradas ao jogador em relação ao original sem ganho. | Mantive o eixo original (`w` diminui Y) |
| 10 | `MapaRenderer` com sobrecargas que fixam o mapa em -2..2 e `Nave.getSimbolo()` = `'N'` enquanto a tela desenha `'@'` | Código que não é usado ou que contradiz o comportamento real. | Um único `desenhar`, e o símbolo vem sempre da entidade |

### A arquitetura está adequada ao tamanho do projeto?

Passei de 10 para 34 arquivos. Reavaliando cada abstração que **eu** acrescentei:

- **Justificadas por um problema concreto:** `Mapa` (removeu quatro parâmetros repetidos em seis métodos), `FabricaPassageiro` (OCP comprovado por teste), `Terminal` (corrigiu a exceção de fim de entrada), `RankingJson` (corrigiu o parser), `Partida` + `ResultadoTurno` + `EventoTurno` (permitiram 8 testes de regra sem console).
- **Discutíveis:** `EstadoPartida` e `EventoTurno` se sobrepõem em parte (`MISSAO_CUMPRIDA` ↔ `VITORIA`); poderiam virar um só enum. `PlataformaPouso` como classe poderia ser uma constante; mantive porque eliminou a coordenada (0,0) repetida em dois lugares e permite desenhar tudo pelo mesmo `getSimbolo()`.
- **Não criei**, de propósito: interface para `MapaRenderer`, interface para `Terminal` e interface para `GeradorMissao`. Hoje existe uma única implementação de cada e os testes conseguem usá-las diretamente (injetando `Scanner` e `PrintStream`); criar a interface seria abstração sem problema a resolver.

Conclusão: para um jogo de console deste tamanho, a divisão em quatro pacotes é adequada; a granularidade dentro de `service` está no limite do razoável.

---

## Testes realizados

### Automatizados — `java -cp out solidexercicio10.ExecutarTestes`

**Resultado: 39 testes, 39 sucessos, 0 falhas** ([saída](docs/evidencias/08-testes-automatizados.txt)). Para confirmar que os testes realmente detectam erros, alterei temporariamente a pontuação do Astronauta para 10 e removi a checagem de limites da `Missao`: 9 testes falharam, como esperado.

| Grupo | Qtd. | O que verifica |
|---|---|---|
| Modelo | 11 | pontuação 10/15/20 via referência `Passageiro` (LSP), `Dificuldade.deString` com acentos, parâmetros e tamanho mínimo, capacidade 5, 3 vidas, limites da nave, inimigo nunca sai do mapa em 10.000 turnos, posições inválidas, prioridade do desenho, embarque, colisões |
| Partida | 8 | custo de movimento, soma polimórfica, sem passageiro, nave cheia, vitória **só** em (0,0) após resgatar todos, 3 colisões → Game Over, pontuação zerada, abortar |
| GeradorMissao / OCP | 4 | quantidades por dificuldade sem sobreposição (30 sementes × 3 níveis × vários mapas), mapa pequeno falha rápido, rotação de tipos igual ao original, novo tipo `Medico` sem alterar código |
| JogoService / DIP | 6 | piloto padrão, registro com data/hora/dificuldade/passageiros, derrota não entra, Top 5 ordenado, reset, **mesmo cenário com JSON e com memória** |
| Repositório JSON | 5 | ida e volta com vírgula/aspas/acentos, arquivo ausente/vazio, corrompido preservado, leitura do formato do exercício 10, limpar |
| Console (ponta a ponta) | 5 | menu/opção inválida/reset, reset cancelado, entradas inválidas, fim de entrada, **partida completa até a vitória** (roteiro gerado pelo `PilotoAutomatico`) |

### Manuais — programa real, saídas em `docs/evidencias/`

| # | Cenário | Resultado |
|---|---|---|
| 01 | Original: menu, partida curta, ranking, reset | OK — registrado como referência |
| 02 | Original: mapa 1 em Médio | **Falha** — travou (encerrado por `timeout`, código 124) |
| 03 | Refatorado `--seed 5`, Médio, mapa 3: partida completa | OK — alerta de retorno à plataforma, vitória, 70 pts, estatísticas, entrou no Top 5 |
| 04 | Refatorado: nova execução lê o ranking salvo; reset cancelado; reset confirmado; reset sem arquivo | OK — ranking persistiu entre execuções; nenhum erro sem arquivo |
| 05 | Refatorado: nome vazio, dificuldade inválida, mapa "1" em Médio, mapa "abc", comando inválido, opção de menu inválida | OK — padrões aplicados, sem travar |
| 06 | Refatorado `--seed 1`, Difícil, mapa 2 | OK — 2 alertas de colisão, Game Over, estatísticas com 0 vidas |
| 07 | Refatorado: entrada termina no meio da partida | OK — partida abortada, saída com código 0 |

---

## Melhoria implementada (extensão opcional)

**Separar a entrada do usuário da orquestração do jogo** (uma das sugestões do tutorial) e **persistência em memória para testes**.

Antes (tutorial), a regra do embarque estava presa ao console:

```java
// JogoService.jogarPartida — tutorial
String entrada = lerLinha(scanner, "Comando (w/s/a/d/c/q): ", "").trim().toLowerCase();
char cmd = entrada.charAt(0);
if (cmd == 'c') {
    Passageiro passageiro = missao.passagemNaPosicao();
    ...
    score += passageiro.getPontuacao();
    System.out.printf("Passageiro %s embarcado com sucesso! +%d pontos!%n", ...);
```

Depois, a regra não conhece teclado nem tela, e é testada em três linhas:

```java
// PartidaTest
partida.executar(Comando.DIREITA);
ResultadoTurno r = partida.executar(Comando.EMBARCAR);
Verifica.igual(49, partida.getPontuacao(), "30 - 1 + 20");
```

O console apenas traduz a tecla (`'c'` → `Comando.EMBARCAR`) e escolhe o texto de cada `EventoTurno`.

---

## Respostas às perguntas orientadoras

- **Quais motivos diferentes faziam a classe original mudar?** Nove, listados em `docs/ANALISE-INICIAL.md`: menu, entrada, regras, dificuldade, criação de passageiros, posicionamento, desenho, estatísticas e persistência.
- **O que seria necessário para trocar o arquivo por banco de dados ou memória?** Criar uma classe que implemente `RankingRepository` e alterar uma linha no `Main`. Já existe `RankingEmMemoria` e o teste de DIP prova que o serviço não muda.
- **Como adicionar um novo tipo de passageiro sem modificar a lógica principal?** Criar a subclasse de `Passageiro` (tipo, pontuação, símbolo) e registrar `(x, y) -> new Medico(...)` na lista do `Main`. Mapa, legenda, embarque e pontuação funcionam sem alteração (teste `Medico`).
- **As subclasses de `Passageiro` respeitam o contrato?** Sim: nenhuma lança exceção, todas devolvem pontuação positiva fixa, tipo e símbolo próprios, e o código cliente nunca usa `instanceof`.
- **Alguma interface tem métodos que seus clientes não usam?** Na minha versão, não. No tutorial, `RankingRepository.salvar(nome, pontuacao)` não era usado e foi removido.
- **A arquitetura está adequada ao tamanho do projeto?** Em geral sim; a seção acima aponta onde ela está no limite (`EstadoPartida`/`EventoTurno`) e as abstrações que deixei de criar de propósito.
- **Qual melhoria eu implementaria primeiro?** Restringir `Movel.mover` ao pacote `model` (prioridade média, contrato de LSP mais forte); depois, adotar Maven + JUnit.
