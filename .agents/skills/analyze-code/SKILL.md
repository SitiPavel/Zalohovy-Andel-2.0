---
name: analyze-code
description: Slouží k hluboké analýze kódu, pochopení problému nebo vysvětlení existující logiky. Generuje pouze analytický výstup, neupravuje kód. Spouští se příkazem /analyze.
---
# Code Analyzer

Tento skill instruuje agenta, aby se přepnul do čistě analytického režimu. Cílem není kód rovnou opravovat nebo přepisovat, ale detailně prozkoumat, jak funguje, proč se chová určitým způsobem a jaké části systému do něj zasahují.

## Kdy použít
- Uživatel napíše `/analyze` a popíše problém (bug), který potřebuje prozkoumat.
- Uživatel napíše `/analyze` a požádá o vysvětlení určité části kódu, funkce nebo celého toku dat.

## Postup analýzy (Workflow)
Při spuštění tohoto skillu dodržuj následující kroky:

1. **Studium a mapování:** Projdi si zadaný kód. Pokud uživatel nepředal kód přímo, ale jen popsal problém, použij své čtecí nástroje (read_file, search) a najdi relevantní třídy a metody.
2. **Identifikace záměru:** Rozhodni se podle zadání, zda jde o hledání příčiny chyby (Bug), nebo o studium logiky (Comprehension).
3. **Příprava odpovědi podle typu zadání:**

   **Varianta A: Analýza chyby (Bug / Problém)**
    - Zjisti a popiš, **proč** kód aktuálně funguje tak, jak funguje (kde je logický zádrhel).
    - Identifikuj přesné umístění problému.
    - Vypiš seznam všech metod a tříd, které na toto chybné chování mají vliv (kdo data posílá, kdo je zpracovává).

   **Varianta B: Pochopení kódu (Comprehension)**
    - Lidsky a technicky vysvětli, jak daný úsek kódu funguje (tok dat od vstupu po výstup).
    - Popiš, jaké další metody nebo služby (services/controllers) do této logiky zasahují nebo na ní závisejí.

## Závazný formát výstupu
Odpověď strukturuj přehledně pomocí nadpisů:

- **1. Shrnutí situace:** Krátké (1-2 věty) potvrzení, co přesně analyzuješ.
- **2. Rozbor logiky / Příčina problému:** Detailní, ale věcné vysvětlení toho, jak kód aktuálně teče a proč dělá to, co dělá.
- **3. Zasažené metody a závislosti:** Bodový seznam (třída -> metoda) s krátkým popisem, jakou roli v daném procesu hrají.
- *(Důležité: NENAVRHUJ kód pro opravu, pokud o to uživatel výslovně nepožádá. Cílem je pouze perfektní zmapování terénu.)*