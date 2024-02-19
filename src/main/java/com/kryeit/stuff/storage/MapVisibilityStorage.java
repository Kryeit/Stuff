package com.kryeit.stuff.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MapVisibilityStorage {
    private final Path filePath;
    private final Set<UUID> playerUUIDs;

    public MapVisibilityStorage(String filename) throws IOException {
        filePath = Paths.get(filename);
        playerUUIDs = new HashSet<>();

        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }

        if (Files.exists(filePath)) {
            try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    playerUUIDs.add(UUID.fromString(line));
                }
            }
        }
    }

    public boolean hasPlayer(UUID uuid) {
        return playerUUIDs.contains(uuid);
    }

    public void addPlayer(UUID uuid) throws IOException {
        if (playerUUIDs.add(uuid)) {
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.APPEND)) {
                writer.write(uuid.toString());
                writer.newLine();
            }
        }
    }

    public void deletePlayer(UUID uuid) throws IOException {
        if (playerUUIDs.remove(uuid)) {
            Files.write(filePath, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
            for (UUID playerUUID : playerUUIDs) {
                try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.APPEND)) {
                    writer.write(playerUUID.toString());
                    writer.newLine();
                }
            }
        }
    }

    public List<UUID> getPlayers() {
        return List.copyOf(playerUUIDs);
    }
}

