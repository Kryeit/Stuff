package com.kryeit.stuff.compat;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.markers.MarkerSet;

import java.util.UUID;

public class BluemapImpl {
    public static final MarkerSet trainsMarkerSet = MarkerSet.builder()
            .label("Trains")
            .build();

    public static void changePlayerVisibility(UUID id, boolean visible) {
        BlueMapAPI.getInstance().ifPresent(api -> {
            api.getWebApp().setPlayerVisibility(id, visible);
        });
    }

    static {
        BlueMapAPI.getInstance()
                .flatMap(api -> api.getMap("world"))
                .ifPresent(map -> map.getMarkerSets().put("trains", trainsMarkerSet));
    }
}
