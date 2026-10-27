---
name: getContext
description: Vytvoří expertní technické shrnutí aktuálního chatu pro přenos do nového vlákna. Spouští se příkazem /getKontext.
---
# Get Kontext

Tento skill analyzuje historii aktuálního chatu a vygeneruje komprimovaný, vysoce technický souhrn. Slouží k tomu, aby uživatel mohl tento výstup zkopírovat, vložit do nového chatu a druhý agent mohl okamžitě navázat na rozdělanou práci bez ztráty kontextu.

## Kdy použít
- Uživatel chce oddělit úkoly v rámci stejného projektu do různých vláken (např. backend logika v jednom, UI v druhém).

## Pravidla analýzy a chování (Kritické)
1. **Žádná redundance (Kritické):** Ve výstupu **NESMÍŠ** opakovat globální informace o projektu (stack Java 21, SQLite, architektura složek, pravidla pro JavaFX, Git, zákazy PowerShellu). Nový chat tyto informace má.
2. **Přesnost souborů:** Přesně identifikuj soubory (ideálně i s cestou), které se v tomto chatu reálně měnily, vytvořily nebo četly a jsou pro daný problém relevantní.
3. **Záznam architektonických rozhodnutí:** Pokud se v chatu rozhodlo o konkrétním přístupu (např. "použijeme HashMap místo stromu kvůli výkonu", "asynchronní běh přes Task"), musí to v kontextu zaznít, aby to nový agent nezměnil.
4. **Stručnost:** Výstup musí být hutný, bodový, bez zbytečné konverzační omáčky. Je to předávací protokol (handover) od programátora k programátorovi.

## Postup generování výstupu
Zanalyzuj celou konverzaci a vygeneruj odpověď přesně v této struktuře (ideálně do Markdown bloku kódu nebo pomocí nadpisů, aby si to uživatel mohl snadno zkopírovat):

### Závazná struktura předávaného kontextu:
1. **Řešený problém:** 1-2 věty o tom, co byl hlavní úkol tohoto vlákna (např. "Optimalizace načítání složek pro UI").
2. **Klíčové soubory:** Seznam souborů (tříd/FXML), na kterých se pracovalo, s krátkou poznámkou co se v nich změnilo.
3. **Zásadní technická rozhodnutí:** Konkrétní návrhové vzory nebo implementační detaily, na kterých jsme se dohodli (např. použití `Files.walkFileTree`, oddělení logiky z `TaskOverviewController`).
4. **Aktuální stav:** Co je hotové a funkční.
5. **Cíl pro nový chat:** Prázdné místo nebo placeholder `[DOPLNIT UŽIVATELEM]`, kam uživatel před odesláním do nového chatu dopíše, na co přesně se má nový agent zaměřit (např. "Teď uděláme UI komponentu").

## Závazný formát výstupu
- Odpověz přímo vygenerovaným kontextem připraveným ke zkopírování.
- Nepřidávej žádné poučky. Buď čistě analytický a věcný.