package cz.sitina.bakalarskaprace.zalohovyandel.services.utilities;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;

public  class GuiUtility {

    private static IconManagerUtility iconManager = new IconManagerUtility();

    public static Image findIconForStrategy(CompareStrategy strategy) {
        return  switch (strategy) {
            case BIDIRECTIONAL_SYNC -> iconManager.getBiDirectionalSync();
            case BACKUP_TO_RIGHT -> iconManager.getOneWayLeftToRight();
            case BACKUP_TO_LEFT -> iconManager.getOneWayRightToLeft();
            case MIRRORING_to_RIGHT -> iconManager.getMirroringLeftToRight();
            case MIRRORING_TO_lEFT -> iconManager.getMirroringRightToLeft();
        };
    }

    public static Image getIconProcessStatus(FinishProcesStatus status) {
        return switch (status) {
            case SUCCESS -> iconManager.getCheckMark();
            case FAILURE -> iconManager.getError();
            case INTERRUPTED -> iconManager.getCros();
        };
    }

    // omezení a pořítání znaků v polích
    public static void setupCharacterLimit(TextInputControl inputField, Label countLabel, int maxChars) {

        // 1. Zabezpečení vstupu (TextFormatter)
        // Zabrání vložení textu, pokud by nová délka přesáhla maximum. Mazání vždy projde.
        inputField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= maxChars) {
                return change;
            }
            return null; // Odmítnutí změny
        }));

        // 2. Vizuální odezva a počítadlo (Listener)
        inputField.textProperty().addListener((observable, oldValue, newValue) -> {
            int currentLength = newValue.length();

            // Aktualizace textu v Labelu
            countLabel.setText(currentLength + " / " + maxChars);

            // Změna barvy rámečku
            if (currentLength >= maxChars) {
                // Přidáme červený rámeček přímo přes styl
                inputField.setStyle("-fx-border-color: -fx-color-mark2; -fx-border-width: 2px;");
                countLabel.setStyle("-fx-text-fill: -fx-color-mark2;"); // Volitelně zčervená i samotné počítadlo
            } else {
                // Vrátíme do původního stavu
                inputField.setStyle("");
                countLabel.setStyle("");
            }
        });

        // 3. Počáteční nastavení při spuštění okna (aby na začátku nebyl Label prázdný)
        countLabel.setText(inputField.getText().length() + " / " + maxChars);
    }



}
