# Projekt Zálohový Anděl
- **Účel**: Desktopová JavaFX aplikace pro zálohování, šifrování a synchronizaci souborů. Zobrazuje přehled změn mezi dvěma adresáři
- **Popis**: Aplikace má 2 typy uloh Zálohové a Sinchonizační. Synchronizace je možná oboěma směry
a rovnou mění soubory. Záloha je jednosměrná zdroj > cíl, neumožňuje mazání v cíli Záloha může
být šifrováná a zaZIPovaná. Každou novou zálohu v rámci jedné úlohy alikace ukládá do skupin = 
ukládá historii souborů. Pro ůlohuje možné nastavit blokované soubory, zcela vynechané složky, 
metody porovnání.  
- **Stack**: Java 21, JavaFX, Maven, jackson, SQLlite.
- **Architektura**:
    - `../src/main/java/cz/sitina/bakalarskaprace/zalohovyandel` - Backend: Java třídy
      - `controllers/` – JavaFX kontrolery oken
      - `services/models/` - Objekty držící data
        - `datPacks` - Pomocné objekty pro pole, listy...
        - `taskType` - Objekty úloh 
      - `services` – servisní třídy, v kořenu této cesty jsou netématické třídy 
        - `utilitis` - menší obecné pomocné třídy
        - `ComparisonStrategies` - porovnávací strategie
        - `DataTransferService` - pro přenos souborů
        - `encryptionServices` - zajišťuje šifrování a práce s archyvy
        - `TableServices` - Dává dokupy data pro TreeTableView
    - `../src/main/resources/cz/sitina/bakalarskaprace/zalohovyandel` - Frontend: fxml, css, images
      - `css` - styly 
      - `fxml` - vzhledové komponenty a okna
        - `components` - malé komponenty určené pro použití v jiných komponentách
        - `popup` - Vyskakovací okna (kliknutím pryvého tlačítka, akona co nemají křížek)
        - `windows` - okna bvěžící nad hl oknem (nastavení, historie...)
      - `images` - obrázky
- **Pravidla**:
    - Pokud budeš potřebovat více podrobností o popisu aplikace koukni do `AppDescription`
    - Zachovávej existující komentáře.
    - Kód piš v souladu s Java konvencemi.
    - Reaktivní UI (Properties): Pro data svázaná s UI nepoužívej obyčejné typy (String, boolean), ale JavaFX Properties (StringProperty, BooleanProperty). Využívej binding (bind(), bindBidirectional()) místo ručního přepisování hodnot v controllerech.
    - Prevence Memory Leaks (Listenery): JavaFX je náchylné na úniky paměti přes listenery. Pokud přidáváš listener na objekt, který přežije UI komponentu, navrhni použití WeakChangeListener, nebo zajisti jeho explicitní odregistrování.
    - Chybové stavy a UI Alert: Nepolykej výjimky prázdným blokem catch. I/O a DB chyby musí probublat do vrstvy, která chybu zaloguje a pomocí Platform.runLater() zobrazí uživateli srozumitelný JavaFX Alert (využívej existující ErrorUtility, pokud je k dispozici).
    - PopupDialogService a WindowManager.java se starají o otevírání oken nebo popap oken (bez křížku)
    - třída Utility obsahuje velmi obecné metody jako převod bitů ná čitelné velikosti, nastavení tooltipů atd...
    - Při změně názvu souboru musíš zkontrolovat veškeré možné závislosti. Všechny soubory na které obsahují odkaz na tento soubor musí být taky změněny.
    - ZÁKAZ používání PowerShellu pro běžné operace se soubory:
      - Pro čtení, prohlížení kódu, počítání řádků a vyhledávání textu VŽDY používej nativní nástroje (read_file, edit_file), NIKDY nespouštěj PowerShell skripty (Get-Content, [System.IO.File], Select-String, Get-ChildItem).
      - PowerShell (run_command) používej POUZE v nevyhnutelných případech: Maven build (`mvn clean compile`, `mvn test`), bezpečné Git příkazy a spuštění aplikace.
    - Pracovní a dočasná složka (.agents/Temp/):
      - Složka `.agents/Temp/` je tvůj výhradní pracovní prostor (scratchpad / playground).
      - Máš plné oprávnění si zde kdykoliv BEZ PTÁNÍ vytvářet, upravovat a mazat jakékoliv dočasné soubory (např. testovací skripty, mezivýsledky analýz, dočasné texty).
      - Kód a soubory z této složky nikdy nepovažuj za součást produkčního kódu aplikace.
      - Po dokončení komplexního úkolu dočasné soubory v `.agents/Temp/` smaž, ať nezůstává zbytečný odpad.
- **Čtení projektu a bezpečné příkazy (ZÁKAZ zbytečného ptaní):**
  - Máš plné povolení ČÍST veškeré soubory v projektu (kód, resources, konfigurace, texty) bez jakéhokoliv dotazování uživatele.
  - Máš plné povolení spouštět bez ptaní bezpečné čtecí Git příkazy: `git status`, `git diff`, `git log`, `git branch`, `git show`. Na tyto operace se NIKDY neptej.
  - ZÁKAZ platí POUZE pro destruktivní a zapisovací Git operace: `git commit`, `git push`, `git reset`, `git checkout .`, `git rebase`, `git clean`. Tyto operace nesmíš spustit bez výslovného schválení. 
  -  Pro čtení souborů, zjišťování počtu řádků nebo vyhledávání používej primárně své interní čtecí nástroje (read_file, search), nespouštěj zbytečně externí PowerShell skripty.
 
  