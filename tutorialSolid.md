# Tutorial SOLID: Refatoração da Missão Marte

Este documento apresenta a ordem recomendada para realizar a refatoração do
jogo **Missão Marte Unifor**. O código-fonte existente deve ser mantido. Este
arquivo reorganiza apenas a leitura e a execução do tutorial.

## 1. Preparação

O código inicial está em [src/exercicio10](src/exercicio10). A implementação
refatorada será criada pelo aluno a partir dos blocos de código deste documento.

Antes de iniciar:

1. Compile e execute o código inicial.
2. Teste o menu, uma missão, o ranking e o reset do ranking.
3. Anote os comportamentos que precisam continuar funcionando.
4. Crie ou use um pacote separado para a sua refatoração.
5. Preserve a versão inicial para comparar o antes e o depois.

O objetivo não é apenas reorganizar arquivos. A equipe deve identificar os
motivos de mudança, reduzir acoplamento, justificar as abstrações e manter o
jogo funcionando.

### Como ler a refatoração

SOLID não é uma lista de passos mecânicos nem significa criar uma classe para
cada método. Ele serve como uma lente para avaliar o desenho do programa:

- **SRP (Responsabilidade Única):** uma classe deve reunir responsabilidades
  relacionadas e ter um motivo principal para mudar.
- **OCP (Aberto/Fechado):** o comportamento deve poder ser ampliado com novas
  implementações sem alterar desnecessariamente código estável.
- **LSP (Substituição de Liskov):** uma subclasse deve poder ocupar o lugar da
  classe base sem surpreender quem usa o contrato.
- **ISP (Segregação de Interfaces):** clientes não devem depender de métodos
  que não precisam utilizar.
- **DIP (Inversão de Dependência):** regras importantes devem depender de
  abstrações; detalhes, como arquivo e console, devem depender dessas regras.

Em cada etapa, use este roteiro de leitura:

1. **Problema:** qual mudança ficaria difícil no código original?
2. **Decisão:** qual classe, interface ou camada passa a cuidar disso?
3. **Princípio:** qual ideia do SOLID essa decisão exemplifica?
4. **Custo:** qual complexidade foi adicionada e por que ela vale a pena?
5. **Evidência:** qual teste ou compilação mostra que o comportamento foi preservado?

Uma boa refatoração não elimina toda responsabilidade de uma classe. Ela
separa responsabilidades que mudam por motivos diferentes e cria contratos
pequenos nos pontos em que existe variação real.

## 2. Ordem da refatoração

Siga obrigatoriamente esta ordem:

1. `model`: entidades, interfaces e regras do domínio;
2. `repository`: contrato e persistência do ranking;
3. `presentation`: exibição do mapa;
4. `service`: orquestração do jogo;
5. `Main`: composição das dependências e ponto de entrada.

A ordem evita que o projeto fique incompleto por muito tempo. Compile ao final
de cada etapa antes de iniciar a próxima.

## 3. Etapa 1: modelo do domínio

Comece pelas classes que representam os objetos do jogo. O domínio não deve
conhecer `Scanner`, arquivos ou detalhes de apresentação.

Crie ou organize os arquivos nesta ordem:

1. `Posicionavel`: contrato para objetos com coordenadas;
2. `Movel`: contrato para objetos que podem se mover;
3. `EntidadeMapa`: classe abstrata comum às entidades do mapa;
4. `Dificuldade`: enum com os níveis da missão;
5. `Passageiro`: classe abstrata com nome, tipo e pontuação;
6. `Professor`, `Engenheiro` e `Astronauta`;
7. `Asteroide` e `Inimigo`;
8. `Nave`: posição, vidas, capacidade e passageiros embarcados;
9. `Missao`: nave, passageiros, perigos, embarque e colisões.

Use os blocos de código da seção 12 para criar os arquivos em
`src/solidexercicio10/model`.

### Por que o modelo aplica SOLID

No código inicial, a classe principal conhece detalhes de passageiros, nave,
perigos, coordenadas e regras da partida. O primeiro movimento é retirar os
objetos do jogo de dentro do fluxo de entrada. Assim, o domínio pode ser
testado sem `Scanner`, console ou arquivo.

- **SRP:** `Nave`, `Missao` e os tipos de passageiro cuidam de conceitos do
  domínio; nenhum deles precisa desenhar o mapa ou salvar o ranking.
- **OCP:** para adicionar um novo tipo de passageiro, a extensao natural e uma
  nova subclasse de `Passageiro`, mantendo o contrato usado pela missão.
