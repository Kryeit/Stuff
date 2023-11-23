package com.kryeit.stuff.queue;

import com.kryeit.stuff.Utils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

import static com.kryeit.stuff.Stuff.queue;

public class QueueHandler implements ServerPlayConnectionEvents.Init {

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
            }
        }
    }
}
