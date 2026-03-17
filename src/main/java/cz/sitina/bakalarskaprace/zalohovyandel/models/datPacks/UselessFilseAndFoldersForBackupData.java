package cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks;

import java.util.HashSet;
import java.util.Set;

public class UselessFilseAndFoldersForBackupData {
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

    public Set<String> getSkippedFolders() {
        return skippedFolders;
    }

    public Set<String> getSkippedFiles() {
        return skippedFiles;
    }
}