- **LSP:** `Professor`, `Engenheiro` e `Astronauta` podem ser armazenados em
  `List<Passageiro>` e usados por `getPontuacao()` e `getSimbolo()` sem testes
  específicos para cada classe.
- **ISP:** `Posicionavel` declara somente coordenadas e `Movel` declara
  somente movimento. Uma classe que apenas tem posição não é obrigada a ser
  móvel.

O ganho não está apenas na quantidade de arquivos. A pergunta de projeto é:
"o que deve mudar se a regra de pontuação de um passageiro mudar?". Com essa
estrutura, a resposta tende a ser a classe daquele passageiro, e não o fluxo
inteiro do jogo.

### O que observar

- `EntidadeMapa` implementa `Posicionavel`.
- `Passageiro` herda de `EntidadeMapa`.
- Os tipos de passageiros podem ser tratados como `Passageiro`.
- `Nave` e `Inimigo` implementam `Movel`.
- `Missao` possui uma nave e coleções de entidades.

### Validação do modelo

```powershell
New-Item -ItemType Directory -Force -Path out | Out-Null
javac -d out (Get-ChildItem -Filter *.java -Path src/solidexercicio10/model | ForEach-Object FullName)
```

### Reflexão sobre o modelo

- Qual é o contrato comum entre as entidades do mapa?
- Por que `Passageiro` é uma classe abstrata?
- As subclasses podem substituir `Passageiro` sem quebrar o comportamento esperado?
- Se uma nova classe implementar `Posicionavel`, ela precisa implementar
  movimento? O que isso revela sobre ISP?
- Qual comportamento comum pertence a `Passageiro` e qual comportamento deve
  continuar nas subclasses?

## 4. Etapa 2: persistência do ranking

Crie a camada de ranking depois que o modelo estiver disponível:

1. `RankingEntry`: dados de uma pontuação;
2. `RankingRepository`: contrato de salvar, listar e limpar;
3. `RankingService`: persistência concreta em arquivo.

Use os blocos de código da seção 12 para criar os arquivos em
`src/solidexercicio10/repository`.

O serviço do jogo deverá depender de `RankingRepository`, e não diretamente
de `RankingService`. Essa decisão aplica o DIP e permite trocar arquivo por
memória, banco ou outra implementação.

### Por que o ranking aplica SOLID

O ranking tem dois motivos diferentes para mudar: o formato dos dados de uma
pontuação e a forma de persistir esses dados. `RankingEntry` representa o
registro, `RankingRepository` define o que o jogo precisa e `RankingService`
resolve o detalhe do arquivo.

- **SRP:** a persistência fica fora da regra da missão. Alterar o arquivo não
  exige alterar embarque, colisão ou pontuação.
- **DIP:** `JogoService` recebe `RankingRepository` no construtor. O serviço
  conhece a operação `salvar/listar/limpar`, mas não conhece `Path` nem
  `Files`.
- **OCP:** uma implementação em memória pode ser adicionada para testes, e uma
  implementação em banco pode substituir a de arquivo sem reescrever o fluxo
  do jogo.

Esse desacoplamento também torna o teste mais barato. Um repositório em
memória pode registrar chamadas e devolver dados previsíveis, sem criar ou
apagar arquivos durante cada teste.

### Validação do ranking

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java -Path src/solidexercicio10/model,src/solidexercicio10/repository | ForEach-Object FullName)
```

### Reflexão sobre o ranking

- O que muda se a persistência deixar de ser um arquivo?
- Por que o contrato deve ser uma interface?
- Como testar o ranking sem criar um arquivo real?
- Qual classe deveria mudar se o formato da linha do arquivo fosse alterado?
- A interface possui alguma operação que `JogoService` não usa? Se possuir,
  isso indica uma possível oportunidade de ISP.

## 5. Etapa 3: apresentação

Crie `presentation/MapaRenderer.java`.

A apresentação deve somente exibir o estado da missão:

- coordenadas do mapa;
- nave;
- plataforma `L` na origem `(0,0)`;
- passageiros e perigos;
- legenda e comandos.

O renderer não deve movimentar a nave, calcular pontuação, embarcar
passageiros ou salvar o ranking.

### Por que a apresentação aplica SOLID

Exibir o mapa e jogar a partida são responsabilidades diferentes: o console
precisa saber como transformar um estado em texto, enquanto o serviço precisa
decidir o que acontece depois de um comando. Manter essas responsabilidades
separadas evita que uma mudança visual altere uma regra do jogo.

- **SRP:** `MapaRenderer` tem um motivo principal para mudar: o formato da
  apresentação.
- **DIP, por composição:** o serviço usa um objeto renderer em vez de misturar
  cada `printf` com movimentação e pontuação.
- **OCP:** uma futura apresentação gráfica pode implementar outra estratégia de
  exibição. Nesta versão, a classe concreta ainda é suficiente; não crie uma
  interface apenas por antecipação.

Aqui aparece uma decisão importante: SOLID não exige que toda dependência seja
uma interface. A abstração deve resolver uma variação real. O contrato do
ranking é útil porque a persistência pode variar; para o renderer, a classe
simples já deixa a responsabilidade isolada.

Use o bloco de código da seção 12 para criar
`src/solidexercicio10/presentation/MapaRenderer.java`.

### Validação da apresentação

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java -Path src/solidexercicio10/model,src/solidexercicio10/presentation | ForEach-Object FullName)
```

