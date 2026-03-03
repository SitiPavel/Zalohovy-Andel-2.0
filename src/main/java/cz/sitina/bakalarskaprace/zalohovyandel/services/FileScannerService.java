package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FileScannerService {


    public Map<String, FileItemModel> scanFolder(String rootDirectory) {


        HashMap<String, FileItemModel> filesMap = new HashMap<>();
        Path rootPath = Path.of(rootDirectory);


        if (!isRightFolderPath(rootPath)) {
            ErrorUtility.getInstance("Chyba",
                    "Zadaná cesta neexistuje!",
                    "Cesta: " + rootDirectory);
        }

        try (Stream<Path> stream = Files.walk(rootPath)) {
            stream.filter(path -> Files.isRegularFile(path))
            .forEach(file -> {
                String relativePath = rootPath.relativize(file).toString();
                relativePath = relativePath.replace("\\", "/");


                FileItemModel newItem = new FileItemModel();
                newItem.setFileName(file.getFileName().toString());
                newItem.setFileAbsolutePath(file);
                newItem.setRelativePath(rootPath.relativize(file));
                try {
                    newItem.setFileSize(Files.size(file));
                    newItem.setLastModified(Files.getLastModifiedTime(file).toMillis());
                } catch (IOException e) {
                    System.out.println("Chyba při čtení atributu" + file);
                }
                filesMap.put(relativePath, newItem);

            });

            return filesMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /// kontroluje zda cesta existuje a jestli je to složka
    public Boolean isRightFolderPath(Path path) {
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            return false;
        } else {
            return true;
        }
    }



}
