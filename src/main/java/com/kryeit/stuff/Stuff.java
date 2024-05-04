package com.kryeit.stuff;

import com.kryeit.stuff.bluemap.BluemapImpl;
import com.kryeit.stuff.command.*;
import com.kryeit.stuff.listener.DragonDeath;
import com.kryeit.stuff.listener.PlayerDeath;
import com.kryeit.stuff.listener.PlayerVote;
import com.kryeit.stuff.storage.MapVisibilityStorage;
import com.kryeit.votifier.model.VotifierEvent;
import de.bluecolored.bluemap.api.BlueMapAPI;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

public class Stuff implements DedicatedServerModInitializer {

   // public static Queue queue = new Queue();
    public static HashMap<UUID, Long> lastActiveTime = new HashMap<>();
    public static MapVisibilityStorage hiddenPlayers;


    @Override
    public void onInitializeServer() {


        try {
            Files.createDirectories(Paths.get("mods/stuff"));
            hiddenPlayers = new MapVisibilityStorage("mods/stuff/hiddenPlayers");
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerEvents();
        registerCommands();
    }

    public void registerEvents() {
   //     ServerPlayConnectionEvents.INIT.register(new QueueHandler(queue));
        ServerLivingEntityEvents.AFTER_DEATH.register(new PlayerDeath());
        ServerLivingEntityEvents.AFTER_DEATH.register(new DragonDeath());
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
            ShowMe.register(dispatcher);
            HideMe.register(dispatcher);

            ChickensAI.register(dispatcher);
        });


        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            for (UUID id : hiddenPlayers.getPlayers()) {
                BluemapImpl.changePlayerVisibility(id, false);
            }
        });
    }
}