### Reflexão sobre a apresentação

- O que seria necessário alterar para trocar o console por uma interface gráfica?
- Por que a regra de embarque não deve ficar no renderer?
- Qual requisito de uma interface gráfica mudaria apenas a apresentação?
- O renderer recebe dados suficientes para desenhar, mas tem poder demais
  para alterar a missão? Como você reduziria esse acoplamento numa próxima
  iteracao?

## 6. Etapa 4: serviço do jogo

Crie `service/JogoService.java`.

O serviço coordena o caso de uso da partida:

- menu e comandos;
- criação da missão;
- movimentação, embarque e colisões;
- pontuação, movimentos e tempo;
- chamada do renderer;
- chamada do repositório para salvar o ranking.

As posições de passageiros e perigos devem ser sorteadas em todo o mapa, sem
sobreposição e sem ocupar a origem. A capacidade da nave deve comportar todos
os passageiros da dificuldade escolhida. A contagem deve mostrar passageiros a
bordo, restantes no mapa e total.

### Pontuação da missão

Use estas regras para conferir o comportamento da partida:

| Situação | Pontos |
| --- | ---: |
| Pontuação inicial no nível fácil | 30 |
| Pontuação inicial no nível médio | 20 |
| Pontuação inicial no nível difícil | 15 |
| Embarcar um Professor | +15 |
| Embarcar um Engenheiro | +20 |
| Embarcar um Astronauta | +10 |
| Realizar um movimento válido | -1 |

A pontuação final é calculada pela pontuação inicial, pelos passageiros
embarcados e pelos movimentos realizados. Colisões reduzem uma vida da nave;
elas não acrescentam nem retiram pontos diretamente. A partida termina quando
todos os passageiros são embarcados e a nave retorna a `(0,0)`, ou quando os
pontos chegam a zero ou as vidas acabam.

Use o bloco de código da seção 12 para criar
`src/solidexercicio10/service/JogoService.java`.

### Por que o serviço aplica SOLID

`JogoService` é um coordenador de caso de uso: recebe comandos, chama o
domínio, atualiza o renderer e salva o resultado. Ele ainda concentra bastante
lógica nesta versão de ensino, e isso é intencional para manter o fluxo
visível. A separação anterior impede que ele também seja responsável pela
persistência concreta e pelos detalhes de desenho.

- **SRP:** o serviço tem como motivo principal de mudança o fluxo da partida.
  Criação aleatória de missões poderia ser extraída depois para uma
  `MissaoFactory`, caso essa regra cresça.
- **DIP:** o construtor recebe `RankingRepository`, invertendo a direção da
  dependência: a regra do jogo não depende do arquivo.
- **OCP:** novos passageiros aproveitam `Passageiro` e novos repositórios
  aproveitam `RankingRepository`; o fluxo estável não precisa conhecer cada
  detalhe novo.
- **LSP:** o serviço trabalha com `Passageiro`, portanto deve funcionar com
  qualquer subclasse que respeite o contrato de pontuação, nome, tipo e
  símbolo.

Observe também o limite da proposta. Um serviço que coordena muitas coisas não
é automaticamente uma violação de SRP; o sinal de alerta é ter vários motivos
independentes para mudar. Se regras de geração, colisão ou pontuação crescerem,
extraia-as por coesão e cubra cada extração com testes.

