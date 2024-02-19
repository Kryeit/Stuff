package com.kryeit.stuff.bluemap;

import de.bluecolored.bluemap.api.BlueMapAPI;

import java.util.UUID;

public class BluemapImpl {
    public static void changePlayerVisibility(UUID id, boolean visible) {
        BlueMapAPI blueMapAPI = BlueMapAPI.getInstance().get();
        blueMapAPI.getWebApp().setPlayerVisibility(id, visible);
    }
}
