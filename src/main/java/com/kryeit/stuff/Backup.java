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
        CREATE_TRAINS("world/data/"),
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

        File backupDir = new File(backupPath);
        if (backupDir.exists()) {
            try {
                deleteDirectory(backupDir.toPath());
                System.out.println("Previous backup folder deleted successfully");
            } catch (IOException e) {
                System.err.println("Failed to delete previous backup folder: " + e.getMessage());
                return;
            }
        }

        backupDir.mkdirs();

        for (BackupFiles fileOrFolder : BackupFiles.getValues()) {
            try {
                String sourcePath = fileOrFolder.getPath();
                String destPath = backupPath + sourcePath;
                System.out.println("Creating backup for " + sourcePath);

                File source = new File(sourcePath);

                if (source.exists()) {
                    Path destParentPath = Paths.get(destPath).getParent();
                    if (destParentPath != null) {
                        Files.createDirectories(destParentPath);
                    }

                    if (source.isFile()) {
                        Files.copy(source.toPath(), Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("File backed up: " + destPath);
                    } else if (source.isDirectory()) {
                        copyDirectory(source.toPath(), Paths.get(destPath));
                        System.out.println("Directory backed up: " + destPath);
                    }
                } else {
                    System.err.println("Source does not exist: " + sourcePath);
                }
            } catch (IOException e) {
                System.err.println("Failed to backup " + fileOrFolder.getPath() + ": " + e.getMessage());
            }
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        if (!Files.exists(target)) {
            Files.createDirectories(target);
        }

        Files.walk(source)
                .forEach(sourcePath -> {
                    try {
                        Path relativePath = source.relativize(sourcePath);
                        Path targetPath = target.resolve(relativePath);

                        if (Files.isDirectory(sourcePath)) {
                            if (!Files.exists(targetPath)) {
                                Files.createDirectories(targetPath);
                            }
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
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Failed to delete " + path + ": " + e.getMessage());
                    }
                });
    }
}