### Validação do serviço

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java -Path src/solidexercicio10/model,src/solidexercicio10/repository,src/solidexercicio10/presentation,src/solidexercicio10/service | ForEach-Object FullName)
```

Teste pelo menos:

- posições aleatórias em mais de uma partida;
- contagem de passageiros;
- plataforma de retorno em `(0,0)`;
- consulta e limpeza do ranking;
- encerramento voluntário e fim de partida.

### Reflexão sobre o serviço

- O `JogoService` ainda possui responsabilidades demais?
- A criação da missão poderia ser extraída para uma `MissaoFactory`?
- O serviço depende de abstrações ou de detalhes concretos?
- Qual mudança pode ser feita sem tocar em `JogoService`: trocar o arquivo,
  criar um passageiro ou alterar a legenda do mapa?
- Em que ponto a classe ficaria grande o bastante para justificar uma
  `MissaoFactory` ou um serviço de pontuação?

## 7. Etapa 5: ponto de entrada

Crie `Main.java` por último.

O `Main` deve apenas:

1. exibir a mensagem inicial;
2. criar uma implementação de `RankingRepository`;
3. criar `JogoService` usando a abstração;
4. iniciar o loop com `Scanner`.

Use o bloco de código da seção 12 para criar `src/solidexercicio10/Main.java`.

### Validação final

```powershell
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path out | Out-Null
javac -d out (Get-ChildItem -Recurse -Filter *.java -Path src/solidexercicio10 | ForEach-Object FullName)
java -cp out solidexercicio10.Main
```

Verifique o menu, uma partida, o ranking, o reset e a saída do jogo.

### Por que esta etapa aplica SOLID

O `Main` é o composition root: o lugar em que os detalhes concretos são
escolhidos e conectados. Ele pode conhecer `RankingService`, porque alguém
precisa escolher a implementação real; o restante da aplicação recebe o
contrato `RankingRepository`.

- **SRP:** `Main` inicia a aplicação e monta dependências. Ele não contém a
  regra de uma partida.
- **DIP:** a dependência concreta fica na borda do sistema, enquanto
  `JogoService` depende da abstração.

Essa distribuição mostra que "depender de abstração" não significa eliminar
todos os detalhes concretos. Significa concentrar as escolhas de infraestrutura
em um ponto pequeno, para que o núcleo da regra de negócio permaneça estável.

### Roteiro de verificação por princípio

Antes de considerar a refatoração concluída, responda com evidência do código:

| Princípio | Pergunta sobre esta implementação | Evidência esperada |
| --- | --- | --- |
| SRP | Uma mudança no ranking exige editar o modelo ou a apresentação? | Classes separadas e compilação por etapa |
| OCP | Um novo passageiro ou repositório exige alterar o fluxo inteiro? | Nova implementação usando os contratos existentes |
| LSP | Toda subclasse de `Passageiro` funciona na lista da missão? | Embarque e pontuação sem `instanceof` |
| ISP | As interfaces representam capacidades pequenas? | `Posicionavel` e `Movel` com contratos distintos |
| DIP | O serviço conhece o arquivo de ranking? | Construtor tipado como `RankingRepository` |

Se alguma resposta for "não", registre o achado em `REVISAO-SOLID.md` em vez
de esconder a limitação. Uma revisão crítica faz parte do aprendizado.

## 8. Relação com SOLID

| Princípio | Aplicação na Missão Marte | Limite ou pergunta crítica |
| --- | --- | --- |
| **SRP** | Modelo, apresentação, serviço e persistência possuem responsabilidades separadas. | `JogoService` ainda pode crescer; extraia apenas quando houver coesão e motivos de mudança distintos. |
| **OCP** | Novos passageiros e repositórios usam contratos existentes. | Se cada variação exigir muitos `if`, ainda existe uma regra difícil de estender. |
| **LSP** | `Professor`, `Engenheiro` e `Astronauta` substituem `Passageiro`. | A subclasse deve respeitar o contrato e não quebrar expectativas de nome, símbolo ou pontuação. |
| **ISP** | `Posicionavel` e `Movel` são interfaces pequenas. | Não misture capacidades que nem todas as entidades possuem. |
| **DIP** | `JogoService` recebe `RankingRepository`; `RankingService` fica na borda. | A abstração deve existir por uma variação real, não apenas para aumentar o número de arquivos. |

### Comparacao antes e depois

Use esta tabela durante a revisão do código original e da versão refatorada:

| Antes | Depois | Resultado esperado |
| --- | --- | --- |
| `Main` lê comandos, desenha, aplica regras e grava arquivo. | `Main` monta dependências e delega para `JogoService`. | Menos motivos para alterar o ponto de entrada. |
| Regras conhecem detalhes do arquivo. | `JogoService` conhece `RankingRepository`. | Persistência substituível e testável. |
| Tipos de passageiros ficam misturados no fluxo. | Subclasses implementam o contrato de `Passageiro`. | Extensão por polimorfismo e substituição. |
| Apresentação e regra usam os mesmos trechos de código. | `MapaRenderer` somente desenha o estado. | Mudanças visuais com menor risco para o domínio. |

SOLID não determina uma única quantidade de classes. A equipe pode escolher
outra estrutura, desde que explique o problema resolvido e os custos da escolha.

## 9. UML

Entregue dois diagramas:

- diagrama de classes das entidades de `model`;
- diagrama de pacotes do projeto.

Os modelos estão em [docs/uml](docs/uml), nos formatos PlantUML e Mermaid.
Inclua os arquivos-fonte e as imagens ou visualizações no repositório.

## 10. Revisão crítica

Crie `REVISAO-SOLID.md` contendo:

- uma observação sobre cada princípio SOLID;
- uma melhoria adicional;
- uma decisão do tutorial com a qual a equipe concorda;
- uma decisão com a qual a equipe discorda, com justificativa;
- testes realizados e resultados;
- prioridade das melhorias.

A revisão deve considerar complexidade, tamanho do projeto, testabilidade e
custo de manutenção.

## 11. Entrega e apresentação

Publique o projeto em um repositório individual no GitHub. Preserve o código
inicial, inclua a versão refatorada, os diagramas, o README, a revisão e os
commits da evolucao. Envie o link pelo Moodle e adicione
`marcelobezerra-dotcom` como colaborador ou mantenha o repositório público.

Todos os integrantes devem participar da apresentação para a turma. A equipe
deverá demonstrar o funcionamento, explicar a arquitetura, apresentar os
diagramas e defender suas decisoes.

## 12. Codigo de referencia para criar a pasta

Os arquivos abaixo sao a referencia completa da solucao. Crie cada arquivo no
caminho indicado. A pasta `src/solidexercicio10` não precisa existir antes do
inicio: ela sera criada durante as etapas do tutorial.

### Etapa 1: `model`

#### `Posicionavel.java`, `Movel.java` e `Dificuldade.java`

```java
package solidexercicio10.model;

