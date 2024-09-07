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
        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(uuid);
        long timePlayed = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
        int availableDragons = (int) (timePlayed / 72000);
        return Stuff.dragonKillers.hasKilledDragons(uuid, availableDragons);
    }

    public boolean hasKilledDragons(UUID uuid, int amount) {
        return getDragonDeaths(uuid) >= amount;
    }

    public int getDragonDeaths(UUID uuid) {
        return Integer.parseInt(properties.getProperty(uuid.toString(), "0"));
    }

    public void addKiller(UUID uuid) {
        int currentKills = getDragonDeaths(uuid);
        properties.setProperty(uuid.toString(), Integer.toString(currentKills + 1));
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