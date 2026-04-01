package cz.sitina.bakalarskaprace.zalohovyandel.services.encryptionServices;

import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.FilesUtility;

import javax.crypto.Cipher;
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
import java.util.zip.ZipOutputStream;

public class SecureArchiveManager implements AutoCloseable{

    private final OutputStream fileOut;
    private final CipherOutputStream cipherOut;
    private final ZipOutputStream zipOut;
    private final boolean useCompression;


    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * Konstruktor sestaví datové potrubí: Disk <- Šifrování <- (Volitelně) Komprese
     */
    public SecureArchiveManager(Path targetPackagePath, SecretKey secretKey, boolean useCompression) throws Exception {
        this.useCompression = useCompression;

        // 1. Otevřeme surový zápis na disk
        this.fileOut = Files.newOutputStream(targetPackagePath);

        // 2. Vygenerujeme náhodný Inicializační vektor (IV) - nutnost pro AES v CBC módu (16 bytů)
        byte[] iv = CryptoManager.generateRandomBytes(16);

        // Zápis IV v čistém textu na úplný začátek souboru (při dešifrování ho musíme načíst jako první)
        this.fileOut.write(iv);

        // 3. Inicializace šifrovacího enginu
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

        // 4. Obalení diskového streamu do šifrovacího streamu
        this.cipherOut = new CipherOutputStream(this.fileOut, cipher);

        // 5. Obalení do komprese (pokud ji uživatel chce)
        if (this.useCompression) {
            this.zipOut = new ZipOutputStream(this.cipherOut);
            // Zvýšení úrovně komprese (0 = žádná, 9 = maximální)
            this.zipOut.setLevel(6);
        } else {
            this.zipOut = null;
        }
    }

    public void addFile(Path sourceAbsolutPath, String relativePath// pro
                        ) throws IOException {
        OutputStream activeStream;

        if (useCompression) {
            // Řekneme ZIPu: "Teď začínám zapisovat tento konkrétní soubor"
            zipOut.putNextEntry(new ZipEntry(relativePath.replace("\\", "/")));
            activeStream = zipOut;
        } else {
            // Bez komprese zapisujeme rovnou do šifrovače
            activeStream = cipherOut;
        }

        // Kopírování dat ze zdroje do našeho potrubí
        try (InputStream in = Files.newInputStream(sourceAbsolutPath)){
            byte[] buffer = new byte[1024 * 1024 * 4]; //4MB bufferem
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                activeStream.write(buffer, 0, bytesRead);
            }
        }

        if (useCompression) {
            zipOut.closeEntry();
        }
    }


    /**
     * Bezpečné uzavření celého potrubí (Flush dat a uzavření souboru)
     */
    @Override
    public void close() throws Exception {
        if (this.zipOut != null) {
            this.zipOut.close(); // // ZipOutputStream interně zavře i cipherOut a fileOut
        } else if (cipherOut != null) {
            cipherOut.close();
        } else {
            fileOut.close();
        }

    }
}