public interface Posicionavel { int getX(); int getY(); }
```

```java
package solidexercicio10.model;

public interface Movel { void mover(int dx, int dy); }
```

```java
package solidexercicio10.model;

public enum Dificuldade {
  FACIL, MEDIO, DIFICIL;
  public static Dificuldade deString(String valor) {
    if (valor == null) return MEDIO;
    return switch (valor.trim().toLowerCase()) {
      case "facil" -> FACIL;
      case "dificil" -> DIFICIL;
      default -> MEDIO;
    };
  }
}
```

#### `EntidadeMapa.java` e `Passageiro.java`

```java
package solidexercicio10.model;

public abstract class EntidadeMapa implements Posicionavel {
  protected int x;
  protected int y;
  protected EntidadeMapa(int x, int y) { this.x = x; this.y = y; }
  public int getX() { return x; }
  public int getY() { return y; }
  public abstract String getSimbolo();
}
```

```java
package solidexercicio10.model;

public abstract class Passageiro extends EntidadeMapa {
  private final String nome;
  private final String tipo;
  protected Passageiro(String nome, String tipo, int x, int y) {
    super(x, y); this.nome = nome; this.tipo = tipo;
  }
  public String getNome() { return nome; }
  public String getTipo() { return tipo; }
  public abstract int getPontuacao();
}
```

#### Passageiros e perigos

```java
package solidexercicio10.model;
public class Professor extends Passageiro {
  public Professor(String n, int x, int y) { super(n, "Professor", x, y); }
  public int getPontuacao() { return 15; }
  public String getSimbolo() { return "P"; }
}
```

```java
package solidexercicio10.model;
public class Engenheiro extends Passageiro {
  public Engenheiro(String n, int x, int y) { super(n, "Engenheiro", x, y); }
  public int getPontuacao() { return 20; }
  public String getSimbolo() { return "E"; }
}
```

```java
package solidexercicio10.model;
public class Astronauta extends Passageiro {
  public Astronauta(String n, int x, int y) { super(n, "Astronauta", x, y); }
  public int getPontuacao() { return 10; }
  public String getSimbolo() { return "T"; }
}
```

```java
package solidexercicio10.model;
public class Asteroide extends EntidadeMapa {
  public Asteroide(int x, int y) { super(x, y); }
  public String getSimbolo() { return "#"; }
}
```

```java
package solidexercicio10.model;
public class Inimigo extends EntidadeMapa implements Movel {
  public Inimigo(int x, int y) { super(x, y); }
  public void mover(int dx, int dy) { x += dx; y += dy; }
  public String getSimbolo() { return "X"; }
}
```

#### `Nave.java`

```java
package solidexercicio10.model;
import java.util.ArrayList;
import java.util.List;

