package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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

        try {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

                // Zavolá se pro každý soubor
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    if (attrs.isDirectory()) {
                        return FileVisitResult.CONTINUE;
                    }
                    try {
                        String relativePath = rootPath.relativize(file).toString();
                        relativePath = relativePath.replace("\\", "/");

                        FileItemModel newItem = new FileItemModel();
                        newItem.setFileName(file.getFileName().toString());
                        newItem.setFileAbsolutePath(file);
                        newItem.setRelativePath(rootPath.relativize(file));

                        newItem.setFileSize(attrs.size());
                        newItem.setLastModified(attrs.lastModifiedTime().toMillis());

                        filesMap.put(relativePath, newItem);

                    } catch (Exception e) {
                        System.out.println("Chyba při čtení souboru" + file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                // zavolá se když se nepodaří vstoupit do složky/souboru
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {

                    // pokud byl odmítnut přístup k souboru/složce (Koš, System Volume Info, zamčené soubory...)
                    if (exc instanceof AccessDeniedException) {
                        System.out.println("přístup odmítnut: " + file.toString());
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return super.visitFileFailed(file, exc);
                }
            });


            return filesMap;
        } catch (IOException e) {
            ErrorUtility.getInstance("Kritická chyba skenování",
                    "Chyba během procesu skenovaní" + rootPath.toString(),
                    "Zkontrolujte zda má počítač přístup k zadané složce. \n" + e.toString());
        }
        return filesMap;

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
