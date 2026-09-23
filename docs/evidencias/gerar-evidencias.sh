#!/usr/bin/env bash
# Gera as evidências de execução em docs/evidencias/ usando o programa real.
# Uso (na raiz do repositório):  bash docs/evidencias/gerar-evidencias.sh
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/../.." && pwd)"
EVID="$RAIZ/docs/evidencias"
OUT="$(mktemp -d)"
TRAB="$(mktemp -d)"            # pasta onde os arquivos de ranking serão criados
JAVA="java -Dstdout.encoding=UTF-8 -Dfile.encoding=UTF-8 -Duser.timezone=America/Fortaleza"

cd "$RAIZ"
javac -encoding UTF-8 -d "$OUT" src/exercicio10/*.java
javac -encoding UTF-8 -d "$OUT" $(find src/solidexercicio10 test/solidexercicio10 -name '*.java')
cd "$TRAB"

cabecalho() { printf '### Comando: %s\n### Entrada digitada (uma por linha): %s\n\n' "$1" "$2"; }

# 1) Jogo original: menu, partida curta, ranking e reset --------------------
ENTRADA='1|Igor|facil|3||d|c|q|2|3|s|4'
{ cabecalho "java -cp out exercicio10.Main" "$ENTRADA"
  echo "$ENTRADA" | tr '|' '\n' | $JAVA -cp "$OUT" exercicio10.Main; } > "$EVID/01-original-fluxo-basico.txt" 2>&1

# 2) Jogo original: mapa pequeno trava (laço infinito) -----------------------
ENTRADA='1|Igor|medio|1||q|4'
{ cabecalho "timeout 5 java -cp out exercicio10.Main" "$ENTRADA"
  set +e
  echo "$ENTRADA" | tr '|' '\n' | timeout 5 $JAVA -cp "$OUT" exercicio10.Main > /dev/null 2>&1
  echo "Código de saída: $? (124 = processo encerrado pelo timeout após 5 s: o jogo travou gerando a missão)"
  set -e; } > "$EVID/02-original-mapa-pequeno-trava.txt"

# 3) Refatorado: partida completa até a vitória (semente fixa) --------------
rm -f ranking-solid-exercicio10.json
TECLAS=$($JAVA -cp "$OUT" solidexercicio10.PilotoAutomatico 5 medio 3 | paste -sd '|')
ENTRADA="1|Igor|medio|3||${TECLAS}|2|4"
{ cabecalho "java -cp out solidexercicio10.Main --seed 5" "$ENTRADA"
  echo "$ENTRADA" | tr '|' '\n' | $JAVA -cp "$OUT" solidexercicio10.Main --seed 5; } > "$EVID/03-refatorado-vitoria.txt" 2>&1
cp ranking-solid-exercicio10.json "$EVID/ranking-solid-exercicio10.exemplo.json"

# 4) Refatorado: ranking persistido entre execuções, reset cancelado e confirmado
ENTRADA='2|3|n|2|3|s|2|3|s|4'
{ cabecalho "java -cp out solidexercicio10.Main (nova execução, mesmo arquivo)" "$ENTRADA"
  echo "$ENTRADA" | tr '|' '\n' | $JAVA -cp "$OUT" solidexercicio10.Main; } > "$EVID/04-refatorado-ranking-e-reset.txt" 2>&1

# 5) Refatorado: entradas inválidas e mapa pequeno (não trava) ---------------
ENTRADA='1||xyz|1||q|1|Ana|dificil|abc||x|q|7|4'
{ cabecalho "java -cp out solidexercicio10.Main --seed 3" "$ENTRADA"
  echo "$ENTRADA" | tr '|' '\n' | $JAVA -cp "$OUT" solidexercicio10.Main --seed 3; } > "$EVID/05-refatorado-entradas-invalidas.txt" 2>&1

# 6) Refatorado: derrota (colisões ou combustível) ---------------------------
ENTRADA="1|Bia|dificil|2||$(printf 'a|s|d|w|%.0s' {1..6})4"
{ cabecalho "java -cp out solidexercicio10.Main --seed 1" "$ENTRADA"
  echo "$ENTRADA" | tr '|' '\n' | $JAVA -cp "$OUT" solidexercicio10.Main --seed 1; } > "$EVID/06-refatorado-derrota.txt" 2>&1

# 7) Refatorado: fim da entrada (Ctrl+D) no meio da partida ------------------
ENTRADA='1|Igor|facil|3||d'
{ cabecalho "java -cp out solidexercicio10.Main --seed 2 (entrada termina após 'd')" "$ENTRADA"
  echo "$ENTRADA" | tr '|' '\n' | $JAVA -cp "$OUT" solidexercicio10.Main --seed 2
  echo "Código de saída: $?"; } > "$EVID/07-refatorado-fim-da-entrada.txt" 2>&1

# 8) Testes automatizados ----------------------------------------------------
{ echo "### Comando: java -cp out solidexercicio10.ExecutarTestes"; echo
  $JAVA -cp "$OUT" solidexercicio10.ExecutarTestes; } > "$EVID/08-testes-automatizados.txt" 2>&1

rm -rf "$OUT" "$TRAB"
echo "Evidências geradas em $EVID"