public class Nave extends EntidadeMapa implements Movel {
  private final String nome;
  private final List<Passageiro> passageiros = new ArrayList<>();
  private final int capacidade;
  private int vidas = 3;
  public Nave(String nome, int x, int y, int capacidade) {
    super(x, y); this.nome = nome; this.capacidade = capacidade;
  }
  public String getNome() { return nome; }
  public int getVidas() { return vidas; }
  public int getCapacidade() { return capacidade; }
  public List<Passageiro> getPassageiros() { return passageiros; }
  public void embarcar(Passageiro p) { if (passageiros.size() < capacidade) passageiros.add(p); }
  public void perderVida() { vidas = Math.max(0, vidas - 1); }
  public void mover(int dx, int dy) { x += dx; y += dy; }
  public String getSimbolo() { return "@"; }
  public void moverComLimites(char c, int minX, int maxX, int minY, int maxY) {
    int dx = 0, dy = 0;
    switch (c) { case 'w' -> dy = 1; case 's' -> dy = -1; case 'a' -> dx = -1; case 'd' -> dx = 1; default -> { } }
    int novoX = x + dx, novoY = y + dy;
    if (novoX >= minX && novoX <= maxX && novoY >= minY && novoY <= maxY) { x = novoX; y = novoY; }
  }
}
```

#### `Missao.java`

```java
package solidexercicio10.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Missao {
  private final Nave nave;
  private final List<Passageiro> passageiros = new ArrayList<>();
  private final List<Asteroide> asteroides = new ArrayList<>();
  private final List<Inimigo> inimigos = new ArrayList<>();
  public Missao(Nave nave) { this.nave = nave; }
  public Nave getNave() { return nave; }
  public List<Passageiro> getPassageiros() { return passageiros; }
  public List<Asteroide> getAsteroides() { return asteroides; }
  public List<Inimigo> getInimigos() { return inimigos; }
  public void adicionarPassageiro(Passageiro p) { passageiros.add(p); }
  public void adicionarAsteroide(Asteroide a) { asteroides.add(a); }
  public void adicionarInimigo(Inimigo i) { inimigos.add(i); }
  public Passageiro passagemNaPosicao() {
    for (Passageiro p : passageiros) if (mesmaPosicao(p, nave)) return p;
    return null;
  }
  public boolean embarcarPassageiroNaPosicao() {
    Passageiro p = passagemNaPosicao();
    if (p == null || nave.getPassageiros().size() >= nave.getCapacidade()) return false;
    nave.embarcar(p); passageiros.remove(p); return true;
  }
  public void moverInimigos(Random r, int minX, int maxX, int minY, int maxY) {
    for (Inimigo i : inimigos) {
      int dx = r.nextInt(3) - 1, dy = r.nextInt(3) - 1;
      if (i.getX() + dx >= minX && i.getX() + dx <= maxX && i.getY() + dy >= minY && i.getY() + dy <= maxY) i.mover(dx, dy);
    }
  }
  public boolean verificaColisao() {
    for (Asteroide a : asteroides) if (mesmaPosicao(a, nave)) return true;
    for (Inimigo i : inimigos) if (mesmaPosicao(i, nave)) return true;
    return false;
  }
  public boolean todosEmbarcados() { return passageiros.isEmpty(); }
  private boolean mesmaPosicao(Posicionavel a, Posicionavel b) { return a.getX() == b.getX() && a.getY() == b.getY(); }
}
```

### Etapa 2: `repository`

Crie os tres arquivos abaixo.

#### `RankingEntry.java`

```java
package solidexercicio10.repository;
import solidexercicio10.model.Dificuldade;
public class RankingEntry {
  public final String name; public final int score; public final Dificuldade dificuldade;
  public final int passageirosColetados; public final String dataHora; public final long tempoJogo;
  public RankingEntry(String name, int score, Dificuldade dificuldade, int passageiros, String data, long tempo) {
    this.name = name; this.score = score; this.dificuldade = dificuldade;
    this.passageirosColetados = passageiros; this.dataHora = data; this.tempoJogo = tempo;
  }
}
```

#### `RankingRepository.java`

```java
package solidexercicio10.repository;
import java.util.List;
import solidexercicio10.model.Dificuldade;
public interface RankingRepository {
  void salvar(String nome, int pontos);
  void salvar(String nome, int pontos, Dificuldade dificuldade, int passageiros, long tempo);
  List<RankingEntry> listar();
  void limpar();
}
```

#### `RankingService.java`

```java
package solidexercicio10.repository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import solidexercicio10.model.Dificuldade;

