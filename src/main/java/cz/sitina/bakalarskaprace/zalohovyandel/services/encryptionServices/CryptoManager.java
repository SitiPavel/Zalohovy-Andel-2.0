package cz.sitina.bakalarskaprace.zalohovyandel.services.encryptionServices;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public class CryptoManager {

    // 600 000 iterací pro PBKDF2
    private static final int ITERATIONS = 600000;
    private static final int KEY_LENGTH = 256;
    private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Vygeneruje kryptograficky bezpečnou náhodnou sůl (Salt) nebo IV (Inicializační vektor).
     * Běžná délka soli je 16 bytů.
     */
    public static byte[] generateRandomBytes(int length) {
        byte[] randomBytes = new byte[length];
        // SecureRandom komunikuje přímo s operačním systémem pro získání skutečné entropie
        new SecureRandom().nextBytes(randomBytes);
        return randomBytes;
    }

    /**
     * Odvodí silný 256-bitový AES klíč z uživatelského hesla.
     */
    public static SecretKey deriveKey(char[] password, byte[] salt) throws Exception{
        // PBEKeySpec si vytvoří interní kopii hesla
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);

        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(KDF_ALGORITHM);
            byte[] key = skf.generateSecret(spec).getEncoded();
            return new SecretKeySpec(key, "AES"); // toto je tedy hotový klíč
            
        }catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new Exception("Kritická chyba kryptografie: Nelze odvodit klíč.", e);
        } finally {
            // BEZPEČNOSTNÍ POJISTKA: Vymaže interní kopii hesla uvnitř PBEKeySpec
            spec.clearPassword();
        }

    }

    /**
     * Vytvoří bezpečný jednosměrný otisk hesla pro ověřování.
     */
    public static String createPasswordHash(String userPassword, byte[] salt) throws Exception {

        SecretKey derivedKey = deriveKey(userPassword.toCharArray(), salt);

        // 2. Proženeme ho přes jednosměrný SHA-256 hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(derivedKey.getEncoded());

        // 3. Převedeme do čitelného Base64 textu pro uložení do JSONu
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    public static boolean verifyPassword(String userPassword, byte[] salt, String storedHash) throws Exception {
        String computedHash =  createPasswordHash(userPassword, salt);
        return storedHash.equals(computedHash);
    }


    /**
     * Zašifruje čistý text (např. JSON) a vrátí ho jako Base64 řetězec.
     */
    public static String encryptText(String plainText, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }


    /**
     * Dešifruje Base64 řetězec zpět na čistý text.
     */
    public static String decryptText(String encryptedBase64, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] decodedBytes = Base64.getDecoder().decode(encryptedBase64);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Fyzicky přepíše pole znaků v RAM nulami, aby heslo nešlo přečíst z paměti.
     * Tuto metodu musíš zavolat VŽDY v bloku 'finally' tam, kde heslo získáš.
     */
    public static void clearPassword(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}
