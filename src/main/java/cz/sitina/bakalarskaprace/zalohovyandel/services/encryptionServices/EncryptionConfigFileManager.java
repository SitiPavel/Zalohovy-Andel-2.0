package cz.sitina.bakalarskaprace.zalohovyandel.services.encryptionServices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.EncryptedBackupTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EncryptionConfigFileManager {

    private static final String CONFIG_FILE_NAME = "taskConfig.json";
    private final ObjectMapper objectMapper;

    public EncryptionConfigFileManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void makeConfigUpToDate(EncryptedBackupTask encryptedBackupTask, Path rootPackageFilePath) {
        if (rootPackageFilePath == null || !Files.exists(rootPackageFilePath)) {
            System.err.println("Root složka neexistuje, nelze vytvořit config.");
            return;
        }
        if (encryptedBackupTask == null) {
            System.err.println("Zašifrovaná úloha neexistuje, encryptedBackupTask = null.");
            return;
        }

        Path configAbsolutPath = rootPackageFilePath.resolve(CONFIG_FILE_NAME);
        try {
            // Pokud soubor existuje a je skrytý (např. z předchozích verzí), zrušíme skrytí pro bezproblémový přepis ve Windows
            if (Files.exists(configAbsolutPath)) {
                try {
                    Files.setAttribute(configAbsolutPath, "dos:hidden", false);
                } catch (Exception ignored) {
                    // Ignorujeme na systémech, které nepodporují dos:hidden (např. Linux)
                }

                if (isConfigUpToDate(configAbsolutPath, encryptedBackupTask)) {
                    return;
                }
            }

            writeConfig(configAbsolutPath, encryptedBackupTask);

        } catch (IOException e ) {
            System.err.println("I/O Chyba při práci se soubory: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Kritická chyba aplikace: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // zjistíme zda je soubor aktuální
    private boolean isConfigUpToDate(Path configAbsolutPath, EncryptedBackupTask currentTask) throws Exception {
        try {
            EncryptedBackupTask loadedTask = objectMapper.readValue(configAbsolutPath.toString(), EncryptedBackupTask.class);
            if (loadedTask.getEncryptedPayloadBase64() != null &&
                    currentTask.getEncryptedPayloadBase64() != null) {
                return loadedTask.getEncryptedPayloadBase64().equals(currentTask.getEncryptedPayloadBase64());
            }
            return false;
        } catch (JsonProcessingException e) { // pokud je json rozbitý tak ho přepíšeme
            return false;
        }
    }

    private void writeConfig(Path configAbsolutPath, EncryptedBackupTask task) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(configAbsolutPath.toFile(), task);
    }
}
