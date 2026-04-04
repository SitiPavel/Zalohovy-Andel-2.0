package cz.sitina.bakalarskaprace.zalohovyandel.services.encryptionServices;

import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.FilesUtility;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SecureArchiveExtractor implements AutoCloseable {

    // pro obnovu
    private  ZipInputStream zipIn;
    private  InputStream fileIn;

    /**
     * Konstruktor sestaví datové potrubí pro zálohu: Disk <- Šifrování <- (Volitelně) Komprese
     */
    public SecureArchiveExtractor(Path archivePath, SecretKey secretKey) throws Exception {
        this.fileIn = Files.newInputStream(archivePath);
        InputStream currentStream = this.fileIn;

        String pathStr = archivePath.toString().toLowerCase();

        if (pathStr.endsWith(".enc")) {
            byte[] iv = new byte[16];
            int bytesRead = currentStream.read(iv);
            if (bytesRead != 16) {
                throw new IOException("Poškozený zašifrovaný soubor (chybí IV).");
            }

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            currentStream = new CipherInputStream(currentStream, cipher);
        }

        this.zipIn = new ZipInputStream(currentStream);
    }

    // Metoda pro čtení hlaviček souborů v balíčku
    public ZipEntry getNextEntry() throws IOException {
        return zipIn.getNextEntry();
    }

    // Metoda pro fyzické vytažení dat a zápis na disk


    public long extractCurrentEntry(Path targetAbsolutPath, long globalProcessedBytes, long totalBytes,
                                    String relativePath, FilesUtility.ProgressCallback callback,
                                    Supplier<Boolean> isCancelled, int processedFileCount) throws IOException {
        long localProcessed = 0;
        byte[] buffer = new byte[4 * 1024 * 1024];
        int read;

        try (OutputStream out = Files.newOutputStream(targetAbsolutPath)){
            while ((read = zipIn.read(buffer)) != -1) {
                if (isCancelled.get()) {
                    throw new IOException("Proces přerušen uživatelem.");
                }

                out.write(buffer, 0, read);
                localProcessed += read;

                if (callback != null) {
                    callback.onProgress(globalProcessedBytes + localProcessed, totalBytes, relativePath, processedFileCount);
                }
            }
        }
        zipIn.closeEntry();
        return localProcessed;
    }


    @Override
    public void close() throws IOException {
        if (zipIn != null) zipIn.close();
        else if (fileIn != null) fileIn.close();
    }
}
