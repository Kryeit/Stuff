package com.kryeit.stuff;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class Backup {
    public enum BackupFiles {
        CREATE_TRAINS("world/data/create_trains.dat"),
        CLAIMS("config/griefdefender/worlds/")
        ;

        private final String path;

        BackupFiles(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public static List<BackupFiles> getValues() {
            return List.of(BackupFiles.values());
        }
    }

    public static void createBackups() {
        String backupPath = "backup/";

        // Delete previous backup folder if it exists
        File backupDir = new File(backupPath);
        if (backupDir.exists()) {
            try {
                deleteDirectory(backupDir.toPath());
                System.out.println("Previous backup folder deleted successfully");
            } catch (IOException e) {
                System.err.println("Failed to delete previous backup folder: " + e.getMessage());
                return; // Exit if we can't delete the previous backup
            }
        }

        // Create fresh backup directory
        backupDir.mkdirs();

        for (BackupFiles fileOrFolder : BackupFiles.getValues()) {
            try {
                System.out.println("Creating backup for " + fileOrFolder.getPath());
                File source = new File(fileOrFolder.getPath());

                // Ensure parent directory exists
                File targetParent = new File(backupPath + source.getParent());
                targetParent.mkdirs();

                // Backup file or directory
                if (source.exists()) {
                    if (source.isFile()) {
                        // Explicitly copy file
                        Files.copy(
                                source.toPath(),
                                Paths.get(backupPath + fileOrFolder.getPath()),
                                StandardCopyOption.REPLACE_EXISTING
                        );
                    } else if (source.isDirectory()) {
                        // Copy directory contents recursively
                        copyDirectory(source.toPath(), new File(backupPath + fileOrFolder.getPath()).toPath());
                    }
                } else {
                    System.err.println("Source does not exist: " + fileOrFolder.getPath());
                }
            } catch (IOException e) {
                System.err.println("Failed to backup " + fileOrFolder.getPath() + ": " + e.getMessage());
            }
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source)
                .forEach(sourcePath -> {
                    try {
                        Path targetPath = target.resolve(source.relativize(sourcePath));
                        if (Files.isDirectory(sourcePath)) {
                            Files.createDirectories(targetPath);
                        } else {
                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to copy " + sourcePath + ": " + e.getMessage());
                    }
                });
    }

    private static void deleteDirectory(Path directory) throws IOException {
        Files.walk(directory)
                .sorted((a, b) -> -a.compareTo(b)) // Reverse order to delete files before directories
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Failed to delete " + path + ": " + e.getMessage());
                    }
                });
    }
}