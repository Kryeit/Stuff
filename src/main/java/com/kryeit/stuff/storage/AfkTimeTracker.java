package com.kryeit.stuff.storage;

import com.kryeit.idler.afk.AfkPlayer;
import com.kryeit.stuff.Stuff;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.util.UUID;

public class AfkTimeTracker implements AutoCloseable {
    private final MVStore store = MVStore.open("mods/stuff/afk_times.mvstore");
    private final MVMap<UUID, long[]> afkTimes = store.openMap("afk_times");
    private int tickCounter = 0;

    public AfkTime getAfkStat(UUID player) {
        long[] value = afkTimes.get(player);
        if (value == null) return new AfkTime(0, 0);
        return new AfkTime(value[0], value[1]);
    }

    public void incrementStat(UUID player, long playtime, long afkTime) {
        afkTimes.operate(player, new long[]{playtime, afkTime}, new MVMap.DecisionMaker<>() {
            @Override
            public long[] selectValue(long[] existingValue, long[] providedValue) {
                if (existingValue == null) return providedValue;
                return new long[]{existingValue[0] + providedValue[0], existingValue[1] + providedValue[1]};
            }

            @Override
            public MVMap.Decision decide(long[] existingValue, long[] providedValue) {
                return MVMap.Decision.PUT;
            }
        });
    }

    public void tick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < 20) return;

        tickCounter = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean isAfk = ((AfkPlayer) player).idler$isAfk() && !Stuff.checkPermission(player.getUUID(), "stuff.afk");

            if (isAfk) incrementStat(player.getUUID(), 0, 1);
            else incrementStat(player.getUUID(), 1, 0);
        }
    }

    public record AfkTime(long playtime, long afkTime) {
    }

    @Override
    public void close() {
        store.close();
    }
}
