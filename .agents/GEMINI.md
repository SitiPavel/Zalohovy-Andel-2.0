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
    - Máž zákas upravovat nebo mazat soubory nebo kód které nesouvisí s právě řešeným problémem.
    - Git můžeš pouze číst (na bezpečné čtení nalinkovaného gitu se ptát nemusíš). ZÁKAZ operací které možou nebo upravují nebo jinak ovlivňují git historii
    - Soubory nebo data která si potřebuješ dočasně vytvořit pro splnění úkolu ukládej do složky .agents/Temp (pokud není, vytvoř si jí) Máš souhlas si v tempu vytvářet soubory které ti pomohou s vyřešením úkolu a taky je mazat (nemusíš se ptát) = Je to tvůj playground pro plnění zadaných úkolů
    - Pro operace čtení pro soubory které josou ve složce ZalohovyAndel (ty které máš nalikované k projektu) můžeš automaticky číst bez optání
    - Na drobné úpravy v CSS se ptát nemusíš 