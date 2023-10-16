package com.kryeit.stuff;

import com.kryeit.stuff.listener.PlayerLogin;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Stuff implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        registerEvents();
    }

    public void registerEvents() {
        ServerPlayConnectionEvents.JOIN.register(new PlayerLogin());
    }
}
