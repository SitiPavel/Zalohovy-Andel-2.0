---
name: function-refactor
description: Přepisuje, rozšiřuje a optimalizuje existující funkce. Striktně se doptává na kontext, neplýtvá tokeny a dodržuje pravidla původní architektury. Spouští se příkazem /prepis.
---
# Function Refactor

Tento skill slouží pro bezpečnou úpravu, rozšíření nebo optimalizaci stávajícího kódu. Zajišťuje, že úpravy budou efektivní, nerozbijí MVC architekturu a nezablokují UI vlákno. Chrání před halucinacemi AI a plýtváním tokenů.

## Kdy použít
- Uživatel použije příkaz `/updateFunction`.
- Uživatel vloží stávající kód s požadavkem na vylepšení, zrychlení, refaktoring nebo přidání dílčí úpravy do již fungující metody.

## Pravidla analýzy a chování (Kritické)
1. **Zákaz domýšlení (Zero Hallucination):** Pokud dodaný kód odkazuje na třídy, proměnné nebo databázová schémata, která nejsou v kontextu a jsou klíčová pro pochopení úpravy, **ZASTAV SE** a vyžádej si je. Nevymýšlej si implementaci neznámých závislostí.
2. **Ochrana tokenů (No Token Wasting):**
    - Vypisuj POUZE ty metody nebo logické bloky, které se reálně mění. Zbytek nahraď komentářem `// EXISTUJE ...`.
    - Neopisuj zbytečně dlouhá zadání zpět uživateli, jdi rovnou k řešení.
3. **Respekt k původní architektuře:** Změny musí zapadat do stávající MVC struktury (Java 21, JavaFX, SQLite). Nezanášej nové knihovny a cizí návrhové vzory, pokud k tomu nejsi výslovně vyzván.
4. **Optimalizace a Vlákna:** Hlídej nechtěné přesunutí náročných operací na JavaFX Application Thread. Pokud přidáváš I/O nebo DB logiku, automaticky ji řeš přes asynchronní zpracování nebo NIO.2.

## Postup při volání příkazu
1. **Validace pochopení:** Máš všechen potřebný kód a je jasný cíl úpravy? Pokud ne -> napiš stručný seznam chybějících informací a **ČEKEJ**.
2. **Identifikace úzkých hrdel:** Pokud je cílem efektivita (zrychlení), velmi stručně (max 1-2 věty) analyticky popiš, v čem byl původní přístup špatný (např. chybějící dávkové zpracování v DB, zbytečné načítání do paměti místo streamu).
3. **Výstup kódu:** Poskytni upravený kód.

## Závazný formát výstupu
- Každou podstatnou změnu označ přímo v kódu komentářem: `// OPRAVENO: [krátké vysvětlení proč je to lepší/správné]`.
- Žádné chválení ani zbytečná "omáčka" okolo.
- Odpovídej jako seniorní analytik a kouč. Pouze fakta, rizika a konkrétní upravený kód.
- Pokud při analýze použiješ domněnku (protože kód neukazuje vše), musíš to na začátku výstupu jasně přiznat.