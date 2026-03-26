package cz.sitina.bakalarskaprace.zalohovyandel.services.BackupCompareServices;

import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.sql.*;

public class DatabaseManager implements AutoCloseable {
    private Connection conn;

    // Konstruktor otevře spojení a zaručí existenci tabulky
    public DatabaseManager(String databaseAbsolutPath) throws SQLException {
        String url = "jdbc:sqlite:" + databaseAbsolutPath;
        this.conn = DriverManager.getConnection(url);
        tableInicializazion();
    }

    private void tableInicializazion() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS backup_catalog ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "relativePath TEXT NOT NULL,"
                + "absolutPath TEXT NOT NULL,"
                + "fileSize INTEGER,"
                + "lastModified INTEGER,"
                + "location TEXT NOT NULL"
                + ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public boolean isFileChanged(String currentRelative, long currentSize, long currentModified) throws SQLException {
        String sql = "SELECT fileSize, lastModified "
                + "FROM backup_catalog "
                + "WHERE relativePath = ? "
                + "ORDER BY lastModified DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentRelative);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long dbSize = rs.getLong("fileSize");
                    long dbDate = rs.getLong("lastModified");

                    // Vrací true, pokud se metadata neshodují
                    return (dbSize != currentSize || dbDate != currentModified);
                } else {
                    // Soubor v databázi vůbec není, musí se zálohovat
                    return true;
                }

            }
        }
    }

    public FileItemModel getLatestItemFromDatabase(Path relativePath) throws SQLException {
        String sql = "SELECT absolutPath, fileSize, lastModified "
                + "FROM backup_catalog "
                + "WHERE relativePath = ? "
                + "ORDER BY lastModified DESC LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Převedeme Path objekt na String pro SQL dotaz
            pstmt.setString(1, relativePath.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Záznam nalezen, jdeme sestavit rightItem
                    FileItemModel rightItem = new FileItemModel();

                    // Převod Stringu z DB zpět na Path
                    String absolutPathStr = rs.getString("absolutPath");
                    rightItem.setFileAbsolutePath(Paths.get(absolutPathStr));
                    rightItem.setRelativePath(relativePath);

                    // Nastavení hodnot (zde používám settery JavaFX properties, uprav si dle svého modelu)
                    rightItem.fileSizeProperty().set(rs.getLong("fileSize"));
                    rightItem.lastModifiedProperty().set(rs.getLong("lastModified"));

                    // Jméno souboru lze odvodit z cesty
                    rightItem.fileNameProperty().set(relativePath.getFileName().toString());

                    return rightItem;
                }else {
                    // Soubor v databázi vůbec není (nikdy nebyl zálohován)
                    return null;
                }
            }

        }
    }

    // Metoda se volá AŽ POTÉ, co je fyzický balíček vytvořen a uložen
    public void insertNewDataPack(BackupDatabaseDTO item) throws SQLException {
        String sql = "INSERT INTO backup_catalog (relativePath, absolutPath, fileSize, lastModified, location) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getRelativePath());
            pstmt.setString(2, item.getAbsolutPath());
            pstmt.setLong(3, item.getSize());
            pstmt.setLong(4, item.getLastModified());
            pstmt.setString(5, item.getLocation());
            pstmt.executeUpdate();
        }
    }


    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}
