package com.kryeit.stuff.bluemap;

import de.bluecolored.bluemap.api.BlueMapAPI;

import java.util.UUID;

public class BluemapImpl {
    public static void changePlayerVisibility(UUID id, boolean visible) {
        BlueMapAPI.getInstance().ifPresent(api -> {
            api.getWebApp().setPlayerVisibility(id, visible);
        });
    }
}
