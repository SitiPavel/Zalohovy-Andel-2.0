package cz.sitina.bakalarskaprace.zalohovyandel.services.encryptionServices;

import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.FilesUtility;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SecureArchiveManager implements AutoCloseable{

    private final OutputStream fileOut;
    private CipherOutputStream cipherOut = null;
    private final ZipOutputStream zipOut;

    // Tady si uložíme tu "nejvyšší" vrstvu potrubí, do které budeme rovnou sypat data
    private final OutputStream activeTopStream;
    private final boolean useCompression;


    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * Konstruktor sestaví datové potrubí pro zálohu: Disk <- Šifrování <- (Volitelně) Komprese
     */
    public SecureArchiveManager(Path targetPackagePath, SecretKey secretKey, boolean useCompression) throws Exception {
        this.useCompression = useCompression;

        // otevře přímou rouru pro zápis na disk
        this.fileOut = Files.newOutputStream(targetPackagePath);
        OutputStream currentStream = this.fileOut;

        // volitelné: Šifrování
        if (secretKey != null) {
            byte[] iv = CryptoManager.generateRandomBytes(16);
            this.fileOut.write(iv); // IV musí být zapsán do čistého souboru jako první

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

            this.cipherOut = new CipherOutputStream(currentStream, cipher);
            currentStream = this.cipherOut;
        }

        this.zipOut = new ZipOutputStream(currentStream);

        if (this.useCompression) {
            this.zipOut.setLevel(6); // Běžná komprese
        } else {
            this.zipOut.setLevel(0); // Úroveň 0 (jen to zabalí a nekomprimuje)
        }
        currentStream = this.zipOut;

        this.activeTopStream = currentStream;
    }

    public void addFile(Path sourceAbsolutePath, String relativePath,
                        long alreadyProcessedGlobal, long totalBytes,
                        FilesUtility.ProgressCallback callback,
                        Supplier<Boolean> isCancelled, int processedFileCount) throws IOException {

        zipOut.putNextEntry(new ZipEntry(relativePath.replace("\\", "/")));

        long localPosition = 0;

        // Kopírování dat ze zdroje do našeho potrubí
        try (InputStream in = Files.newInputStream(sourceAbsolutePath)){
            byte[] buffer = new byte[1024 * 1024 * 4]; //4MB bufferem
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                if (isCancelled.get()) {
                    throw new IOException("Proces byl přerušen uživatelem.");
                }

                activeTopStream.write(buffer, 0, bytesRead);
                localPosition += bytesRead;

                if (callback != null) {
                    callback.onProgress(alreadyProcessedGlobal + localPosition, totalBytes, relativePath, processedFileCount);
                }
            }
        }

        zipOut.closeEntry();
    }


    /**
     * Bezpečné uzavření celého potrubí (Flush dat a uzavření souboru)
     */
    @Override
    public void close() throws Exception {

        if (this.zipOut != null) {
            this.zipOut.close();
        }

    }
}
