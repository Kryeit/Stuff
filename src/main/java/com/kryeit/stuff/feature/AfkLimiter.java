package com.kryeit.stuff.feature;

import com.kryeit.idler.afk.AfkPlayer;
import com.kryeit.stuff.GerenteClient;
import com.kryeit.stuff.Stuff;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AfkLimiter {
    private static final int AFK_KICK_TICKS = (int) (3.5 * 60 * 20); // 3.5 minutes
    private final Map<UUID, Integer> afkTime = new HashMap<>();

    public void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean isAfk = ((AfkPlayer) player).idler$isAfk() && !Stuff.checkPermission(player.getUUID(), "stuff.afk");

            if (isAfk) {
                int newTime = afkTime.getOrDefault(player.getUUID(), 0) + 1;
                afkTime.put(player.getUUID(), newTime);

                if (newTime == 1 && Stuff.GERENTE.getCachedJoinInfo(player.getUUID())
                        .map(GerenteClient.PlayerJoinInfo::afkLimited)
                        .orElse(false)) {

                    player.sendSystemMessage(Component.literal("""
                            You've been flagged for excessive AFK time in the past.
                            Please keep your AFK sessions shorter going forward.
                            Staying AFK for longer than 3 minutes will get you kicked.
                            """).withStyle(ChatFormatting.RED));
                }

                if (newTime > AFK_KICK_TICKS) {
                    player.connection.disconnect(Component.literal("Kicked for being AFK too long. Try to stay more active relative to your AFK time to avoid this in the future.")
                            .withStyle(ChatFormatting.RED));
                }
            } else {
                afkTime.remove(player.getUUID());
            }
        }
    }
}
