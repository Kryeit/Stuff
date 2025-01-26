package com.kryeit.stuff.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigReader.class);

    public static String DB_URL;
    public static String DB_USER;
    public static String DB_PASSWORD;
    public static String IPGS_KEY;
    public static String CLICKHOUSE_PASSWORD;

    public static void readFile(Path path) throws IOException {
        String config = readOrCopyFile(path.resolve("config.json"), "/config.json");
        JsonObject configObject = JsonParser.parseString(config).getAsJsonObject();

        DB_URL = configObject.get("db-url").getAsString();
        DB_USER = configObject.get("db-user").getAsString();
        DB_PASSWORD = configObject.get("db-password").getAsString();
        IPGS_KEY = configObject.get("ipgs-key").getAsString();
        CLICKHOUSE_PASSWORD = configObject.get("clickhouse-password").getAsString();
    }

    public static String readOrCopyFile(Path path, String exampleFile) throws IOException {
        File file = path.toFile();
        if (!file.exists()) {
            LOGGER.info("File does not exist, attempting to copy from resources: " + exampleFile);
            InputStream stream = ConfigReader.class.getResourceAsStream(exampleFile);
            if (stream == null) {
                LOGGER.error("Cannot load example file: " + exampleFile);
                throw new NullPointerException("Cannot load example file");
            }

            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            Files.copy(stream, path);
            LOGGER.info("File copied to: " + path.toString());
        } else {
            LOGGER.info("File already exists: " + path.toString());
        }
        return Files.readString(path);
    }
}