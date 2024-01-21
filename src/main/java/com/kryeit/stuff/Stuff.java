package com.kryeit.stuff;

import com.kryeit.stuff.command.*;
import com.kryeit.stuff.listener.PlayerDeath;
import com.kryeit.stuff.listener.PlayerVote;
import com.kryeit.votifier.model.VotifierEvent;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

import java.util.HashMap;
import java.util.UUID;

public class Stuff implements DedicatedServerModInitializer {

   // public static Queue queue = new Queue();
    public static HashMap<UUID, Long> lastActiveTime = new HashMap<>();

    @Override
    public void onInitializeServer() {
        registerEvents();
        registerCommands();
    }

    public void registerEvents() {
   //     ServerPlayConnectionEvents.INIT.register(new QueueHandler(queue));
        ServerLivingEntityEvents.AFTER_DEATH.register(new PlayerDeath());
        VotifierEvent.EVENT.register(new PlayerVote());
    }

    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicatedServer, commandFunction) -> {
            Kofi.register(dispatcher);
            Map.register(dispatcher);
            Rules.register(dispatcher);
            SendCoords.register(dispatcher);
            TPS.register(dispatcher);
            AFK.register(dispatcher);
        });
    }
}
