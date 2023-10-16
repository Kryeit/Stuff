package com.kryeit.stuff;

import net.fabricmc.api.DedicatedServerModInitializer;
import com.kryeit.stuff.listener.ServerLogin;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Stuff implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        registerEvents();
    }

    public void registerEvents() {
        ServerPlayConnectionEvents.INIT.register(new ServerLogin());
    }
}
