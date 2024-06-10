package com.kryeit.stuff.storage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class DragonKillers {
    private static final String FILE_NAME = "mods/stuff/dragon_killers.properties";
    private Properties properties;

    public DragonKillers() {
        properties = new Properties();
        try {
            FileInputStream in = new FileInputStream(FILE_NAME);
            properties.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasKilledDragon(UUID uuid) {
        return properties.containsKey(uuid.toString());
    }

    public void addKiller(UUID uuid) {
        properties.setProperty(uuid.toString(), "true");
        saveProperties();
    }

    private void saveProperties() {
        try {
            FileOutputStream out = new FileOutputStream(FILE_NAME);
            properties.store(out, null);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
