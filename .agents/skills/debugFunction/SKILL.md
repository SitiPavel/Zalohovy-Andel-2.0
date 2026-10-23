---
name: debugFunction
description: Analyzuje chybnou metodu, pochopí záměr opravy, navrhne řešení a odprezentuje kód s vysvětlením. Zabraňuje bezhlavému přepisování. Spouští se příkazem /oprav.
---
# Method Fixer

Tento skill slouží k cílené opravě nebo úpravě konkrétní metody, která obsahuje chybu nebo nesplňuje původní představu uživatele. Klade extrémní důraz na pochopení kontextu, analytický přístup a obhajobu řešení před samotným vypsáním kódu.

## Kdy použít
- Uživatel vloží kód metody a popíše, co na ní nefunguje, padá, nebo co je potřeba změnit.

## Postup při volání příkazu (Kritické Workflow)
Při použití tohoto skillu MUSÍŠ dodržet tento přesný myšlenkový a pracovní postup:

1. **Analýza stávajícího kódu:** Přečti si související kód a interně zanalyzuj, co metoda aktuálně dělá, co vrací a jaké volá závislosti.
2. **Pochopení myšlenky:** Zanalyzuj instrukce od uživatele. Pochop, v čem spočívá cíl opravy.
3. **Ochrana před domýšlením (Zero Hallucination):** Pokud k pochopení nebo bezpečné opravě chybí kontext (např. definice cizího objektu, na kterém se volá metoda, nebo struktura DB), **ZASTAV SE** a vyžádej si chybějící kód. Nevymýšlej si ho.
4. **Odprezentování návrhu (Vysvětlení):** Než ukážeš kód, analyticky uživateli vysvětli:
    - Kde přesně byla chyba / proč původní řešení nebylo ideální.
    - Jakou myšlenku nebo vzor (pattern) použiješ k opravě a proč (např. optimalizace, bezpečnost vlákna, čistší syntaxe).
5. **Prezentace opraveného kódu:** Až po vysvětlení vypiš opravenou metodu.


## Závazný formát výstupu
- **Struktura odpovědi:** 1. Zhodnocení problému -> 2. Návrh a obhajoba řešení -> 3. Zdrojový kód.
- **Označování změn:** Každou úpravu nebo nový blok v kódu označ komentářem: `// OPRAVENO: [krátké vysvětlení proč]`.
- **Ochrana tokenů:** Vypisuj pouze upravovanou metodu, zbytek třídy nahraď zástupným komentářem `// ... zbytek třídy ...`.
- Odpovídej věcně jako technický kouč. Vyhni se chválení a zbytečným konverzačním frázím.