package com.kryeit.stuff.queue;

import com.kryeit.stuff.Utils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class QueueHandler implements ServerPlayConnectionEvents.Init {
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private static final long THREE_MINUTES_IN_MILLIS = 3 * 60 * 1000;
    private final Queue queue;

    public QueueHandler(Queue queue) {
        this.queue = queue;
        executor.scheduleAtFixedRate(this::removePlayersWaitingTooLong, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void onPlayInit(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ServerPlayerEntity player = handler.player;
        UUID id = player.getUuid();

        if (Utils.isServerFull()) {
            queue.addPlayer(id);
            handler.disconnect(queue.getKickMessage(id));
        } else {
            if (queue.isEmpty()) return;

            if (queue.getPos(id) == 1) {
                queue.removePlayer(id);
            } else {
                handler.disconnect(queue.getKickMessage(id));
                queue.resetCooldown(id);
            }
        }
    }

    private void removePlayersWaitingTooLong() {
        long currentTime = System.currentTimeMillis();

        queue.queue.entrySet().removeIf(entry -> currentTime - entry.getValue() > THREE_MINUTES_IN_MILLIS);
    }
}
