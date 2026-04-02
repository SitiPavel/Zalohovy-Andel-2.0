package cz.sitina.bakalarskaprace.zalohovyandel.models.taskType;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.services.encryptionServices.CryptoManager;

import javax.crypto.SecretKey;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes.BACKUP;

/// objekt sloužící jako Reprezentant šifrované zálohy
/// neobsahuje citlivá data a jen data která se zobrazují v tabulce a ty které jsou nutné k dešifrování
public class EncryptedBackupTask extends TaskModel {
    private boolean isCompressed;
    private boolean isEncrypted = true;
    private String rootFolderPathStr;

    private byte[] cryptoSalt;
    private byte[] cryptoIv;
    private String passwordHash;

    // zašifrovaný objekt task
    private String encryptedPayloadBase64;

    // prázdný pro json
    public EncryptedBackupTask()  {
        super();
    }

    public EncryptedBackupTask( BackupTask plainTask, String userPassword) throws Exception{
        super(plainTask.getTaskType(),
                plainTask.getName(),
                "",
                "",
                plainTask.getDescription(),
                plainTask.getCompareStrategy());


        this.creationDate = plainTask.getCreationDate();
        this.isCompressed = plainTask.isCompressed();
        this.rootFolderPathStr = plainTask.getRootBackupPath();

        // generování soly a inicializačního vektoru
        this.cryptoSalt = CryptoManager.generateRandomBytes(16);
        this.cryptoIv = CryptoManager.generateRandomBytes(16);

        this.passwordHash = CryptoManager.createPasswordHash(userPassword, cryptoSalt);
        SecretKey key =CryptoManager.deriveKey(userPassword.toCharArray(), cryptoSalt);

        ObjectMapper mapper = new ObjectMapper();
        String plainTaskJson = mapper.writeValueAsString(plainTask);
        this.encryptedPayloadBase64 = CryptoManager.encryptText(plainTaskJson, key, this.cryptoIv);

    }

    public boolean isCompressed() {
        return isCompressed;
    }
    public boolean isEncrypted() {
        return isEncrypted;
    }
    public String getRootFolderPathStr() {
        return rootFolderPathStr;
    }
    public byte[] getCryptoSalt() {
        return cryptoSalt;
    }
    public byte[] getCryptoIv() {
        return cryptoIv;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public String getEncryptedPayloadBase64() {
        return encryptedPayloadBase64;
    }


    public void setCompressed(boolean compressed) {
        isCompressed = compressed;
    }
    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }
    public void setRootFolderPathStr(String rootFolderPathStr) {
        this.rootFolderPathStr = rootFolderPathStr;
    }
    public void setCryptoSalt(byte[] cryptoSalt) {
        this.cryptoSalt = cryptoSalt;
    }
    public void setCryptoIv(byte[] cryptoIv) {
        this.cryptoIv = cryptoIv;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public void setEncryptedPayloadBase64(String encryptedPayloadBase64) {
        this.encryptedPayloadBase64 = encryptedPayloadBase64;
    }
}
