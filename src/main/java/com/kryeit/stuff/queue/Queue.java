package com.kryeit.stuff.queue;

import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.UUID;

public class Queue {

    HashMap<UUID, Long> queue = new HashMap<>();

    public Queue() {

    }

    public void addPlayer(UUID id) {
        if (!queue.containsKey(id)) queue.put(id, System.currentTimeMillis());
    }

    public void removePlayer(UUID id) {
        queue.remove(id);
    }

    public int getPos(UUID id) {
        int i = 1;
        for (UUID uuid : queue.keySet()) {
            if (uuid.equals(id)) return i;
            i++;
        }
        return -1;
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void resetCooldown(UUID player) {
        queue.replace(player, System.currentTimeMillis());
    }

    public Text getKickMessage(UUID id) {
        return  Text.of("Server is full!\n"
                + "You are in the queue, your position is "
                + getPos(id) + " out of " + size()
                + "\nReconnect within 3 minutes to keep your position");
    }
}
