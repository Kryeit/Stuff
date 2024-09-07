package com.kryeit.stuff.storage;

import com.kryeit.stuff.MinecraftServerSupplier;
import com.kryeit.stuff.Stuff;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;

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

    public static boolean canKillAnotherDragon(UUID uuid) {
        long currentTime = System.currentTimeMillis();
        long lastKillTime = Stuff.dragonKillers.getLastKillTime(uuid);
        long hoursSinceLastKill = (currentTime - lastKillTime) / (1000 * 60 * 60);
        return hoursSinceLastKill >= 100;
    }

    public long getLastKillTime(UUID uuid) {
        return Long.parseLong(properties.getProperty(uuid.toString(), "0"));
    }

    public void addKiller(UUID uuid) {
        long currentTime = System.currentTimeMillis();
        properties.setProperty(uuid.toString(), Long.toString(currentTime));
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