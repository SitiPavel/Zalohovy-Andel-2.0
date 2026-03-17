package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;


public class FileScannerService {

    private final Set<String> skippedFolders = new HashSet<>(Set.of(
            /// WINDOWS SYSTÉMOVÉ
            "$recycle.bin",                    // Koš
            "system volume information",       // Systémové info
            "recovery",                        // Recovery partition
            "$windows.~bt",                    // Windows Update temp
            "$windows.~ws",                    // Windows Update workspace
            "config.msi",                      // MSI installer cache
            "windowsapps",                     // Windows Store apps
            "softwaredistribution",            // Windows Update cache
            "windows defender",                // Defender cache
            "perflogs",                        // Performance logs
            "temp",
            "tmp",

            /// MACOS SYSTÉMOVÉ

            ".spotlight-v100",                 // Spotlight index
            ".trashes",                        // Koš
            ".fseventsd",                      // File system events
            ".documentrevisions-v100",         // Verzování
            ".temporaryitems",                 // Temp soubory

            // === LINUX SYSTÉMOVÉ ===
            "proc",                            // Virtual FS
            "sys",                             // Sysfs
            "dev",                             // Devices
            "run"                              // Runtime
    ));

    private final Set<String> skippedFiles = new HashSet<>(Set.of(
            "pagefile.sys",
            "hiberfil.sys",
            "swapfile.sys",

            ///  WINDOWS CACHE
            "thumbs.db",                  // Thumbnail cache (XP)
            "ehthumbs.db",                // Thumbnail cache (Vista+)
            "ehthumbs_vista.db",
            "desktop.ini",                // Folder customization

            ///  MACOS CACHE
            ".ds_store",                  // Folder settings
            ".localized",                 // Localization
            "._*"
            ));


    public Map<String, FileItemModel> scanFolder(String rootDirectory, List<String> filtredFiles) {


        HashMap<String, FileItemModel> filesMap = new HashMap<>();
        Path rootPath = Path.of(rootDirectory);


        if (!isRightFolderPath(rootPath)) {
            ErrorUtility.getInstance("Chyba",
                    "Zadaná cesta neexistuje!",
                    "Cesta: " + rootDirectory);
        }

        Set<Path> ignoredPaths = new HashSet<>();
        if (!filtredFiles.isEmpty() ) {
            for (String folder : filtredFiles) {
                ignoredPaths.add(Paths.get(folder));
            }
        }


        try {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.getFileName() != null) {
                        String fileName = dir.getFileName().toString().toLowerCase();

                        // pokud název v malích písmenech je na seznamu blokovaných složek
                        if (skippedFolders.contains(fileName) || skippedFiles.contains(fileName) || ignoredPaths.contains(dir)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                // Zavolá se pro každý soubor
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    if (attrs.isDirectory()) {
                        return FileVisitResult.CONTINUE;
                    }

                    // pokud název v malích písmenech je na seznamu blokovaných souborů
                    if (skippedFiles.contains(file.getFileName().toString().toLowerCase())) {
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

    // najde použe složky v zadané cestě
    public List<String> loadDirectSubfolders(Path rootPath) {

        if (!isRightFolderPath(rootPath)) {
            ErrorUtility.getInstance("Chyba cesty",
                    "Zadaná cesta neexistuje nebo není složka",
                    "Cesta: " + rootPath.toString());
            return new ArrayList<>();
        }


        List<String> result = new ArrayList<>();

        try (Stream<Path> paths = Files.list(rootPath)) {
            paths.filter(Files::isDirectory)
                    .forEach(path -> {
                        String fullPath = path.toAbsolutePath().toString();
                        result.add(fullPath);
                    });


        }catch (IOException e) {
            e.printStackTrace();
            ErrorUtility.getInstance("Chyba souboru",
                    "Nepodařilo se načíst soubory",
                    "Cesta: " + rootPath.toString());
        }
        return result;
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
