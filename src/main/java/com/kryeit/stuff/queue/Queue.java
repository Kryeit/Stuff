package com.kryeit.stuff.queue;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Queue {

    List<UUID> queue = new ArrayList<>();

    public Queue() {

    }

    public void addPlayer(UUID id) {
        if (!queue.contains(id)) queue.add(id);
    }

    public void removePlayer(UUID id) {
        queue.remove(id);
    }

    public int getPos(UUID id) {
        int i = 1;
        for (UUID uuid : queue) {
            if (uuid.equals(id)) return i;
            i++;
        }
        return -1;
    }

    public UUID get(int i) {
        return queue.get(i);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public Text getKickMessage(UUID id) {
        return  Text.of("Server is full!\n"
                + "You are in the queue, your position is "
                + getPos(id) + " out of " + size()
                + "\nReconnect within 3 minutes to keep your position");
    }
}
