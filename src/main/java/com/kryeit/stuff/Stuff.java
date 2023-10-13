package com.kryeit.stuff;

import com.kryeit.stuff.listener.ServerLogin;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Stuff implements ModInitializer {
    @Override
    public void onInitialize() {
        registerEvents();
    }

    public void registerEvents() {
        ServerPlayConnectionEvents.INIT.register(new ServerLogin());
    }
}
