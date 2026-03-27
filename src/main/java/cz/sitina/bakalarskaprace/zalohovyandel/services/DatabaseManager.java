package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void insertNewItem(BackupDatabaseDTO item) throws SQLException {
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

    // najde ty nejaktuálnější data a ty nyčte do paměti
    public Map<String, FileItemModel> loadLatestIndexToMemory() throws SQLException {
        Map<String, FileItemModel> map = new HashMap<>();

        String sql = "SELECT relativePath, absolutPath, fileSize, lastModified " +
                "FROM backup_catalog " +
                "WHERE id IN (SELECT MAX(id) FROM backup_catalog GROUP BY relativePath)";

        try (java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                FileItemModel item = new FileItemModel();

                String relCestaStr = rs.getString("relativePath");
                item.setRelativePath(Paths.get(relCestaStr));
                item.setFileAbsolutePath(Paths.get(rs.getString("absolutPath")));
                item.fileSizeProperty().set(rs.getLong("fileSize"));
                item.lastModifiedProperty().set(rs.getLong("lastModified"));
                item.fileNameProperty().set(item.getRelativePath().getFileName().toString());

                map.put(relCestaStr, item);
            }

        }
        return map;
    }

    // zjistí jestli obsahuje nějaké záznamy
    public boolean isDatabaseEmpty() throws SQLException {
        String sql = "SELECT COUNT(*) FROM backup_catalog";
        try(java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0; // Pokud je počet řádků 0, vrátí true když víc tak false
            }
        }
        return true;
    }

    public void insertNewItemsBatch(List<BackupDatabaseDTO> items) throws SQLException {
        String sql = "INSERT INTO backup_catalog (absolutPath, relativePath, fileSize, lastModified, location) VALUES (?, ?, ?, ?, ?)";

        // Vypnutí automatického ukládání (ZÁSADNÍ PRO RYCHLOST)
        conn.setAutoCommit(false);

        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (BackupDatabaseDTO item : items) {
                pstmt.setString(1, item.getAbsolutPath());
                pstmt.setString(2, item.getRelativePath());
                pstmt.setLong(3, item.getSize());
                pstmt.setLong(4, item.getLastModified());
                pstmt.setString(5, item.getLocation());

                pstmt.addBatch(); // Přidá příkaz do paměťové fronty
            }
            pstmt.executeBatch(); // Pošle celou frontu do databáze najednou
            conn.commit();        // Fyzicky zapíše na disk (pouze JEDNOU!)
        } catch (SQLException e) {
            conn.rollback();      // V případě chyby zruší všechny zápisy
            throw e;
        } finally {
            conn.setAutoCommit(true); // Vrácení do původního stavu
        }
    }


    // Spočítá kolik balíčků eviduje databáze
    public int getPackageCount() throws SQLException {
        // Spočítá počet UNIKÁTNÍCH názvů balíčků v historii
        String sql = "SELECT COUNT(DISTINCT location) FROM backup_catalog";

        try (java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0; // Pokud nastane nečekaná chyba, vrátí 0
    }


    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}