public class RankingService implements RankingRepository {
  private final Path arquivo;
  public RankingService(String nome) { arquivo = Paths.get(nome); }
  public void salvar(String nome, int pontos) { salvar(nome, pontos, Dificuldade.MEDIO, 0, 0); }
  public void salvar(String nome, int pontos, Dificuldade dificuldade, int passageiros, long tempo) {
    List<String> linhas = new ArrayList<>();
    if (Files.exists(arquivo)) try { linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8); } catch (IOException ignored) { }
    String data = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    linhas.add(nome + "|" + pontos + "|" + dificuldade + "|" + passageiros + "|" + data + "|" + tempo);
    try {
      Path parent = arquivo.getParent();
      if (parent != null) Files.createDirectories(parent);
      Files.write(arquivo, linhas, StandardCharsets.UTF_8);
    } catch (IOException e) { throw new IllegalStateException("Falha ao salvar ranking", e); }
  }
  public List<RankingEntry> listar() {
    List<RankingEntry> ranking = new ArrayList<>();
    if (!Files.exists(arquivo)) return ranking;
    try {
      for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
        String[] p = linha.split("\\|", -1);
        if (p.length < 6) continue;
        try { ranking.add(new RankingEntry(p[0], Integer.parseInt(p[1]), Dificuldade.deString(p[2]), Integer.parseInt(p[3]), p[4], Long.parseLong(p[5]))); }
        catch (NumberFormatException ignored) { }
      }
    } catch (IOException ignored) { return ranking; }
    ranking.sort(Comparator.comparingInt((RankingEntry e) -> e.score).reversed());
    return ranking;
  }
  public void limpar() { try { Files.deleteIfExists(arquivo); } catch (IOException e) { throw new IllegalStateException(e); } }
}
```

### Etapa 3: `presentation`

Crie `MapaRenderer.java`:

```java
package solidexercicio10.presentation;
import solidexercicio10.model.Missao;
public class MapaRenderer {
  public void desenhar(Missao m, int pontos, String piloto, int minX, int maxX, int minY, int maxY) {
    System.out.printf("%nMapa | Pontos: %d | Piloto: %s%n", pontos, piloto);
    for (int y = maxY; y >= minY; y--) {
      System.out.printf("%3d|", y);
      for (int x = minX; x <= maxX; x++) System.out.printf(" %2c", simbolo(m, x, y));
      System.out.println();
    }
    System.out.println("Legenda: @=Nave, L=Plataforma, P=Professor, E=Engenheiro, T=Astronauta, #=Asteroide, X=Inimigo, .=Vazio");
    System.out.println("Comandos: w/s/a/d (mover), c (embarcar), q (sair)");
  }
  private char simbolo(Missao m, int x, int y) {
    if (m.getNave().getX() == x && m.getNave().getY() == y) return '@';
    if (x == 0 && y == 0) return 'L';
    for (var p : m.getPassageiros()) if (p.getX() == x && p.getY() == y) return p.getSimbolo().charAt(0);
    for (var a : m.getAsteroides()) if (a.getX() == x && a.getY() == y) return '#';
    for (var i : m.getInimigos()) if (i.getX() == x && i.getY() == y) return 'X';
    return '.';
  }
}
```

### Etapa 4: `service`

Crie `JogoService.java`. Este arquivo coordena o menu, a criacao aleatoria da
missao, o embarque, a contagem, a apresentacao e o ranking. Para manter o
tutorial legivel, a implementacao completa esta no bloco a seguir.

```java
package solidexercicio10.service;
import java.util.*;
import solidexercicio10.model.*;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.repository.*;

