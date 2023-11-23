package com.kryeit.stuff.queue;

import com.kryeit.stuff.Utils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.kryeit.stuff.Stuff.queue;

public class QueueHandler implements ServerPlayConnectionEvents.Init {
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private static final long THREE_MINUTES_IN_MILLIS = 3 * 60 * 1000;

    public QueueHandler() {
        executor.scheduleAtFixedRate(this::removePlayersWaitingTooLong, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void onPlayInit(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ServerPlayerEntity player = handler.player;
        UUID id = player.getUuid();

        if (Utils.isServerFull()) {
            // Server full, add then to the queue or leave them where they are at the queue
            queue.addPlayer(id);
            handler.disconnect(queue.getKickMessage(id));
        } else {
            // If nobody in the queue, let them join
            if (queue.isEmpty()) return;

            if (queue.getPos(id) == 1) {
                // First player on queue, therefore let them join
                queue.removePlayer(id);
            } else {
                // Not his turn to join
                handler.disconnect(queue.getKickMessage(id));
                queue.resetCooldown(id);
            }
        }
    }

    private void removePlayersWaitingTooLong() {
        long currentTime = System.currentTimeMillis();

        Map<UUID, Long> copy = new HashMap<>(queue.queue);
        for (Map.Entry<UUID, Long> entry : copy.entrySet()) {
            if (currentTime - entry.getValue() > THREE_MINUTES_IN_MILLIS) {
                queue.removePlayer(entry.getKey());
            }
        }
    }
}
