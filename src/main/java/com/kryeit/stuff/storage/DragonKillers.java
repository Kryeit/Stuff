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

    public boolean canKillAnotherDragon(UUID uuid) {
        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(uuid);
        long currentTimePlayed = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
        long lastKillTime = Stuff.dragonKillers.getLastKillTime(uuid);

        if (lastKillTime == 0) return true; // First time killing the dragon

        long ticksSinceLastKill = currentTimePlayed - lastKillTime;
        long hoursSinceLastKill = ticksSinceLastKill / (20 * 60 * 60);
        return hoursSinceLastKill >= 100;
    }

    public long getLastKillTime(UUID uuid) {
        return Long.parseLong(properties.getProperty(uuid.toString(), "0"));
    }

    public void addKiller(UUID uuid) {
        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(uuid);
        long currentTimePlayed = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
        properties.setProperty(uuid.toString(), Long.toString(currentTimePlayed));
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