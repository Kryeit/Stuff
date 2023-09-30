package com.kryeit.stuff;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

public class Stuff implements ModInitializer {
    @Override
    public void onInitialize() {
        registerEvents();
    }

    public void registerEvents() {
    }
}
