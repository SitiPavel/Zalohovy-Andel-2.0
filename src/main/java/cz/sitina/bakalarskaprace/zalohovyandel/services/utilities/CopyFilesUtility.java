package cz.sitina.bakalarskaprace.zalohovyandel.services.utilities;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.BackupProcessService;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;

public class CopyFilesUtility {


    public interface ProgressCallback {
        void onProgress(long bytesProcessed, long totalBytes, String currentFile,
                        int processedFileCount);
        void onItemCompleted(boolean success, ComparedItemOperItemRowModel row);
        void onItemCompleted(boolean success, BackupDatabaseDTO dto);
    }



}