public class JogoService {
  private final RankingRepository ranking; private final MapaRenderer renderer = new MapaRenderer(); private final Random random = new Random();
  public JogoService(RankingRepository ranking) { this.ranking = ranking; }
  public void executarLoop(Scanner s) {
    boolean ativo = true;
    while (ativo) {
      System.out.println("\n1. Nova missao\n2. Ranking\n3. Limpar ranking\n4. Sair");
      String op = ler(s, "Opcao: ", "4");
      switch (op) {
        case "1" -> jogar(s); case "2" -> exibirRanking();
        case "3" -> { ranking.limpar(); System.out.println("Ranking limpo."); }
        case "4" -> ativo = false; default -> System.out.println("Opcao invalida.");
      }
    }
  }
  private void jogar(Scanner s) {
    String nome = ler(s, "Piloto: ", "Piloto");
    Dificuldade dificuldade = Dificuldade.deString(ler(s, "Dificuldade: ", "medio"));
    int tamanho;
    try { tamanho = Integer.parseInt(ler(s, "Tamanho: ", "5")); } catch (NumberFormatException e) { tamanho = 5; }
    tamanho = Math.max(1, tamanho);
    int min = -tamanho, max = tamanho, total = dificuldade == Dificuldade.FACIL ? 4 : 5;
    if (dificuldade == Dificuldade.DIFICIL) total = 6;
    Nave nave = new Nave("A-1", 0, 0, total); Missao missao = new Missao(nave);
    colocar(missao, total, min, max, true, nave); colocar(missao, dificuldade == Dificuldade.DIFICIL ? 3 : 2, min, max, false, nave);
    colocarPerigos(missao, dificuldade == Dificuldade.DIFICIL ? 3 : 2, min, max, nave);
    int pontos = dificuldade == Dificuldade.FACIL ? 30 : dificuldade == Dificuldade.DIFICIL ? 15 : 20;
    int movimentos = 0; long inicio = System.currentTimeMillis(); ler(s, "Enter para iniciar", "");
    while (true) {
      renderer.desenhar(missao, pontos, nome, min, max, min, max);
      System.out.printf("A bordo: %d/%d | Restantes: %d | Total: %d%n", nave.getPassageiros().size(), nave.getCapacidade(), missao.getPassageiros().size(), nave.getPassageiros().size() + missao.getPassageiros().size());
      char c = ler(s, "Comando: ", "q").toLowerCase().charAt(0);
      if (c == 'q') return;
      if (c == 'c') { Passageiro p = missao.passagemNaPosicao(); if (p != null && missao.embarcarPassageiroNaPosicao()) pontos += p.getPontuacao(); }
      else if ("wsad".indexOf(c) >= 0) { nave.moverComLimites(c, min, max, min, max); pontos--; movimentos++; }
      missao.moverInimigos(random, min, max, min, max);
      if (missao.verificaColisao()) nave.perderVida();
      if (pontos <= 0 || nave.getVidas() == 0) return;
      if (missao.todosEmbarcados() && nave.getX() == 0 && nave.getY() == 0) {
        long tempo = (System.currentTimeMillis() - inicio) / 1000;
        System.out.printf("Missao concluida. Pontos: %d, movimentos: %d, tempo: %ds%n", pontos, movimentos, tempo);
        ranking.salvar(nome, pontos, dificuldade, nave.getPassageiros().size(), tempo); return;
      }
    }
  }
  private void colocar(Missao m, int quantidade, int min, int max, boolean passageiro, Nave n) {
    for (int i = 0; i < quantidade; i++) { int[] p = livre(m, min, max, n); if (passageiro) { if (i % 3 == 0) m.adicionarPassageiro(new Professor("Professor", p[0], p[1])); else if (i % 3 == 1) m.adicionarPassageiro(new Engenheiro("Engenheiro", p[0], p[1])); else m.adicionarPassageiro(new Astronauta("Astronauta", p[0], p[1])); } }
  }
  private void colocarPerigos(Missao m, int quantidade, int min, int max, Nave n) { for (int i = 0; i < quantidade; i++) { int[] p = livre(m, min, max, n); m.adicionarInimigo(new Inimigo(p[0], p[1])); } }
  private int[] livre(Missao m, int min, int max, Nave n) { int x, y; do { x = random.nextInt(max - min + 1) + min; y = random.nextInt(max - min + 1) + min; } while ((x == 0 && y == 0) || ocupado(m, x, y)); return new int[] { x, y }; }
  private boolean ocupado(Missao m, int x, int y) { for (Passageiro p : m.getPassageiros()) if (p.getX() == x && p.getY() == y) return true; for (Inimigo i : m.getInimigos()) if (i.getX() == x && i.getY() == y) return true; return false; }
  private void exibirRanking() { for (RankingEntry e : ranking.listar()) System.out.printf("%s: %d%n", e.name, e.score); }
  private String ler(Scanner s, String mensagem, String padrao) { System.out.print(mensagem); if (!s.hasNextLine()) return padrao; String valor = s.nextLine(); return valor.isBlank() ? padrao : valor; }
}
```

### Etapa 5: `Main`

```java
package solidexercicio10;
import java.util.Scanner;
import solidexercicio10.repository.RankingRepository;
import solidexercicio10.repository.RankingService;
import solidexercicio10.service.JogoService;

public class Main {
  public static void main(String[] args) {
    RankingRepository repository = new RankingService("ranking-solid-exercicio10.json");
    JogoService jogo = new JogoService(repository);
    try (Scanner scanner = new Scanner(System.in)) { jogo.executarLoop(scanner); }
  }
}
```

Depois de criar todos os arquivos, execute a validacao final da secao 7.
