package cz.sitina.bakalarskaprace.zalohovyandel.services.encryptionServices;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class CryptoManager {

    // 600 000 iterací pro PBKDF2
    private static final int ITERATIONS = 100000;
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
     * Fyzicky přepíše pole znaků v RAM nulami, aby heslo nešlo přečíst z paměti.
     * Tuto metodu musíš zavolat VŽDY v bloku 'finally' tam, kde heslo získáš.
     */
    public static void clearPassword(char[] password) {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
    }
}
